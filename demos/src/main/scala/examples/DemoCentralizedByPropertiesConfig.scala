/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
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
