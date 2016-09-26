package demos

/**
 * @author Roberto Casadei
 * Demo 0-C
 * - Peer-to-peer system
 * - Ad-hoc network
 * - Command-line configuration
 */

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.{ BasicActorP2P => Platform }

// STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
class Demo0C_AggregateProgram extends Platform.AggregateProgram {
  override def main(): Any = foldhood(0){_ + _}(1)
}

// STEP 3: DEFINE MAIN PROGRAM
object Demo0C_MainProgram extends Platform.CmdLineMain
