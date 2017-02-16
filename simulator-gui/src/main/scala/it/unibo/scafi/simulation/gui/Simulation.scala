package it.unibo.scafi.simulation.gui

import it.unibo.scafi.simulation.gui.model.{Network, Node}

import BasicSpatialIncarnation._

/**
  * @author Roberto Casadei
  *
  */

trait Simulation {
  var network: Network

  def getRunProgram: ()=>(Int,Export)

  def setRunProgram(program: Any)

  def setDeltaRound(deltaRound: Double)

  def getDeltaRound(): Double

  def setStrategy(strategy: Any)

  def setSensor(sensor: String, value: Any, nodes: Set[Node] = Set()): Unit

  def getSensorValue(s: String): Option[Any]

  def setPosition(n: Node)
}
