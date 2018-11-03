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
    extends SpatialDeviceActor(selfId, _aggregateExecutor, _execScope, server) with Demo6DeviceActor {

    override def propagateProgramToNeighbors(program: () => Any): Unit = {
      nbrs.foreach { case (_, NbrInfo(_, _, mailboxOpt, _)) =>
        mailboxOpt.foreach(ref => ref ! MsgUpdateProgram(selfId, program))
      }
    }
  }
  object HybridDemo6DeviceActor {
    def props(selfId: UID, program: Option[ProgramContract], execStrategy: ExecScope, serverActor: ActorRef): Props =
      Props(classOf[HybridDemo6DeviceActor], selfId, program, execStrategy, serverActor)
  }
}

import demos.{Demo6C_Platform => Platform}

class Demo6C_AggregateProgram extends Platform.AggregateProgram {
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
