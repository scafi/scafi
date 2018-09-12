package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.Configuration
import it.unibo.scafi.simulation.gui.configuration.Configuration.ConfigurationBuilder
import it.unibo.scafi.simulation.gui.configuration.command.CommandBinding
import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment.{NearRealTimePolicy, PerformancePolicy}
import it.unibo.scafi.simulation.gui.configuration.logger.LogConfiguration
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiCommandBinding.StandardBinding
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.{ScafiSimulationInitializer, SimulationInfo}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer
import it.unibo.scafi.simulation.gui.view.OutputPolicy
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutput

/**
  * the implementation of configuration in scafi context
  * @param scafiWorldInfo information used to create a scafi program
  * @param worldInitializer a strategy used to initialize the world
  * @param commandMapping a command mapping used to map keyboard value to some execution logic
  * @param scafiSimulationInformation information used to initialize simulation
  * @param simulationInitializer a strategy used to initialize the simulation
  * @param outputPolicy a policy that choose how to output node information
  * @param neighbourRender render or not neighbour
  * @param performance the scafi perfomance program
  */
case class ScafiConfiguration (scafiWorldInfo : ScafiWorldInformation,
                               worldInitializer: ScafiWorldInitializer,
                               commandMapping: CommandBinding,
                               scafiSimulationInformation : SimulationInfo,
                               simulationInitializer: ScafiSimulationInitializer,
                               outputPolicy: OutputPolicy,
                               neighbourRender: Boolean = true,
                               logConfiguration : LogConfiguration,
                               performance: PerformancePolicy) extends Configuration

object ScafiConfiguration {

  /**
    * scafi configuration builder, has the same configuration value,
    * are optional because some of these can be empty or not,
    * to argument description {@see ScafiConfiguration}
    * @param scafiWorldInfo (standard => ScafiSeed.standard)
    * @param worldInitializer (no standard value)
    * @param commandMapping (standard => standardMapping)
    * @param scafiSimulationInformation (no standard value
    * @param simulationInitializer (no standard value)
    * @param outputPolicy (standard => StandardFXOutputPolicy)
    * @param neighbourRender (standard => true)
    * @param performance (standard => NearRealTimePolicy)
    */
  class ScafiConfigurationBuilder(var scafiWorldInfo : ScafiWorldInformation = ScafiWorldInformation.standard,
                                  var worldInitializer: Option[ScafiWorldInitializer] = None,
                                  var commandMapping: CommandBinding = StandardBinding,
                                  var scafiSimulationInformation : Option[SimulationInfo] = None,
                                  var simulationInitializer: Option[ScafiSimulationInitializer] = None,
                                  var outputPolicy: OutputPolicy = StandardFXOutput,
                                  var neighbourRender: Boolean = true,
                                  var logConfiguration: LogConfiguration = LogConfiguration.GraphicsLog,
                                  var performance: PerformancePolicy = NearRealTimePolicy) extends ConfigurationBuilder[ScafiConfiguration] {
    private var _created = false
    def create() : Option[ScafiConfiguration] = {
      if(created) return None
      if(worldInitializer.isEmpty || simulationInitializer.isEmpty) {
        None
      } else {
        _created = true
        Some(ScafiConfiguration(scafiWorldInfo,worldInitializer.get,commandMapping,scafiSimulationInformation.get,
          simulationInitializer.get,outputPolicy,neighbourRender,logConfiguration,performance))
      }
    }

    override def created: Boolean = _created
  }
}
