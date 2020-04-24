/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import Builtins.Bounded
import sims.SensorDefinitions

class MobilityLib extends BuildingBlocks { self: AggregateProgram with SensorDefinitions =>
  /*****************************************/
  /* FORTE15: Code Mobility Meets Self-Org */
  /*****************************************/

  def snsInjectedFun: ()=>Double = forte15logic //sense("injectedFun")
  def snsSource: Boolean = sense("source")
  def snsInjectionPoint: Boolean = sense("injectionPoint")
  def snsPatron: Double = if(sense("patron")){ 1 } else { 0 }
  // ;; Simple low-pass filter for smoothing noisy signal ’value’ with rate constant ’alpha’
  def lowPass(alpha: Double, value: Double): Double = {
    rep(value){ filtered =>
      (value * alpha) + (filtered * (1 - alpha))
    }
  }

  //;; Evaluate a function field, running ’f’ from ’source’ within ’range’ meters, and ’no-op’ elsewhere
  def deploy[T: Bounded](range:Double, source:Boolean, g: ()=>T, noOp: ()=>T): T = {
    val f: ()=>T = if (distanceTo(source) < range) {
      G(source, g, identity[()=>T], nbrRange)
    } else {
      noOp
    }
    f()
  }

  /**
    * The entry-point function executed to run the virtual machine on each device.
    * @return
    */
  def virtualMachine(): Double = {
    deploy(nbrRange, snsInjectionPoint, snsInjectedFun, ()=>0.0)
  }

  def forte15logic() = lowPass(alpha = 0.5,
    value = C[Double, Double](potential = distanceTo(snsInjectionPoint),
      acc = _ + _, local = snsPatron, Null = 0.0))

  def forte15example() = {
    virtualMachine()
  }
}
