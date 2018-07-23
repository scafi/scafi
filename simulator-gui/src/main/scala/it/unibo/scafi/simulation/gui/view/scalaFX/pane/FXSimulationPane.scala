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
class FXSimulationPane (val inputController : InputCommandController[_],
                                    override val drawer : FXDrawer) extends AbstractFXSimulationPane {
  //internal representation of node
  private val _nodes : mutable.Map[ID,(drawer.OUTPUTNODE,Point2D)] = mutable.Map()
  //internal representation of neighbour
  private val  neighbours : mutable.Map[ID,mutable.Map[ID,NodeLine]] = mutable.Map()
  //internal representation of devices
  private val devices : mutable.Map[ID,mutable.Map[Any,drawer.OUTPUTNODE]] = mutable.Map()
  //used to flush method
  type ACTION = ()=>Unit
  //a list of changes that are show only when a presenter call flush
  private var changes : List[ACTION] = List.empty
  def nodes : Map[ID,(drawer.OUTPUTNODE,Point2D)] =_nodes.toMap

  override def outNode(node: NODE): Unit = {
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
  override def removeNode(node: ID): Unit = {}

  override def outNeighbour(node : (ID,Set[_ <: ID])): Unit = {
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
      val ntox : mutable.Map[ID,NodeLine] = this.neighbours(node._1)
      //CHECK IF THE LINK IS ALREADY SHOW
      //add each new link
      val action : ACTION = (() => this.children.add(link))
      changes = action :: changes
      ntox += x -> link
    }}
  }

  def removeNeighbour(nodes : (ID,Set[_ <: ID])) = {
    //utility method used to remove a neigbour
    def erase(start: ID, end: ID): Unit = {
      //take the neighbour of current node
      val map = this.neighbours(start)
      val toRemove = map(end)
      //unbind the link with proprieties
      toRemove.unbind()
      //remove the current link
      val action : ACTION = (() => this.children -= toRemove)
      changes = action :: changes
      map -= end
    }
    //verify if neighbour is already show or not
    def checkPresence(start: ID, end: ID) = this.neighbours.get(start).isDefined && this.neighbours(start).contains(end)
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
  override def outDevice(node: NODE): Unit = {
    var toAdd : List[javafx.scene.Node] = List()

    val devs = node.devices
    if(!devices.get(node.id).isDefined) devices += node.id -> mutable.Map()
    val oldDev = devices(node.id)
    devs foreach {
      dev => {
        val current : drawer.OUTPUTNODE = _nodes(node.id)._1
        val oldPrintedDev = oldDev.get(dev.name)
        val newDev : Option[drawer.OUTPUTNODE] = if(!oldPrintedDev.isDefined) {
          drawer.deviceToGraphicsNode(current,dev)
        } else {
          None
        }

        if(newDev.isDefined){
          oldDev += dev.name.toString -> newDev.get
          val action : ACTION = (() => this.children.add(newDev.get))
          changes = action :: changes
        }

        val action : ACTION = (() => drawer.updateDevice(current,dev,oldDev.get(dev.name)))
        changes = action :: changes
      }
    }
  }

  override def clearDevice(node: ID): Unit = {
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
