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

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.{Demo, SimulationType}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.{Actuator, SimulationInfo}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import lib.{FlockingLib, Movement2DSupport}

object BlobDroneSystemExploration extends App {
  val worldSize = (500,500)
  val radius = 40
  def tupleToWorldSize(tuple : (Double,Double)) = (tuple._1 * worldSize._1, tuple._2 * worldSize._2)
  ScafiProgramBuilder (
    Random(500,worldSize._1,worldSize._1),
    SimulationInfo(program = classOf[BlobDroneSystemExplorationDemo], actuators = List(Actuator.movementDtActuator),
      exportValutations = List.empty),
    RadiusSimulation(radius),
    neighbourRender = true
  ).launch()
}

/**
  * Scenario: the drones explore a region while keeping constant connection with the base.
  *  If the network gets disconnected, the drones are able to go back to the
  *  last known position of the base.
  *   - Sense1: drones
  *   - Sense2: obstacles
  *   - Sense3: base
  */

@Demo(simulationType = SimulationType.MOVEMENT)
class BlobDroneSystemExplorationDemo extends AggregateProgram with SensorDefinitions with FlockingLib with Movement2DSupport with BlockG {

  private val attractionForce: Double = 10.0
  private val alignmentForce: Double = 40.0
  private val repulsionForce: Double = 80.0
  private val repulsionRange: Double = BlobDroneSystemExploration.radius * 60.0 / 100
  private val obstacleForce: Double = 400.0

  private val separationThr = BlobDroneSystemExploration.radius * 80.0
  private val neighboursThr = 4


  override def main(): (Double, Double) = BlobDroneSystemExploration.tupleToWorldSize(rep(randomMovement(), (0.5,0.5))(behaviour)._1)

  private def flockWithBase(myTuple: ((Double, Double),(Double,Double))): ((Double, Double),(Double,Double)) = {
    val myPosition = currentPosition()
    val gradient = distanceTo(sense3)
    val minGradHood = minHood(nbr(gradient))
    val nbrCount: Int = foldhoodPlus(0)(_ + _){1}
    val basePosition: (Double, Double) = broadcast(sense3, (myPosition.x, myPosition.y))
    mux(((gradient - minGradHood) > separationThr | gradient > 100) & nbrCount < neighboursThr) {
      val baseVector = goToPointWithSeparation(myTuple._2, repulsionRange)
      (baseVector, myTuple._2)
    } {
      val flockVector = flock(myTuple._1, Seq(sense1), Seq(sense2), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)

      mux(basePosition._1 > 1.0 | basePosition._2 > 1.0 | (basePosition._1 == 0.0 & basePosition._2 == 0.0)) {
        ((flockVector._1, flockVector._2), myTuple._2)
      } {
        ((flockVector._1, flockVector._2), basePosition)
      }
    }
  }

  private def behaviour(tuple:((Double, Double),(Double,Double))): ((Double, Double),(Double,Double)) = {
    val myPosition = currentPosition()
    mux(sense1){
      flockWithBase(tuple)
    }
    {
      mux(sense3){
        val bp: (Double, Double) = broadcast(sense3, (myPosition.x, myPosition.y))
        ((0.0,0.0), bp)
      }
      {
        val fv = flock(tuple._1, Seq(sense1), Seq(sense2), repulsionRange, 0.0, 0.0, repulsionForce, 0.0)
        (fv, (0.5,0.5))
      }
    }
  }

}