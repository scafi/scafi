package it.unibo.scafi.simulation.gui.launcher.scalaFX


import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
object Test extends App {
  import Launcher._
  Launcher.program = classOf[Simple]
  Launcher.nodes = 1000
  Launcher.maxPoint = 1000
  Launcher.radius = 70
  Launcher.launch()
}
class Simple extends AggregateProgram  with BlockG with StandardSensors {
  self: AggregateProgram =>


  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width
  import WorldConfig._
  override def main() = branch(sense[Boolean](obstacle.name)) {false} {channel(sense[Boolean](source.name), sense[Boolean](destination.name), 1)}
}