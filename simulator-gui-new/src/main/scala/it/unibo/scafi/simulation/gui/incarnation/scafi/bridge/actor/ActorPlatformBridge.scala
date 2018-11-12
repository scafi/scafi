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

package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.actor

import akka.actor.ActorRef
import it.unibo.scafi.simulation.SimulationObserver
import it.unibo.scafi.simulation.gui.controller.logical.ExternalSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.actor.PlatformSimulatorActor.MsgAttachObserver
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiLikeWorld, scafiWorld}

abstract class ActorPlatformBridge extends ExternalSimulation[ScafiLikeWorld]("scafi-bridge") {
  override type EXTERNAL_SIMULATION = ActorRef
  override type SIMULATION_PROTOTYPE = () => EXTERNAL_SIMULATION
  override type SIMULATION_CONTRACT = ExternalSimulationContract
  protected var idsObserved : Set[world.ID] = Set.empty
  protected var simulationObserver = new SimulationObserver[ID,LSNS]
  val world : ScafiLikeWorld = scafiWorld
  /**
    * current simulation prototype, at begging no prototype defined
    */
  var simulationPrototype: Option[SIMULATION_PROTOTYPE] = None
  //scafi execution context
  private var context : Option[CONTEXT=>EXPORT] = None

  def observeExport(id : world.ID): Unit = {
    if(idsObserved.contains(id)) {
      idsObserved -= id
    } else {
      idsObserved += id
    }
  }
  /**
    * @return current running context (if it is defined)
    */
  protected def runningContext : CONTEXT=>EXPORT = {
    require(context.isDefined)
    context.get
  }

  private var simSeed : Option[SimulationInfo] = None

  /**
    * @return the current simulation seed
    */
  def simulationInfo : Option[SimulationInfo] = simSeed

  /**
    * @param simulationSeed the simulation seed used to initialize the simulation
    */
  def simulationInfo_=(simulationSeed: SimulationInfo) : Unit = {
    require(simulationSeed != null)
    simSeed = Some(simulationSeed)
  }
  //describe scafi contract like
  override val contract : ExternalSimulationContract = new ExternalSimulationContract {
    private var currentSimulation : Option[EXTERNAL_SIMULATION] = None
    override def simulation: Option[ActorRef] = this.currentSimulation
    override def initialize(prototype: SIMULATION_PROTOTYPE): Unit = {
      //to initialize the simulation, current simulation must be empty and program must be defined
      require(currentSimulation.isEmpty && simulationInfo.isDefined)
      //create context by program passed
      context = Some(simulationInfo.get.program.newInstance().asInstanceOf[CONTEXT=>EXPORT])
      //create new simulation
      this.currentSimulation = Some(prototype())
      currentSimulation.get ! MsgAttachObserver(simulationObserver)
    }
    override def restart(prototype: SIMULATION_PROTOTYPE): Unit = {
      //to restart simulation current simulation must be defined
      require(currentSimulation.isDefined)
      //create the instance of program
      context = Some(simulationInfo.get.program.newInstance().asInstanceOf[CONTEXT=>EXPORT])
      //set current simulation to another
      this.currentSimulation = Some(prototype())
      simulationObserver.idMoved
      currentSimulation.get ! MsgAttachObserver(simulationObserver)
    }
  }
}

object ActorPlatformBridge {
  /**
    * implicit class used to compute the path level in the tree
    * @param path the path passed
    */
  implicit class RichPath(path : Path) {
    def level : Int = if(path.isRoot) { 0 } else { path.toString.split("/").length + 1 }
  }
}

