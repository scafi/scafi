package it.unibo.scafi.simulation.gui.demos

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, _}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulation.RadiusSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.gui.launcher.scafi.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.FastFXOutputPolicy
object Test extends App {
  ScafiProgramBuilder (
    worldInitializer = Random(10000,500,500),
    simulation = RadiusSimulation(program = classOf[Simple], radius = 10),
    outputPolicy = FastFXOutputPolicy,
    neighbourRender = true
  ).launch()
}
@Demo
class Simple extends AggregateProgram  with BlockG with StandardSensors {
  self: AggregateProgram =>


  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  import it.unibo.scafi.simulation.gui.configuration.SensorName._
  override def main() = branch(sense[Boolean](sens3.name)) {false} {channel(sense[Boolean](sens1.name), sense[Boolean](sens2.name), 1)}
}