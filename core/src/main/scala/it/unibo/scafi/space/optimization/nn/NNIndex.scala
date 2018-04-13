package it.unibo.scafi.space.optimization.nn

import it.unibo.scafi.space.Point3D

import scala.collection.mutable

/**
  * a data structure used to search the neighbour
  * @tparam A the value ok key
  */
trait NNIndex[A] extends mutable.Map[Point3D,A]{
  /**
    * search the neighbours near a point p
    * @param p the center of search
    * @param r the radius
    */
  def neighbours(p : Point3D, r : Double) : Iterable[(Point3D,A)]

  def ++=(elems : Iterable[(Point3D,A)]) : this.type

  final def <--> (p : Point3D, r: Double) = neighbours(p,r)
}

object NNIndex {
  def apply[A](elems : Iterable[(A,Point3D)]) : NNIndex[A] = QuadTree(elems)
}