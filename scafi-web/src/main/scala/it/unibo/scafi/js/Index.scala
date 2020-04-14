package it.unibo.scafi.js

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.incarnations.BasicSimulationIncarnation
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.js.{WebIncarnation => web}
import it.unibo.scafi.space.Point3D
import org.scalajs.dom

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.timers.{SetIntervalHandle, clearInterval, setInterval}

/**
  * from the main body, scala js produce a javascript file.
  * it is an example of a ScaFi simulation transcompilated in javascript.
  */
@JSExportTopLevel("Index")
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

  def appendCanvas(target: dom.Node, id: String): Element = {
    val div = document.createElement("div")
    div.setAttribute("style", "width:1000px; height:1000px; border:5px solid #ababab;")
    div.id = "netDiv"
    target.appendChild(div)
    div
  }

  var handle: Option[SetIntervalHandle] = None
  var net: NETWORK = _
  var program: CONTEXT => EXPORT = _

  @JSExport
  def main(args: Array[String]): Unit = {
    appendPar(document.body, "Hello Scala.js")

    println("Index.main !!!")

    val spatialSim = web.simulatorFactory.gridLike(
      GridSettings(),
      rng = 50
    )

    val spatialGraph = NetUtils.graph()
    var k = 0
    for(i <- 1 to 10;
        j <- 1 to 10){
      spatialGraph.addNode(""+k, new js.Object {
        val position = Point3D(i*2, j*2, 0.0)
      })
      k += 1
    }

    /*
    val devsToPos: Map[Int, Point3D] = g.nodes.mapValues(n => new Point3D(n.position.x, n.position.y, n.position.z)).toMap // Map id->position
    val net = new web.SpaceAwareSimulator(
      space = new Basic3DSpace(devsToPos,
        proximityThreshold = 50.0
      ),
      devs = devsToPos.map { case (d, p) => d -> new DevInfo(d, p,
        Map.empty,
        nsns => nbr => null)
      },
      simulationSeed = simulationSeed,
      randomSensorSeed = configurationSeed
    )

    network.setNeighbours(net.getAllNeighbours)
     */

    sigma()

    jnetworkx()

    plainSimulation()
  }

  def sigma() = {
    //val s: sigma.Sigma = sigma.Sigma("#sigmaDiv")
    // s.graph.addNode(new sigma.Node("1"))
  }

  def jnetworkx() = {
    val nxDiv = appendCanvas(dom.document.body, "netDiv")
    val g: jsnetworkx.Graph = jsnetworkx.Network.gnpRandomGraph(10,0.4)
    jsnetworkx.Network.draw(g, jsnetworkx.DrawOptions("#netDiv"))
  }

  def plainSimulation() = {
    // Add button to start/stop simulation
    val btn = document.createElement("button")
    btn.setAttribute("onClick", "switchSimulation()")
    btn.textContent = "Start simulation";
    document.body.appendChild(btn)

    // Plain simulation
    val nodes = ArrayBuffer((0 to 100):_*)
    net = BasicSimulationIncarnation.simulatorFactory.simulator(
      idArray = nodes,
      nbrMap = mutable.Map(nodes.map(id => id->(id-3 to id+3+1).toSet.filter(x => x>=0 && x<100)):_*),
      nbrSensors = {
        case NBR_RANGE => { case (id,idn) => 1 }
      },
      localSensors = {
        case "source" => { case id => id == 10 || id == 50 || id == 70 }
      }
    )
    program = new FooProgram()
  }

  @JSExportTopLevel("switchSimulation")
  def addClickedMessage(): Unit = handle match {
    case Some(h) => { clearInterval(h); handle = None }
    case None => handle = Some(setInterval(100) { println(net.exec(program)) })
  }
}
