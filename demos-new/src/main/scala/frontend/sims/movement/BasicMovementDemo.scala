/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims.movement

import frontend.lib.{FlockingLib, Movement2DSupport}
import frontend.sims.{SensorDefinitions, SizeConversion}
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ScafiStandardAggregateProgram, ScafiStandardLibraries}
import ScafiStandardLibraries.BlockG
import it.unibo.scafi.simulation.s2.frontend.configuration.environment.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.{Demo, SimulationType}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.{MetaActionProducer, SimulationInfo}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.{ScafiInformation, ScafiProgramBuilder}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.FastFXOutput

//use -Djavafx.animation.fullspeed=true to increase perfomance
object BasicMovementDemo extends App {
  ScafiProgramBuilder (
    Random(1000,1000,1000),
    SimulationInfo(program = classOf[BasicMovement],
      metaActions = List(MetaActionProducer.movementDtActionProducer),
      exportEvaluations = List.empty),
    RadiusSimulation(40),
    neighbourRender = true,
    outputPolicy = FastFXOutput,
    performance = NearRealTimePolicy
  ).launch()
}
/**
  * This program allows you to see some basic movement that can be done with scafi simulation.
  * Here are described three behaviours:
  * 1 - behaviour1: select a set of node and mark it with sensor1. the selected set start to move randomly
  * 2 - behaviour2: select a set of node and mark it with sensor2. the selected set start to move in clockwise way. the node are not influenced by another
  * 3 - behaviour2: select a set of node and mark it with sensor2. the selected set start to move in clockwise way. the node are influenced by another
  */

@Demo(simulationType = SimulationType.MOVEMENT)
class BasicMovement extends ScafiStandardAggregateProgram with SensorDefinitions with FlockingLib with BlockG with Movement2DSupport {

  private val attractionForce: Double = 10.0
  private val alignmentForce: Double = 40.0
  private val repulsionForce: Double = 80.0
  private lazy val repulsionRange: Double = ScafiInformation.configuration.simulationInitializer match {
    case RadiusSimulation(radius) => radius * 60.0 / 200
    case _ => 60.0 / 200
  }
  lazy val centerX = ScafiInformation.configuration.worldInitializer.size._1 / 2
  lazy val centerY = ScafiInformation.configuration.worldInitializer.size._2 / 2
  private val obstacleForce: Double = 400.0

  override def main:(Double, Double) = SizeConversion.normalSizeToWorldSize(rep(randomMovement())(behaviour2))

  private def behaviour1(tuple: ((Double, Double))): (Double, Double) =
    mux(sense1) {
      flock(tuple, Seq(sense1), Seq(sense3), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
    } {
      tuple
    }

  private def behaviour2(tuple: ((Double, Double))): (Double, Double) =
    mux(sense1) {
      val m = clockwiseRotation(centerX, centerY)
      normalizeToScale(m._1,m._2)
    } {
      (.0, .0)
    }

  private def behaviour3(tuple: ((Double, Double))): (Double, Double) =
    mux(sense1) {
      val m = clockwiseRotation(centerX, centerY)
      val f = flock(tuple, Seq(sense1), Seq(sense3), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
      normalizeToScale(m._1 + f._1, m._2 + f._2)
    } {
      (.0, .0)
    }
}