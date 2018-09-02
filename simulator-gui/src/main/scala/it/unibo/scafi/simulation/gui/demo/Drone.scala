package it.unibo.scafi.simulation.gui.demo
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, _}
import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment.{FastPerformancePolicy, NearRealTimePolicy, StandardPolicy}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulationInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.{Demo, SimulationType}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.{Actions, ScafiSimulationInformation}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.{ScafiProgramBuilder, ScafiWorldInformation}
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.command.ScafiParser
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.{Grid, Random}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Circle
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutputPolicy

object Drone extends App {
  ScafiProgramBuilder (
    worldInitializer = Random(500,500,500),
    scafiSimulationInfo = ScafiSimulationInformation(program = classOf[BlobDroneSystemExplorationDemo],action = Actions.movementAction),
    simulationInitializer = RadiusSimulationInitializer( radius = 40),
    scafiWorldInfo = ScafiWorldInformation(shape = Some(Circle(4))),
    outputPolicy = StandardFXOutputPolicy,
    neighbourRender = true,
    perfomance = NearRealTimePolicy
  ).launch()
}
@Demo(simulationType = SimulationType.MOVEMENT)
class BasicMovement extends AggregateProgram with SensorDefinitions with FlockingLib with BlockG with Movement2DSupport {
  override def main:(Double, Double) = randomMovement()
}
@Demo(simulationType = SimulationType.MOVEMENT)
class Movement extends AggregateProgram with SensorDefinitions with FlockingLib with BlockG with Movement2DSupport {

  private val attractionForce: Double = 10.0
  private val alignmentForce: Double = 40.0
  private val repulsionForce: Double = 80.0
  private val repulsionRange: Double = 100
  private val obstacleForce: Double = 400.0

  override def main:(Double, Double) = rep(randomMovement())(behaviour3)

  private def behaviour1(tuple: ((Double, Double))): (Double, Double) =
    mux(sense1) {
      flock(tuple, Seq(sense1), Seq(sense3), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
    } {
      tuple
    }

  private def behaviour2(tuple: ((Double, Double))): (Double, Double) =
    mux(sense1) {
      clockwiseRotation(.5, .5)
    } {
      (.0, .0)
    }

  private def behaviour3(tuple: ((Double, Double))): (Double, Double) =
    mux(sense1) {
      val m = clockwiseRotation(.5, .5)
      val f = flock(tuple, Seq(sense1), Seq(sense3), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
      normalizeToScale(m._1 + f._1, m._2 + f._2)
    } {
      (.0, .0)
    }
}
class BlobDroneSystemExplorationDemo extends AggregateProgram with SensorDefinitions with FlockingLib with Movement2DSupport with BlockG {

  private val attractionForce: Double = 10.0
  private val alignmentForce: Double = 40.0
  private val repulsionForce: Double = 80.0
  private val repulsionRange: Double = 100
  private val obstacleForce: Double = 400.0

  private val separationThr = 200
  private val neighboursThr = 4


  override def main(): (Double, Double) = rep(randomMovement(), (0.5,0.5))(behaviour)._1

  private def flockWithBase(myTuple: ((Double, Double),(Double,Double))): ((Double, Double),(Double,Double)) = {
  val myPosition = currentPosition()
  val gradient = distanceTo(sense3)
  val minGradHood = minHood(nbr(gradient))
  val nbrCount: Int = foldhoodPlus(0)(_ + _){1}
  val basePosition: (Double, Double) = broadcast(sense3, (myPosition.x, myPosition.y))
  mux(((gradient - minGradHood) > separationThr | gradient > 100) & nbrCount < neighboursThr) {
  val baseVector = goToPointWithSeparation(myTuple._2, repulsionRange)
  (baseVector, myTuple._2)
} {
  val flockVector = flock(myTuple._1, Seq(sense1), Seq(sense2), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)

  mux(basePosition._1 > 1.0 | basePosition._2 > 1.0 | (basePosition._1 == 0.0 & basePosition._2 == 0.0)) {
  ((flockVector._1, flockVector._2), myTuple._2)
} {
  ((flockVector._1, flockVector._2), basePosition)
}
}
}

  private def behaviour(tuple:((Double, Double),(Double,Double))): ((Double, Double),(Double,Double)) = {
  val myPosition = currentPosition()
  mux(sense1){
  flockWithBase(tuple)
}
{
  mux(sense3){
  val bp: (Double, Double) = broadcast(sense3, (myPosition.x, myPosition.y))
  ((0.0,0.0), bp)
}
{
  val fv = flock(tuple._1, Seq(sense1), Seq(sense2), repulsionRange, 0.0, 0.0, repulsionForce, 0.0)
  (fv, (0.5,0.5))
}
}
}

}