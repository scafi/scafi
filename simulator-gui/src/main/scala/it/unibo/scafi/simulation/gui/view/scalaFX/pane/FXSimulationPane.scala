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
  private val _nodes : mutable.Map[W#ID,(drawer.OUTPUTNODE,Point2D)] = mutable.Map()
  private val  neighbours : mutable.Map[W#ID,mutable.Map[W#ID,NodeLine]] = mutable.Map()
  private val devices : mutable.Map[W#ID,mutable.Map[String,drawer.OUTPUTNODE]] = mutable.Map()
  //TODO REMBER TO REMOVE
  private val color = Color.color(0.8,0.8,0.8,0.2)
  def nodes : Map[W#ID,(drawer.OUTPUTNODE,Point2D)] =_nodes.toMap
  override def outNode(node: Set[W#NODE]): Unit = {
    val nodesToAdd : mutable.Set[javafx.scene.Node] = mutable.Set[javafx.scene.Node]()
    val x = System.currentTimeMillis()
    var operation : List[()=>Unit] = List()
    node foreach  { x => {
        val p : Point2D = x.position
        if(_nodes.contains(x.id)) {
          val (node,oldP) = _nodes(x.id)
          Platform.runLater{
            node.translateX = p.x - oldP.x
            node.translateY = p.y - oldP.y
          }

        } else {
          val shape : drawer.OUTPUTNODE = drawer.nodeGraphicsNode(x)
          this._nodes += x.id -> (shape,p)
          nodesToAdd += shape
        }
      }
    }
    if(!nodesToAdd.isEmpty) {
      Platform.runLater{
        this.children.addAll(nodesToAdd.toSeq:_*)
      }
    }
  }
  //TODO
  override def removeNode(node: Set[W#ID]): Unit = {
    val toRemove : TraversableOnce[javafx.scene.Node] = node filter {_nodes.get(_).isDefined} map {x => Node.sfxNode2jfx(_nodes(x)._1)}  //EXPLICIT CONVERSION
    this.children --= toRemove
    node foreach {this._nodes -= _}
  }

  override def outNeighbour(nodes : Map[W#NODE,Set[W#NODE]]): Unit = {
    val linkToAdd : mutable.Set[javafx.scene.Node] = mutable.Set[javafx.scene.Node]()
    nodes foreach { node => {
      val gnode = this._nodes(node._1.id)._1
      node._2 foreach { x => {
        val endGnode = this._nodes(x.id)._1
        val link : NodeLine = new NodeLine(gnode,endGnode,color)
        if(!this.neighbours.get(node._1.id).isDefined) {
          this.neighbours += node._1.id -> mutable.Map()
        }

        val ntox : mutable.Map[W#ID,NodeLine] = this.neighbours(node._1.id)
        //CHECK IF THE LINK IS ALREADY SHOW
          linkToAdd += link
          ntox += (x.id -> link)
      }}
      }
    }
    Platform.runLater{
      this.children.addAll(linkToAdd.toSeq:_*)
    }
  }

  override def removeNeighbour(nodes : Map[W#ID,Set[W#ID]]): Unit = {

    val linkRemove: mutable.Set[javafx.scene.Node] = mutable.Set[javafx.scene.Node]()

    def erase(start: W#ID, end: W#ID): Unit = {
      val map = this.neighbours(start)
      val toRemove = map(end)
      toRemove.unbind()
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
    var toAdd : List[javafx.scene.Node] = List()
    nodes foreach {
      node => {
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
              toAdd = newDev.get :: toAdd
            }
          }
        }
      }
    }
    Platform.runLater{
      this.children.addAll(toAdd:_*)
    }

  }

  override def clearDevice(nodes: Set[W#ID]): Unit = {
    nodes foreach{ node =>
      if(this.devices.get(node).isDefined) {
        this.devices(node) foreach {this.children -= _._2 }
        this.devices -= node
      }
    }
  }
}
