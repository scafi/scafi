/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.config


abstract class ShapeSettings(val seed: Option[Long] = None)

// scalastyle:off magic.number
case class GridSettings(nrows: Int = 10,
                        ncols: Int = 10,
                        stepx: Double = 100,
                        stepy: Double = 80,
                        tolerance: Double = 0,
                        offsetx: Double = 0,
                        offsety: Double = 0,
                        mapPos: (Int, Int, Double,Double) => (Double,Double) = (_,_,x,y) => (x,y)) extends ShapeSettings

case class SimpleRandomSettings(min: Double = 0,
                                max: Double = 1000) extends ShapeSettings
