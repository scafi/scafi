/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend

import it.unibo.scafi.simulation.frontend.BasicSpatialIncarnation._
import it.unibo.scafi.simulation.frontend.controller.Controller
import it.unibo.scafi.simulation.frontend.model.implementation.SensorEnum
import it.unibo.scafi.simulation.frontend.model.{EuclideanDistanceNbr, Node}
import it.unibo.scafi.space.{Point2D, Point3D}

class SimulationImpl(val configurationSeed: Long = System.nanoTime(),
                     val simulationSeed: Long = System.nanoTime(),
                     simulatorManager: SimulationManager) extends Simulation {
  //private Thread runProgram;  //should implements runnable
  private var controller: Controller = null
  private var net: SpaceAwareSimulator = null
  var network: model.Network = null
  var runProgram: Function0[(Int,Export)] = null
  var deltaRound: Double = .0
  var strategy: Any = null

  this.deltaRound = 0.00
  this.strategy = null

  def setRunProgram(program: Any): Unit = {

    val devsToPos: Map[Int, Point3D] = network.nodes.mapValues(_.position) // Map id->position
    net = new SpaceAwareSimulator(
      space = new Basic3DSpace(devsToPos,
        proximityThreshold = this.network.neighbourhoodPolicy match {
          case EuclideanDistanceNbr(radius) => radius
        }),
        devs = devsToPos.map { case (d, p) => d -> new DevInfo(d, p,
          Map.empty,
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
    simulatorManager.setPauseFire(deltaRound)
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

  override def setController(controller: Controller): Unit =
    this.controller = controller
}

object SimulationImpl {
  def apply(simulatorManager: SimulationManager): SimulationImpl =
    new SimulationImpl(System.nanoTime(), System.nanoTime(), simulatorManager)
}
