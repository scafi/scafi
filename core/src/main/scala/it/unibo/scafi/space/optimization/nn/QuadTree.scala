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

import it.unibo.scafi.space.optimization.distances.{DistanceMetric, EuclideanDistanceMetric, SquaredEuclideanDistanceMetric}
import it.unibo.scafi.space.optimization.helper.MultiVector

import scala.annotation.tailrec
import scala.collection.mutable

/** n-dimensional QuadTree data structure; partitions
  * spatial data for faster queries (e.g. KNN query)
  * The skeleton of the data structure was initially
  * based off of the 2D Quadtree found here:
  * http://www.cs.trinity.edu/~mlewis/CSCI1321-F11/Code/src/util/Quadtree.scala
  *
  * Many additional methods were added to the class both for
  * efficient KNN queries and generalizing to n-dim.
  *
  * @param minVec     vector of the corner of the bounding box with smallest coordinates
  * @param maxVec     vector of the corner of the bounding box with smallest coordinates
  * @param distMetric metric, must be Euclidean or squareEuclidean
  * @param maxPerBox  threshold for number of points in each box before slitting a box
  */
class QuadTree[A](
                   minVec: MultiVector,
                   maxVec: MultiVector,
                   distMetric: DistanceMetric,
                   maxPerBox: Int) {

  class Node(
              center: MultiVector,
              width: MultiVector,
              var children: Seq[Node]) {

    override def toString: String = s"center $center width $width"
    val nodeElements = new mutable.ListBuffer[(MultiVector,A)]

    /** for testing purposes only; used in QuadTreeSuite.scala
      *
      * @return center and width of the box
      */
    def getCenterWidth(): (MultiVector, MultiVector) = (center, width)

    /** Tests whether the queryPoint is in the node, or a child of that node
      *
      * @param queryPoint a point to test
      * @return whether the given point is in the node, or a child of this node
      */
    def contains(queryPoint: MultiVector): Boolean = overlap(queryPoint, 0.0)

    /** Tests if queryPoint is within a radius of the node
      *
      * @param queryPoint a point to test
      * @param radius     radius of test area
      * @return whether the given point is in the area
      */
    def overlap(queryPoint: MultiVector, radius: Double): Boolean = {
      (0 until queryPoint.size).forall { i =>
        (queryPoint(i) - radius <= center(i) + width(i) / 2) &&
          (queryPoint(i) + radius >= center(i) - width(i) / 2)
      }
    }

    /** Tests if queryPoint is near a node
      *
      * @param queryPoint a point to test
      * @param radius     radius of covered area
      */
    def isNear(queryPoint: MultiVector, radius: Double): Boolean = {minDist(queryPoint) < radius}

    /** minDist is defined so that every point in the box has distance to queryPoint greater
      * than minDist (minDist adopted from "Nearest Neighbors Queries" by N. Roussopoulos et al.)
      *
      * @param queryPoint
      */
    def minDist(queryPoint: MultiVector): Double = {
      val minDist = (0 until queryPoint.size).map { i =>
        if (queryPoint(i) < center(i) - width(i) / 2) {
          math.pow(queryPoint(i) - center(i) + width(i) / 2, 2)
        } else if (queryPoint(i) > center(i) + width(i) / 2) {
          math.pow(queryPoint(i) - center(i) - width(i) / 2, 2)
        } else {
          0
        }
      }.sum

      distMetric match {
        case _: EuclideanDistanceMetric => math.sqrt(minDist)
        case _ => throw new IllegalArgumentException(s" Error: metric must be" +
          s" Euclidean or SquaredEuclidean!")
      }
    }

    /** Finds which child queryPoint lies in. node.children is a Seq[Node], and
      * [[whichChild]] finds the appropriate index of that Seq.
      *
      * @param queryPoint
      * @return
      */
    def whichChild(queryPoint: MultiVector): Int = {
      (0 until queryPoint.size).map { i =>
        if (queryPoint(i) > center(i)) {
          scala.math.pow(2, queryPoint.size - 1 - i).toInt
        } else {
          0
        }
      }.sum
    }

    /** Makes children nodes by partitioning the box into equal sub-boxes
      * and adding a node for each sub-box
      */
    def makeChildren() {
      val centerClone = center.copy
      val cPart = partitionBox(centerClone, width)
      val mappedWidth = width * 0.5
      children = cPart.map(p => new Node(p, mappedWidth, null))
    }

    /** Recursive function that partitions a n-dim box by taking the (n-1) dimensional
      * plane through the center of the box keeping the n-th coordinate fixed,
      * then shifting it in the n-th direction up and down
      * and recursively applying partitionBox to the two shifted (n-1) dimensional planes.
      *
      * @param center the center of the box
      * @param width  a vector of lengths of each dimension of the box
      * @return
      */
    def partitionBox(center: MultiVector, width: MultiVector): Seq[MultiVector] = {
      @tailrec
      def partitionHelper(box: Seq[MultiVector], dim: Int): Seq[MultiVector] = {
        if (dim >= width.size) {
          box
        } else {
          val newBox = box.flatMap { vector =>
            val (up, down) = (vector.copy, vector)
            up.update(dim, up(dim) - width(dim) / 4)
            down.update(dim, down(dim) + width(dim) / 4)

            Seq(up, down)
          }
          partitionHelper(newBox, dim + 1)
        }
      }
      partitionHelper(Seq(center), 0)
    }
  }

  val root = new Node((minVec ++ maxVec) * 0.5,
    minVec -- maxVec, null)


  /** Recursively adds an object to the tree
    *
    * @param queryPoint an object which is added
    */
  def insert(queryPoint: MultiVector, info : A) = {
    def insertRecur(queryPoint: MultiVector, info: A, node: Node): Unit = {
      if (node.children == null) {
        if (node.nodeElements.length < maxPerBox) {
          val insert = (queryPoint,info)
          node.nodeElements += insert
        } else {
          node.makeChildren()
          for (o <- node.nodeElements) {
            insertRecur(o._1, o._2,node.children(node.whichChild(o._1)))
          }
          node.nodeElements.clear()
          insertRecur(queryPoint, info, node.children(node.whichChild(queryPoint)))
        }
      } else {
        insertRecur(queryPoint, info,node.children(node.whichChild(queryPoint)))
      }
    }
    insertRecur(queryPoint, info, root)
  }

  def remove(queryPoint: MultiVector) : Boolean = {
    @tailrec
    def findAndDelete(
                       queryPoint: MultiVector,
                       node: Node
                   ): Boolean = {
      if (node.children == null) {
        val toRemove = node.nodeElements find {x => x._1 == queryPoint}
        if(toRemove.isDefined) {
          node.nodeElements -= toRemove.get
          true
        } else {
          false
        }
      } else {
        findAndDelete(queryPoint,node.children(node.whichChild(queryPoint)))
      }
    }
    findAndDelete(queryPoint, root)
  }

  def elems : mutable.Iterable[(MultiVector,A)] = {
    def elemsRec(node : Node) : mutable.Iterable[(MultiVector,A)] = {
      if(node.children == null) {
        return node.nodeElements
      } else {
        var elems = mutable.ListBuffer.empty[(MultiVector,A)]
        for(child <- node.children) {
          elems ++= elemsRec(child)
        }
        elems
      }
    }
    elemsRec(root)
  }


  /** Finds all objects within a neighborhood of queryPoint of a specified radius
    * scope is modified from original 2D version in:
    * http://www.cs.trinity.edu/~mlewis/CSCI1321-F11/Code/src/util/Quadtree.scala
    *
    * original version only looks in minimal box; for the KNN Query, we look at
    * all nearby boxes. The radius is determined from searchNeighborsSiblingQueue
    * by defining a min-heap on the leaf nodes
    *
    * @param queryPoint a point which is center
    * @param radius     radius of scope
    * @return all points within queryPoint with given radius
    */
  def searchNeighbors(queryPoint: MultiVector, radius: Double): mutable.ListBuffer[(MultiVector,A)] = {
    def searchRecur(
                     queryPoint: MultiVector,
                     radius: Double,
                     node: Node,
                     ret: mutable.ListBuffer[(MultiVector,A)]
    ): Unit = {
      if (node.children == null) {
        ret ++= node.nodeElements filter {x => {queryPoint.distance(x._1) <= radius}}
      } else {
        node.children filter {_.isNear(queryPoint,radius)} foreach {searchRecur(queryPoint,radius,_,ret)}
      }
    }

    val ret = new mutable.ListBuffer[(MultiVector,A)]
    searchRecur(queryPoint, radius, root, ret)
    ret
  }
}
