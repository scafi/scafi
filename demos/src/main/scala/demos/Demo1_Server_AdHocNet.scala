package demos

/**
 * @author Roberto Casadei
 * Demo 1
 * - Client/server system
 * - Ad-hoc network
 * - Command-line configuration
 */


// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.{ BasicActorServerBased => Platform }

// STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
class Demo1_AggregateProgram extends Platform.AggregateProgram {
  override def main(): Any = foldhood(0){_ + _}(1)
}

// STEP 3: DEFINE MAIN PROGRAM
object Demo1_MainProgram extends Platform.CmdLineMain