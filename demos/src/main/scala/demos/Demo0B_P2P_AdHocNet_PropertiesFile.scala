package demos

/**
 * @author Roberto Casadei
 * Demo 0-B
 * - Peer-to-peer system
 * - Ad-hoc network
 * - Configuration via properties file
 */


// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.{ BasicActorP2P => Platform }

// STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
class Demo0B_AggregateProgram extends Platform.AggregateProgram {
  override def main(): Any = foldhood(0){_ + _}(1)
}

// STEP 3: DEFINE MAIN PROGRAM
object Demo0B_MainProgram_Subsys1 extends
  Platform.FileMain("demos/src/main/scala/demos/Demo0B_Subsys1.conf")
object Demo0B_MainProgram_Subsys2 extends
  Platform.FileMain("demos/src/main/scala/demos/Demo0B_Subsys2.conf")
