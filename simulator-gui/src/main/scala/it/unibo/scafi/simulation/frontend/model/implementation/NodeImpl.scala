/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.model.implementation

import it.unibo.scafi.simulation.frontend.model.{Node, Sensor}
import it.unibo.scafi.space.Point2D

import scala.util.Random

class NodeImpl(val id: Int, var position: Point2D) extends Node {
  var export: Any = ""
  var neighbours = Set[Node]()
  var sensors = Map[Sensor, Any](
    SensorEnum.TEMPERATURE -> 0,
    SensorEnum.SOURCE -> false,
    SensorEnum.OBSTACLE -> false,
    SensorEnum.SENS1 -> false,
    SensorEnum.SENS2 -> false,
    SensorEnum.SENS3 -> false,
    SensorEnum.SENS4 -> false
  )

  def this(id: Int) {
    this(id, new Point2D(new Random().nextDouble, new Random().nextDouble))
  }

  def addNeighbour(neighbour: Node): Boolean = {
    this.neighbours += neighbour
    true
  }

  def addAllNeighbours(nbrs: Set[Node]): Boolean = {
    this.neighbours = this.neighbours ++ nbrs
    true
  }

  def removeNeighbour(neighbour: Node): Boolean = {
    this.neighbours -= neighbour
    true
  }

  def removeAllNeghbours() =
    this.neighbours = Set()

  def getSensorValue(sensor: String): Any =
    this.sensors.find(_._1.name==sensor).map(_._2).getOrElse(null)

  def getSensorValue(sensor: Sensor): Any =
    this.sensors.get(sensor).get

  def sensorValues: Map[Sensor, Any] =
    this.sensors

  def setSensor(sensor: Sensor, value: Any) =
    this.sensors += sensor -> value

  def setSensor(sensorName: String, value: Any) =
    this.sensors += new Sensor(sensorName, value) -> value
}
