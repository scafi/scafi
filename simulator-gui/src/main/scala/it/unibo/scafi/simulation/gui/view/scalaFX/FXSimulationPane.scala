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
import scalafx.scene.shape.Line

class FXSimulationPane extends Pane with SimulationOutput with GraphicsOutput {
  private var nodes : Map[World#ID,Set[Node]] = Map()
  private var neighbours : Map[World#ID,mutable.Set[Node]] = Map()
  private val SCALE_DELTA = 1.1;
  override def out[N <: World#Node](node: Set[N]): Unit = {
    node foreach  { x => {
        if(nodes.contains(x.id)) remove(Set(x.id))
        val shape = nodeToScalaFXNode(x)
        this.nodes += x.id -> Set(shape)
        this.children.add(shape)

      }
    }
  }
  // SCROLLING TO REMOVE HERE
  import scalafx.Includes._
  this.onScroll = (e : ScrollEvent) => {
    e.consume();
      val scaleFactor = if(e.getDeltaY() > 0) SCALE_DELTA else 1 / SCALE_DELTA
      this.setScaleX(this.getScaleX() * scaleFactor);
      this.setScaleY(this.getScaleY() * scaleFactor);

  }
  //TODO
  override def remove[ID <: World#ID](node: Set[ID]): Unit = {
    node foreach  {x => {nodes(x).foreach(this.children.remove(_))}}
    this.requestLayout()
  }

  override def outNeighbour[N <: World#Node](node: N, neighbour: Set[N]): Unit = {
    if(this.neighbours.get(node.id).isDefined) {
      this.neighbours(node.id) foreach {this.children.remove(_)}
      this.neighbours(node.id).clear()
    }
    val p : Point2D= (node.position)
    neighbour foreach {x => {
      val ep : Point2D = x.position
      val l = new Line {
        startX = p.x
        startY = p.y
        endX = ep.x
        endY = ep.y
        stroke = Color.Gray
      }
      this.children.add(l)
      /**
        * THINK TO ANOTHER SOLUTION
        */
      if(!this.neighbours.get(node.id).isDefined) {
        this.neighbours += node.id -> mutable.Set()
      } else {
        val s = this.neighbours(node.id)
        s += l        
      }
    }}
  }
}
