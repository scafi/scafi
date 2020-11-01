/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object ReplicatedGossipDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.ReplicatedGossipProgram" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.25 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  Settings.ConfigurationSeed = 0
  launch()
}

class ReplicatedGossipProgram extends ScafiStandardAggregateProgram with StateManagement with SensorDefinitions with GradientAlgorithms with Processes with BlockT
  with HFCSpawn {
  def main: String = {
    val g = classic(sense1)
    val grepl = replicatedGossip2(sense1, numActiveProcs = 5, startEvery = 2.second, considerAfter = 2.second)
    f"$g%5.1f; ${grepl}%5.1f"
  }

  def replicatedGossip(src: => Boolean, numActiveProcs: Int, startEvery: Int, considerAfter: Int): Option[Double] = {
    val generators = remember{Set(
      ProcessGenerator(
        trigger = () => sense2 && impulsesEvery(startEvery),
        generator = () => ProcessDef(PID("1"), comp = () => mux[Option[Double]](timer(considerAfter)==0){ Some(classic(src)) }{ None },
        stopCondition = () => timer(startEvery*numActiveProcs+startEvery/2+considerAfter)==0))
    )}
    val replicated = processExecution(generators).values.collect{ case Some(x) => x }
    if(replicated.isEmpty) None else Some(replicated.max)
  }

  def replicatedGossip2(src: => Boolean, numActiveProcs: Int, startEvery: FiniteDuration, considerAfter: FiniteDuration): Double = {
      spawn[Unit,Boolean,Double]( (_) => source => {
          val status = mux[Status](timerLocalTime(startEvery * numActiveProcs + startEvery/2 + considerAfter)!=0){
            mux[Status](timerLocalTime(considerAfter)!=0){ Bubble }{ Output }
          }{ External }
          (classic(source), status)
        }, mux(sense2 & impulsesEvery(startEvery)){ List(()) }{ List() },
      src).lastOption.getOrElse(Double.PositiveInfinity)
  }
}