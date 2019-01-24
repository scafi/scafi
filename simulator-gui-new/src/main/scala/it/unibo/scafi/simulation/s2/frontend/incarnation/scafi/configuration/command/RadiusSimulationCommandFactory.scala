package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.s2.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiWorldInformation
import it.unibo.scafi.simulation.s2.frontend.launcher.scafi.ListDemo
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail

/**
  * a factory used to create a command that set a simulation initializer as radius simulation
  * @param scafiConfiguration the scafi configuration
  */
class RadiusSimulationCommandFactory(implicit val scafiConfiguration: ScafiConfigurationBuilder) extends CommandFactory {
  import CommandFactory._
  import RadiusSimulationCommandFactory._
  import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
  override val name: String = "radius-simulation"

  private lazy val scafiDemo = ListDemo.demos.map {_.getSimpleName}

  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(CommandArgDescription(DemoValue,LimitedValueType(scafiDemo:_*),description = international(name, DemoValue)),
      CommandArgDescription(Radius,DoubleType,description = international(name, Radius)))

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
    //argument used to create this command
    var demoClass : Option[Class[_]] = None
    var radius : Option[Double] = None

    //verify if demo is present and if demo value is correct
    args.get(DemoValue) match {
      case Some(stringFrom : String) if ListDemo.nameToDemoClass.get(stringFrom).isDefined => demoClass = ListDemo.nameToDemoClass.get(stringFrom)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(LimitedValueType(scafiDemo:_*),DemoValue)))
      case _ =>
    }

    //verify if radius is present and if radius value is correct
    args.get(Radius) match {
      case Some(radiusValue : Double) => radius = Some(radiusValue)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType,Radius)))
      case _ =>
    }

    if(demoClass.isDefined && radius.isDefined) {
      easyResultCreation(() => {
        val demo : Demo = demoClass.get.getAnnotation(classOf[Demo])
        //take the profile marked
        val profile = demo.simulationType.profile
        //change configuration value by profile value description
        scafiConfiguration.scafiWorldInfo = ScafiWorldInformation(deviceSeed = profile.sensorSeed)
        scafiConfiguration.commandMapping = profile.commandMapping
        scafiConfiguration.simulationInitializer = Some(RadiusSimulation(radius.get))
        scafiConfiguration.scafiSimulationInformation = Some(SimulationInfo(program = demoClass.get,
          metaActions = profile.actions,
          exportEvaluations = profile.evaluations))
      })
    } else {
      //if the value name is different return a failed result
      creationFailed(Fail(wrongParameterName(DemoValue, Radius)))
    }
  }
}

object RadiusSimulationCommandFactory {
  val DemoValue = "demo"
  val Radius = "radius"
}