package it.unibo.scafi.simulation.gui.test.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulationInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationSeed
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import org.scalatest.{FunSpec, Matchers}

class ConfigurationBuilderTest extends FunSpec with Matchers{
  val checkThat = new ItWord
  val worldInitializer = new Random(100,10,10)
  val simulationInitializer = new RadiusSimulationInitializer(10)
  val simulationSeed = ScafiSimulationSeed(program = classOf[ConfigurationBuilderTest])
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
      scafiSimulationSeed = Some(simulationSeed)
    )
    withNamedArgument.create() match {
      case Some(configuration : ScafiConfiguration) => {
        assert(configuration.worldInitializer == worldInitializer &&
        configuration.simulationInitializer == simulationInitializer &&
        configuration.scafiSimulationSeed == simulationSeed)
      }
      case _ => assert(false)
    }
    assert(withNamedArgument.created)
    builder.worldInitializer = Some(worldInitializer)
    builder.simulationInitializer = Some(simulationInitializer)
    builder.scafiSimulationSeed = Some(simulationSeed)

    builder.create() match {

      case Some(configuration : ScafiConfiguration)  => {
        assert(configuration.worldInitializer == worldInitializer &&
          configuration.simulationInitializer == simulationInitializer &&
          configuration.scafiSimulationSeed == simulationSeed)
      }
      case _ => assert(false)
    }
    assert(builder.created)
  }
}
