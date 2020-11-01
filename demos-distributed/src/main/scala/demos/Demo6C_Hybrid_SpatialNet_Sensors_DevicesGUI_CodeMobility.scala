/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package demos

/**
  * Demo 6-C
  * - Hybrid system
  * - (Dynamic) "Spatial" network
  * - Sensors are attached to devices
  * - A common GUI for all devices
  * - Command-line configuration
  * - Code mobility
  */

import akka.actor.{ActorRef, Props}
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point2D}
import it.unibo.scafi.distrib.actor.hybrid.{SpatialPlatform => SpatialHybridActorPlatform}
import examples.gui.hybrid.{DevViewActor => HybridDevViewActor}

object Demo6C_Platform extends Demo6_Platform with SpatialHybridActorPlatform with BasicSpatialAbstraction {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  override type P = Point2D
  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] = new Basic3DSpace(elems.toMap) {
    override val proximityThreshold = 1.1
  }

  class HybridDemo6DeviceActor(override val selfId: UID,
                                    _aggregateExecutor: Option[ProgramContract],
                                    _execScope: ExecScope,
                                    override val server: ActorRef)
    extends SpatialDeviceActor(selfId, _aggregateExecutor, _execScope, server) with Demo6DeviceActor

  object HybridDemo6DeviceActor {
    def props(selfId: UID, program: Option[ProgramContract], execStrategy: ExecScope, serverActor: ActorRef): Props =
      Props(classOf[HybridDemo6DeviceActor], selfId, program, execStrategy, serverActor)
  }
}

import demos.{Demo6C_Platform => Platform}

class Demo6C_AggregateProgram extends Platform.ScafiStandardAggregateProgram {
  override def main(): String = "ready"
}

object Demo6C_MainProgram extends Platform.CmdLineMain {
  override def refineSettings(s: Platform.Settings): Platform.Settings = {
    s.copy(profile = s.profile.copy(
      devActorProps = (id, program, scope, server) => Some(Platform.HybridDemo6DeviceActor.props(id, program, scope, server)),
      devGuiActorProps = ref => Some(HybridDevViewActor.props(Platform, ref))
    ))
  }
  override def onDeviceStarted(dm: Platform.DeviceManager, sys: Platform.SystemFacade): Unit = {
    val devInRow = HybridDevViewActor.DevicesInRow
    dm.addSensorValue(Platform.LocationSensorName, Point2D(dm.selfId%devInRow,(dm.selfId/devInRow).floor))
    dm.addSensorValue(Platform.SourceSensorName, false)
    dm.start
  }
}

object Demo6C_ServerMain extends Platform.ServerCmdLineMain
