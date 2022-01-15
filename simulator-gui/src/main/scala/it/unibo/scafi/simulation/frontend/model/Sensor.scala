package it.unibo.scafi.simulation.frontend.model

case class Sensor(name: String, value: Any) {
  override def hashCode = name.##
  override def equals(other: Any): Boolean = other match {
    case x: Sensor => this.name == x.name
    case _ => false
  }
}
