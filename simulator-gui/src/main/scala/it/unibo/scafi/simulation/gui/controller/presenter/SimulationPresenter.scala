package it.unibo.scafi.simulation.gui.controller.presenter

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.NodesMoved
import it.unibo.scafi.simulation.gui.model.common.network.ConnectedWorld.NeighbourChanged
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.{NodesAdded, NodesRemoved}
import it.unibo.scafi.simulation.gui.model.sensor.SensorEvent.SensorChanged
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.view.SimulationView

/**
  * @param world to show
  * @param neighbourRender a boolean value to define if show the neighbours or not
  */
class SimulationPresenter[W <: SensorPlatform](val world : W,
                                               val neighbourRender : Boolean) extends Presenter[W,SimulationView]{

  val removed = world.createObserver(Set(NodesRemoved))
  val moved = world.createObserver(Set(NodesMoved,NodesAdded))
  val devChanged = world.createObserver(Set(SensorChanged))
  val networkChanged = world.createObserver(Set(NeighbourChanged))
  private var out : Option[SimulationView] = None
  private var prevNeighbour : Map[world.ID,Set[world.ID]] = Map()
  world <-- removed <-- moved <-- devChanged <-- networkChanged
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
    var toAdd : Map[world.ID,Set[world.ID]] = Map()
    var toRemove : Map[world.ID,Set[world.ID]] = Map()

    val neighbourChanged = networkChanged.nodeChanged()
    neighbourChanged foreach ( x => {
      val oldIds = this.prevNeighbour.get(x)
      val newIds = world.network.neighbours(x)
      val id: world.ID = world(x).get.id
      val ids: Set[world.ID] = oldIds.getOrElse(Set()) -- newIds
      val add = newIds -- oldIds.getOrElse(Set())
      if (!ids.isEmpty) {
        toRemove += id -> ids
      }
      if (!add.isEmpty) {
        toAdd += id -> add
      }
    })

    this.prevNeighbour = world.network.neighbours()
    nodesMoved foreach {x => out.get.outNode(world(x).get)}
    if(neighbourRender) {
      toRemove foreach {x => out.get.removeNeighbour(x)}
      toAdd foreach { x => out.get.outNeighbour(x)}
    }
    val deviceToOut = devChanged.deviceChanged()
    if(!deviceToOut.isEmpty) {
      deviceToOut map {x => x._1 -> {
        x._2 foreach { name => {
          out.get.outDevice(x._1,world(x._1).get.getDevice(name).get)
        }}
      }}
    }
    out.get.flush()
  }

  /**
    * add output to current presenter
    *
    * @param view the output where presenter put changes
    */
  override def output(view: SimulationView): Unit = out = Some(view)
}
