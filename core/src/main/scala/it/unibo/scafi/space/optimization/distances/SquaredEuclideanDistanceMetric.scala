package it.unibo.scafi.space.optimization.distances

import it.unibo.scafi.space.optimization.helper.MultiVector

class SquaredEuclideanDistanceMetric extends DistanceMetric {
  override def distance(a: MultiVector, b: MultiVector): Double = {
    checkValidArguments(a, b)
    (0 until a.size).map(i => math.pow(a(i) - b(i), 2)).sum
  }
}

object SquaredEuclideanDistanceMetric {
  def apply() = new SquaredEuclideanDistanceMetric()
}
