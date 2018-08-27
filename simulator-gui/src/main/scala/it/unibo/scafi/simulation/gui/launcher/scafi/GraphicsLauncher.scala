package it.unibo.scafi.simulation.gui.launcher.scafi

import java.util.Locale

import com.sun.xml.internal.bind.api.impl.NameConverter.Standard
import it.unibo.scafi.simulation.gui.configuration.command.factory.WindowConfigurationCommandFactory
import it.unibo.scafi.simulation.gui.configuration.parser.{ConfigurationMachine, UnixLikeParser}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command._
import it.unibo.scafi.simulation.gui.view.WindowConfiguration
import it.unibo.scafi.simulation.gui.view.scalaFX._
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FastFXOutputPolicy, StandardFXOutputPolicy}
import it.unibo.scafi.simulation.gui.view.scalaFX.launcher.ScalaFXLauncher
import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiWindowInfo

import scalafx.application.Platform

/**
  * a graphics launcher used to launch scafi simulation
  */
object GraphicsLauncher extends App {
  import ScafiWindowInfo._
  implicit val scafiConfiguration = new ScafiConfigurationBuilder
  implicit val window = ScafiWindowInfo(ScalaFXEnvironment.windowConfiguration)

  locale = Locale.ITALY
  val randomFactory = new RandomWorldCommandFactory
  val gridFactory = new GridWorldCommandFactory
  val radiusFactory = new RadiusSimulationCommandFactory
  val outputFactory = new OutputCommandFactory(FastFXOutputPolicy,StandardFXOutputPolicy)
  val performanceFactory = new PerformanceCommandFactory
  val windowConfigurationFactory = new WindowConfigurationCommandFactory(ScalaFXEnvironment)
  val factories = List(radiusFactory,gridFactory,randomFactory,outputFactory,performanceFactory,windowConfigurationFactory)

  val parser = new UnixLikeParser(new LaunchCommandFactory :: factories:_*)
  val machine = new ConfigurationMachine(parser)
  val map = Map(international("world-initializer")(KeyFile.Configuration) -> List(randomFactory,gridFactory))
  initializeScalaFXPlatform
  Platform.runLater{new ScalaFXLauncher(factories,map,machine).show()}
}
