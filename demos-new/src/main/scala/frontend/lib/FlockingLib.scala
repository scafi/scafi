/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.space.Point3D

import scala.util.Random

trait FlockingLib extends FieldUtils with StandardSensors {
  self: AggregateProgram =>
  import includingSelf._

  lazy val r: Random = sense[Random](LSNS_RANDOM)
  val SCALE = 1000

  def flock(lastVec: (Double, Double), flockingSensor: Seq[Boolean], obstacleSensor: Seq[Boolean], separationDistance: Double,
            attractionForce: Double, alignmentForce: Double, repulsionForce: Double, obstacleForce: Double): (Double, Double) = {

    val n: Map[Int,Point3D] = reifyField[Point3D](nbrVector())
    val activeNodes = foldhood[Seq[Point3D]](Seq[(Point3D)]())(_ ++ _){
      mux(nbr(getBooleanResult(flockingSensor)))(Seq(n(nbr(mid()))))(Seq())
    }

    val vectorRepulsion: (Double, Double) = this.separation(separationDistance, n)
    val vectorAlignment: (Double, Double) = this.alignment(lastVec, flockingSensor)
    val vectorAttraction: (Double, Double) = this.cohesion(activeNodes)
    val vectorObstacle: (Double, Double) = this.obstacle(obstacleSensor, n)

    val vectorX: Double = (vectorAttraction._1.toDouble * attractionForce
      + vectorRepulsion._1.toDouble * repulsionForce
      + vectorAlignment._1.toDouble * alignmentForce
      + vectorObstacle._1.toDouble * obstacleForce)
    val vectorY: Double = (vectorAttraction._2.toDouble * attractionForce
      + vectorRepulsion._2.toDouble * repulsionForce
      + vectorAlignment._2.toDouble * alignmentForce
      + vectorObstacle._2.toDouble * obstacleForce)

    val (newX, newY) = normalizeToScale(vectorX, vectorY)
    (newX * 2, newY * 2)
  }

  private def obstacle(obstacleSensor: Seq[Boolean], n: Map[ID, Point3D]): (Double, Double) = {
    val obstaclesVector = foldhood[Seq[Point3D]](Seq[(Point3D)]())(_ ++ _){
      mux(nbr(getBooleanResult(obstacleSensor)))(Seq(n(nbr(mid()))))(Seq())
    }.fold(new Point3D(0.0,0.0,0.0))((a,b) => new Point3D(a.x + b.x, a.y + b.y, 0.0))
    val normObstacle = normalize(obstaclesVector.x, obstaclesVector.y)
    (-normObstacle._1, -normObstacle._2)
  }

  private def alignment(lastVec: (Double, Double), flockingSensor: Seq[Boolean]): (Double, Double) = {
    val alignmentVector: (Double, Double) =
      foldhood((0.0, 0.0))((d1, d2) => (d1._1 + d2._1, d1._2 + d2._2)){
        nbr(mux(getBooleanResult(flockingSensor)) {
          lastVec
        } {
          (.0, .0)
        })
      }
    normalize(alignmentVector._1, alignmentVector._2)
  }

  private def cohesion(neighbors: Seq[Point3D]): (Double, Double) = {
    val cohesionVector = neighbors.fold(new Point3D(0.0,0.0,0.0))((a,b) => new Point3D(a.x + b.x, a.y + b.y, 0.0))
    normalize(cohesionVector.x, cohesionVector.y)
  }

  private def separation(separationDistance: Double, neighbors: Map[ID, Point3D]): (Double,Double) = {
    val closestNeighbours = neighbors.filter(p => {
      val hyp = Math.hypot(p._2.x, p._2.y)
      hyp < separationDistance && hyp != 0
    })
    val separationVector = closestNeighbours.values.fold(new Point3D(0.0,0.0,0.0))((a,b) => new Point3D(a.x + b.x, a.y + b.y, 0.0))
    val normSeparation = normalize(separationVector.x, separationVector.y)
    (-normSeparation._1 * SCALE, -normSeparation._2 * SCALE)
  }

  def normalize(x: Double, y: Double): (Double, Double) = {
    mux(x == 0.0 && y == 0.0){
      (0.0, 0.0)
    } {
      val hyp: Double = math.hypot(x, y)
      val newX: Double = x / hyp
      val newY: Double = y / hyp
      (newX, newY)
    }
  }

  def normalizeToScale(x: Double, y: Double): (Double, Double) = {
    mux(x == 0.0 && y == 0.0) {
      (0.0, 0.0)
    } {
      val hyp: Double = math.hypot(x, y)
      val newX: Double = x / hyp
      val newY: Double = y / hyp
      (newX / SCALE, newY / SCALE)
    }
  }

  def goToPointWithSeparation(point: (Double,Double), separationDistance: Double): (Double,Double) = {
    val neighbors = reifyField(nbrVector())

    val currentPos = currentPosition()

    val toPointVectorX = point._1 - currentPos.x
    val toPointVectorY = point._2 - currentPos.y

    val separationVector: (Double,Double) = this.separation(separationDistance, neighbors)

    val x: Double = toPointVectorX + separationVector._1
    val y: Double = toPointVectorY + separationVector._2

    val (newX, newY) = normalizeToScale(x, y)
    (newX * 2, newY * 2)
  }

  def getBooleanResult(sensor: Seq[Boolean]): Boolean =
    sensor.fold(false)((sensor1, sensor2) => sensor1 || sensor2)
}
