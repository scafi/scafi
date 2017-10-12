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


import it.unibo.scafi.simulation.gui.model.{Action, Node, Sensor}
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
