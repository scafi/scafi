package it.unibo.scafi.simulation.gui.launcher.scafi

import com.sun.xml.internal.bind.api.impl.NameConverter.Standard
import it.unibo.scafi.simulation.gui.configuration.parser.{ConfigurationMachine, UnixLikeParser}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command._
import it.unibo.scafi.simulation.gui.view.WindowConfiguration
import it.unibo.scafi.simulation.gui.view.scalaFX._
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{FastFXOutputPolicy, StandardFXOutputPolicy}
import it.unibo.scafi.simulation.gui.view.scalaFX.launcher.ScalaFXLauncher

import scalafx.application.Platform

/**
  * a graphics launcher used to launch scafi simulation
  */
object GraphicsLauncher extends App {
  implicit val scafiConfiguration = new ScafiConfigurationBuilder
  implicit val window = new WindowConfiguration {override val name: String = "Scafi"
    override val width: Int = 800
    override val height: Int = 600
  }
  val randomFactory = new RandomWorldCommandFactory
  val gridFactory = new GridWorldCommandFactory
  val radiusFactory = new RadiusSimulationCommandFactory
  val outputFactory = new OutputCommandFactory(FastFXOutputPolicy,StandardFXOutputPolicy)
  val factories = List(radiusFactory,gridFactory,randomFactory,outputFactory)

  val parser = new UnixLikeParser(new LaunchCommandFactory :: factories:_*)
  val machine = new ConfigurationMachine(parser)
  val map = Map("world initializer" -> List(randomFactory.name,gridFactory.name))
  initializeScalaFXPlatform
  Platform.runLater{new ScalaFXLauncher(factories,map,machine).show()}
}
