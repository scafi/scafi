package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.controller.logical.AsyncLogicController
import it.unibo.scafi.simulation.gui.model.space.Point3D

class MovementSyncController[N <: ScafiLikeWorld#NODE] (velocity : Float, world : ScafiLikeWorld)(nodes : Set[N]) extends AsyncLogicController[ScafiLikeWorld]{

  override def onTick(float: Float): Unit = {
    nodes foreach {x => {
      val nodeToMove = world(x.id).get
      world.moveNode(nodeToMove.id,Point3D(nodeToMove.position.x + velocity * float,nodeToMove.position.y,nodeToMove.position.z))
    }}
  }
  override protected var delta: Int = 100
  override protected val minDelta: Int = 100
  override protected val maxDelta = Some(1000)
  override protected var currentExecutor: ActorExecutor = _

  override protected def AsyncLogicExecution(): Unit = { }

  override protected val threadName: String = "movement-logic"
}
