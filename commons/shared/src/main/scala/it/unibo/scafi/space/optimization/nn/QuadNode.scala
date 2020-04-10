package it.unibo.scafi.space.optimization.nn

import it.unibo.scafi.space.Point3D
import it.unibo.scafi.space.optimization._


import scala.annotation.tailrec
import scala.collection.mutable

private[nn] class QuadNode[A] (center: Point3D,
                   width: Point3D,
                   val tree : QuadTree[A],
                   var children: Seq[QuadNode[A]]){
  override def toString: String = s"center $center width $width"
  private val Dim = 3
  val nodeElements : mutable.ListBuffer[(Point3D,A)] = mutable.ListBuffer[(Point3D,A)]()

  /** Tests whether the queryPoint is in the node, or a child of that node
    *
    * @param queryPoint a point to test
    * @return whether the given point is in the node, or a child of this node
    */
  def contains(queryPoint: Point3D): Boolean = overlap(queryPoint,0.0)

  /** Tests if queryPoint is within a radius of the node
    *
    * @param queryPoint a point to test
    * @param radius     radius of test area
    * @return whether the given point is in the area
    */
  def overlap(queryPoint: Point3D, radius: Double): Boolean = {
    def testAxis(x : Double, center : Double, width : Double) : Boolean = (x - radius <= center + width / 2) && (x + radius >= center - width / 2)
    testAxis(queryPoint.x,center.x,width.x) && testAxis(queryPoint.y,center.y,width.y) && testAxis(queryPoint.z,center.z,width.z)
  }


  /** Tests if queryPoint is near a node
    *
    * @param queryPoint a point to test
    * @param radius     radius of covered area
    */
  def isNear(queryPoint: Point3D, radius: Double): Boolean = minDist(queryPoint) < radius

  /** minDist is defined so that every point in the box has distance to queryPoint greater
    * than minDist (minDist adopted from "Nearest Neighbors Queries" by N. Roussopoulos et al.)
    * @param queryPoint
    */
  def minDist(queryPoint: Point3D): Double = {
    def computeDist(p : Double, c : Double, w : Double) = {
      if (p < c - w / 2) {
        math.pow(p - c + w / 2,2)
      } else if (p > c + w / 2) {
        math.pow(p - c - w / 2,2)
      } else {
        0.0
      }
    }
    math.sqrt(computeDist(queryPoint.x,center.x,width.x) + computeDist(queryPoint.y,center.y,width.y) + computeDist(queryPoint.z,center.z,width.z))
  }

  /** Finds which child queryPoint lies in. node.children is a Seq[Node], and
    * [[whichChild]] finds the appropriate index of that Seq.
    *
    * @param queryPoint
    * @return
    */
  def whichChild(queryPoint: Point3D): Int = {
    //the weight in the algorithm
    val x = 0
    val y = 1
    val z = 2
    val pow = 2
    def computeAxis(p : Double, c : Double, weight : Int) = {
      if(p > c) {
        math.pow(pow,Dim - 1 - weight).toInt
      } else {
        0
      }
    }
    computeAxis(queryPoint.x,center.x,x) + computeAxis(queryPoint.y,center.y,y) + computeAxis(queryPoint.z,center.z,z)
  }

  /** Makes children nodes by partitioning the box into equal sub-boxes
    * and adding a node for each sub-box
    */
  def makeChildren() {
    val centerClone = center.copy
    val cPart = partitionBox(centerClone, width)
    val mappedWidth = width * 0.5
    children = cPart.map(p => new QuadNode[A](p, mappedWidth, tree,Seq.empty))
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
  def partitionBox(center: Point3D, width: Point3D): Seq[Point3D] = {
    @tailrec
    def partitionHelper(box: Seq[Point3D], dim: Int): Seq[Point3D] = {
      if (dim >= Dim) {
        box
      } else {
        val newBox = box.flatMap { vector =>
          val (up, down) = (vector.copy, vector)
          Seq(up.update(dim, up(dim) - width(dim) / 4), down.update(dim, down(dim) + width(dim) / 4))
        }
        partitionHelper(newBox, dim + 1)
      }
    }
    partitionHelper(Seq(center), 0)
  }

}
