/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package demos

/**
  * Demo 6-A
  * - Client/server system
  * - (Dynamic) "Spatial" network
  * - Sensors are attached to devices
  * - A common GUI for all devices
  * - Command-line configuration
  * - Code mobility
  */

import akka.actor.{ActorRef, Props}
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point2D}
import it.unibo.scafi.distrib.actor.server.{SpatialPlatform => SpatialServerBasedActorPlatform}
import examples.gui.server.{DevViewActor => ServerBasedDevViewActor}

object Demo6A_Platform extends Demo6_Platform with SpatialServerBasedActorPlatform with BasicSpatialAbstraction {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  override type P = Point2D
  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] = new Basic3DSpace(elems.toMap) {
    override val proximityThreshold = 1.1
  }

  class ServerBasedDemo6DeviceActor(override val selfId: UID,
                                    _aggregateExecutor: Option[ProgramContract],
                                    _execScope: ExecScope,
                                    override val server: ActorRef)
    extends DeviceActor(selfId, _aggregateExecutor, _execScope, server) with Demo6DeviceActor

  object CodeMobilityDeviceActor {
    def props(selfId: UID, program: Option[ProgramContract], execStrategy: ExecScope, serverActor: ActorRef): Props =
      Props(classOf[ServerBasedDemo6DeviceActor], selfId, program, execStrategy, serverActor)
  }
}

import demos.{Demo6A_Platform => Platform}

class Demo6A_AggregateProgram extends Platform.ScafiStandardAggregateProgram {
  override def main(): String = "ready"
}

object Demo6A_MainProgram extends Platform.CmdLineMain {
  override def refineSettings(s: Platform.Settings): Platform.Settings = {
    s.copy(profile = s.profile.copy(
      devActorProps = (id, program, scope, server) => Some(Platform.CodeMobilityDeviceActor.props(id, program, scope, server)),
      devGuiActorProps = ref => Some(ServerBasedDevViewActor.props(Platform, ref))
    ))
  }
  override def onDeviceStarted(dm: Platform.DeviceManager, sys: Platform.SystemFacade): Unit = {
    val devInRow = ServerBasedDevViewActor.DevicesInRow
    dm.addSensorValue(Platform.LocationSensorName, Point2D(dm.selfId%devInRow,(dm.selfId/devInRow).floor))
    dm.addSensorValue(Platform.SourceSensorName, false)
    dm.start
  }
}

object Demo6A_ServerMain extends Platform.ServerCmdLineMain
