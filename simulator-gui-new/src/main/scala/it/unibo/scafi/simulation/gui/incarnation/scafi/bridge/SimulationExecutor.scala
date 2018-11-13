/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._

trait SimulationExecutor extends ScafiBridge {
  protected var exportProduced: Map[ID,EXPORT] = Map()
  override protected val maxDelta: Option[Int] = None
  private val indexToName = (i: Int) => "output" + (i + 1)

  SimulationExecutor.Instance = Some(this)

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

object SimulationExecutor {
  var Instance: Option[SimulationExecutor] = None
}
