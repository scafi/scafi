package it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge

import it.unibo.scafi.simulation.frontend.controller.logger.LogManager
import it.unibo.scafi.simulation.frontend.controller.logger.LogManager.{Channel, TreeLog}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._

/**
  * scafi bridge implementation, this object execute each tick scafi logic
  */
object scafiSimulationExecutor extends ScafiBridge {
  import ScafiBridge._
  //observer used to verify world changes
  private var exportProduced : Map[ID,EXPORT] = Map.empty
  private val indexToName = (i : Int) => "output"+(i+1)
  override protected val maxDelta: Option[Int] = None

  override protected def asyncLogicExecution(): Unit = {
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
      //an the meta actions associated to this simulation
      val metaActions = this.simulationInfo.get.metaActions
      metaActions.filter(x => x.valueParser(result._2.root()).isDefined).foreach(x => net.add(x(result._1,result._2)))
      net.process()
    }
  }
  override def onTick(float: Float): Unit = {()
    //get the modification of simulation logic world
    val simulationMoved = simulationObserver.idMoved
    if(contract.simulation.isDefined) {
      val bridge = contract.simulation.get
      //for each export produced the bridge valutate export value and produced output associated
      val exportValutations = simulationInfo.get.exportValutations
      if(exportValutations.nonEmpty) {
        var exportToUpdate = Map.empty[ID,EXPORT]
        exportToUpdate = exportProduced
        exportProduced = Map.empty
        //for each export the bridge valutate it and put value in the sensor associated
        for(export <- exportToUpdate) {
          for(i <- exportValutations.indices) {
            world.changeSensorValue(export._1,indexToName(i),exportValutations(i)(export._2))
          }
        }
      }
      //used update the gui world network
      var idsNetworkUpdate = Set.empty[Int]
      //check the node move by simulation logic
      simulationMoved foreach {id =>
        val p = contract.simulation.get.space.getLocation(id)
        //verify if the position is realy changed (the moved can be produced by gui itself)
        world.moveNode(id,p)
        //the id to update in gui network
        idsNetworkUpdate ++= world.network.neighbours(id)
        idsNetworkUpdate ++= contract.simulation.get.neighbourhood(id)
        idsNetworkUpdate += id
      }
      //update the neighbourhood foreach node
      idsNetworkUpdate foreach {x => {world.network.setNeighbours(x,contract.simulation.get.neighbourhood(x))}}
      //change the value of sensor in model world

      val simulationSensor = simulationObserver.idSensorChanged
      simulationSensor.foreach(nodeChanged => nodeChanged._2.foreach(name => world.changeSensorValue(nodeChanged._1,name,bridge.localSensor(name)(nodeChanged._1))))

    }
  }
}
