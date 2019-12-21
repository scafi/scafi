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
                        mapPos: (Int, Int, Double,Double) => (Double,Double) = (_,_,x,y) => (x,y)) extends ShapeSettings) extends ShapeSettings {

  def to3DPlane: Grid3DSettings =
    Grid3DSettings(nrows, ncols, 1, stepx, stepy, (stepx + stepy)/2, tolerance, offsetx, offsety, (offsetx + offsety)/2)
}

case class Grid3DSettings(nRows: Int = 5,
                          nColumns: Int = 5,
                          nSlices: Int = 5,
                          stepX: Double = 100,
                          stepY: Double = 100,
                          stepZ: Double = 100,
                          tolerance: Double = 0,
                          offsetX: Double = 0,
                          offsetY: Double = 0,
                          offsetZ: Double = 0) extends ShapeSettings

object Grid3DSettings {
  def cube(nodeCountInSide: Int, step: Double, tolerance: Double, offset: Double): Grid3DSettings =
    Grid3DSettings(nodeCountInSide, nodeCountInSide, nodeCountInSide, step, step, step, tolerance, offset, offset, offset)
}

case class SimpleRandomSettings(min: Double = 0,
                                max: Double = 1000) extends ShapeSettings
