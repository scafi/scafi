package it.unibo.scafi.simulation.gui.demo

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, _}
import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.gui.configuration.logger.LogConfiguration
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.{Grid, Random}
import it.unibo.scafi.simulation.gui.view.OutputPolicy.noOutput
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FastFXOutput, StandardFXOutput}
object Test extends App {
  ScafiProgramBuilder (
    Grid(space = 10,
           row = 200,
           column = 200),
    SimulationInfo(program = classOf[Simple]),
    RadiusSimulation(radius = 10),
    outputPolicy = noOutput
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