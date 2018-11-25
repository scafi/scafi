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
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ExportEvaluation.EXPORT_EVALUATION
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.reflection.{Demo, SimulationType}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.{MetaActionProducer, SimulationInfo}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.{ScafiInformation, ScafiProgramBuilder}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation.EXPORT

object DroneRescue extends App {
  val worldSize = (500,500)
  val simRadius = 100
  type E = ((Double,Double),Boolean)
  MetaActionProducer.movementDtActionProducer.valueParser = (export : Any) => Some(export.asInstanceOf[E]._1)
  val evaluation : EXPORT_EVALUATION [Boolean] = (e : EXPORT) => e.root().asInstanceOf[E]._2
  ScafiProgramBuilder (
    Random(100,worldSize._1,worldSize._1),
    SimulationInfo(program = classOf[DroneRescueDemo],
      metaActions = List(MetaActionProducer.movementDtActionProducer),
      exportEvaluations = List[EXPORT_EVALUATION[Any]](evaluation)),
    RadiusSimulation(simRadius),
    neighbourRender = true
  ).launch()
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
  *
  * if you want to avoid the drones escape from the scene you can
  * select a set of nodes and mark it as obstacles. they work like boundary (delimit a section of space where the node must be inside of it)
  * select a set of nodes and mark it as drones. the node start to move to find survivors.
  * select a set of nodes and mark it as survivors.
  * with the purple circle (standard color of output) the simulation denote:
  *   - drone need to refuel
  *   - injured is saved
  */

@Demo(simulationType = SimulationType.MOVEMENT)
class DroneRescueDemo extends AggregateProgram with SensorDefinitions with FlockingLib with Movement2DSupport with BlockG  {

  private val attractionForce: Double = 2.0
  private val alignmentForce: Double = 80.0
  private val repulsionForce: Double = 40.0
  private lazy val repulsionRange: Double = ScafiInformation.configuration.simulationInitializer match {
    case RadiusSimulation(radius) => radius * 40.0 / 200
    case _ => 60.0 / 200
  }
  private lazy val width = ScafiInformation.configuration.worldInitializer.size._1
  private lazy val height = ScafiInformation.configuration.worldInitializer.size._2

  private val obstacleForce: Double = 100.0

  private val DRONE_RANGE = 20
  private lazy val BASE_POSITION = (width / 2,height / 2)
  private val INIT_VECTOR = (0.0,0.0)

  private val KIT_DROP_TIME = 300.0
  private val RECHARGE_TIME = 400.0
  private val AID_TIME = 1500.0

  override def main():((Double, Double),Boolean) = {
    val result =  rep(INIT_VECTOR, false)(startSearch)
    (SizeConversion.normalSizeToWorldSize(result._1),result._2)
  }


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