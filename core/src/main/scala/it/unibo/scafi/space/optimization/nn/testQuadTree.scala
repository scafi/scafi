package it.unibo.scafi.space.optimization.nn

import it.unibo.scafi.space.Point3D
import it.unibo.scafi.space.optimization.distances.EuclideanDistanceMetric

import scala.util.Random

//PUT IN TEST PACKAGE
object testQuadTree extends App {
  val random = new Random()
  val maxPoint = 1000
  val tree = new QuadTree[Int](Point3D(0, 0, 0), Point3D(maxPoint, maxPoint, maxPoint), EuclideanDistanceMetric(), 100)

  val point = (0 to 1000) map { x => x -> Point3D(random.nextInt(maxPoint), random.nextInt(maxPoint), random.nextInt(maxPoint)) } toList

  point foreach { x => tree.insert(x._2, x._1) }
  println(tree.elems.size)
  var x = System.nanoTime()
  tree.remove(point(0)._2)
  println(System.nanoTime() - x)
  println(tree.elems.size)
  x = System.currentTimeMillis()
  tree.searchNeighbors(Point3D(10,10,10),10)

  println(System.currentTimeMillis() - x)
  point foreach {x => tree.remove((x._2))}
  println(tree.elems.size)
}
