package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.configuration.SensorName
import it.unibo.scafi.simulation.gui.controller.logger.LogManager
import it.unibo.scafi.simulation.gui.controller.logger.LogManager.{Channel, TreeLog}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateEvent.{NodeDeviceChanged, NodesMoved}
import it.unibo.scafi.simulation.gui.model.common.world.CommonWorldEvent.NodesAdded
import it.unibo.scafi.simulation.gui.model.sensor.SensorConcept.sensorInput
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.simulation.gui.util.Sync

/**
  * scafi bridge implementation, this object execute each tick scafi logic
  */
object scafiSimulationExecutor extends ScafiBridge {
  import ScafiBridge._
  //observer used to verify world changes
  private val checkMoved = world.createObserver(Set(NodesMoved))
  private val checkChanged = world.createObserver(Set(NodeDeviceChanged))
  private var actuations : Map[ID,world.ID => Unit] = Map()
  private var exportProduced : Map[ID,EXPORT] = Map.empty
  var oldNeigh : Map[ID,Iterable[ID]] = Map.empty
  //variable used to block asyncLogicExecution
  private val sync = Sync.apply
  override protected val maxDelta: Option[Int] = None
  override protected def AsyncLogicExecution(): Unit = {
    //if block is true, async logic execution do nothing
    if(contract.simulation.isDefined) {
      val net = contract.simulation.get
      val result = net.exec(runningContext)
      exportProduced += result._1 -> result._2
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
      val actuators = this.simulationInfo.get.actuators
      actuators.filter(x => x.valueParser(result._2.root()).isDefined).foreach(x => x(result._1,result._2,this.contract.simulation.get))
    }
  }

  override def onTick(float: Float): Unit = {
    //get the modification of world
    val moved = checkMoved.nodeChanged()
    val devs = checkChanged.nodeChanged()
    val simulationMoved : Set[Int] = simulationObserver.idMoved

    if(contract.simulation.isDefined) {
      val bridge = contract.simulation.get
      val exportValutations = simulationInfo.get.exportValutations
      val indexToName = (i : Int) => "output"+(i+1)
      if(exportValutations.nonEmpty) {
        var exportToUpdate = Map.empty[ID,EXPORT]
        sync {
          exportToUpdate = exportProduced
          exportProduced = Map.empty
        }
        val bridge = contract.simulation.get
        for(export <- exportToUpdate) {
          for(i <- exportValutations.indices) {
            world.changeSensorValue(export._1,indexToName(i),exportValutations(i)(export._2))
          }
        }
      }
      var idsModified = Set.empty[Int]
      (simulationMoved -- moved) foreach {id =>
        val newP = contract.simulation.get.space.getLocation(id)
        val modelPoint = Point3D(newP.x,newP.y,newP.z)
        idsModified ++= world.network.neighbours(id)
        idsModified ++= contract.simulation.get.neighbourhood(id)
        idsModified += id
        if(modelPoint != world(id).get.position) {
          world.moveNode(id,modelPoint)
        }
      }
      moved foreach { x =>
        //update the state of node moved
        val node = world(x).get
        idsModified ++= world.network.neighbours(x)
        Actuator.movementActuator(x, (node.position.x, node.position.y), bridge)
        //update the neighbours
        idsModified ++= contract.simulation.get.neighbourhood(node.id)
        idsModified += x
      }
      idsModified foreach {x => {world.network.setNeighbours(x,contract.simulation.get.neighbourhood(x))}}

      checkMoved.nodeChanged()
      //change the value of sensor in scafi simulation
      devs map {world(_).get} foreach {x => x.devices.filter{y => y.stream == sensorInput} foreach(y => {bridge.chgSensorValue(y.name,Set(x.id),y.value)})}

    }
  }
}


