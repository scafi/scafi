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
 * - Command-line **main program schema**
 */

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.BasicActorServerBased._

// STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
trait MyAggregateProgram extends AggregateProgram with Serializable {
  override def main(): Any = foldhood(0){_+_}(1)
}

// STEP 3: DEFINE MAIN PROGRAM
object DemoEasy extends CmdLineMain {
  override def programBuilder = None//Some(new MyAggregateProgram {})

  var k = 0
  override def onDeviceStarted(dm: DeviceManager, sys: SystemFacade) = {
    k = k+1
    if(k>=2){
      sys.start()
    }
  }
}

/**
 * Configurations:
 * (0) --tmstart -p 9000 -h 127.0.0.1 --sched-global rr --loglevel debug
 * (1) -P 9000 -H 127.0.0.1 -p 9005 -e 2:1;3:1,2 --sched-global rr --loglevel debug
 */
