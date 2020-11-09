/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims.movement

import frontend.lib.{FlockingLib, Movement2DSupport}
import frontend.sims.{SensorDefinitions, SizeConversion}
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ScafiStandardAggregateProgram, ScafiStandardLibraries}
import ScafiStandardLibraries.BlockG
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.{Demo, SimulationType}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.{MetaActionProducer, SimulationInfo}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.{ScafiInformation, ScafiProgramBuilder}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random

object SupportNetworkExploration extends App {
  ScafiProgramBuilder (
    Random(1000,1000,1000),
    SimulationInfo(program = classOf[SupportNetworkExplorationDemo], metaActions = List(MetaActionProducer.movementDtActionProducer),exportEvaluations = List.empty),
    RadiusSimulation(20),
    neighbourRender = true
  ).launch()
}

/**
  * Scenario: exploration from a fixed or mobile base.
  *  The nodes should explore the space without losing connections with the base;
  *  when this happens, they should return to the base.
  *  Other nodes build flocks with loose rules, sporadically interacting with the main network.
  *   - sense1: nodes of the main network;
  *   - sense2: obstacles.
  *   - sense3: base of operations;
  *   - sense4: explorer nodes, connected to the network.
  * choose a set of nodes and mark it as sense3. choose another set of nodes and mark it as sens1.
  * sens1 nodes start to explore network, after some turn the set comes back to the base.
  * if you choose another set and mark it as sense4 it moves randomly trying to keep linking to the network.
  */
@Demo(simulationType = SimulationType.MOVEMENT)
class SupportNetworkExplorationDemo extends ScafiStandardAggregateProgram with SensorDefinitions with FlockingLib with Movement2DSupport with BlockG {

  private val attractionForce: Double = 10.0
  private val alignmentForce: Double = 40.0
  private val repulsionForce: Double = 80.0
  private lazy val repulsionRange: Double = ScafiInformation.configuration.simulationInitializer match {
    case RadiusSimulation(radius) => radius * 60.0 / 100
    case _ => 60.0 / 100
  }
  private val obstacleForce: Double = 100.0
  private lazy val separationThr: Double = ScafiInformation.configuration.simulationInitializer match {
    case RadiusSimulation(radius) => radius * 80
    case _ => 80
  }
  private val neighboursThr: Int = 7
  lazy val width = ScafiInformation.configuration.worldInitializer.size._1
  lazy val height = ScafiInformation.configuration.worldInitializer.size._2
  private val SPACE_DIMENSION: Int = 1000

  override def main():(Double, Double) = SizeConversion.normalSizeToWorldSize(rep(randomMovement(), (width / 2, height / 2))(round)._1)

  private def behaviourSense1(tuple: ((Double, Double), (Double, Double))) = {
    var gradient = Double.MaxValue
    var minGradHood = 0.0
    branch(sense4){} {
      gradient = distanceTo(sense3)
      minGradHood = minHood(nbr(gradient))
    }
    mux((gradient - minGradHood) > separationThr | gradient > SPACE_DIMENSION) ((goToPointWithSeparation(tuple._2,repulsionRange), tuple._2)) {
      var temp = flock(tuple._1, Seq(sense1), Seq(sense2), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
      val basePosition: (Double, Double) = branch(sense4){(0.0,0.0)}{broadcast(sense3, (currentPosition().x, currentPosition().y))}
      mux(basePosition._1 > width | basePosition._2 > height | (basePosition._1 == 0.0 & basePosition._2 == 0.0))((temp, tuple._2))((temp, basePosition))
    }
  }

  private def behaviourSense4(tuple: ((Double, Double), (Double, Double))) = {
    val gradient = distanceTo(sense1)
    val minGradHood = minHood(nbr(gradient))
    val nbrCount: Int = foldhoodPlus(0)(_ + _){1}
    mux(((gradient - minGradHood) > separationThr * 2 | gradient > SPACE_DIMENSION) & nbrCount < neighboursThr) {
      val s = goToPointWithSeparation(tuple._2, repulsionRange)
      (s, tuple._2)
    } {
      val temp = flock(tuple._1, Seq(sense4), Seq(sense1), repulsionRange, attractionForce, alignmentForce, repulsionForce, obstacleForce)
      val p: (Double, Double) = broadcast(sense1, (currentPosition().x, currentPosition().y))
      mux(p._1 > width | p._2 > height | (p._1 == 0.0 & p._2 == 0.0)) {
        ((temp._1, temp._2), tuple._2)
      } {
        ((temp._1, temp._2), p)
      }
    }
  }

  private def round(tuple: ((Double, Double), (Double, Double))): ((Double, Double), (Double, Double)) =
    mux(sense1) {
      behaviourSense1(tuple)
    } (
      mux(sense4){
        behaviourSense4(tuple)
      } {
        mux(sense3){
          ((.0,.0),(.0,.0))
        } {
          (flock(tuple._1, Seq(sense1), Seq(sense2), 4, 0.0, 0.0, 10.0, 0.0), tuple._2)
        }
      }
    )

}