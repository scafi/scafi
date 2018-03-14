package it.unibo.scafi.simulation.gui.view.scalaFX
import it.unibo.scafi.simulation.gui.controller.{InputCommandSingleton, SimpleInputController}
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiLikeWorld
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.{KeyboardManager, FXSelectionArea}

import scala.collection.mutable
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.input.KeyEvent
import scalafx.scene.paint.Color
//TODO MAKE INPUT CONTROLLER GENERIC IN WORLD
class FXSimulationPane (val inputController : InputCommandSingleton) extends AbstractFXSimulationPane {
  private val _nodes : mutable.Map[World#ID,(Node,Point2D)] = mutable.Map()
  private val  neighbours : mutable.Map[World#ID,mutable.Map[World#ID,Node]] = mutable.Map()
  private val devices : mutable.Map[World#ID,Set[javafx.scene.Node]] = mutable.Map()
  //TODO REMBER TO REMOVE
  private val color = Color.rgb(225,225,225)
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
    Platform.runLater{
      this.children.addAll(nodeAdding.toSeq:_*)
    }
  }
  //TODO
  override def removeNode[ID <: World#ID](node: Set[ID]): Unit = {
    val toRemove : TraversableOnce[javafx.scene.Node] = node filter {_nodes.get(_).isDefined} map {x => Node.sfxNode2jfx(_nodes(x)._1)}  //EXPLICIT CONVERSION
    this.children --= toRemove
    node foreach {this._nodes -= _}
  }

  override def outNeighbour[N <: World#Node](nodes : Map[N,Set[N]]): Unit = {
    val linkAdding : mutable.Set[javafx.scene.Node] = mutable.Set[javafx.scene.Node]()
    nodes foreach { node => {
      val gnode = this._nodes(node._1.id)._1
      node._2 foreach { x => {
        val endGnode = this._nodes(x.id)._1
        //TODO REMEMBER TO REMOVE THIS MAGIC NUMBER
        val link = new NodeLine(gnode,endGnode,color)
        if(!this.neighbours.get(node._1.id).isDefined) {
          this.neighbours += node._1.id -> mutable.Map()
        }

        val ntox : mutable.Map[World#ID,Node] = this.neighbours(node._1.id)
        //CHECK IF THE LINK IS ALREADY SHOW
          linkAdding += link
          ntox += (x.id -> link)
      }}
      }
    }
    Platform.runLater{
      this.children.addAll(linkAdding.toSeq:_*)
    }
  }

  override def removeNeighbour[ID <: World#ID](nodes : Map[ID,Set[ID]]): Unit = {

    val linkRemove: mutable.Set[javafx.scene.Node] = mutable.Set[javafx.scene.Node]()

    def erase(start: ID, end: ID): Unit = {
      val map = this.neighbours(start)
      val toRemove = map(end)
      linkRemove += toRemove
      map -= end
    }

    def checkPresence(start: ID, end: ID) = this.neighbours.get(start).isDefined && this.neighbours(start).contains(end)

    nodes foreach { node => {
      node._2.foreach { x => {
        if (checkPresence(node._1, x)) {
          erase(node._1, x)
        }
        if (checkPresence(x, node._1)) {
          erase(x, node._1)
        }
      }}
    }}

    val removing: TraversableOnce[javafx.scene.Node] = linkRemove.toSeq
    Platform.runLater{
      this.children --= removing
    }
  }

  override def outDevice[N <: World#Node](nodes: Set[N]): Unit = {
    var toRemove : List[javafx.scene.Node] = List()
    var toAdd : List[javafx.scene.Node] = List()
    nodes.foreach{ node => {
      if(this.devices.get(node.id).isEmpty) {
        this.devices += node.id -> Set()
      }
      toRemove ++= (this.devices(node.id))
      val devs : Set[javafx.scene.Node] = deviceToNode(node.devices,_nodes(node.id)._1)
      toAdd ++= devs
      this.devices += node.id -> devs
    }}
    Platform.runLater{
      this.children.removeAll(toRemove:_*)
      this.children.addAll(toAdd:_*)
    }
  }

  override def clearDevice[ID <: World#ID](nodes: Set[ID]): Unit = {
    nodes foreach{ node =>
      if(this.devices.get(node).isDefined) {
        this.devices(node) foreach {this.children -= _ }
        this.devices -= node
      }
    }
  }
}
