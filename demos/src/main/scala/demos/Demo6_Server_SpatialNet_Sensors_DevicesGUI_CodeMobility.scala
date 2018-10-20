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

package demos

/**
  * Demo 6
  * - Client/server system
  * - (Dynamic) "Spatial" network
  * - Sensors are attached to devices
  * - A common GUI for all devices
  * - Command-line configuration
  * - Code mobility
  */

import examples.gui.ServerGUIActor
import it.unibo.scafi.distrib.actor.server.{SpatialPlatform => SpatialServerBasedActorPlatform}
import examples.gui.server.{DevViewActor => ServerBasedDevViewActor}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point2D}

object Demo6_Platform extends BasicAbstractActorIncarnation with SpatialServerBasedActorPlatform with BasicSpatialAbstraction {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  override type P = Point2D
  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] = new Basic3DSpace(elems.toMap) {
    override val proximityThreshold = 1.1
  }

  val SourceSensorName: String = "source"
  val program1: () => String = () => "idle"
  val program2: () => String = () => "working"
}

import demos.{Demo6_Platform => Platform}

// STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
class Demo6_AggregateProgram extends Platform.AggregateProgram {
  override def main(): String = Platform.program1()
}

// STEP 3: DEFINE MAIN PROGRAMS
object Demo6_MainProgram extends Platform.CmdLineMain {
  override def refineSettings(s: Platform.Settings): Platform.Settings = {
    s.copy(profile = s.profile.copy(
      devGuiActorProps = ref => Some(ServerBasedDevViewActor.props(Platform, ref))
    ))
  }
  override def onDeviceStarted(dm: Platform.DeviceManager, sys: Platform.SystemFacade): Unit = {
    val devInRow = ServerBasedDevViewActor.DevicesInRow
    dm.addSensorValue(Platform.LocationSensorName, Point2D(dm.selfId%devInRow,(dm.selfId/devInRow).floor))
    if (dm.selfId == 4) {
      dm.addSensorValue(Platform.SourceSensorName, true)
      dm.actorRef ! Platform.MsgShipLambda(dm.selfId, Platform.program2)
    } else {
      dm.addSensorValue(Platform.SourceSensorName, false)
    }
    dm.start
  }
}

object Demo6_ServerMain extends Platform.ServerCmdLineMain {
  override def refineSettings(s: Platform.Settings): Platform.Settings = {
    s.copy(profile = s.profile.copy(
      serverGuiActorProps = tm => Some(ServerGUIActor.props(Platform, tm))
    ))
  }
}
