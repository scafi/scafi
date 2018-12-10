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

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.scafi.distrib.actor.MsgAddObserver
import it.unibo.scafi.simulation.SimulationObserver.{MovementEvent, SensorChangedEvent}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._

class ActorSpaceAwareSimulator(override val space: SPACE[ID],
                               override val devs: Map[ID, ScafiWorldIncarnation.DevInfo],
                               val programClass: Class[_])
  extends SpaceAwareSimulator(space = space, devs = devs, simulationSeed = 0, randomSensorSeed = 0) {

  var I: Option[SimulationActorPlatform] = None
  def startPlatform(): Unit = {
    val nbrSns: Map[ID, Map[NSNS, Map[ID, Any]]] =
      devs.keySet.map { id =>
        id -> Set(NBR_RANGE_NAME).map { nsnsName =>
          nsnsName -> neighbourhood(id).map { nid =>
            nid -> space.getDistance(space.getLocation(id), space.getLocation(nid))
          }.toMap
        }.toMap
      }.toMap

    I = Some(LocalSimulationActorPlatform(
      devIds = devs.keySet,
      nbrs = devs.keySet.map(id => id -> (neighbourhood(id) - id)).toMap,
      sensors = devs.keySet.map(id => id -> devs(id).lsns).toMap,
      nbrSensors = nbrSns,
      programClass = programClass
    ))
    I.get.start(Set(("127.0.0.1", 9000)))

    ActorSystem().actorOf(Props(classOf[PlatformObserverActor], this, I.get))
  }

  private def setExport(id: ID, export: EXPORT): Unit = {
    eMap.put(id, export)
  }

  override def setPosition(id: ID, newPos: P): Unit = {
    val previousNbrs = neighbourhood(id) - id
    devs(id).pos = newPos
    space.setLocation(id,newPos)
    val nextNbrs = neighbourhood(id) - id
    updateNbrs(previousNbrs + id ++ nextNbrs)
    updateNbrSensors(previousNbrs + id ++ nextNbrs)
    this.notify(MovementEvent(id))
  }

  override def chgSensorValue[A](name: String, ids: Set[Int], value: A): Unit = {
    ids.foreach(id => {
      devs(id).lsns += name -> value
      if (I.isDefined) {
        I.get.devices(id) ! I.get.MsgLocalSensorValue(name, value)
      }
      this.notify(SensorChangedEvent(id, name))
    })
  }

  private def updateNbrs(ids: Set[ID]): Unit = {
    ids.foreach { id =>
      I.get.devices(id) ! I.get.MsgNeighborhoodUpdate(id, neighbourhood(id).map(nbr => nbr -> I.get.devices(nbr)).toMap)
    }
  }

  private def updateNbrSensors(ids: Set[ID]): Unit = {
    ids.foreach { id =>
      I.get.devices(id) ! I.get.MsgNbrSensorValue(NBR_RANGE_NAME,
        neighbourhood(id).map(nid => nid -> space.getDistance(space.getLocation(id), space.getLocation(nid))).toMap)
    }
  }

  class PlatformObserverActor(platform: SimulationActorPlatform) extends Actor {
    override def preStart(): Unit = {
      super.preStart()
      platform.devices.values.foreach(dev => dev ! MsgAddObserver(self))
    }

    override def receive: Receive = {
      case platform.MsgExport(id, export) => setExport(id, export.asInstanceOf[EXPORT])
      case _ =>
    }
  }
}

import it.unibo.scafi.distrib.actor.p2p.{Platform => P2PActorPlatform}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation

trait SimulationActorPlatform extends P2PActorPlatform with BasicAbstractActorIncarnation {
  var devices: Map[ID, ActorRef] = Map()
  def start(hosts: Set[(String, Int)])
}

case class LocalSimulationActorPlatform(devIds: Set[ID],
                                        nbrs: Map[ID, Set[ID]],
                                        sensors: Map[ID, Map[LSNS, Any]],
                                        nbrSensors: Map[ID, Map[NSNS, Map[ID, Any]]],
                                        programClass: Class[_]) extends SimulationActorPlatform {

  val aggregateAppSettings = AggregateApplicationSettings(
    name = "LocalAggregateSimulation",
    program = () => Some(programClass.newInstance().asInstanceOf[AggregateProgram])
  )

  override def start(hosts: Set[(String, Int)]): Unit = {
    val host: (String, Int) = hosts.head
    val settings: Settings = settingsFactory.defaultSettings().copy(
      aggregate = aggregateAppSettings,
      platform = PlatformSettings(subsystemDeployment = DeploymentSettings(host._1, host._2)),
      deviceConfig = DeviceConfigurationSettings(ids = devIds, nbs = nbrs)
    )
    new BasicMain(settings) {
      override def onDeviceStarted(dm: DeviceManager, sys: SystemFacade): Unit = {
        devices += dm.selfId -> dm.actorRef
        sensors(dm.selfId).foreach { sns => dm.addSensorValue(sns._1, sns._2) }
        nbrSensors(dm.selfId).foreach(sns => dm.actorRef ! MsgNbrSensorValue(sns._1, sns._2))
        dm.start
      }
    }.main(Array())
  }
}

/*case class DistributedSimulationActorPlatform(devIds: Set[ID],
                                              nbrs: Map[ID, Set[ID]],
                                              sensors: Map[ID, Map[LSNS, Any]],
                                              nbrSensors: Map[ID, Map[NSNS, Map[ID, Any]]],
                                              programClass: Class[_]) extends SimulationActorPlatform {

  val aggregateAppSettings = AggregateApplicationSettings(
    name = "DistributedAggregateSimulation",
    program = () => Some(programClass.newInstance().asInstanceOf[AggregateProgram])
  )

  override def start(hosts: Set[(String, Int)]): Unit = {
    val distributedDevices: Map[ID, (String, Int)] = (devIds zip hosts).toMap
    val distributedSettings: Set[Settings] = buildDistributedSettings(distributedDevices)

    distributedSettings.foreach(ds => new BasicMain(ds) {
      override def onDeviceStarted(dm: DeviceManager, sys: SystemFacade): Unit = {
        devices += dm.selfId -> dm.actorRef
        sensors(dm.selfId).foreach { sns => dm.addSensorValue(sns._1, sns._2) }
        nbrSensors(dm.selfId).foreach(sns => dm.actorRef ! MsgNbrSensorValue(sns._1, sns._2))
        dm.start
      }
    }.main(Array()))
  }

  private def buildDistributedSettings(distributedDevs: Map[ID, (String, Int)]): Set[Settings] = {
    def recBuildDistributedSettings(devs: List[(ID, String, Int)], result: Set[Settings]): Set[Settings] = devs match {
      case h::t => recBuildDistributedSettings(t, result + buildSettings(h, t))
      case _ => result
    }
    recBuildDistributedSettings(distributedDevs.map(dd => (dd._1, dd._2._1, dd._2._2)).toList, Set())
  }

  private def buildSettings(dev: (ID, String, Int), subsys: List[(ID, String, Int)]): Settings = {
    settingsFactory.defaultSettings().copy(
      aggregate = aggregateAppSettings,
      platform = PlatformSettings(
        subsystemDeployment = DeploymentSettings(dev._2, dev._3),
        otherSubsystems = subsys.map(other => {
          SubsystemSettings(
            subsystemDeployment = DeploymentSettings(other._2, other._3),
            ids = Set(other._1)
          )
        }).toSet
      ),
      deviceConfig = DeviceConfigurationSettings(
        ids = Set(dev._1),
        nbs = nbrs.map {
          case (i, s) if i == dev._1 => dev._1 -> s
          case (i, _) => i -> Set[ID]()
        }
      )
    )
  }
}*/
