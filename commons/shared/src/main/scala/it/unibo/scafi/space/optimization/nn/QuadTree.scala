/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibo.scafi.space.optimization.nn
import it.unibo.scafi.space.optimization._
import it.unibo.scafi.space.Point3D

import scala.annotation.tailrec
import scala.collection.mutable

/** n-dimensional QuadTree data structure; partitions
  * spatial data for faster queries (e.g. KNN query)
  * The skeleton of the data structure was initially
  * based off of the 2D Quadtree found here:
  * http://www.cs.trinity.edu/~mlewis/CSCI1321-F11/Code/src/util/Quadtree.scala
  *
  * @param minVec     vector of the corner of the bounding box with smallest coordinates
  * @param maxVec     vector of the corner of the bounding box with smallest coordinates
  * @param maxPerBox  threshold for number of points in each box before slitting a box
  */
private[nn] class QuadTree[A] private (
                                      minVec: Point3D,
                                      maxVec: Point3D,
                                      maxPerBox: Int) extends  NNIndex[A]{
  val mmap: mutable.Map[Point3D,A] = mutable.Map.empty

  val root = new QuadNode((minVec + maxVec) * 0.5,
    maxVec - minVec, this, Seq.empty)

  def +=(elem : (Point3D,A)) : this.type = {
    def insertRecur(elem: (Point3D,A), node: QuadNode[A]): Unit = {
      if (node.children.isEmpty) {
        if (node.nodeElements.length < maxPerBox) {
          if(!node.nodeElements.contains(elem)) {
            node.nodeElements += elem
          }
        } else {
          node.makeChildren()
          for (o <- node.nodeElements) {
            insertRecur(o,node.children(node.whichChild(o._1)))
          }
          node.nodeElements.clear()
          insertRecur(elem, node.children(node.whichChild(elem._1)))
        }
      } else {
        insertRecur(elem,node.children(node.whichChild(elem._1)))
      }
    }
    insertRecur(elem, root)
    this
  }

  override def ++=(points : Iterable[(Point3D,A)]) : this.type = {
    points foreach {this += _}
    this
  }
  /**
    * find the node where the point could be store
    */
  private def findPointContainsInNode(p: Point3D, node : QuadNode[A]) : Option[QuadNode[A]] = {
    if (node.children.isEmpty) {
      Some(node)
    } else {
      findPointContainsInNode(p,node.children(node.whichChild(p)))
    }
  }

  def -=(queryPoint: Point3D) : this.type = {
    val node = findPointContainsInNode(queryPoint,root).get
    val key = node.nodeElements.find(_._1 === queryPoint)
    if(key.isDefined) {
      node.nodeElements -= key.get
    }
    this
  }

  def get(queryPoint : Point3D) : Option[A] = {
    val node = findPointContainsInNode(queryPoint,root).get
    node.nodeElements.find(_._1 === queryPoint).map(_._2)
  }

  /** Finds all objects within a neighborhood of queryPoint of a specified radius
    * scope is modified from original 2D version in:
    * http://www.cs.trinity.edu/~mlewis/CSCI1321-F11/Code/src/util/Quadtree.scala
    *
    * @param queryPoint a point which is center
    * @param radius     radius of scope
    * @return all points within queryPoint with given radius
    */
  override def neighbours(queryPoint: Point3D, radius: Double): Iterable[(Point3D,A)] = {
    def searchRecur(
                     queryPoint: Point3D,
                     radius: Double,
                     node: QuadNode[A],
                     ret: mutable.ListBuffer[(Point3D,A)]
    ): Unit = {
      if (node.children.isEmpty) {
        ret ++= node.nodeElements filter {x => {queryPoint.distance(x._1) <= radius}}
      } else {
        node.children.foreach {
          x => {
            if(x.isNear(queryPoint,radius)) {
              searchRecur(queryPoint,radius,x,ret)
            }
          }
        }
      }
    }

    val ret = new mutable.ListBuffer[(Point3D,A)]
    searchRecur(queryPoint, radius, root, ret)
    ret
  }

  def iterator: Iterator[(Point3D,A)] = elems.iterator

  private def elems : mutable.Iterable[(Point3D,A)] = {
    def elemsRec(node : QuadNode[A]) : mutable.Iterable[(Point3D,A)] = {
      if(node.children.isEmpty) {
        node.nodeElements
      } else {
        var elems = mutable.ListBuffer.empty[(Point3D,A)]
        for(child <- node.children) {
          elems ++= elemsRec(child)
        }
        elems
      }
    }
    elemsRec(root)
  }
}

object QuadTree {
  /**
    * create an neighbour index
    * @param elems the elements to add in the index
    * @tparam A the type of element wrapped in node
    * @return the index created
    */
  def apply[A](elems : Iterable[(A,Point3D)]) : NNIndex[A] = {
    //a max threshold to create a larger space
    val maxThr = 20000
    //a big threshold used to create nnindex, allow to create a fake unlimited space
    val thr = Point3D(maxThr,maxThr,maxThr)
    /**
      * used to compute the lowest point and the highest
      */
    val min = Point3D(elems.minBy(_._2.x)._2.x,elems.minBy(_._2.y)._2.y,elems.minBy(_._2.z)._2.z) - thr
    val max = Point3D(elems.maxBy(_._2.x)._2.x,elems.maxBy(_._2.y)._2.y,elems.maxBy(_._2.z)._2.z) + thr
    val q = new QuadTree[A](min,max,computeElemsForBox(elems.size))
    q ++= (elems map {x => (x._2,x._1)})
  }
  /*
   * a constant used to normalize the function computeElemsForBox
   */
  private val normalizeConstantBox = 10
  /**
    * minimum number of node in a box
    */
  private val minNode = 10
  /*
    * a function used to compute the number of elements in box (empirical value)
    * @param elems the number of elements
    * @return the max number of elements in a box
    */
  private def computeElemsForBox(elems : Int) = if(elems > minNode ) normalizeConstantBox * math.log10(elems).toInt else minNode

  /**
    * create a neighbour index with bound specified
    * @param min the lowest point in the space
    * @param max the biggest point in the space
    * @param elems the number of element in the space
    * @tparam A the type of element wrapped in node
    * @return the index created
    */
  def apply[A](min : Point3D, max : Point3D, elems: Int) : NNIndex[A] = new QuadTree[A](min,max,computeElemsForBox(elems))
}
