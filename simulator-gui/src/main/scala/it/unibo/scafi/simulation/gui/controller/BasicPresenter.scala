package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.{NodesDeviceChanged, NodesMoved}
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.NodesRemoved
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.view.SimulationView

/**
  * @param world to show
  * @param neighbourRender a boolean value to define if show the neighbours or not
  */
class BasicPresenter[W <: SensorPlatform](val world : W,
                                          val neighbourRender : Boolean) extends Presenter[W]{

  val removed = world.createObserver(Set(NodesRemoved))
  val moved = world.createObserver(Set(NodesMoved))
  val devChanged = world.createObserver(Set(NodesDeviceChanged))
  var out : Option[SimulationView[world.type]] = None
  private var prevNeighbour : Map[world.ID,Set[world.ID]] = world.network.neighbours()
  world <-- removed <-- moved <-- devChanged
  override type OUTPUT = SimulationView[world.type]
  //RENDER COMPONENT DECIDE WHAT RENDER TO OUTPUT AND HOW
  override def onTick(float: Float): Unit = {
    if(out.isEmpty) {
      return;
    }
    val nodesRemoved = removed.nodeChanged()
    if (!nodesRemoved.isEmpty) {
      LogManager.log("view erasing..", LogManager.Middle)
      nodesRemoved foreach { out.get.removeNode(_)}
    }
    //MOVING
    val nodesMoved = moved.nodeChanged()
    val x = System.currentTimeMillis()
    if (!nodesMoved.isEmpty) {
      LogManager.log("NODE MOVED : " + nodesMoved, LogManager.Low)
      var toAdd : Map[world.ID,Set[world.ID]] = Map()
      var toRemove : Map[world.ID,Set[world.ID]] = Map()
      nodesMoved foreach { x =>
        val node = world(x).get
        if (this.neighbourRender) {
          val oldIds = this.prevNeighbour(x)
          val newIds = world.network.neighbours(x)
          val id: world.ID = world(x).get.id
          val ids: Set[world.ID] = oldIds -- newIds
          val add = newIds -- oldIds
          if (!ids.isEmpty) {
            toRemove += id -> ids
          }
          if (!add.isEmpty) {
            toAdd += node.id -> add
          }
        }
      }
      this.prevNeighbour = world.network.neighbours()
      val x = System.currentTimeMillis()
      nodesMoved foreach {x => out.get.outNode(world(x).get)}
      if(neighbourRender) {
        toAdd foreach { x => out.get.outNeighbour(x)}
        toRemove foreach {x => out.get.removeNeighbour(x)}
      }
    }
    val deviceToOut = devChanged.nodeChanged()
    if(!deviceToOut.isEmpty) {
      deviceToOut foreach {x => out.get.outDevice(world(x).get)}
    }
    out.get.flush()
  }
}
