/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend

import it.unibo.scafi.incarnations.BasicAbstractIncarnation
import it.unibo.scafi.simulation.frontend.model.{Network, Node}

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
