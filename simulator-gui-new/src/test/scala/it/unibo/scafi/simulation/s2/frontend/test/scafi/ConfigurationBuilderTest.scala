package it.unibo.scafi.simulation.s2.frontend.test.scafi

import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiConfiguration
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.AggregateProgram
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class BasicProgram extends AggregateProgram {
  override def main() = rep(0)(_ + 1) // the aggregate program to run
}
//noinspection NameBooleanParameters,NameBooleanParameters
class ConfigurationBuilderTest extends AnyFunSpec with Matchers{


  val checkThat = new ItWord
  val worldInitializer = Random(100,10,10)
  val simulationInitializer = RadiusSimulation(10)
  val simulationSeed = SimulationInfo(program = classOf[BasicProgram])
  checkThat("an empty scafi builder can't create a configuration") {
    val builder = new ScafiConfigurationBuilder
    assert(builder.create().isEmpty)
    assert(!builder.created)
  }
  checkThat("pass right argument i can create a scafi configuration") {
    val builder = new ScafiConfigurationBuilder()
    val withNamedArgument = new ScafiConfigurationBuilder(
      worldInitializer = Some(worldInitializer),
      simulationInitializer = Some(simulationInitializer),
      scafiSimulationInformation = Some(simulationSeed)
    )
    withNamedArgument.create() match {
      case Some(configuration : ScafiConfiguration) => assert(configuration.worldInitializer == worldInitializer &&
        configuration.simulationInitializer == simulationInitializer &&
        configuration.scafiSimulationInformation == simulationSeed)

      case _ => assert(false)
    }
    assert(withNamedArgument.created)
    builder.worldInitializer = Some(worldInitializer)
    builder.simulationInitializer = Some(simulationInitializer)
    builder.scafiSimulationInformation = Some(simulationSeed)

    builder.create() match {

      case Some(configuration : ScafiConfiguration) => assert(configuration.worldInitializer == worldInitializer &&
          configuration.simulationInitializer == simulationInitializer &&
          configuration.scafiSimulationInformation == simulationSeed)

      case _ => assert(false)
    }
    assert(builder.created)
  }
}
