/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_GenericUtils {
  self: StandardLibrary.Subcomponent =>

  trait GenericUtils {
    self: FieldCalculusSyntax with StandardSensors =>

    def meanCounter(value: Double, frequency: Long): Double = {
      val time = timestamp()
      val dt = deltaTime().toMillis
      val count = rep ((0.0,0.0)) { case x => { // (accumulated value, last time)
        // Splits into windows of multiples of 'frequency'
        // and restarts at the beginning of each new window.
        // E.g., for frequency=5
        // Time:           0_____5_____10_____15_____20 ...
        // Restart:              ^      ^      ^      ^ ...
        // Floor(Time/freq):  0     1      2      3     ...
        val restart = rep((false, time)) { t =>
          (Math.floor(time/frequency) > Math.floor(t._2/frequency), time)
        }._1
        // Reset value and time on restart
        val old = if (restart){ (0.0,0.0) }  else x
        // Filters infinite values out
        if (Double.NegativeInfinity < value && value < Double.PositiveInfinity) {
          // Sums value weighed by time
          (old._1 + value*dt, old._2 + dt)
        } else {
          old
        }
      } }
      // E.g., consider these values and deltas: (5.0,2), (6,1), (Inf,2), (7,1), (5,1)
      // You'll finally have (5.0*2 + 6*1 + 7*1 + 5*1) / (2+1+1+1) = 28/5 = 5.6
      count._1 / count._2
    }
  }

}
