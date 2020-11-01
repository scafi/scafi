/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ScafiStandardAggregateProgram, BlockG}
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}
import lib.{FlockingLib, Movement2DSupport}

object DroneRescue extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.DroneRescueDemo" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.1 // neighbourhood radius
  Settings.Sim_NumNodes = 20 // number of nodes
  Settings.Movement_Activator = (b: Any) => b.asInstanceOf[(Double, Double)]
  Settings.To_String = _ => ""
  Settings.Sim_DrawConnections = false
  Settings.Sim_realTimeMovementUpdate = true
  // Settings.Sim_Draw_Sensor_Radius = true
  launch()
}

/**
  * Scenario: search of survivors after natural disasters.
  * When a drone finds a survivor, it stays at that point for some time,
  *  then leaves there a medical kit and goes back to the base to restock.
  * All the nodes know the position of the base of operations (which is fixed)
  *  and so are able to go back to the base without depending on other nodes.
  *   - Sense1: drones looking for survivors
  *   - Sense2: survivors
  *   - Sense3: obstacles
  */
class DroneRescueDemo extends ScafiStandardAggregateProgram with SensorDefinitions with FlockingLib with Movement2DSupport with BlockG  {

  private val attractionForce: Double = 2.0
  private val alignmentForce: Double = 80.0
  private val repulsionForce: Double = 40.0
  private val repulsionRange: Double = 0.04
  private val obstacleForce: Double = 100.0

  private val DRONE_RANGE = 4
  private val BASE_POSITION = (0.2,0.2)
  private val INIT_VECTOR = (0.0,0.0)

  private val KIT_DROP_TIME = 300.0
  private val RECHARGE_TIME = 400.0
  private val AID_TIME = 1500.0

  override def main():(Double, Double) = rep(INIT_VECTOR, false)(startSearch)._1

  /**
    * Sense1 - Drone
    * Sense2 - Survivors
    * tuple._1 - Vector movement
    * tuple._2 - Drone is operative or Injured is saved
    */
  private def startSearch(tuple: ((Double,Double), Boolean)): ((Double,Double), Boolean) = {
    mux(sense1){
      searchTarget(tuple, sense1, sense2)
    }(
      mux(sense2){
        processingTarget(tuple, sense1, sense2)
      }{
        tuple
      }
    )
  }

  private def searchTarget(tuple: ((Double,Double), Boolean), isDrone: Boolean, isTarget: Boolean): ((Double,Double), Boolean) ={
    // Then Drone is operating and distance < DRONE_RANGE to target
    mux(!tuple._2 & distanceTo(isTarget) < DRONE_RANGE){
      val survivorFound = foldhood(false)(_ | _)(nbr(isTarget) & !nbr(tuple._2))

      branch(survivorFound)(
        // The Drone found a target and stop itself
        ((0.0,0.0), timer(KIT_DROP_TIME))
      ) (
        // The Drone flock to find a target
        flock(tuple._1, Seq(isDrone), Seq(sense3), repulsionRange, attractionForce, alignmentForce, repulsionForce,
          obstacleForce), tuple._2
      )

    }(
      //The drone needs to refuel itself
      mux(isDrone && tuple._2){
        branch(distanceToPoint(BASE_POSITION._1, BASE_POSITION._2) < 0.1){
          // Drone is refueling before resuming
          mux(timer(RECHARGE_TIME)){
            (flock(tuple._1, Seq(isDrone), Seq(sense3), repulsionRange, attractionForce, alignmentForce,
              repulsionForce, obstacleForce), false)
          } {
            ((0.0,0.0), tuple._2)
          }
        } {
          (goToPointWithSeparation(BASE_POSITION, repulsionRange), true)
        }
      }{
        // The Drone flock to find a target
        (flock(tuple._1, Seq(isDrone), Seq(sense3), repulsionRange, attractionForce, alignmentForce,
          repulsionForce, obstacleForce), tuple._2)
      }
    )
  }

  private def processingTarget(tuple: ((Double,Double), Boolean), isDrone: Boolean, isTarget: Boolean): ((Double,Double), Boolean) = {
    mux(distanceTo(isDrone) < DRONE_RANGE){
      // The survivor is closer to a drone
      branch(!tuple._2){
        // Target is processing
        mux(timer(AID_TIME)){
          // After 1500 step is processed
          ((0.0,0.0), true)
        }((0.0,0.0),tuple._2)
      }{
        tuple
      }
    }((0.0,0.0), tuple._2)
  }

  // Calculate the distance from current point to a point
  def distanceToPoint(point: (Double,Double)): Double = {
    val pos = currentPosition()
    Math.hypot(point._1 - pos.x, point._2 - pos.y)
  }

}