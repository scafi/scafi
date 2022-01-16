package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command

import it.unibo.scafi.simulation.s2.frontend.configuration.command.Command
import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandFactory
import it.unibo.scafi.simulation.s2.frontend.configuration.environment.ProgramEnvironment.FastPolicy
import it.unibo.scafi.simulation.s2.frontend.configuration.environment.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.s2.frontend.configuration.environment.ProgramEnvironment.StandardPolicy
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiConfiguration.ScafiConfigurationBuilder
import it.unibo.scafi.simulation.s2.frontend.util.Result
import it.unibo.scafi.simulation.s2.frontend.util.Result.Fail

/**
 * a factory used to set a program performance
 * @param scafiConfiguration
 *   the configuration builder used to build configuration
 */
class PerformanceCommandFactory(implicit val scafiConfiguration: ScafiConfigurationBuilder) extends CommandFactory {
  import CommandFactory._
  import PerformanceCommandFactory._
  import it.unibo.scafi.simulation.s2.frontend.configuration.launguage.ResourceBundleManager._
  override val name: String = "performance"
  private val performanceMap = Map(
    NearRealTimePolicy.toString -> NearRealTimePolicy,
    FastPolicy.toString -> FastPolicy,
    StandardPolicy.toString -> StandardPolicy
  )
  val argType: LimitedValueType = LimitedValueType(performanceMap.keySet.toSeq: _*)
  override def commandArgsDescription: Seq[CommandFactory.CommandArgDescription] =
    List(
      CommandArgDescription(
        Name,
        argType,
        description = international(name, Name),
        defaultValue = Some(scafiConfiguration.performance.toString)
      )
    )

  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = args.get(Name) match {
    case Some(value: String) =>
      if (performanceMap.contains(value)) {
        easyResultCreation(() => scafiConfiguration.performance = performanceMap(value))
      } else {
        creationFailed(Fail(wrongTypeParameter(argType, Name)))
      }
    case _ => creationFailed(Fail(wrongParameterName(Name)))
  }
}

object PerformanceCommandFactory {
  val Name = "name"
}
