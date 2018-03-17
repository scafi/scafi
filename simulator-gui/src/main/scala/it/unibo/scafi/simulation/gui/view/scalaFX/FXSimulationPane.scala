package it.unibo.scafi.simulation.gui.view.scalaFX
import it.unibo.scafi.simulation.gui.controller.InputCommandController
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.FXDrawer

import scala.collection.mutable
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.paint.Color
//TODO MAKE INPUT CONTROLLER GENERIC IN WORLD
class FXSimulationPane[W <: World] (val inputController : InputCommandController[_],
                                    override val drawer : FXDrawer) extends AbstractFXSimulationPane[W] {
  private val _nodes : mutable.Map[W#ID,(Node,Point2D)] = mutable.Map()
  private val  neighbours : mutable.Map[W#ID,mutable.Map[W#ID,Node]] = mutable.Map()
  private val devices : mutable.Map[W#ID,Set[javafx.scene.Node]] = mutable.Map()
  //TODO REMBER TO REMOVE
  private val color = Color.rgb(225,225,225)
  def nodes : Map[W#ID,(Node,Point2D)] =_nodes.toMap
  override def outNode(node: Set[W#NODE]): Unit = {
    val nodeAdding : mutable.Set[javafx.scene.Node] = mutable.Set[javafx.scene.Node]()
    node foreach  { x => {
        val p : Point2D = x.position
        if(_nodes.contains(x.id)) {
          val (node,oldP) = _nodes(x.id)
          node.translateX = p.x - oldP.x
          node.translateY = p.y - oldP.y
        } else {
          val shape : Node = drawer.nodeGraphicsNode(x)
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
  override def removeNode(node: Set[W#ID]): Unit = {
    val toRemove : TraversableOnce[javafx.scene.Node] = node filter {_nodes.get(_).isDefined} map {x => Node.sfxNode2jfx(_nodes(x)._1)}  //EXPLICIT CONVERSION
    this.children --= toRemove
    node foreach {this._nodes -= _}
  }

  override def outNeighbour(nodes : Map[W#NODE,Set[W#NODE]]): Unit = {
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

        val ntox : mutable.Map[W#ID,Node] = this.neighbours(node._1.id)
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

  override def removeNeighbour(nodes : Map[W#ID,Set[W#ID]]): Unit = {

    val linkRemove: mutable.Set[javafx.scene.Node] = mutable.Set[javafx.scene.Node]()

    def erase(start: W#ID, end: W#ID): Unit = {
      val map = this.neighbours(start)
      val toRemove = map(end)
      linkRemove += toRemove
      map -= end
    }

    def checkPresence(start: W#ID, end: W#ID) = this.neighbours.get(start).isDefined && this.neighbours(start).contains(end)

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

  override def outDevice(nodes: Set[W#NODE]): Unit = {
    var toRemove : List[javafx.scene.Node] = List()
    var toAdd : List[javafx.scene.Node] = List()
    nodes.foreach{ node => {
      if(this.devices.get(node.id).isEmpty) {
        this.devices += node.id -> Set()
      }
      toRemove ++= (this.devices(node.id))
      val devs : Set[javafx.scene.Node] = drawer.deviceToGraphicsNode(node.devices,_nodes(node.id)._1)
      toAdd ++= devs
      this.devices += node.id -> devs
    }}
    Platform.runLater{
      this.children.removeAll(toRemove:_*)
      this.children.addAll(toAdd:_*)
    }
  }

  override def clearDevice(nodes: Set[W#ID]): Unit = {
    nodes foreach{ node =>
      if(this.devices.get(node).isDefined) {
        this.devices(node) foreach {this.children -= _ }
        this.devices -= node
      }
    }
  }
}
