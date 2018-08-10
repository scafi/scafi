package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.configuration.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.controller.input.Command.{CommandDescription, Fail, Success}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulationInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationSeed
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiSeed
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.gui.launcher.ConsoleConfigurator
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutputPolicy

object scafiConsoleConfigurator extends ConsoleConfigurator{
  var launched: Boolean = false
  private val w = 800
  private val h = 600
  override lazy val descriptors: List[Command.CommandDescription] = super.descriptors ::: List(launchDescription)

  private lazy val launchDescription = new CommandDescription("use launch to run scafi simulation", "Launch is used to launch the simulation by parameter passed") {
    /**
      * try to create command descripted by this object
      *
      * @param command
      */
    override def parseFromString(command: String): Option[Command] = if(command == "launch"){
      Some(new GeneralCommand(() => {
        if(node.isDefined && radius.isDefined && demo.isDefined) {
          val profile = demo.get.getDeclaredAnnotation(classOf[Demo]).simulationType.profile
          launched = true
          ScafiProgramBuilder (
            worldInitializer = Random(node.get,w,h),
            scafiSimulationSeed = ScafiSimulationSeed(program = demo.get, action = profile.action),
            scafiSeed = ScafiSeed(deviceSeed = profile.sensorSeed),
            simulationInitializer = RadiusSimulationInitializer(radius = radius.get),
            commandMapping = profile.commandMapping,
            outputPolicy = StandardFXOutputPolicy,
            neighbourRender = true,
            perfomance = NearRealTimePolicy
          ).launch()
          Success
        } else {
          Fail("the argument aren't set ")
        }
      }))
    } else {
      None
    }

  }
}
