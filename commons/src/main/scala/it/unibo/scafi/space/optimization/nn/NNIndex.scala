package it.unibo.scafi.space.optimization.nn

import it.unibo.scafi.space.Point3D

import scala.collection.mutable

/**
  * a data structure used to search the neighbour
  * you can add elements like map structure:
  * <pre>
  * @{code
  *   val index[Int] = NNIndex(Map.empty)
  *   index += Point3D -> 0
  *
  *   val elems = (0 to 100) map {x => Point3D(math.random,math.random,math.random) -> x}
  *
  *   index ++= elems
  * }
  * </pre>
  * you can find neighbours using in this way:
  * <pre>
  * @{code
  *   val radiusSearch = 10
  *   val neighbour = index.neighbours(Point3D(0,0,0),radiusSearch)
  * }
  * </pre>
  * @tparam A the value ok key
  */
trait NNIndex[A] extends mutable.Map[Point3D,A]{
  /**
    * search the neighbours near a point p
    * @param p the center of search
    * @param r the radius
    */
  def neighbours(p : Point3D, r : Double) : Iterable[(Point3D,A)]

  /**
    * allow to add a set of elements in the neighbour index
    * @param elems the elements to add in the data structure
    * @return the data structure
    */
  def ++=(elems : Iterable[(Point3D,A)]) : this.type
}

object NNIndex {
  /**
    *
    * @param elems
    * @tparam A
    * @return
    */
  def apply[A](elems : Iterable[(A,Point3D)]) : NNIndex[A] = QuadTree(elems)


  def apply[A](elems : Iterable[(A,Point3D)], min : Point3D, max : Point3D) : NNIndex[A]= {
    val tree : NNIndex[A] = QuadTree(min,max,elems.size)
    tree ++= elems.map{ x => x._2 -> x._1}
    tree
  }
}
