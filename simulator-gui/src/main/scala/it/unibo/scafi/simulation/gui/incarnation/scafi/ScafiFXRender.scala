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

/**
  * the render of output to a graphics view (scalafx library)
  * @param world to show
  * @param out where render show thing
  * @param contract the scafi simulation
  * @param neighbourRender a boolean value to define if show the neighbours or not
  */
//TODO VERY SIMPLE VERSION
class ScafiFXRender(val world : ScafiLikeWorld,
                    val out : FXSimulationPane,
                    val contract : ScafiSimulationContract[ScafiLikeWorld,ScafiPrototype],
                    val neighbourRender : Boolean) extends Presenter[BasicPlatform with Source]{

  val removed = world.createObserver(Set(NodesRemoved))
  val moved = world.createObserver(Set(NodesMoved))
  world <-- removed <-- moved
  override type OUTPUT = FXSimulationPane
  //RENDER COMPONENT DECIDE WHAT RENDER TO OUTPUT AND HOW
  override def onTick(float: Float): Unit = {
    val nodesRemoved = removed.nodeChanged()
    if(!nodesRemoved.isEmpty) {
      LogManager.log("view erasing..",LogManager.Middle)
      Platform.runLater{
        out.removeNode(nodesRemoved)
      }
    }
    //MOVING
    val nodesMoved = moved.nodeChanged()
    if(!nodesMoved.isEmpty) {
      LogManager.log("view moving..",LogManager.Middle)
      Platform.runLater{
        out.outNode(world(nodesMoved))
        nodesMoved foreach { x =>
          val oldIds = contract.getSimulation.get.neighbourhood(x)
          val node = world(x).get
          contract.getSimulation.get.setPosition((x),Point3D(node.position.x,node.position.y,node.position.z))
          if(this.neighbourRender) {
            val newIds = contract.getSimulation.get.neighbourhood(x)
            val id : world.ID = world(x).get.id
            val ids : Set[world.ID] = oldIds -- newIds
            if(!ids.isEmpty) {
              out.removeNeighbour(id,ids)
            }
            if(!(newIds -- oldIds).isEmpty) {
              out.outNeighbour(world(x).get,world.apply(newIds -- oldIds))
            }
          }
        }
      }
    }
  }
}
