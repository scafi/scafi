/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package demos

import examples.gui.DevGUIActor
import it.unibo.scafi.space.Point2D

/**
 * Demo 4
 * - Client/server system
 * - (Dynamic) "Spatial" network
 * - Sensors are attached to devices
 * - Each device has a GUI
 * - Command-line configuration
 */

object Demo4_MainProgram extends Demo3_Platform.CmdLineMain {
  override def refineSettings(s: Demo3_Platform.Settings): Demo3_Platform.Settings = {
    s.copy(profile = s.profile.copy(
      devGuiActorProps = ref => Some(DevGUIActor.props(Demo3_Platform, ref))
    ))
  }

  override def onDeviceStarted(dm: Demo3_Platform.DeviceManager, sys: Demo3_Platform.SystemFacade): Unit = {
    dm.addSensorValue(Demo3_Platform.LocationSensorName, Point2D(dm.selfId%5,(dm.selfId/5.0).floor))
    dm.addSensorValue("source", dm.selfId==4)
    dm.start
  }
}
