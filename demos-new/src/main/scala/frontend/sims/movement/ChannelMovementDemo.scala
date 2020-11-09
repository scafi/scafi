package frontend.sims.movement

import frontend.lib.{FlockingLib, Movement2DSupport}
import frontend.sims.{SensorDefinitions, SizeConversion}
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ScafiStandardAggregateProgram, ScafiStandardLibraries}
import ScafiStandardLibraries.BlockG
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ExportEvaluation.EXPORT_EVALUATION
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation.EXPORT
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.{MetaActionProducer, SimulationInfo}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random


object ChannelMovementDemo extends App {
  val worldSize = (500,500)
  val simRadius = 100
  type E = (Boolean,(Double,Double))
  MetaActionProducer.movementDtActionProducer.valueParser = (export : Any) => export match {
    case (_ , c : (Double, Double)) => Some(c)
    case _ => None
  }
  val evaluation : EXPORT_EVALUATION [Boolean] = (e : EXPORT) => e.root().asInstanceOf[E]._1
  ScafiProgramBuilder (
    Random(100,worldSize._1,worldSize._1),
    SimulationInfo(program = classOf[ChannelMovement],
      metaActions = List(MetaActionProducer.movementDtActionProducer),
      exportEvaluations = List[EXPORT_EVALUATION[Any]](evaluation)),
    RadiusSimulation(simRadius),
    neighbourRender = true
  ).launch()
}

/**
  * @see ChannelDemo to see how to interact with this program
  * you can enable random movement with select a set of nodes and mark it with sensor4
  */
class ChannelMovement  extends ScafiStandardAggregateProgram with SensorDefinitions with BlockG with FlockingLib with Movement2DSupport  {
  private val scale = 5
  def customChannel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  override def main(): ((Boolean),(Double,Double)) = branch(sense3) {
    (false, (.0, .0))
  } {
    (mux(sense4) {
      val m = randomMovement()
      (customChannel(sense1, sense2, 1), SizeConversion.normalSizeToWorldSize((scale * m._1, scale * m._2)))
    }{
      (customChannel(sense1, sense2, 1),(.0,.0))
    })
  }

}

