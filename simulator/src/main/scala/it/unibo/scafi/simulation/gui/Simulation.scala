package it.unibo.scafi.simulation.gui

import it.unibo.scafi.simulation.gui.model.{Network, Node}

import BasicSpatialIncarnation._

/**
  * @author Roberto Casadei
  *
  */

trait Simulation {
  def getNetwork: Network

  def getRunProgram: ()=>(Int,Export)

  def setNetwork(network: Network)

  def setRunProgram(program: Any)

  def setDeltaRound(deltaRound: Double)

  def setStrategy(strategy: Any)

  def setSensor(sensor: String, ids: java.util.Set[Node], value: AnyRef)

  def setPosition(n: Node)
}
