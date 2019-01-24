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

package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.space.Point2D

import scala.util.Random

trait Movement2DSupport { self: AggregateProgram with StandardSensors =>
  lazy val random: Random = sense[Random](LSNS_RANDOM)

  def randomMovement(): (Double, Double) = {
    ((random.nextDouble() - 0.5) / 1000, (random.nextDouble() - 0.5) / 1000)
  }

  def movement(lastVector: (Double,Double)): (Double,Double) = {
    if(random.nextDouble() < 0.02){
      val x = lastVector._1 + ((random.nextDouble() - 0.5) / 500.0)
      val y = lastVector._2 + ((random.nextDouble() - 0.5) / 500.0)
      val i = math.hypot(x, y) * 1000
      (x/i, y/i)
    } else {
      lastVector
    }
  }

  def clockwiseRotation(centerX: Double, centerY: Double): (Double,Double) = {
    val currentPos = currentPosition()
    val center = new Point2D(centerX, centerY)
    val pointPosition: Point2D = new Point2D(currentPos.x, currentPos.y)
    var angle = getAngleToCenter(center, pointPosition)
    angle += 1

    this.rotate(angle, center, pointPosition)
  }

  def anticlockwiseRotation(centerX: Double, centerY: Double): (Double,Double) = {
    val currentPos = currentPosition()
    val center = new Point2D(centerX, centerY)
    val pointPosition: Point2D = new Point2D(currentPos.x, currentPos.y)
    var angle = getAngleToCenter(center, pointPosition)
    angle -= 1

    this.rotate(angle, center, pointPosition)
  }

  def goToPoint(point: (Double,Double)): (Double,Double) = {
    val currentPos = currentPosition()
    val distance: Double = math.hypot(point._1 - currentPos.x, point._2 - currentPos.y)

    val newX = point._1 - currentPos.x
    val newY = point._2 - currentPos.y
    val i = math.hypot(newX,newY) * 1000

    if(distance < 0.005){
      (0.0,0.0)
    } else {
      (newX/i, newY/i)
    }
  }

  private def rotate(angle: Double, center: Point2D, pointPosition: Point2D): (Double,Double) = {
    val dist = Math.hypot(center.x - pointPosition.x, center.y - pointPosition.y)

    val newX: Double = center.x + dist * math.cos(angle * math.Pi / 180.0)
    val newY: Double = center.y + dist * math.sin(angle * math.Pi / 180.0)

    (newX - pointPosition.x, newY - pointPosition.y)
  }

  private def getAngleToCenter(center: Point2D, positionPoint: Point2D): Double = {
    var angle = math.atan2(positionPoint.y - center.y, positionPoint.x - center.x) * 180.0 / math.Pi
    if(angle < 0){
      angle + 360
    } else {
      angle
    }
  }

  def implicitMin[V: Numeric](a: V, b: V): V = implicitly[Numeric[V]].min(a, b)
  def implicitMax[V: Numeric](a: V, b: V): V = implicitly[Numeric[V]].max(a, b)

  def T[V: Numeric](initial: V)(floor: V)(decay: V => V): V = {
    rep(initial) { v => implicitMin(initial, implicitMax(floor, decay(v))) }
  }

  def linearFlow(time: Double): Double = T(time)(0.0)(v => v - 1)

  def timer(time: Double): Boolean =  linearFlow(time) == 0.0
}
