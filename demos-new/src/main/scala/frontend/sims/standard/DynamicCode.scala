package frontend.sims.standard

import frontend.sims.MyDemoLauncher.formatter_evaluation
import frontend.sims.{SensorDefinitions, UpgradableMetricProgram}
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.{ScafiProgramBuilder, ScafiWorldInformation}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.StandardFXOutput
import it.unibo.scafi.space.graphics2D.BasicShape2D.Circle

object DynamicCode extends App{
  ScafiProgramBuilder (
    Random(300,1000,1000),
    SimulationInfo(program = classOf[UpgradableMetricScafiFC]),
    RadiusSimulation(radius = 100),
    neighbourRender = true,
    scafiWorldInfo = ScafiWorldInformation(shape = Some(Circle(4))),
    outputPolicy = StandardFXOutput
  ).launch()
}

trait UpgradableMetricMain extends AggregateProgram
  with StandardSensors
  with SensorDefinitions {
  self: ScafiBaseLanguage with NeighbourhoodSensorReader with BuildingBlocksInterface with DynamicCodeInterface =>
  override def main(): Any = {
    val injecter: Injecter[this.type,NbrSensorRead[Double]] = () => {
      branch(rep(0)(_+1)<100){
        Fun[this.type,NbrSensorRead[Double]](1, (p) => p.nbrRange())
      }{
        Fun[this.type,NbrSensorRead[Double]](2, (p) => p.nbrRange().map(_+5))
      }
    }
    val metric = up[this.type,NbrSensorRead[Double]](injecter)
    distanceTo(sense1, ()=>metric.fun(this))
  }
}

class UpgradableMetricScafiStandard extends AggregateProgram with ScafiStandardLanguage
  with UpgradableMetricMain
  with ScafiStandardLibraries.BuildingBlocks
  with ScafiStandardLibraries.DynamicCode

class UpgradableMetricScafiFC extends AggregateProgram with ScafiFCLanguage
  with UpgradableMetricMain
  with ScafiFCLibraries.BuildingBlocks
  with ScafiFCLibraries.DynamicCode