package it.unibo.scafi.simulation.gui.controller.logical

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld

import scala.util.Random
//TODO ONLY SIMPLE VERSION
class RemoveLogicController[W <: AggregateWorld](val world : AggregateWorld) extends AsyncLogicController[W]{
  override protected var delta: Int = 1000
  override protected val minDelta: Int = 1000
  override protected val maxDelta: Int = 1000
  private var toRemove : Set[world.ID] = Set()
  private val r = new Random()
  override protected var currentExecutor: ActorExecutor = _

  override protected def AsyncLogicExecution(): Unit = {
    if(world.nodes.size > 0) {
      val deleted = r.nextInt(world.nodes.size)
      val idDelete : world.ID = world.nodes.toSet.toIndexedSeq(deleted).id
      toRemove +=  idDelete
    }
  }

  override def onTick(float: Float): Unit = {
    if(!toRemove.isEmpty) {
      LogManager.log("Erasing.." + toRemove,LogManager.Middle)
      world.removeNodes(toRemove)
      toRemove = Set.empty
    }
  }
}
