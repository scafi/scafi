package it.unibo.scafi.simulation.gui.model

case class Sensor(name: String, value: Any) {
  override def hashCode = name.##
  override def equals(other: Any) = other match {
    case x: Sensor => this.name == x.name
    case _ => false
  }
}
