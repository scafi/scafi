package it.unibo.scafi.js.sigma

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSImport}

/**
  * You may want to take a look at https://github.com/gilcu2/sigma.js4scala.js
  * which unfortunately targets only ScalaJS 0.6
  * Docs: https://github.com/jacomyal/sigma.js/wiki
  */
@js.native
@JSImport("sigma", JSImport.Namespace)
object Sigma extends js.Object {
  val sigma: js.Function0[js.Object] = js.native
}

@js.native
@JSImport("sigma", "Renderer")
class Renderer extends js.Object { }

@js.native
@JSImport("sigma", "Camera")
class Camera extends js.Object { }

@js.native
@JSImport("sigma", "QuadTree")
class QuadTree extends js.Object { }

@js.native
@JSImport("sigma", "MouseCaptor")
class MouseCaptor extends js.Object { }

@js.native
@JSImport("sigma", "WebGLRenderer")
class WebGLRenderer extends js.Object { }

@js.native
@JSImport("sigma.classes", "graph")
class Graph extends js.Object {
  def nodes(): Iterable[Node] = js.native
  def nodes(id: String): Node = js.native
  def nodes(nodes: Iterable[String]): Iterable[Node] = js.native

  def edges(): Iterable[Edge] =js.native
  def edges(id: String): Edge = js.native

  def addNode(node: Node): Graph = js.native
  def addEdge(edge: Edge): Graph = js.native

  def dropNode(node: Node): Graph = js.native
  def dropEdge(edge: Edge): Graph = js.native

  def read(descriptor: GraphDescriptor): Graph = js.native

  def clear(): Graph = js.native
  def kill(): Graph = js.native
}

@js.native
@JSImport("sigma.classes", "graph")
object Graph extends js.Object {
  def apply(): Graph = js.native
}

class Node(val id: String) extends js.Object {
}

@js.native
trait Edge extends js.Object {
  val id: String = js.native
  val source: String = js.native
  val target: String = js.native
}

@js.native
trait GraphDescriptor extends js.Object {
  val nodes: Iterable[Node]
  val edges: Iterable[Edge]
}