package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.Configuration
import it.unibo.scafi.simulation.gui.configuration.Configuration.ConfigurationBuilder
import it.unibo.scafi.simulation.gui.configuration.command.CommandMapping
import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment.{NearRealTimePolicy, PerformancePolicy}
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiCommandMapping.standardMapping
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.{ScafiSimulationInitializer, ScafiSimulationSeed}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer
import it.unibo.scafi.simulation.gui.view.OutputPolicy
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutputPolicy

/**
  * the implementation of configuration in scafi context
  * @param scafiSeed a seed used to describe general information used to create a scafi program
  * @param worldInitializer a strategy used to initialize the world
  * @param commandMapping a command mapping used to map keyboard value to some execution logic
  * @param scafiSimulationSeed a seed used to initialize simulation
  * @param simulationInitializer a strategy used to initialize the simulation
  * @param outputPolicy a policy that choose how to output node information
  * @param neighbourRender render or not neighbour
  * @param perfomance the scafi perfomance program
  */
case class ScafiConfiguration (scafiSeed : ScafiSeed,
                          worldInitializer: ScafiWorldInitializer,
                          commandMapping: CommandMapping,
                          scafiSimulationSeed : ScafiSimulationSeed,
                          simulationInitializer: ScafiSimulationInitializer,
                          outputPolicy: OutputPolicy,
                          neighbourRender: Boolean = true,
                          perfomance: PerformancePolicy) extends Configuration

object ScafiConfiguration {

  /**
    * scafi configuration builder, has the same configuration value,
    * are optional because some of these can be empty or not,
    * to argument description {@see ScafiConfiguration}
    * @param scafiSeed (standard => ScafiSeed.standard)
    * @param worldInitializer (no standard value)
    * @param commandMapping (standard => standardMapping)
    * @param scafiSimulationSeed (no standard value
    * @param simulationInitializer (no standard value)
    * @param outputPolicy (standard => StandardFXOutputPolicy)
    * @param neighbourRender (standard => true)
    * @param perfomance (standard => NearRealTimePolicy)
    */
  class ScafiConfigurationBuilder(var scafiSeed : Option[ScafiSeed] = Some(ScafiSeed.standard),
                             var worldInitializer: Option[ScafiWorldInitializer] = None,
                             var commandMapping: Option[CommandMapping] = Some(standardMapping),
                             var scafiSimulationSeed : Option[ScafiSimulationSeed] = None,
                             var simulationInitializer: Option[ScafiSimulationInitializer] = None,
                             var outputPolicy: Option[OutputPolicy] = Some(StandardFXOutputPolicy) ,
                             var neighbourRender: Boolean = true,
                             var perfomance: Option[PerformancePolicy] = Some(NearRealTimePolicy)) extends ConfigurationBuilder[ScafiConfiguration] {
    private var _created = false
    def create() : Option[ScafiConfiguration] = {
      if(created) return None
      if(scafiSeed.isEmpty || worldInitializer.isEmpty || commandMapping.isEmpty || simulationInitializer.isEmpty
      || outputPolicy.isEmpty || perfomance.isEmpty) {
        None
      } else {
        _created = true
        Some(ScafiConfiguration(scafiSeed.get,worldInitializer.get,commandMapping.get,scafiSimulationSeed.get,
          simulationInitializer.get,outputPolicy.get,neighbourRender,perfomance.get))
      }
    }

    override def created: Boolean = _created
  }
}
