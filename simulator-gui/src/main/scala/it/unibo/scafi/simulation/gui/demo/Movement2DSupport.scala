package it.unibo.scafi.simulation.gui.demo

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, LSNS_RANDOM, _}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.{Random => RandomWorld}
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
}
