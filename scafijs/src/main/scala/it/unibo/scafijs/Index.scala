package it.unibo.scafijs

import it.unibo.scafi.incarnations.BasicSimulationIncarnation
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

import scala.collection.mutable.ArrayBuffer

/**
  * from the main body, scala js produce a javascript file.
  * it is an example of a ScaFi simulation transcompilated in javascript.
  */
object Index {
  import org.scalajs.dom._

  class FooProgram extends AggregateProgram {
    override def main(): Any = rep(Math.random())(x => x)
  }

  def main(args: Array[String]): Unit = {
    val nodes = ArrayBuffer((0 to 100):_*)
    val net = BasicSimulationIncarnation.simulatorFactory.basicSimulator(
      nodes
    )
    val elem : CONTEXT => EXPORT = new FooProgram()
    import scalajs.js.timers._

    setInterval(1000) {
      println(net.exec(elem))
    }
  }
}
