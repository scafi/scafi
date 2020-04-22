/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_StateManagement{
  self: StandardLibrary.Subcomponent =>

  trait StateManagement {
    self: FieldCalculusSyntax with StandardSensors =>

    def roundCounter(): Long =
      rep(0L)(_ + 1)

    def remember[T](value: T): T =
      rep(value)(identity)

    /**
      * @return true only when a discontinuity (i.e., a change) is observed on `x` (you may choose how to handle the first observation)
      */
    def captureChange[T](x: T, initially: Boolean = true): Boolean = rep((Option.empty[T],false)) { case (value, _) =>
      (Some(x), value.map(_ != x).getOrElse(initially))
    }._2

    def countChanges[T](x: T, initially: Boolean = true): (Long,Boolean) = {
      val changed = captureChange(x, initially)
      (rep(0L)(k => if(changed) k + 1 else k), changed)
    }

    /**
      * @return true when the given parameter goes from false to true (starting from false); false otherwise
      */
    def goesUp(value: Boolean): Boolean = rep((false, false)) { case (old, trigger) =>
      (value, value && value != old)
    }._2

    /**
      * @return true when the given parameter goes from true to false (starting from false); false otherwise
      */
    def goesDown(value: Boolean): Boolean = rep((false, false)) { case (old, trigger) =>
      (value, !value && value != old)
    }._2

    /**
      * It is a simple building block which returns the same values
      *  it receives in input delayed by one computation round.
      */
    def delay[T](value: T): T = {
      var res = value
      rep(value){ old => res = old; value }
      res
    }
  }
}
