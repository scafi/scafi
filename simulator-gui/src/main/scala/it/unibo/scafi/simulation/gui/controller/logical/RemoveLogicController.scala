package it.unibo.scafi.simulation.gui.controller.logical

import it.unibo.scafi.simulation.gui.model.aggregate.implementation.immutable.AggregateWorld

import scala.util.Random
class RemoveLogicController[W <: AggregateWorld](val world : AggregateWorld) extends LogicController[W]{
  private var execute = false
  private val r = new Random()

  override def onTick(float: Float): Unit = {
      if(world.nodes.size > 0 && execute) {
        val deleted = r.nextInt(world.nodes.size)
        val idDelete : world.ID = world.nodes.toSet.toIndexedSeq(deleted).id
        world.removeNode(idDelete)
      }
  }
  override def start: Unit = execute = true

  override def stop: Unit = execute = false
}
