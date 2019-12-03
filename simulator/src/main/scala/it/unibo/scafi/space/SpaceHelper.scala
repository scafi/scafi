/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.space

import it.unibo.scafi.config.{Grid3DSettings, GridSettings, SimpleRandomSettings}

import scala.language.postfixOps
import scala.util.Random

object SpaceHelper {

  def randomLocations(rs: SimpleRandomSettings,
                      n: Int,
                      seed: Long = System.currentTimeMillis()): List[Point2D] = {
    val random = new Random(seed)
    (1 to n) map (_ => Point2D(randomLocation(rs, random), randomLocation(rs, random))) toList
  }

  private def randomLocation(rs: SimpleRandomSettings, random: Random): Double =
    rs.min + random.nextDouble() * (rs.max - rs.min)

  def random3DLocations(settings: SimpleRandomSettings, locationCount: Int,
                        seed: Long = System.currentTimeMillis()): List[Point3D] = {
    val random = new Random(seed)
    (1 to locationCount).map(_ => Point3D(randomLocation(settings, random),
                              randomLocation(settings, random),
                              randomLocation(settings, random))).toList
  }

  def gridLocations(gs: GridSettings, seed: Long = System.currentTimeMillis()): List[Point2D] =
    grid3DLocations(gs.to3D, seed).map(point => new Point2D(point.x, point.y))

  // the tolerance is doubled because it can be positive or negative
  def grid3DLocations(settings: Grid3DSettings, seed: Long = System.currentTimeMillis()): List[Point3D] =
    get3DPositions(new Random(seed), settings.tolerance*2, settings, seed).toList

  private def get3DPositions(random: Random, variance: Double, settings: Grid3DSettings, seed: Long): Seq[Point3D] =
    for(columnIndex <- 0 until settings.nColumns;
        rowIndex <- 0 until settings.nRows;
        sliceIndex <- 0 until settings.nSlices;
        idealX = getIdealCoordinate(columnIndex, settings.stepX, settings.offsetX);
        idealY = getIdealCoordinate(rowIndex, settings.stepY, settings.offsetY);
        idealZ = getIdealCoordinate(sliceIndex, settings.stepZ, settings.offsetZ);
        randomPoint = new Point3D(random.nextDouble(), random.nextDouble(), random.nextDouble());
        x = getCoordinate(idealX, randomPoint.x, variance);
        y = getCoordinate(idealY, randomPoint.y, variance);
        z = getCoordinate(idealZ, randomPoint.z, variance)
        ) yield Point3D(x, y, z)

  private def getIdealCoordinate(index: Int, step: Double, offset: Double): Double = index * step + offset

  private def getCoordinate(idealCoordinate: Double, randomComponent: Double, variance: Double): Double =
    idealCoordinate + (randomComponent*variance - variance/2)
}
