package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.controller.Presenter
import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.NodesMoved
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.NodesRemoved
import it.unibo.scafi.simulation.gui.model.simulation.BasicPlatform
import it.unibo.scafi.simulation.gui.pattern.observer.Source
import it.unibo.scafi.simulation.gui.view.scalaFX.FXSimulationPane
import it.unibo.scafi.space.Point3D

import scalafx.application.Platform
//TODO VERY SIMPLE VERSION
class ScafiFXRender(val world : ScafiLikeWorld,
                    val out : FXSimulationPane,
                    val contract : ScafiSimulationContract[ScafiLikeWorld,ScafiPrototype]) extends Presenter[BasicPlatform with Source]{
  val removed = world.createObserver(Set(NodesRemoved))
  val moved = world.createObserver(Set(NodesMoved))
  world <-- removed <-- moved
  override type OUTPUT = FXSimulationPane

  override def onTick(float: Float): Unit = {
    val nodesRemoved = removed.nodeChanged()
    if(!nodesRemoved.isEmpty) {
      LogManager.log("view erasing..",LogManager.Middle)
      Platform.runLater{
        out.remove(nodesRemoved)
      }
    }
    val nodesMoved = moved.nodeChanged()
    if(!nodesMoved.isEmpty) {
      LogManager.log("view moving..",LogManager.Middle)
      world(nodesMoved) foreach {x => contract.getSimulation.get.setPosition(x.id,Point3D(x.position.x,x.position.y,x.position.z))}
      Platform.runLater{
        out.out(world(nodesMoved))
        nodesMoved foreach { x =>
          val ids = contract.getSimulation.get.neighbourhood(x)
          out.outNeighbour(world(x).get,world.apply(ids))
        }
      }
    }
  }
}
