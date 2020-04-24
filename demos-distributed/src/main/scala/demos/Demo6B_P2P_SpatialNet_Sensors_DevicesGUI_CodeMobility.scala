/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package demos

/**
  * Demo 6-B
  * - Peer-to-peer system
  * - (Dynamic) "Spatial" network
  * - Sensors are attached to devices
  * - A common GUI for all devices
  * - Command-line configuration
  * - Code mobility
  */

import akka.actor.Props
import it.unibo.scafi.space.Point2D
import examples.gui.p2p.{DevViewActor => P2PDevViewActor}
import it.unibo.scafi.distrib.actor.p2p.{SpatialPlatform => SpatialP2PActorPlatform}

object Demo6B_Platform extends Demo6_Platform with SpatialP2PActorPlatform {
  override val LocationSensorName: String = "LOCATION_SENSOR"

  class P2PDemo6DeviceActor(override val selfId: UID,
                            _aggregateExecutor: Option[ProgramContract],
                            _execScope: ExecScope)
    extends DeviceActor(selfId, _aggregateExecutor, _execScope) with Demo6DeviceActor

  object CodeMobilityDeviceActor {
    def props(selfId: UID, program: Option[ProgramContract], execStrategy: ExecScope): Props =
      Props(classOf[P2PDemo6DeviceActor], selfId, program, execStrategy)
  }
}

import demos.{Demo6B_Platform => Platform}

class Demo6B_AggregateProgram extends Platform.AggregateProgram {
  override def main(): String = "ready"
}

object Demo6B_MainProgram extends Platform.CmdLineMain {
  override def refineSettings(s: Platform.Settings): Platform.Settings = {
    s.copy(profile = s.profile.copy(
      devActorProps = (id, program, scope) => Some(Platform.CodeMobilityDeviceActor.props(id, program, scope)),
      devGuiActorProps = ref => Some(P2PDevViewActor.props(Platform, ref))
    ))
  }
  override def onDeviceStarted(dm: Platform.DeviceManager, sys: Platform.SystemFacade): Unit = {
    val devInRow = P2PDevViewActor.DevicesInRow
    dm.addSensorValue(Platform.LocationSensorName, Point2D(dm.selfId%devInRow,(dm.selfId/devInRow).floor))
    dm.addSensorValue(Platform.SourceSensorName, false)
    dm.start
  }
}
