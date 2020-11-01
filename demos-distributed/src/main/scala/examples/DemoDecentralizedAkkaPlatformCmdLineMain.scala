/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package examples

/**
 * Demo program with:
 * - Decentralized (peer-to-peer) actor platform
 * - Settings specified by command-line
 * - All-in-one setup
 */

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.BasicActorP2P._

// STEP 2: DEFINE MAIN PROGRAM
object DemoDecentralizedAkkaPlatformCmdLineMain extends App {
  val SENSOR_SRC = "source"

  // STEP 3: DEFINE AGGREGATE PROGRAM SCHEMA
  trait MyAggregateProgram extends ScafiStandardAggregateProgram with Serializable {
    def main() = foldhood(()=>{})((a,b)=>()=>{a();b()}){(nbr { () => print("YYY") })}()
  }

  // STEP 4: PARSE COMMAND-LINE ARGS TO SETTINGS
  cmdLineParser.parse(args, Settings()) foreach{ settings =>
    // STEP 5: ADJUST SETTINGS
    val s: Settings = settings.copy(
      aggregate = settings.aggregate.copy(
        program = () => Some(new MyAggregateProgram{})
      )
    )

    // STEP 6: SETUP AGGREGATE APPLICATION
    val sys = PlatformConfigurator.allInOneSetup(s)

    // STEP 7: CONFIGURE PARTS OF THE APPLICATION (e.g., add sensors)
    var first = true
    s.deviceConfig.ids.foreach{ idd =>
      val dm = sys.devices(idd)
      dm.addSensor(SENSOR_SRC, () => first)
      first = false
    }

    // STEP 8: START SYSTEM
    sys.start()
  }
}
