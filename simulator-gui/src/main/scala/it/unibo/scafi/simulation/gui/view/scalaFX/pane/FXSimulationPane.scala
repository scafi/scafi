package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import it.unibo.scafi.simulation.gui.controller.InputCommandController
import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.scalaFX.common.AbstractFXSimulationPane
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.FXDrawer
import it.unibo.scafi.simulation.gui.view.scalaFX.{NodeLine, _}

import scala.collection.mutable
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.paint.Color
class FXSimulationPane[W <: World] (val inputController : InputCommandController[_],
                                    override val drawer : FXDrawer) extends AbstractFXSimulationPane[W] {
  //internal representation of node
  private val _nodes : mutable.Map[W#ID,(drawer.OUTPUTNODE,Point2D)] = mutable.Map()
  //internal representation of neighbour
  private val  neighbours : mutable.Map[W#ID,mutable.Map[W#ID,NodeLine]] = mutable.Map()
  //internal representation of devices
  private val devices : mutable.Map[W#ID,mutable.Map[String,drawer.OUTPUTNODE]] = mutable.Map()
  //used to flush method
  type ACTION = ()=>Unit
  //a list of changes that are show only when a presenter call flush
  private var changes : List[ACTION] = List.empty
  def nodes : Map[W#ID,(drawer.OUTPUTNODE,Point2D)] =_nodes.toMap

  override def outNode(node: W#NODE): Unit = {
    //take the current node position
    val p : Point2D = node.position
    if(_nodes.contains(node.id)) {
      //if the node is already show, the view change the node position
      val (n,oldP) = _nodes(node.id)
      //with bind propriety, i can moved the node
      val action : ACTION = () => {
        n.translateX = p.x - oldP.x
        n.translateY = p.y - oldP.y
      }
      changes = action :: changes
    } else {
      //if the node isn't show, with drawer i create new graphics instanced associated with the node
      val shape : drawer.OUTPUTNODE = drawer.nodeGraphicsNode(node)
      this._nodes += node.id -> (shape,p)
      //add the new shape in the view
      val action : ACTION = () => {
        this.children += shape
      }
      changes = action :: changes
    }
  }
  //TODO
  override def removeNode(node: W#ID): Unit = {}

  override def outNeighbour(node : (W#ID,Set[W#ID])): Unit = {
    //i take the current graphics node
    val gnode = this._nodes(node._1)._1
    //foreach neighbour i create a new link
    node._2 foreach { x => {
      val endGnode = this._nodes(x)._1
      val link : NodeLine = new NodeLine(gnode,endGnode,lineColor)
      if(!this.neighbours.get(node._1).isDefined) {
        this.neighbours += node._1 -> mutable.Map()
      }
      //the new map used to represent the neighbours
      val ntox : mutable.Map[W#ID,NodeLine] = this.neighbours(node._1)
      //CHECK IF THE LINK IS ALREADY SHOW
      //add each new link
      val action : ACTION = (() => this.children.add(link))
      changes = action :: changes
      ntox += (x -> link)
    }}
  }

  def removeNeighbour(nodes : (W#ID,Set[W#ID])) = {
    //utility method used to remove a neigbour
    def erase(start: W#ID, end: W#ID): Unit = {
      //take the neighbour of current node
      val map = this.neighbours(start)
      val toRemove = map(end)
      //unbind the link with proprieties
      toRemove.unbind()
      //remove the current link
      val action : ACTION = (() => this.children.remove(toRemove))
      changes = action :: changes
      map -= end
    }
    //verify if neighbour is already show or not
    def checkPresence(start: W#ID, end: W#ID) = this.neighbours.get(start).isDefined && this.neighbours(start).contains(end)
    //remove the linking
    nodes._2.foreach { x => {
      if (checkPresence(nodes._1, x)) {
        erase(nodes._1, x)
      }
      if (checkPresence(x, nodes._1)) {
        erase(x, nodes._1)
      }
    }}
  }
  //TODO METTI A POSTO IN SEGUITO, EVITA DI ITERARE OGNI VOLTA TUTTI I DEVICE PER DINCI
  override def outDevice(node: W#NODE): Unit = {
    var toAdd : List[javafx.scene.Node] = List()

    val devs = node.devices
    if(!devices.get(node.id).isDefined) devices += node.id -> mutable.Map()
    val oldDev = devices(node.id)
    devs foreach {
      dev => {
        val current : drawer.OUTPUTNODE = _nodes(node.id)._1
        val oldPrintedDev = oldDev.get(dev.name.toString)
        val newDev = if(oldPrintedDev.isDefined) {
          val oldJFX : drawer.OUTPUTNODE = oldPrintedDev.get
          drawer.deviceToGraphicsNode(current,dev,Some(oldJFX))
        } else {
          drawer.deviceToGraphicsNode(current,dev,None)
        }
        if(newDev.isDefined){
          oldDev += dev.name.toString -> newDev.get
          val action : ACTION = (() => this.children.add(newDev.get))
          changes = action :: changes
        }
      }
    }
  }

  override def clearDevice(node: W#ID): Unit = {
    //remove all device associated with a id
    val toRemove : List[Node] = List()
    if (this.devices.get(node).isDefined) {
      this.devices(node) foreach {x => this.changes = (() => this.children -= x._2).asInstanceOf[ACTION] :: changes}
      this.devices -= node
    }
  }

  override def flush(): Unit = Platform.runLater {
      changes.foreach{_()}
      changes = List.empty
  }
}
