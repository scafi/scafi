package it.unibo.scafi.simulation.gui

import it.unibo.scafi.incarnations.BasicAbstractIncarnation
import it.unibo.scafi.simulation.gui.model.{Network, Node}

/**
  * @author Roberto Casadei
  *
  */

trait Simulation {
  var network: Network

  def getRunProgram: ()=>(Int,BasicAbstractIncarnation#Export)

  def setRunProgram(program: Any)

  def setDeltaRound(deltaRound: Double)

  def getDeltaRound(): Double

  def setStrategy(strategy: Any)

  def setSensor(sensor: String, value: Any, nodes: Set[Node] = Set()): Unit

  def getSensorValue(s: String): Option[Any]

  def setPosition(n: Node)
}
