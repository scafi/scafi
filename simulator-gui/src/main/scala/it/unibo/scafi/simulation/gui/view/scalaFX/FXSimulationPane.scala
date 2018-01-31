package it.unibo.scafi.simulation.gui.view.scalaFX
//TODO TRY TO CONVERT LIST[NODE] TO LIST[JAVAFX.NODE}
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiInputController
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.SelectionArea

import scala.collection.mutable
import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.paint.Color
import scalafx.Includes._
//TODO PROBLEMS WITH NEIGHBOURS, 
class FXSimulationPane (val inputController : ScafiInputController)extends AbstractFXSimulationPane with SelectionArea {
  private val _nodes : mutable.Map[World#ID,(Node,Point2D)] = mutable.Map()
  private val  neighbours : mutable.Map[World#ID,mutable.Map[World#ID,Node]] = mutable.Map()
  private val devices : mutable.Map[World#ID,Set[Node]] = mutable.Map()

  def nodes : Map[World#ID,(Node,Point2D)] =_nodes.toMap
  override def outNode[N <: World#Node](node: Set[N]): Unit = {
    val nodeAdding : mutable.Set[javafx.scene.Node] = mutable.Set[javafx.scene.Node]()
    node foreach  { x => {
        val p : Point2D = x.position
        if(_nodes.contains(x.id)) {
          val (node,oldP) = _nodes(x.id)
          node.translateX = p.x - oldP.x
          node.translateY = p.y - oldP.y
        } else {
          val shape : Node = nodeToScalaFXNode(x)
          this._nodes += x.id -> (shape,p)
          nodeAdding += shape
        }
      }
    }
    this.children.addAll(nodeAdding.toSeq:_*)
  }
  //TODO
  override def removeNode[ID <: World#ID](node: Set[ID]): Unit = {
    val toRemove : TraversableOnce[javafx.scene.Node] = node filter {_nodes.get(_).isDefined} map {x => Node.sfxNode2jfx(_nodes(x)._1)}  //EXPLICIT CONVERSION
    this.children --= toRemove
    node foreach {this._nodes -= _}
  }

  override def outNeighbour[N <: World#Node](node: N, neighbour: Set[N]): Unit = {
    val gnode = this._nodes(node.id)._1
    val linkAdding : mutable.Set[javafx.scene.Node] = mutable.Set[javafx.scene.Node]()
    neighbour foreach {x => {
      val endGnode = this._nodes(x.id)._1
      val link = new NodeLine(gnode,endGnode,Color.web("rgba(125,125,125,0.1)"))
      if(!this.neighbours.get(node.id).isDefined) {
        this.neighbours += node.id -> mutable.Map()
      }

      val ntox : mutable.Map[World#ID,Node] = this.neighbours(node.id)
      //CHECK IF THE LINK IS ALREADY SHOW
      if(!ntox.contains(x.id)) {
        linkAdding += link
        ntox += (x.id -> link)
      }
    }}
    this.children.addAll(linkAdding.toSeq:_*)
  }

  override def removeNeighbour[ID <: World#ID](node: ID, neighbour: Set[ID]): Unit = {

    val linkRemove : mutable.Set[javafx.scene.Node] = mutable.Set[javafx.scene.Node]()

    def erase(start : ID, end : ID): Unit = {
      val map = this.neighbours(start)
      val toRemove = map(end)
      linkRemove += toRemove
      this.children -= toRemove
      map -= end
    }

    def checkPresence(start :ID, end : ID) = this.neighbours.get(start).isDefined && this.neighbours(start).contains(end)
    neighbour foreach { x =>
      if(checkPresence(node,x)) {
        erase(node,x)
      }
      if (checkPresence(x,node)) {
        erase(x,node)
      }
    }
    val removing : TraversableOnce[javafx.scene.Node] = linkRemove.toSeq
    this.children --= removing
  }

  override def outDevice[N <: World#Node](node: N): Unit = {
    val devs : Set[javafx.scene.Node] = deviceToNode(node.devices,_nodes(node.id)._1)
    this.children.addAll(devs.toSeq:_*)
  }

  def test(node :Node *) {

  }
  /**
    * remove all devices associated to a node
    *
    * @param node the node
    * @tparam N the type of node
    */
  override def clearDevice[N <: World#ID](node: N): Unit = {
    if(this.devices.get(node).isDefined) {
      this.devices(node) foreach {this.children -= _ }
      this.devices -= node
    }
  }
}
