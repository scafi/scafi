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

@js.native
@JSImport("sigma", JSImport.Namespace)
object Sigma extends js.Object {
}

@JSExportTopLevel("DrawOptions")
class DrawOptions(@JSName("element") val domElement: String,
                  @JSName("d3") val d3: d3facade.type = d3facade) extends js.Object {
  @JSName("withLabels") val withLabels = true
  @JSName("stickyDrag") val stickyDrag = true
  @JSName("labelStyle") val labelStyle = new js.Object { @JSName("fill") val fill = "#fff" }
  @JSName("nodeStyle") val nodeStyle = new js.Object { @JSName("fill") val fill = (d: Any) => "#a00" /* d.data.color */ }
  @JSName("layoutAttr") val layoutAttr = new js.Object {
    //@JSName("linkDistance") val linkDistance = "50px"
  }

}
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

  @JSExport
  def d3(): d3facade.type = d3facade

  @JSExport
  def sigma() = Sigma
}