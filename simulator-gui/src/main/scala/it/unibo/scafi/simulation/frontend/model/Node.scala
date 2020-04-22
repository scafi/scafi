package it.unibo.scafi.simulation.frontend.model

import it.unibo.scafi.space.{Point2D, Point3D}

trait Node {
  def id: Int

  var export: Any

  var position: Point3D

  def neighbours: Set[Node]

  def addNeighbour(neighbour: Node): Boolean

  def addAllNeighbours(neighbours: Set[Node]): Boolean

  def removeNeighbour(neighbour: Node): Boolean

  def removeAllNeghbours()

  def sensors: Map[Sensor, Any]

  def getSensorValue(sensorName: String): Any

  def setSensor(sensor: Sensor, value: Any)

  def setSensor(sensor: String, value: Any)
}
