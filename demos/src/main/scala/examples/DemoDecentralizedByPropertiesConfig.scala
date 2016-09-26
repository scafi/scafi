package examples

/**
 * @author Roberto Casadei
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

