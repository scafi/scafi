package it.unibo.scafi.js

import it.unibo.scafi.incarnations.BasicSimulationIncarnation
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import org.scalajs.dom

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.annotation.JSExport

/**
  * from the main body, scala js produce a javascript file.
  * it is an example of a ScaFi simulation transcompilated in javascript.
  */
object Index {
  import org.scalajs.dom._

  class FooProgram extends AggregateProgram with StandardSensors {
    override def main(): Any = rep(Double.PositiveInfinity){ case g =>
      mux(sense[Boolean]("source")){ 0.0 }{
        minHoodPlus { nbr(g) + nbrRange() }
      }
    }
  }

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    parNode.textContent = text
    targetNode.appendChild(parNode)
  }

  @JSExport
  def main(args: Array[String]): Unit = {
    appendPar(document.body, "Hello Scala.js")

    println("Index.main")

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
