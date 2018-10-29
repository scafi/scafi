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

package frontend.sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.simulation.frontend.configuration.environment.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.reflection.{Demo, SimulationType}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.{MetaActionProducer, SimulationInfo}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.{ScafiProgramBuilder, ScafiWorldInformation}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.frontend.view.ViewSetting
import it.unibo.scafi.simulation.frontend.view.scalaFX.drawer.StandardFXOutput
import it.unibo.scafi.space.graphics2D.BasicShape2D.{Circle, Polygon}
import it.unibo.scafi.space.{Point2D, SpatialAbstraction}
import frontend.lib.{FlockingLib, Movement2DSupport}

import scalafx.scene.paint.Color

object SupplyRescue extends App {

  /*
  SenseImageFXOutput.addRepresentation(SensorName.sensor1, "file:///sensor1")
  SenseImageFXOutput.addRepresentation(SensorName.sensor3, "file:///sensor2")
  SenseImageFXOutput.addRepresentation(SensorName.sensor4, "file:///sensor3")
  */
  ViewSetting.backgroundColor = Color.Beige
  val externalBound = Polygon(orientation = 0, Point2D(0,0), Point2D(400,0),Point2D(400,200),Point2D(700,200),Point2D(700,0),
    Point2D(1400,0),Point2D(1400,1100),Point2D(400,1100), Point2D(400,900),Point2D(0,900))
  val worldBound = SpatialAbstraction.Bound(externalBound)
  ScafiProgramBuilder (
    Random(50,2000,2000),
    SimulationInfo(program = classOf[SupplyRescueDemo], metaActions = List(MetaActionProducer.movementDtActionProducer),exportValutations = List.empty),
    RadiusSimulation(500),
    neighbourRender = true,
    scafiWorldInfo = ScafiWorldInformation(boundary = Some(worldBound),
      shape = Some(Circle(20))),
    outputPolicy = StandardFXOutput, /*use SenseImageFXOutput to see node image*/
    performance = NearRealTimePolicy
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
  private val base = (1300.0,400.0)
  override def main():(Double, Double) = SizeConversion.normalSizeToWorldSize(rep({(0.0, 0.0)}, true)(behavior)._1)

  private def behavior(tuple: ((Double,Double), Boolean)): ((Double,Double), Boolean) = {
    mux(sense1){
      mux(tuple._2){
        var grad = distanceTo(sense3)
        mux(distanceTo(sense4) < 25){
          (goToPoint(base), false)
        } {
          mux(grad > 400) {
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
        (goToPoint(base), tuple._2)
      }
    }{
      mux(sense3){
        branch(distanceTo(sense4) > 5){
          mux(timer(600.0))
          {
            (goToPointWithSeparation((base), 0.1), tuple._2)
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
          (goToPoint(base), tuple._2)
        } {
          (flock(tuple._1, Seq(sense1), Seq(sense2), 0.01, 0.0, 0.0, 3.0, 0.0), tuple._2)
        }
      }
    }
  }
}