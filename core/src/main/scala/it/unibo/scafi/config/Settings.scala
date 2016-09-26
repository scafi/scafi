package it.unibo.scafi.config

/**
 * @author Roberto Casadei
 *
 */

abstract class ShapeSettings(val seed: Option[Long] = None)

case class GridSettings(nrows: Int = 10,
                        ncols: Int = 10,
                        stepx: Double = 100,
                        stepy: Double = 80,
                        tolerance: Double = 0,
                        offsetx: Double = 0,
                        offsety: Double = 0) extends ShapeSettings

case class SimpleRandomSettings(min: Double = 0,
                                max: Double = 1000) extends ShapeSettings