/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.space

import it.unibo.scafi.config.{GridSettings, SimpleRandomSettings}

import scala.language.postfixOps
import scala.util.Random

object SpaceHelper {

  def randomLocations(rs: SimpleRandomSettings,
                      n: Int,
                      seed: Long = System.currentTimeMillis()): List[Point2D] = {
    val rand = new Random(seed)
    (1 to n) map { i => Point2D(rs.min + rand.nextDouble() * (rs.max - rs.min),
      rs.min + rand.nextDouble() * (rs.max - rs.min)) } toList
  }

  def gridLocations(gs: GridSettings,
                    seed: Long = System.currentTimeMillis()): List[Point2D] = {
    val rand = new Random(seed)
    val tolerance = gs.tolerance * 2 // doubled because can be positive or negative

    val positions = for(
      ncols <- 0 to (gs.ncols - 1);
      nrows <- 0 to (gs.nrows - 1);
      idealx = ncols * gs.stepx + gs.offsetx;
      idealy = nrows * gs.stepy + gs.offsety;
      rx = rand.nextDouble();
      ry = rand.nextDouble();
      x = idealx + (rx*tolerance - tolerance/2);
      y = idealy + (ry*tolerance - tolerance/2)
    )
      yield Point2D(x,y)

    positions.toList
  }
}
