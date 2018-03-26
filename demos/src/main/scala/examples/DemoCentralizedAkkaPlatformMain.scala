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
 * - Settings defined programmatically
 * - Manual setup
 */

import examples.gui.ServerGUIActor
import it.unibo.scafi.incarnations.BasicActorServerBased

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.BasicActorServerBased._

// STEP 2: DEFINE MAIN PROGRAM
object DemoCentralizedAkkaPlatformMain extends App {
  val SENSOR_SRC = "source"

  // STEP 3: DEFINE AGGREGATE PROGRAM SCHEMA
  trait DemoLocalActorProgram extends AggregateProgram with Serializable {
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

  // STEP 4: DEFINE SETTINGS PROGRAMMATICALLY
  var settings = Settings(
    aggregate = AggregateApplicationSettings(
      name = "test1",
      program = () => Some(new DemoLocalActorProgram {}) // PROGRAM BUILDER
    ),
    profile = ServerBasedActorSystemSettings(startServer = true),
    execution = ExecutionSettings(scope =
      Global(RandomExecStrategy(0))
    ))

  // STEP 6: SETUP PLATFORM
  val platform = PlatformConfigurator.setupPlatform(settings.platform,
    settings.profile)

  // STEP 7: CREATE AGGREGATE APPLICATION
  val sys = platform.newAggregateApplication(settings.aggregate,
    settings.profile,
    settings.execution.scope)

  // STEP 8: START OBSERVER GUI FOR TOPOLOGY MANAGER
  sys.actorSys.actorOf(ServerGUIActor.props(BasicActorServerBased, sys.server))

  // STEP 9: CREATE DEVICES
  val dm1 = sys.newDevice(1)
  val dm2 = sys.newDevice(2)

  // STEP 10: CONFIGURE DEVICES
  dm1.addSensor(SENSOR_SRC, () => true)
  dm2.addSensor(SENSOR_SRC, () => false)

  // STEP 11: START SYSTEM (may start scheduler)
  sys.start()
}
