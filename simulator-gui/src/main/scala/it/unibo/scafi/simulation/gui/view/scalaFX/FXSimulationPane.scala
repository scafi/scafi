package it.unibo.scafi.simulation.gui.view.scalaFX

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.{Circle => InternalCircle, Rectangle => InternalRectangle}
import it.unibo.scafi.simulation.gui.view.{GraphicsOutput, SimulationOutput}

import scala.collection.mutable
import scalafx.geometry.Point2D
import scalafx.scene.Node
import scalafx.scene.input.ScrollEvent
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

class FXSimulationPane extends Pane with SimulationOutput with GraphicsOutput {
  private val nodes : mutable.Map[World#ID,(Node,Point2D)] = mutable.Map()
  private val neighbours : mutable.Map[World#ID,mutable.Map[World#ID,Node]] = mutable.Map()
  private val devices : mutable.Map[World#ID,Set[Node]] = mutable.Map()
  private val SCALE_DELTA = 1.1;
  // SCROLLING TO REMOVE HERE
  import scalafx.Includes._
  this.onScroll = (e : ScrollEvent) => {
    e.consume();
    val scaleFactor = if(e.getDeltaY() > 0) SCALE_DELTA else 1 / SCALE_DELTA
    this.setScaleX(this.getScaleX() * scaleFactor);
    this.setScaleY(this.getScaleY() * scaleFactor);
  }
  override def outNode[N <: World#Node](node: Set[N]): Unit = {
    node foreach  { x => {
        val p : Point2D = x.position
        if(nodes.contains(x.id)) {
          val (node,oldP) = nodes(x.id)
          node.translateX = p.x - oldP.x
          node.translateY = p.y - oldP.y
        } else {
          val shape = nodeToScalaFXNode(x)
          this.nodes += x.id -> (shape,p)
          this.children.add(shape)
        }
      }
    }
  }
  //TODO
  override def removeNode[ID <: World#ID](node: Set[ID]): Unit = {
    node foreach  {x => {this.children.remove(nodes(x)._1 )}}
    this.requestLayout()
  }

  override def outNeighbour[N <: World#Node](node: N, neighbour: Set[N]): Unit = {
    val gnode = this.nodes(node.id)._1
    neighbour foreach {x => {
      val endGnode = this.nodes(x.id)._1
      val link = new NodeLine(gnode,endGnode,Color.Gray)
      if(!this.neighbours.get(node.id).isDefined) {
        this.neighbours += node.id -> mutable.Map()
      }
      val s : mutable.Map[World#ID,Node] = this.neighbours(node.id)
      //CHECK IF THE LINK IS ALREADY SHOW
      if(!s.contains(x.id)) {
        this.children.add(link)
        s += (x.id -> link)
      }
    }}
  }

  override def removeNeighbour[ID <: World#ID](node: ID, neighbour: Set[ID]): Unit = {
    def erase(start : ID, end : ID): Unit = {
      val map = this.neighbours(start)
      val toRemove = map(end)
      this.children.remove(toRemove)
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
  }

  override def outDevice[N <: World#Node](node: N): Unit = {
    val devs = deviceToNode(node.devices,nodes(node.id)._1)
    devs.foreach(this.children.add(_))
    this.devices += node.id -> devs
  }

  /**
    * remove all devices associated to a node
    *
    * @param node the node
    * @tparam N the type of node
    */
  override def clearDevice[N <: World#ID](node: N): Unit = {
    if(this.devices.get(node).isDefined) {
      this.devices(node) foreach {this.children.remove(_)}
      this.devices -= node
    }
  }
}
