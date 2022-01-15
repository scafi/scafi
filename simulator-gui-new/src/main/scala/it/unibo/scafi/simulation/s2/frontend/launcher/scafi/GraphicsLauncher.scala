package it.unibo.scafi.simulation.s2.frontend.launcher.scafi

import it.unibo.scafi.simulation.s2.frontend.configuration.command.factory.WindowConfigurationCommandFactory
import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
import it.unibo.scafi.simulation.s2.frontend.configuration.logger.LogConfiguration._
import it.unibo.scafi.simulation.s2.frontend.configuration.parser.{ConfigurationMachine, UnixLikeParser}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.{ScafiInformation, ScafiWindowInfo}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command._
import it.unibo.scafi.simulation.s2.frontend.view.WindowConfiguration
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX._
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.{FastFXOutput, ImageFXOutput, StandardFXOutput}
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.launcher.ScalaFXLauncher

import scalafx.application.Platform
import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory

/**
  * a graphics launcher used to launch scafi simulation
  */
object GraphicsLauncher extends App {
  implicit val scafiConfiguration : ScafiConfigurationBuilder = ScafiInformation.configurationBuilder
  implicit val window : WindowConfiguration = ScafiWindowInfo(ScalaFXEnvironment.windowConfiguration)

  val randomFactory = new RandomWorldCommandFactory
  val gridFactory = new GridWorldCommandFactory
  val radiusFactory = new RadiusSimulationCommandFactory
  val outputFactory = new OutputCommandFactory(FastFXOutput,StandardFXOutput,ImageFXOutput)
  val performanceFactory = new PerformanceCommandFactory
  val windowConfigurationFactory = new WindowConfigurationCommandFactory
  val logConfiguration = new LogCommandFactory(NoLog,StandardLog,GraphicsLog)
  val renderConfiguration = new RenderCommandFactory
  val factories: List[CommandFactory] = List(radiusFactory,gridFactory,randomFactory,
    outputFactory,performanceFactory,windowConfigurationFactory,
    logConfiguration,renderConfiguration)

  val parser = new UnixLikeParser(new LaunchCommandFactory :: factories:_*)
  val machine = new ConfigurationMachine(parser)
  val map: Map[String,List[CommandFactory]] = Map(international("world-initializer")(KeyFile.Configuration) -> List(randomFactory,gridFactory))
  initializeScalaFXPlatform()
  Platform.runLater{new ScalaFXLauncher(factories,map,machine).show()}
}
