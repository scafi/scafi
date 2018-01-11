package it.unibo.scafi.simulation.gui.test.core

import it.unibo.scafi.simulation.gui.model.core.{Node, TransformNode}
import it.unibo.scafi.simulation.gui.model.space.{Position, Position2D}
import org.scalatest.{FunSpec, Matchers}
import it.unibo.scafi.simulation.gui.model.space.Position3D
class NodeTest extends FunSpec with Matchers{
  val checkThat = new ItWord

  class SimpleNode(val id: Int) extends Node{
    override type ID = Int
  }

  val node = new SimpleNode(1) with TransformNode {
    override type P = Position2D

    override var position: Position2D = _
  }

  node.position = Position3D.toPosition2D(Position.ZERO)
}
