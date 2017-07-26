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
 * - Settings specified by configuration file
 * - All-in-one setup
 */

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.BasicActorServerBased._

// STEP 2: DEFINE MAIN PROGRAM
object DemoCentralizedByPropertiesConfigMain extends App {
  // STEP 3: DEFINE AGGREGATE PROGRAM SCHEMA (OPTIONAL)
  // NOTE: it MUST be a class (not a trait!) -- unless you manually specify the program builder
  class MyAggregateProgram extends AggregateProgram with Serializable {
    override def main(): Any = foldhood(0){_+_}(1)
  }

  // STEP 4: BUILD SETTINGS FROM CONFIGURATION FILE
  val configPath = "demos/src/main/scala/examples/cen_app1.conf"
  val s = Settings.fromConfig(configPath)

  // STEP 5: SETUP APPLICATION BY SETTINGS
  val sys = PlatformConfigurator.allInOneSetup(s)

  // STEP 6: START SYSTEM (may start scheduler)
  sys.start()
}
