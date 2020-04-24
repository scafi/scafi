/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.model

/**
  * Kinds of node values to be shown.
  */

sealed trait  NodeValue

object NodeValue {

  case object ID extends NodeValue

  case object EXPORT extends NodeValue

  case object POSITION extends NodeValue

  case object NONE extends NodeValue

  case class SENSOR(name: String) extends NodeValue

  case object POSITION_IN_GUI extends NodeValue

}
