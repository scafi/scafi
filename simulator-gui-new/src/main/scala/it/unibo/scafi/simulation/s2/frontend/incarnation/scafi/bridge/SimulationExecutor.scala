/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
 */

package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge

import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._

import java.util.concurrent.atomic.AtomicReference

trait SimulationExecutor extends ScafiBridge {
  protected val exportProduced = new AtomicReference[Map[ID, EXPORT]](Map.empty)
  override protected val maxDelta: Option[Int] = None
  private val indexToName = (i: Int) => "output" + (i + 1)
  override def onTick(float: Float): Unit = {
    // get the modification of simulation logic world
    val simulationMoved = simulationObserver.idMoved
    if (contract.simulation.isDefined) {
      val bridge = contract.simulation.get
      // for each export produced the bridge evaluates export value and produced output associated
      val exportEvaluations = simulationInfo.get.exportEvaluations
      if (exportEvaluations.nonEmpty) {
        var exportToUpdate = Map.empty[ID, EXPORT]
        exportToUpdate = exportProduced.getAndSet(Map.empty)
        // for each export the bridge evaluates it and put value in the sensor associated
        for (export <- exportToUpdate)
          for (i <- exportEvaluations.indices)
            world.changeSensorValue(export._1, indexToName(i), exportEvaluations(i)(export._2))
      }
      // used update the gui world network
      var idsNetworkUpdate = Set.empty[Int]
      // check the node move by simulation logic
      simulationMoved foreach { id =>
        val p = contract.simulation.get.space.getLocation(id)
        // verify if the position is really changed (the moved can be produced by gui itself)
        world.moveNode(id, p)
        // the id to update in gui network
        idsNetworkUpdate ++= world.network.neighbours(id)
        idsNetworkUpdate ++= contract.simulation.get.neighbourhood(id)
        idsNetworkUpdate += id
      }
      // update the neighbourhood foreach node
      idsNetworkUpdate foreach { x => world.network.setNeighbours(x, contract.simulation.get.neighbourhood(x)) }
      // change the value of sensor in model world

      val simulationSensor = simulationObserver.idSensorChanged
      simulationSensor.foreach(nodeChanged =>
        nodeChanged._2.foreach(name =>
          world.changeSensorValue(nodeChanged._1, name, bridge.localSensor(name)(nodeChanged._1))
        )
      )
    }
  }
}
