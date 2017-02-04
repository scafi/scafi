package it.unibo.scafi.simulation.gui.model.implementation

import java.awt.geom.Point2D

import it.unibo.scafi.simulation.gui.model.{Action, Node, Sensor}

import scala.util.Random

/**
  * Created by Varini on 14/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
class NodeImpl(val id: Int, var position: Point2D) extends Node {
  var export: Any = ""
  var neighbours = Set[Node]()
  var sensors = Map[Sensor, Any](
    SensorEnum.TEMPERATURE -> 0,
    SensorEnum.SOURCE -> false,
    SensorEnum.OBSTACLE -> false
  )

  def this(id: Int) {
    this(id, new Point2D.Double(new Random().nextDouble, new Random().nextDouble))
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
    this.sensors.find(_._1.name==sensor).getOrElse(null)

  def getSensorValue(sensor: Sensor): Any =
    this.sensors.get(sensor).get

  def sensorValues: Map[Sensor, Any] =
    this.sensors

  def setSensor(sensor: Sensor, value: Any) =
    this.sensors += sensor -> value

  def setSensor(sensorName: String, value: Any) =
    this.sensors += sensors.find(_._1.name==sensorName).get._1 -> value
}