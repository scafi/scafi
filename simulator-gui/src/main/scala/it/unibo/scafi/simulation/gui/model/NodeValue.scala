package it.unibo.scafi.simulation.gui.model

/**
  * @author Roberto Casadei
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