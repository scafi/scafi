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

import examples.gui.DevViewActor
import it.unibo.scafi.space.Point2D

/**
  * Demo 4-B
  * - Client/server system
  * - (Dynamic) "Spatial" network
  * - Sensors are attached to devices
  * - A common GUI for all devices
  * - Command-line configuration
  */

object Demo4B_MainProgram extends Demo3_Platform.CmdLineMain {
  override def refineSettings(s: Demo3_Platform.Settings): Demo3_Platform.Settings = {
    s.copy(profile = s.profile.copy(
      devGuiActorProps = ref => Some(DevViewActor.props(Demo3_Platform, ref))
    ))
  }

  override def onDeviceStarted(dm: Demo3_Platform.DeviceManager, sys: Demo3_Platform.SystemFacade): Unit = {
    dm.addSensorValue(Demo3_Platform.LocationSensorName, Point2D(dm.selfId%5,(dm.selfId/5.0).floor))
    dm.addSensorValue("source", dm.selfId==4)
    dm.start
  }
}
