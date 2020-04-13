package it.unibo.scafi.js

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSImport, JSName}

@js.native
@JSImport("jsnetworkx", JSImport.Namespace)
//@JSExportTopLevel("net")
object Network extends js.Object {
  // Random graph of n nodes and m edges
  def gnpRandomGraph(n: Int, m: Int): Graph = js.native

  def draw(g: Graph, options: DrawOptions): Unit = js.native
}

class DrawOptions(@JSName("element") val domElement: String,
                  @JSName("d3") val d3: d3facade.type = d3facade) extends js.Object
object DrawOptions {
  def apply(elem: String, d3: d3facade.type = d3facade) = new DrawOptions(elem, d3)
}

@js.native
@JSImport("jsnetworkx", "Graph")
class Graph extends js.Object {
  def addNode(node: String, data: Any): Unit = js.native
}

@JSExportTopLevel("net")
object NetUtils {
  @JSExport
  def utils(): Network.type = Network

  @JSExport
  def graph(): Graph = new Graph()
}