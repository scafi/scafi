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
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.{Actuator, SimulationInfo}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.{Demo, SimulationType}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import lib.{FlockingLib, Movement2DSupport}

object SupplyRescue extends App {
  val worldSize = (500,500)
  val simRadius = 140
  def tupleToWorldSize(tuple : (Double,Double)) = (tuple._1 * worldSize._1, tuple._2 * worldSize._2)
  ScafiProgramBuilder (
    Random(50,worldSize._1,worldSize._1),
    SimulationInfo(program = classOf[SupplyRescueDemo], actuators = List(Actuator.movementDtActuator),exportValutations = List.empty),
    RadiusSimulation(simRadius),
    neighbourRender = true
  ).launch()
}

/**
  * Scenario: military mission where drones have to retrieve supplies/provisions
  *   parachuted in the region.
  *    - Sense1 - Nodes that deliver supplies
  *    - Sense2 - Obstacle
  *    - Sense3 - Nodes that retrieve supplies and bring them to the base
  *    - Sense4 - Base
  */

@Demo(simulationType = SimulationType.MOVEMENT)
class SupplyRescueDemo extends AggregateProgram with SensorDefinitions with FlockingLib with Movement2DSupport with BlockG {

  override def main():(Double, Double) = SupplyRescue.tupleToWorldSize(rep({
    (0.0, 0.0)
  }, true)(behavior)._1)

  private def behavior(tuple: ((Double,Double), Boolean)): ((Double,Double), Boolean) = {
    mux(sense1){
      mux(tuple._2){
        var grad = distanceTo(sense3)
        mux(distanceTo(sense4) < 9){
          (goToPoint(0.5,0.5), false)
        } {
          mux(grad > 100) {
            ((0.0,0.0), tuple._2)
          } {
            mux(grad > 9){
              (goToPointWithSeparation(broadcast(sense3, (currentPosition().x, currentPosition().y)), 0.02), tuple._2)
            } {
              (flock(tuple._1, Seq(sense1), Seq(sense2), 0.02, 1.0, 20.0, 20.0, 0.0), tuple._2)
            }
          }
        }
      }{
        (goToPoint(0.5,0.5), tuple._2)
      }
    }{
      mux(sense3){
        branch(distanceTo(sense4) > 5){
          mux(timer(600.0))
          {
            (goToPointWithSeparation((0.5,0.5), 0.02), tuple._2)
          } {
            (movement(tuple._1), tuple._2)
          }
        } {
          (flock(tuple._1, Seq(sense3), Seq(sense2), 0.01, 10.0, 40.0, 120.0, 0.0), tuple._2)
        }
      }
      {
        mux(sense4)
        {
          (goToPoint(0.5,0.5), tuple._2)
        } {
          (flock(tuple._1, Seq(sense1), Seq(sense2), 0.01, 0.0, 0.0, 3.0, 0.0), tuple._2)
        }
      }
    }
  }
}