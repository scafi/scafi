/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLibStateManagement{
  self: StandardLibrary.Subcomponent =>

  trait StateManagement {
    self: FieldCalculusSyntax =>

    /**
      * Counts the number of rounds, refreshing each time the computation is re-entered.
      * TODO: consider boundedness as a limitation for long-lived/eternal systems
      * @return the number of the round
      */
    def roundCounter(): Long =
      rep(0L)(_ + 1)

    /**
      * Remembers the provided value
      */
    def remember[T](value: => T): T =
      rep(value)(identity)

    /**
      * Alias for [[remember()]]
      */
    def constant[T](value: => T): T = remember(value)

    /**
      * Remembers the provided optional value, unless empty
      */
    def keep[T](expr: => Option[T]): Option[T] = rep[Option[T]](None){ _.orElse(expr) }

    /**
      * Remembers the occurrence of some condition or event
      */
    def keep(expr: => Boolean): Boolean = rep(false)(b => if(b) b else expr)

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

    private def none[T]: Option[T] = None

    /**
      * Returns a non-empty optional with the provided value just once, then None
      */
    def once[T](expr: => T): Option[T] = rep((true,none[T])){ case (first,value) => (false, if(first) Some(expr)  else None) }._2
  }
}
