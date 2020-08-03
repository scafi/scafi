package it.unibo.utils

object Filters {

  def expFilter(value: Double, factor: Double): Double =
    (value * factor) + (value * (1 - factor))

}
