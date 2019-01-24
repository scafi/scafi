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
 * - Decentralized (peer-to-peer) actor platform
 * - Settings specified by configuration file
 * - All-in-one setup
 */

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.BasicActorP2P._

// STEP 2: DEFINE PROGRAM
object DemoDecentralizedByPropertiesConfigMain extends App {
  // STEP 3: DEFINE AGGREGATE PROGRAM SCHEMA (OPTIONAL)
  // NOTE: MUST be a class (not a trait!) -- unless you provide a custom program builder
  class MyAggregateProgram extends AggregateProgram with Serializable {
    override def main(): Any = foldhood(0){_+_}(1)
  }

  // STEP 4: BUILD SETTINGS FROM CONFIGURATION FILE
  val configPath = "demos/src/main/scala/examples/dec_app1.conf"
  val s = Settings.fromConfig(configPath)

  // STEP 5: SETUP AGGREGATE APPLICATION
  val sys = PlatformConfigurator.allInOneSetup(s)

  // STEP 6: START SYSTEM
  sys.start()
}
