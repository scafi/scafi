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

package examples

/**
 * Demo program with:
 * - Centralized actor platform
 * - Settings specified by command-line
 * - Manual setup
 * - Infrastructure-managed code mobility -- i.e., the program to be shipped
 *  is sent to the TM which works as a distributor; the TM will propagate
 *  the program to all its registered devices.
 */

import examples.gui.ServerGUIActor
import it.unibo.scafi.incarnations.BasicActorServerBased

import scala.io.StdIn

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.BasicActorServerBased._

// STEP 2: DEFINE MAIN PROGRAM
object DemoCentralizedCodeMobilityMain extends App {
  val SENSOR_SRC = "source"

  // STEP 3: DEFINE AGGREGATE PROGRAM SCHEMA
  trait MyAggregateProgram extends AggregateProgram with Serializable {
    def hopGradient(source: Boolean): Int = {
      rep(10){
        hops => { mux(source){ 0 } {
          1+minHood[Int](nbr[Int]{ hops }) } }
      }
    }

    //override def main(): Any = foldhood(0){_+_}(1)
    override def main(): Any = "aaa" //hopGradient(sense[Boolean]("source"))
  }

  // STEP 4: PARSE COMMAND-LINE ARGS TO SETTINGS
  CmdLineParser.parse(args, Settings()) foreach { settings =>
    // STEP 5: ADJUST SETTINGS
    val s: Settings = settings.copy(
      aggregate = settings.aggregate.copy(
        program = () => None
      ),
      profile = settings.profile.copy(
        serverGuiActorProps = (tmRef) => Some(ServerGUIActor.props(BasicActorServerBased, tmRef))
      )
    )

    // STEP 6: SETUP PLATFORM
    val platform = PlatformConfigurator.setupPlatform(settings.platform,
      settings.profile)

    // STEP 7: CREATE AGGREGATE APPLICATION
    val sys = platform.newAggregateApplication(settings.aggregate,
      settings.profile,
      settings.execution.scope)

    // STEP 8: START OBSERVER GUI FOR TOPOLOGY MANAGER
    if (s.profile.serverGui) {
      sys.actorSys.actorOf(ServerGUIActor.props(BasicActorServerBased, sys.server))
    }

    // STEP 8: START AND CONFIGURE DEVICES
    val dm1 = sys.newDevice(1)
    val dm2 = sys.newDevice(2)

    dm1.addSensor(SENSOR_SRC, () => true)
    dm2.addSensor(SENSOR_SRC, () => false)

    // STEP 9: INITIATE PROGRAM SHIPPING BY SENDING THE PROGRAM TO THE DISTRIBUTOR
    if(s.profile.startServer) {
      StdIn.readLine()

      sys.server ! MsgShipProgram(MsgProgram(new MyAggregateProgram {}, Set(this.getClass)))

      StdIn.readLine()

      sys.startScheduling

      StdIn.readLine()

      sys.server ! MsgShipProgram(MsgProgram(new AggregateProgram with Serializable {
        override def main(): Any = "CODE_MOBILITY"
      }, Set(this.getClass)))
    }
  }

}
