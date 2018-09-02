package it.unibo.scafi.simulation.gui.demo

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, _}
import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulationInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInformation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutputPolicy
object Test extends App {
  ScafiProgramBuilder (
    worldInitializer = Random(1000,1920,1080),
    scafiSimulationInfo = ScafiSimulationInformation(program = classOf[Simple]),
    simulationInitializer = RadiusSimulationInitializer( radius = 80),
    outputPolicy = StandardFXOutputPolicy,
    neighbourRender = true,
    perfomance = NearRealTimePolicy
  ).launch()
  //FileLauncher{"C:\\Users\\paggi\\Desktop\\init.txt"}
  /*StringLauncher {
    "grid-world " +
      "10 100 100;radius-simulation Simple 15"
  }*/
}
@Demo
class Simple extends AggregateProgram  with BlockG with StandardSensors {
  self: AggregateProgram =>


  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  import it.unibo.scafi.simulation.gui.configuration.SensorName._
  override def main() = branch(sense[Boolean](sensor3)) {false} {channel(sense[Boolean](sensor1), sense[Boolean](sensor2), 1)}
}