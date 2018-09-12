package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.configuration.command.factory.WindowConfigurationCommandFactory
import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
import it.unibo.scafi.simulation.gui.configuration.logger.LogConfiguration._
import it.unibo.scafi.simulation.gui.configuration.parser.{ConfigurationMachine, UnixLikeParser}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiWindowInfo
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command._
import it.unibo.scafi.simulation.gui.view.WindowConfiguration
import it.unibo.scafi.simulation.gui.view.scalaFX._
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FastFXOutput, ImageFXOutput, StandardFXOutput}
import it.unibo.scafi.simulation.gui.view.scalaFX.launcher.ScalaFXLauncher

import scalafx.application.Platform

/**
  * a graphics launcher used to launch scafi simulation
  */
object GraphicsLauncher extends App {
  implicit val scafiConfiguration : ScafiConfigurationBuilder = new ScafiConfigurationBuilder
  implicit val window : WindowConfiguration = ScafiWindowInfo(ScalaFXEnvironment.windowConfiguration)

  val randomFactory = new RandomWorldCommandFactory
  val gridFactory = new GridWorldCommandFactory
  val radiusFactory = new RadiusSimulationCommandFactory
  val outputFactory = new OutputCommandFactory(FastFXOutput,StandardFXOutput,ImageFXOutput)
  val performanceFactory = new PerformanceCommandFactory
  val windowConfigurationFactory = new WindowConfigurationCommandFactory(ScalaFXEnvironment)
  val logConfiguration = new LogCommandFactory(NoLog,StandardLog,GraphicsLog)
  val renderConfiguration = new RenderCommandFactory
  val factories = List(radiusFactory,gridFactory,randomFactory,
    outputFactory,performanceFactory,windowConfigurationFactory,
    logConfiguration,renderConfiguration)

  val parser = new UnixLikeParser(new LaunchCommandFactory :: factories:_*)
  val machine = new ConfigurationMachine(parser)
  val map = Map(international("world-initializer")(KeyFile.Configuration) -> List(randomFactory,gridFactory))
  initializeScalaFXPlatform()
  Platform.runLater{new ScalaFXLauncher(factories,map,machine).show()}
}
