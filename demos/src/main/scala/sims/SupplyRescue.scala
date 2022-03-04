/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}
import lib.{FlockingLib, Movement2DSupport}

object SupplyRescue extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.SupplyRescueDemo" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  //Settings.Sim_Topology = Topologies.Grid_HighVar
  Settings.Sim_NbrRadius = 0.1 // neighbourhood radius
  Settings.Sim_NumNodes = 50 // number of nodes
  //Settings.Led_Activator = (b: Any) => b.asInstanceOf[Boolean]
  Settings.Movement_Activator = (b: Any) => b.asInstanceOf[(Double, Double)]
  //Settings.To_String = (b: Any) => ""
  Settings.Sim_DrawConnections = true
  Settings.Sim_realTimeMovementUpdate = false
  //Settings.Sim_Draw_Sensor_Radius = true
  launch()
}

/**
  * Scenario: military mission where drones have to retrieve supplies/provisions
  *   parachuted in the region.
  *    - Sense1 - Nodes that deliver supplies
  *    - Sense2 - Obstacle
  *    - Sense3 - Nodes that retrieve supplies and bring them to the base
  *    - Sense4 - Base
  */
class SupplyRescueDemo extends AggregateProgram with SensorDefinitions with FlockingLib with Movement2DSupport with BlockG {

  override def main():(Double, Double) = rep({
    (0.0, 0.0)
  }, true)(behavior)._1

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
