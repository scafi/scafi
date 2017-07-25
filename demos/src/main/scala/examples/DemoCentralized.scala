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
 * - All-in-one setup
 */

import examples.gui.ServerGUIActor
import it.unibo.scafi.incarnations.BasicActorServerBased

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.BasicActorServerBased._

// STEP 2: DEFINE MAIN PROGRAM
object DemoCentralizedMain extends App {
  val SENSOR_SRC = "source"

  // STEP 3: DEFINE AGGREGATE PROGRAM SCHEMA
  trait MyAggregateProgram extends AggregateProgram with Serializable {
    def isObstacle = mid()%2==1 // sense("obstacle")
    def numOfNeighbors: Int = foldhood(0)(_+_)(nbr { 1 })

    //override def main(): Any = foldhood(0){_+_}(1)
    //override def main() = mux(isObstacle)(() => aggregate { -numOfNeighbors } )(() => aggregate { numOfNeighbors })()
    def main() = {
      val midv = mid()
      foldhood(()=>{})((a,b)=>()=>{a();b()}){(nbr { () => print(s"Y${midv}Y") })}()
    }
  }

  // STEP 4: PARSE COMMAND-LINE ARGS TO SETTINGS
  CmdLineParser.parse(args, Settings()) foreach{ settings =>
    // STEP 5: ADJUST SETTINGS
    val s: Settings = settings.copy(
      aggregate = settings.aggregate.copy(
        program = () => Some(new MyAggregateProgram{})
      ),
      profile = settings.profile.copy(
        serverGuiActorProps = (tmRef) => Some(ServerGUIActor.props(BasicActorServerBased,tmRef))
      )
    )

    // STEP 6: SETUP APPLICATION BY SETTINGS
    val sys = PlatformConfigurator.allInOneSetup(s)

    // STEP 7: START SYSTEM (may start scheduler)
    sys.start()
  }
}
