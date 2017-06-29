package it.unibo.scafi.simulation.gui.model

/**
  * Created by Varini on 14/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
case class Sensor(name: String, value: Any) {
  override def hashCode = name.##
  override def equals(other: Any) = other match {
    case x: Sensor => this.name == x.name
    case _ => false
  }
}