package it.unibo.scafi.simulation.gui.controller.presenter

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.NodesMoved
import it.unibo.scafi.simulation.gui.model.common.network.ConnectedWorld.NeighbourChanged
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.{NodesAdded, NodesRemoved}
import it.unibo.scafi.simulation.gui.model.sensor.SensorEvent.SensorChanged
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.view.SimulationView

/**
  * describe a simulation preseneter
  * @param world to show
  * @param neighbourRender a boolean value to define if show the neighbours or not
  */
class SimulationPresenter[W <: SensorPlatform](val world : W,
                                               val neighbourRender : Boolean) extends Presenter[W,SimulationView]{

  //observer used to check world chages
  private val removed = world.createObserver(Set(NodesRemoved))
  private val moved = world.createObserver(Set(NodesMoved,NodesAdded))
  private val devChanged = world.createObserver(Set(SensorChanged))
  private val networkChanged = world.createObserver(Set(NeighbourChanged))
  private var out : Option[SimulationView] = None
  //used to render neigbhbour correctly
  private var prevNeighbour : Map[world.ID,Set[world.ID]] = Map()
  //RENDER COMPONENT DECIDE WHAT RENDER TO OUTPUT AND HOW
  override def onTick(float: Float): Unit = {
    if(out.isEmpty) {
      return
    }
    val nodesRemoved = removed.nodeChanged()
    if (!nodesRemoved.isEmpty) {
      nodesRemoved foreach { out.get.removeNode(_)}
    }
    //set of node moved
    val nodesMoved = moved.nodeChanged()
    //used to remove or add neighbour
    var toAdd : Map[world.ID,Set[world.ID]] = Map()
    var toRemove : Map[world.ID,Set[world.ID]] = Map()

    val neighbourChanged = networkChanged.nodeChanged()
    neighbourChanged foreach ( x => {
      //take old neighbour
      val oldIds = this.prevNeighbour.get(x)
      //take new neighbour
      val newIds = world.network.neighbours(x)
      val id: world.ID = world(x).get.id
      //ids to remove
      val ids: Set[world.ID] = oldIds.getOrElse(Set()) -- newIds
      //neighbour to add
      val add = newIds -- oldIds.getOrElse(Set())
      if (ids.nonEmpty) {
        toRemove += id -> ids
      }
      if (add.nonEmpty) {
        toAdd += id -> add
      }
    })
    //change the neigbour map with new neigbour map
    this.prevNeighbour = world.network.neighbours()
    //put the node moved in out
    nodesMoved foreach {x => out.get.outNode(world(x).get)}
    //if neighbour render il enable, show the neighbour
    if(neighbourRender) {
      toRemove foreach {x => out.get.removeNeighbour(x)}
      toAdd foreach { x => out.get.outNeighbour(x)}
    }
    val deviceToOut = devChanged.deviceChanged()
    //show the device changes in the view
    if(!deviceToOut.isEmpty) {
      deviceToOut map {x => x._1 -> {
        x._2 foreach { name => {
          out.get.outDevice(x._1,world(x._1).get.getDevice(name).get)
        }}
      }}
    }
    //flush the changes
    out.get.flush()
  }

  /**
    * add output to current presenter
    *
    * @param view the output where presenter put changes
    */
  override def output(view: SimulationView): Unit = out = Some(view)
}
