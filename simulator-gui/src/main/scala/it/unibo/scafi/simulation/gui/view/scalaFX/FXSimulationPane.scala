package it.unibo.scafi.simulation.gui.view.scalaFX

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.model.space.Point2D
import it.unibo.scafi.simulation.gui.view.{GraphicsOutput, SimulationOutput}

import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.{Group, Node}

class FXSimulationPane extends Group with SimulationOutput with GraphicsOutput {
  private var nodes : Map[World#ID,Node] = Map()
  override def out[N <: World#Node](node: Set[N]): Unit = {
    println(node)
    node foreach  { x => {
        val p = x.position.asInstanceOf[Point2D]
        if(nodes.contains(x.id)) {
          this.children.removeAll(nodes.get(x.id).get)
        }
        val rect = new Rectangle {
          this.x = p.x
          this.y = p.y
          this.width = 10
          this.height = 10
          this.fill = Color.Red
        }
        this.nodes += x.id -> rect
        this.children.add(rect)
      }
    }
  }

  override def remove[N <: World#Node](node: Set[N]): Unit = {
    val toremove = node map {x => nodes(x.id)} foreach {this.children.removeAll(_)}
  }

  override def outNeighbour[N <: World#Node](node: N, neighbour: Set[N]): Unit = {

  }
}
