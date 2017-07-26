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

import it.unibo.scafi.incarnations.BasicAbstractDistributedIncarnation
import it.unibo.scafi.space.{Point2D, BasicSpatialAbstraction}

/**
 * Demo 2
 * - Client/server system
 * - (Fixed) "Spatial" network
 * - Command-line configuration
 */

import it.unibo.scafi.distrib.actor.server.{SpatialPlatform => SpatialServerBasedActorPlatform}

object Demo2_Platform extends BasicAbstractDistributedIncarnation
  with SpatialServerBasedActorPlatform
  with BasicSpatialAbstraction with Serializable {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  override type P = Point2D

  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] =
    new Basic3DSpace(elems.toMap) {
      override val proximityThreshold = 2.5
    }
}

// STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
class Demo2_AggregateProgram extends Demo2_Platform.AggregateProgram {
  override def main(): Any = foldhood(0){_ + _}(1)
}

// STEP 3: DEFINE MAIN PROGRAM
object Demo2_MainProgram extends Demo2_Platform.CmdLineMain {
  override def onDeviceStarted(dm: Demo2_Platform.DeviceManager,
                               sys: Demo2_Platform.SystemFacade) = {
    dm.addSensorValue(Demo2_Platform.LocationSensorName, Point2D(dm.selfId,0))
  }
}

object Demo2_Server extends Demo2_Platform.ServerCmdLineMain
