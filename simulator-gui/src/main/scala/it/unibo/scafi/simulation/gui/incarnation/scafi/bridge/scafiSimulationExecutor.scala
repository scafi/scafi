package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.logger.LogManager.{Channel, IntLog, TreeLog}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.{NodeDeviceChanged, NodesMoved}
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.NodesAdded
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.sensorInput
import it.unibo.scafi.simulation.gui.util.Sync
import it.unibo.scafi.space.Point3D

/**
  * scafi bridge implementation, this object execute each tick scafi logic
  */
object scafiSimulationExecutor extends ScafiBridge {
  import ScafiBridge._
  //observer used to verify world changes
  private val checkMoved = world.createObserver(Set(NodesMoved))
  private val checkChanged = world.createObserver(Set(NodeDeviceChanged))
  private val checkAdded = world.createObserver(Set(NodesAdded))
  private var exportProduced : Map[ID,world.ID => Unit] = Map()
  //variable used to block asyncLogicExecution
  private val sync = Sync.apply
  override protected val maxDelta: Option[Int] = None
  override protected def AsyncLogicExecution(): Unit = {
    //if block is true, async logic execution do nothing
    if(sync.blocked) return;

    if(contract.simulation.isDefined) {
      val net = contract.simulation.get
      val result = net.exec(runningContext)
      //verify it there are some id observed to put export
      if(idsObserved.contains(result._1)) {
        //get the path associated to the node
        val mapped = result._2.paths.toSeq.map {x => {
          if(x._1.isRoot) {
            (None,x._1,x._2)
          } else {
            (Some(x._1.pull()),x._1,x._2)
          }
        }}.sortWith((x,y) => x._2.level < y._2.level)
        LogManager.notify(TreeLog[Path](Channel.Export,result._1.toString,mapped))
      }
      //an the actuator associated to this simulation
      val actuator = this.simulationSeed.get.actuator
      if(actuator.isDefinedAt(result._2)) {
        exportProduced += result._1 -> actuator(result._2)
      }
    }
  }

  override def onTick(float: Float): Unit = {
    //get the modification of world
    val moved = checkMoved.nodeChanged()
    val devs = checkChanged.nodeChanged()
    val added = checkAdded.nodeChanged()
    if(contract.simulation.isDefined) {
      val bridge = contract.simulation.get
      //change the value of sensor in scafi simulation
      devs map {world(_).get} foreach {x => x.devices.filter{y => y.stream == sensorInput} foreach(y => {bridge.chgSensorValue(y.name,Set(x.id),y.value)})}
      moved foreach { x =>
        //update the state of node moved
        val node = world(x).get
        //update the neighbours
        val oldNeigh = contract.simulation.get.neighbourhood(x)
        contract.simulation.get.setPosition((x), Point3D(node.position.x, node.position.y, node.position.z))
        val neigh = contract.simulation.get.neighbourhood(x)
        world.network.setNeighbours(x,neigh)
        (oldNeigh ++ neigh) foreach {x => {world.network.setNeighbours(x,contract.simulation.get.neighbourhood(x))}}
      }
    }
    var toCompute : Map[ID,world.ID => Unit] = Map.empty

    //mutual access to map
    sync {
      //actuate the changes in the world
      toCompute = exportProduced
      exportProduced = Map.empty
    }
    val toComputeMap = toCompute
    toComputeMap foreach { x => x._2(x._1)}
  }
}


