package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.Configuration
import it.unibo.scafi.simulation.gui.configuration.Configuration.ConfigurationBuilder
import it.unibo.scafi.simulation.gui.configuration.command.CommandBinding
import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment.{NearRealTimePolicy, PerformancePolicy}
import it.unibo.scafi.simulation.gui.configuration.logger.LogConfiguration
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiCommandBinding.standardBinding
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiProgramEnvironment
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.{ScafiSimulationInitializer, ScafiSimulationInformation}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer
import it.unibo.scafi.simulation.gui.view.OutputPolicy
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutputPolicy

/**
  * the implementation of configuration in scafi context
  * @param scafiWorldInfo a seed used to describe general information used to create a scafi program
  * @param worldInitializer a strategy used to initialize the world
  * @param commandMapping a command mapping used to map keyboard value to some execution logic
  * @param scafiSimulationSeed a seed used to initialize simulation
  * @param simulationInitializer a strategy used to initialize the simulation
  * @param outputPolicy a policy that choose how to output node information
  * @param neighbourRender render or not neighbour
  * @param perfomance the scafi perfomance program
  */
case class ScafiConfiguration (scafiWorldInfo : ScafiWorldInformation,
                               worldInitializer: ScafiWorldInitializer,
                               commandMapping: CommandBinding,
                               scafiSimulationSeed : ScafiSimulationInformation,
                               simulationInitializer: ScafiSimulationInitializer,
                               outputPolicy: OutputPolicy,
                               neighbourRender: Boolean = true,
                               logConfiguration : LogConfiguration,
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
  class ScafiConfigurationBuilder(var scafiSeed : ScafiWorldInformation = ScafiWorldInformation.standard,
                                  var worldInitializer: Option[ScafiWorldInitializer] = None,
                                  var commandMapping: CommandBinding = standardBinding,
                                  var scafiSimulationSeed : Option[ScafiSimulationInformation] = None,
                                  var simulationInitializer: Option[ScafiSimulationInitializer] = None,
                                  var outputPolicy: OutputPolicy = StandardFXOutputPolicy,
                                  var neighbourRender: Boolean = true,
                                  var logConfiguration: LogConfiguration = ScafiProgramEnvironment.scafiStandardLog,
                                  var perfomance: PerformancePolicy = NearRealTimePolicy) extends ConfigurationBuilder[ScafiConfiguration] {
    private var _created = false
    def create() : Option[ScafiConfiguration] = {
      if(created) return None
      if(worldInitializer.isEmpty || simulationInitializer.isEmpty) {
        None
      } else {
        _created = true
        Some(ScafiConfiguration(scafiSeed,worldInitializer.get,commandMapping,scafiSimulationSeed.get,
          simulationInitializer.get,outputPolicy,neighbourRender,logConfiguration,perfomance))
      }
    }

    override def created: Boolean = _created
  }
}
