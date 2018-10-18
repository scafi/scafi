/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
