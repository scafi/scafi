package it.unibo.scafi.simulation.gui.model

import java.awt.geom.Point2D

/**
  * Created by Varini on 14/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
trait Node {
  def id: Int

  var export: Any

  var position: Point2D

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