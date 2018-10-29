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
 * Demo 3
 * - Client/server system
 * - (Dynamic) "Spatial" network
 * - Sensors are attached to devices
 * - Command-line configuration
 * - Server GUI
 */

import examples.gui.ServerGUIActor
import it.unibo.scafi.distrib.actor.server.{SpatialPlatform => SpatialServerBasedActorPlatform}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.space.{Point2D, BasicSpatialAbstraction}

object Demo3_Platform extends BasicAbstractActorIncarnation
  with SpatialServerBasedActorPlatform
  with BasicSpatialAbstraction with Serializable {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  override type P = Point2D

  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] =
    new Basic3DSpace(elems.toMap) {
      override val proximityThreshold = 1.1
    }
}

// STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
class Demo3_AggregateProgram extends Demo3_Platform.AggregateProgram {
  def hopGradient(source: Boolean): Double = {
    rep(Double.PositiveInfinity){
      hops => { mux(source) { 0.0 } { 1+minHood(nbr{ hops }) } }
    }
  }

  def main() = hopGradient(sense("source"))
}

// STEP 3: DEFINE MAIN PROGRAMS
object Demo3_MainProgram extends Demo3_Platform.CmdLineMain {
  override def onDeviceStarted(dm: Demo3_Platform.DeviceManager,
                               sys: Demo3_Platform.SystemFacade) = {
    val random = new scala.util.Random(System.currentTimeMillis())
    var k = 0
    var positions = (1 to 5).map(_ => random.nextInt(10))
    dm.addSensor(Demo3_Platform.LocationSensorName, () => {
      k += 1
      Point2D(if(k>=positions.size) positions.last else positions(k), 0)
    })
    dm.addSensorValue("source", dm.selfId==4)
  }
}

object Demo3_ServerMain extends Demo3_Platform.ServerCmdLineMain {
  override def refineSettings(s: Demo3_Platform.Settings) = {
    s.copy(profile = s.profile.copy(
      serverGuiActorProps = tm => Some(ServerGUIActor.props(Demo3_Platform, tm))
    ))
  }
}
