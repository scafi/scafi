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
  * Demo 5
  * - Peer-to-peer system
  * - (Dynamic) "Spatial" network
  * - Sensors are attached to devices
  * - A common GUI for all devices
  * - Command-line configuration
  */

import examples.gui.p2p.DevViewActor
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.distrib.actor.p2p.{SpatialPlatform => SpatialP2PActorPlatform}
import it.unibo.scafi.space.Point2D

object Demo5_Platform extends BasicAbstractActorIncarnation with SpatialP2PActorPlatform {
  override val LocationSensorName: String = "LOCATION_SENSOR"
}

class Demo5_AggregateProgram extends Demo5_Platform.AggregateProgram {
  def hopGradient(source: Boolean): Double = {
    rep(Double.PositiveInfinity){
      hops => { mux(source) { 0.0 } { 1 + minHood(nbr { hops }) } }
    }
  }
  def main(): Double = hopGradient(sense("source"))
}

object Demo5_MainProgram extends Demo5_Platform.CmdLineMain {
  override def refineSettings(s: Demo5_Platform.Settings): Demo5_Platform.Settings = {
    s.copy(profile = s.profile.copy(
      devGuiActorProps = ref => Some(DevViewActor.props(Demo5_Platform, ref))
    ))
  }
  override def onDeviceStarted(dm: Demo5_Platform.DeviceManager, sys: Demo5_Platform.SystemFacade): Unit = {
    val devInRow = DevViewActor.DevicesInRow
    dm.addSensorValue(Demo5_Platform.LocationSensorName, Point2D(dm.selfId%devInRow,(dm.selfId/devInRow).floor))
    dm.addSensorValue("source", dm.selfId==4)
    dm.start
  }
}