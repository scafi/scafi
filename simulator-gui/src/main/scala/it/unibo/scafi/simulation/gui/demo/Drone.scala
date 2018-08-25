package it.unibo.scafi.simulation.gui.demo
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, _}
import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment.{FastPerformancePolicy, NearRealTimePolicy, StandardPolicy}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulationInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.{Demo, SimulationType}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.{Actions, ScafiSimulationSeed}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.{ScafiProgramBuilder, ScafiSeed}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command.ScafiParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.{Grid, Random}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Circle
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutputPolicy

object Drone extends App {
  ScafiProgramBuilder (
    worldInitializer = Random(500,500,500),
    scafiSimulationSeed = ScafiSimulationSeed(program = classOf[BasicMovement],action = Actions.movementAction),
    simulationInitializer = RadiusSimulationInitializer( radius = 40),
    scafiSeed = ScafiSeed(shape = Some(Circle(4))),
    outputPolicy = StandardFXOutputPolicy,
    neighbourRender = true,
    perfomance = NearRealTimePolicy
  ).launch()
}
@Demo(simulationType = SimulationType.MOVEMENT)
class BasicMovement extends AggregateProgram with SensorDefinitions with FlockingLib with BlockG with Movement2DSupport {
  override def main:(Double, Double) = randomMovement()
}