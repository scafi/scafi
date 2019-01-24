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

package frontend.sims.movement

import frontend.lib.{FlockingLib, Movement2DSupport}
import frontend.sims.{SensorDefinitions, SizeConversion}
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.simulation.s2.frontend.configuration.environment.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.{Demo, SimulationType}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.{MetaActionProducer, SimulationInfo}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.{ScafiProgramBuilder, ScafiWorldInformation}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.s2.frontend.view.ViewSetting
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.StandardFXOutput
import it.unibo.scafi.space.graphics2D.BasicShape2D.{Circle, Polygon}
import it.unibo.scafi.space.{Point2D, SpatialAbstraction}

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
    Random(50,1400,1100),
    SimulationInfo(program = classOf[SupplyRescueDemo], metaActions = List(MetaActionProducer.movementDtActionProducer),exportEvaluations = List.empty),
    RadiusSimulation(100),
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
  *
  * first of all, select a node and mark it as sense4 (base)
  * after that you can select the node that deliver supplies (with sense1)
  * with the selection of a set of nodes marked sense3 the simulation starts.
  * sense3 explore the map, when some exploring node find a sense1 node it brings it to the base.
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
              (goToPointWithSeparation(broadcast(sense3, (currentPosition().x, currentPosition().y)), 10), tuple._2)
            } {
              (flock(tuple._1, Seq(sense1), Seq(sense2), 5, 1.0, 20.0, 20.0, 0.0), tuple._2)
            }
          }
        }
      }{
        (goToPoint(base), tuple._2)
      }
    }{
      mux(sense3){
        branch(distanceTo(sense4) > 40){
          mux(timer(600.0))
          {
            (goToPointWithSeparation((base), 50), tuple._2)
          } {
            (movement(tuple._1), tuple._2)
          }
        } {
          (flock(tuple._1, Seq(sense3), Seq(sense2), 5, 10.0, 40.0, 120.0, 0.0), tuple._2)
        }
      }
      {
        mux(sense4)
        {
          (goToPoint(base), tuple._2)
        } {
          (flock(tuple._1, Seq(sense1), Seq(sense2), 5, 0.0, 0.0, 3.0, 0.0), tuple._2)
        }
      }
    }
  }
}