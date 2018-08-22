package it.unibo.scafi.simulation.gui.demo

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, _}
import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment.FastPerformancePolicy
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulationInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationSeed
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.{Grid, Random}
import it.unibo.scafi.simulation.gui.launcher.scafi.StringLauncher
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutputPolicy
object Test extends App {
  ScafiProgramBuilder (
    worldInitializer = Grid(10,200,100),
    scafiSimulationSeed = ScafiSimulationSeed(program = classOf[Simple]),
    simulationInitializer = RadiusSimulationInitializer( radius = 10),
    outputPolicy = StandardFXOutputPolicy,
    neighbourRender = false,
    perfomance = FastPerformancePolicy
  ).launch()
  //FileLauncher{"C:\\Users\\paggi\\Desktop\\init.txt"}
  /*StringLauncher{"random-world " +
    "1000 400 400;radius-simulation Simple 40"}*/
}
@Demo
class Simple extends AggregateProgram  with BlockG with StandardSensors {
  self: AggregateProgram =>


  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  import it.unibo.scafi.simulation.gui.configuration.SensorName._
  override def main() = branch(sense[Boolean](sensor3)) {false} {channel(sense[Boolean](sensor1), sense[Boolean](sensor2), 1)}
}