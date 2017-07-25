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
 */

import examples.gui.ServerGUIActor
import it.unibo.scafi.incarnations.BasicActorServerBased

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.BasicActorServerBased._

// STEP 2: DEFINE MAIN PROGRAM
object DemoCentralizedAkkaPlatformCmdLineMain extends App {
  val SENSOR_SRC = "source"

  // STEP 3: DEFINE AGGREGATE PROGRAM SCHEMA
  trait MyAggregateProgram extends AggregateProgram with Serializable {
    def hopGradient(source: Boolean): Int = {
      rep(10){
        hops => { mux(source){ 0 } {
          1+minHood[Int](nbr[Int]{ hops }) } }
      }
    }

    def main() = {
      Thread.sleep(500)
      hopGradient(sense(SENSOR_SRC))
    }
    //foldhood(0)(_+_){1}
  }

  // STEP 4: PARSE COMMAND-LINE ARGS TO SETTINGS
  CmdLineParser.parse(args, Settings()) foreach{ settings =>
    // STEP 5: ADJUST SETTINGS
    val s: Settings = settings.copy(
      aggregate = settings.aggregate.copy(
        program = () => Some(new MyAggregateProgram{})
      ),
      profile = settings.profile.copy(
        serverGuiActorProps = (tmRef) => Some(ServerGUIActor.props(BasicActorServerBased, tmRef))
      )
    )

    // STEP 6: SETUP PLATFORM
    val platform = PlatformConfigurator.setupPlatform(s.platform,
      s.profile)

    // STEP 7: CREATE AGGREGATE APPLICATION
    val sys = platform.newAggregateApplication(s.aggregate,
      s.profile,
      s.execution.scope)

    // STEP 8: CREATE AND CONFIG DEVICES FOR THE APPLICATION
    var first = true
    s.deviceConfig.ids.foreach{ idd =>
      // STEP 8a: CREATE DEVICE
      val dm = sys.newDevice(idd)
      // STEP 8b: ADD SENSOR TO DEVICE
      dm.addSensor(SENSOR_SRC, () => first)
      first = false
    }
    // STEP 8c: SPECIFY NEIGHBORS FOR DEVICES
    s.deviceConfig.nbs.foreach { case (id,nbs) =>
      nbs.foreach(nbrId => sys.addNeighbor(id,nbrId))
    }

    // STEP 9: START SYSTEM (may start scheduler)
    sys.start()
  }
}

// EXAMPLE. RUN CONFIGURATIONS
// Config A starts TM; config B starts devices
// A) --tmstart  -p 9000 -h 127.0.0.1 --tmgui --sched-global rr --loglevel info
// B) start  -P 9000 -p 9002 -e 2:2,1;3:3,1,2 -H 127.0.0.1 -h 127.0.0.1 --sched-global rr --loglevel debug
