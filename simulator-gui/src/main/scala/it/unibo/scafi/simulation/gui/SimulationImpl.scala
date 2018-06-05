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

package it.unibo.scafi.simulation.gui.model.implementation

import it.unibo.scafi.simulation.gui.Simulation
import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.{EuclideanDistanceNbr, Network, Node, Sensor}
import it.unibo.scafi.space.Point2D
import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation._

class SimulationImpl(val configurationSeed: Long = System.nanoTime(),
                     val simulationSeed: Long = System.nanoTime()) extends Simulation {
  //private Thread runProgram;  //should implements runnable
  private var net: SpaceAwareSimulator = null
  var network: Network = null
  var runProgram: Function0[(Int,Export)] = null
  var deltaRound: Double = .0
  var strategy: Any = null
  final private val controller: Controller = Controller.getInstance

  this.deltaRound = 0.00
  this.strategy = null

  def setRunProgram(program: Any): Unit = {

    val devsToPos: Map[Int, Point2D] = network.nodes.mapValues(n => new Point2D(n.position.x, n.position.y)) // Map id->position
    net = new SpaceAwareSimulator(
      space = new Basic3DSpace(devsToPos,
        proximityThreshold = this.network.neighbourhoodPolicy match {
          case EuclideanDistanceNbr(radius) => radius
        }),
        devs = devsToPos.map { case (d, p) => d -> new DevInfo(d, p,
          lsns => if (lsns == "sensor" && d == 3) 1 else 0,
          nsns => nbr => null)
        },
      simulationSeed = simulationSeed,
      randomSensorSeed = configurationSeed
    )

    network.setNeighbours(net.getAllNeighbours)

    SensorEnum.sensors.foreach(se => {
      // TODO: println(se);
      net.addSensor(se.name, se.value) }
    )

    val ap = Class.forName(program.toString).newInstance().asInstanceOf[CONTEXT=>EXPORT]
    this.runProgram = () => net.exec(ap)
  }

  def setDeltaRound(deltaRound: Double) {
    this.deltaRound = deltaRound
    this.controller.simManager.setPauseFire(deltaRound)
  }

  def getDeltaRound(): Double = this.deltaRound

  def setStrategy(strategy: Any) {
    this.strategy = strategy
  }

  def getRunProgram: ()=>(Int,Export) = this.runProgram

  private var sensors = Map[String,Any]()

  override def setSensor(sensor: String, value: Any, nodes: Set[Node] = Set()): Unit = {
    val idSet: Set[Int] = nodes.map(_.id)
    if(nodes.size==0 && !controller.selectionAttempted) {
      net.addSensor(sensor, value)
      sensors += sensor -> value
    } else {
      net.chgSensorValue(sensor, idSet, value)
    }
  }

  def getSensorValue(sensorName: String): Option[Any] = {
    net.getSensor(sensorName)
  }

  override def setPosition(n: Node): Unit = {
    net.setPosition(n.id, new Point2D(n.position.x, n.position.y))
    network.setNodeNeighbours(n.id, net.neighbourhood(n.id))
  }
}
