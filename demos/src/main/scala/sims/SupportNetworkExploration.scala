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

object SupportNetworkExploration extends App {
  val worldSize = (500,500)
  val simRadius = 20
  def tupleToWorldSize(tuple : (Double,Double)) = (tuple._1 * worldSize._1, tuple._2 * worldSize._2)
  ScafiProgramBuilder (
    Random(1000,worldSize._1,worldSize._1),
    SimulationInfo(program = classOf[SupportNetworkExplorationDemo], actuators = List(Actuator.movementDtActuator),exportValutations = List.empty),
    RadiusSimulation(simRadius),
    neighbourRender = true
  ).launch()
}

/**
  * Scenario: exploration from a fixed or mobile base.
  *  The nodes should explore the space without losing connections with the base;
  *  when this happens, they should return to the base.
  *  Other nodes build flocks with loose rules, sporadically interacting with the main network.
  *   - sense1: nodes of the main network;
  *   - sense2: obstacles.
  *   - sense3: base of operations;
  *   - sense4: explorer nodes, connected to the network.
  */

@Demo(simulationType = SimulationType.MOVEMENT)
class SupportNetworkExplorationDemo extends AggregateProgram with SensorDefinitions with FlockingLib with Movement2DSupport with BlockG {

  private val attractionForce: Double = 10.0
  private val alignmentForce: Double = 40.0
  private val repulsionForce: Double = 80.0
  private val repulsionRange: Double = SupportNetworkExploration.simRadius * 60.0 / 100
  private val obstacleForce: Double = 100.0
  private val separationThr: Double = SupportNetworkExploration.simRadius * 80.0
  private val neighboursThr: Int = 7
  private val SPACE_DIMENSION: Int = 100

  override def main():(Double, Double) = SupportNetworkExploration.tupleToWorldSize(rep(randomMovement(), (0.5, 0.5))(round)._1)

  private def behaviourSense1(tuple: ((Double, Double), (Double, Double))) = {
    var gradient = Double.MaxValue
    var minGradHood = 0.0
    branch(sense4)() {
      gradient = distanceTo(sense3)
      minGradHood = minHood(nbr(gradient))
    }
    mux((gradient - minGradHood) > separationThr | gradient > SPACE_DIMENSION) ((goToPointWithSeparation(tuple._2,repulsionRange), tuple._2)) {
      var temp = flock(tuple._1, Seq(sense1), Seq(sense2), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
      temp = ((temp._1 + .001)/2, (temp._2 + .001)/2)
      val basePosition: (Double, Double) = branch(sense4){(0.0,0.0)}{broadcast(sense3, (currentPosition().x, currentPosition().y))}
      mux(basePosition._1 > 1.0 | basePosition._2 > 1.0 | (basePosition._1 == 0.0 & basePosition._2 == 0.0))((temp, tuple._2))((temp, basePosition))
    }
  }

  private def behaviourSense4(tuple: ((Double, Double), (Double, Double))) = {
    val gradient = distanceTo(sense1)
    val minGradHood = minHood(nbr(gradient))
    val nbrCount: Int = foldhoodPlus(0)(_ + _){1}
    mux(((gradient - minGradHood) > separationThr * 2 | gradient > SPACE_DIMENSION) & nbrCount < neighboursThr) {
      val s = goToPointWithSeparation(tuple._2, repulsionRange)
      (s, tuple._2)
    } {
      val temp = flock(tuple._1, Seq(sense4), Seq(sense1), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
      val p: (Double, Double) = broadcast(sense1, (currentPosition().x, currentPosition().y))
      mux(p._1 > 1.0 | p._2 > 1.0 | (p._1 == 0.0 & p._2 == 0.0)) {
        ((temp._1, temp._2), tuple._2)
      } {
        ((temp._1, temp._2), p)
      }
    }
  }

  private def round(tuple: ((Double, Double), (Double, Double))): ((Double, Double), (Double, Double)) =
    mux(sense1) {
      behaviourSense1(tuple)
    } (
      mux(sense4){
        behaviourSense4(tuple)
      } {
        mux(sense3){
          ((.0,.0),(.0,.0))
        } {
          (flock(tuple._1, Seq(sense1), Seq(sense2), 0.04, 0.0, 0.0, 10.0, 0.0), tuple._2)
        }
      }
    )

}