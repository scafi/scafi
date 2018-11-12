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

import akka.actor.{ActorRef, ActorSystem}
import it.unibo.scafi.distrib.actor.p2p.{Platform => P2PActorPlatform}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge._
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.actor.PlatformSimulatorActor.{MsgNeighborhood, MsgObserveDevices}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.scafiWorld

trait SimulationActorPlatform extends P2PActorPlatform with BasicAbstractActorIncarnation {
  var devices: Map[ID, ActorRef] = Map()
  def start(): Unit
}

object SimulationActorPlatform {
  def apply(devIds: Set[ID],
            nbrs: Map[ID, Set[ID]],
            sensors: Map[String, Boolean],
            programClass: Class[_]): SimulationActorPlatform =

    new SimulationActorPlatform() {
      val aggregateAppSettings = AggregateApplicationSettings(
        name = "AggregateSimulation",
        program = () => Some(programClass.newInstance().asInstanceOf[AggregateProgram])
      )
      val settings: Settings = settingsFactory.defaultSettings().copy(
        aggregate = aggregateAppSettings,
        platform = PlatformSettings(subsystemDeployment = DeploymentSettings()),
        deviceConfig = DeviceConfigurationSettings(ids = devIds, nbs = nbrs)
      )
      override def start(): Unit = new BasicMain(settings) {
        override def onDeviceStarted(dm: DeviceManager, sys: SystemFacade): Unit = {
          devices += dm.selfId -> dm.actorRef
          sensors.foreach { sns => dm.addSensorValue(sns._1, false) }
          if (dm.selfId == 965) {
            dm.addSensorValue("sens1", true)
          }
          dm.start
        }
      }.main(Array())
    }
}

object ActorPlatformInitializer {
  case class RadiusSimulation(radius: Double = 0.0) extends ScafiSimulationInitializer {
    val actorSystem = ActorSystem()
    def create(scafiSimulationSeed: SimulationInfo): ActorPlatformBridge = {
      val bridge = ActorPlatformSimulationExecutor
      val nodes: Map[ID, P] = bridge.world.nodes.map { n => n.id -> new P(n.position.x, n.position.y, n.position.z) }.toMap
      bridge.space = Some(new QuadTreeSpace(nodes, radius, scafiWorld.boundary))
      val sensors = bridge.world.nodes.head.devices.filter(_.name.contains("sens")).map(s => s.name -> s.value).toMap
      val platform = SimulationActorPlatform(
        devIds = bridge.world.nodes.map(_.id),
        nbrs = bridge.world.nodes.map(n => n.id -> bridge.space.get.getNeighbors(n.id).toSet).toMap,
        sensors = sensors,
        programClass = scafiSimulationSeed.program
      )
      platform.start()
      val platformActor = actorSystem.actorOf(PlatformSimulatorActor.props(platform))
      platformActor ! MsgObserveDevices()
      bridge.simulationPrototype = Some(() => platformActor)
      bridge.simulationInfo = scafiSimulationSeed
      bridge
    }
  }
}
