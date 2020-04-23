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
    div.setAttribute("style", "width:100%; height:100%; border:5px solid #ababab;")
    div.id = id
    target.appendChild(div)
    div
  }

  var handle: Option[SetIntervalHandle] = None
  var net: NETWORK = _
  var program: CONTEXT => EXPORT = _

  var spatialNet: web.NETWORK = _
  var spatialProgram: web.CONTEXT => web.EXPORT = _

  @JSExport
  def main(args: Array[String]): Unit = {
    appendPar(document.body, "Hello Scala.js")
    println("Index.main !!!")

    configurePage()

    spatialSim()

    sigma()

    jnetworkx()

    plainSimulation()
  }

  def spatialSim() = {
    val spatialSim = web.simulatorFactory.gridLike(
      GridSettings(),
      rng = 150.0
    ).asInstanceOf[web.SpaceAwareSimulator]
    spatialSim.addSensor("source", false)
    spatialSim.chgSensorValue("source", Set("1", "55", "86"), true)

    val spatialGraph = NetUtils.graph()

    for((id,pos@Point3D(x,y,z)) <- spatialSim.space.elemPositions){
      spatialGraph.addNode(id, new js.Object {
        val position = (x,y,z)
      })
      for(nbr <- spatialSim.space.getNeighbors(id)){
        spatialGraph.addEdge(id,nbr)
      }
    }

    val nxDiv = appendCanvas(dom.document.getElementById("canvasContainer"), "netDiv")
    jsnetworkx.Network.draw(spatialGraph, jsnetworkx.DrawOptions("#netDiv"))

    spatialNet = spatialSim

    /*
    val devsToPos: Map[Int, Point3D] = spatialSim.map(n => new Point3D(n.position.x, n.position.y, n.position.z)).toMap // Map id->position
    val net = new web.SpaceAwareSimulator(
      space = new Basic3DSpace(devsToPos,
        proximityThreshold = 50.0
      ),
      devs = devsToPos.map { case (d, p) => d -> new web.DevInfo(d, p,
        Map.empty,
        nsns => nbr => null)
      },
      simulationSeed = simulationSeed,
      randomSensorSeed = configurationSeed
    )

    network.setNeighbours(net.getAllNeighbours)
     */
  }

  def sigma() = {
    //val s: sigma.Sigma = sigma.Sigma("#sigmaDiv")
    // s.graph.addNode(new sigma.Node("1"))
  }

  def jnetworkx() = {
    //val nxDiv = appendCanvas(dom.document.getElementById("canvasContainer"), "netDiv")
    val g: jsnetworkx.Graph = jsnetworkx.Network.gnpRandomGraph(10,0.4)
    //jsnetworkx.Network.draw(g, jsnetworkx.DrawOptions("#netDiv"))
  }

  def plainSimulation() = {
    // Plain simulation
    val nodes = ArrayBuffer((0 to 100):_*)
    net = BasicSimulationIncarnation.simulatorFactory.simulator(
      idArray = nodes,
      nbrMap = mutable.Map(nodes.map((id: Int) => id->(id-3 to id+3+1).filter(x => x>=0 && x<100).toSet).toSeq:_*),
      nbrSensors = {
        case NBR_RANGE => { case (id,idn) => 1 }
      },
      localSensors = {
        case "source" => { case id => id == 10 || id == 50 || id == 70 }
      }
    )
    program = new FooProgram()
  }

  @JSExportTopLevel("loadNewProgram")
  def loadNewProgram(): Unit = {
    val programText = s"""var dsl = new ScafiDsl();
                      var f = () => { with(dsl){
                        var res = ${document.getElementById("program").asInstanceOf[html.Input].value};
                        return res;
                      }; };
                      dsl.programExpression = f;
                      [dsl, f]
    """
    println(s"Evaluating: ${programText}")

    val programFunctionAndProgram = js.eval(programText).asInstanceOf[js.Array[Any]]
    val aggregateProgram = programFunctionAndProgram(0).asInstanceOf[ScafiDsl]
    val programFunction = programFunctionAndProgram(1).asInstanceOf[js.Function0[Any]]
    // TODO: use aggregateProgram for running simulation
    //program = aggregateProgram.asInstanceOf[CONTEXT => EXPORT]
    spatialProgram = aggregateProgram.asInstanceOf[web.CONTEXT => web.EXPORT]
  }

  @JSExportTopLevel("switchSimulation")
  def switchSimulation(): Unit = {
    handle match {
      case Some(h) => { clearInterval(h); handle = None }
      case None => handle = Some(setInterval(100) { println(spatialNet.exec(spatialProgram)) })
    }
  }

  @JSExportTopLevel("onSelectProgram")
  def onSelectProgram(): Unit = {
    val p = selectProgram.asInstanceOf[html.Input].value
    println(s"Selected ${p}")
    document.getElementById("program").innerHTML = programs(p)
    loadNewProgram()
  }

  val programs = Map(
    "hello scafi" -> "\"hello scafi\"",
    "round counter" -> "rep(0, (k) => k+1)",
    "gradient" ->
      """
        | rep(Infinity, (d) => {
        |  return mux(sense("source"), 0.0,
        |    foldhoodPlus(() => Infinity, Math.min, () => nbr(() => d) + nbrvar("nbrRange"))
        |  )
        | })
        |""".stripMargin
  )

  var selectProgram: Element = _

  def configurePage(): Unit = {
    // Add button to set the program
    selectProgram = document.createElement("select")
    selectProgram.setAttribute("onchange", "if(this.selectedIndex) onSelectProgram()")
    var k = 0
    for(p <- programs.keySet){
      val c = document.createElement("option")
      c.setAttribute("value",p)
      c.innerHTML = p
      if(k==0) {
        c.setAttribute("selected","selected")
        selectProgram.asInstanceOf[html.Input].value = p
      }
      selectProgram.appendChild(c)
      k+=1
    }
    onSelectProgram()
    document.getElementById("config").appendChild(selectProgram)

    // Add button to set the program
    val loadProgramBtn = document.createElement("button")
    loadProgramBtn.setAttribute("onClick", "loadNewProgram()")
    loadProgramBtn.textContent = "Load new program";
    document.getElementById("config").appendChild(loadProgramBtn)

    // Add button to start/stop simulation
    val runSimBtn = document.createElement("button")
    runSimBtn.setAttribute("onClick", "switchSimulation()")
    runSimBtn.textContent = "Start/stop simulation";
    document.getElementById("config").appendChild(runSimBtn)
  }
}
