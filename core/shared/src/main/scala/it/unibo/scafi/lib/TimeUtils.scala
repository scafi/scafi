/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

import scala.concurrent.duration._

trait StdLib_TimeUtils {
  self: StandardLibrary.Subcomponent =>

  // scalastyle:off method.name

  trait BlockTInterface {
    self: ScafiBaseLanguage with LanguageDependant =>

    def T[V](initial: V, floor: V, decay: V => V)
            (implicit ev: Numeric[V]): V =
      rep(initial) { v =>
        ev.min(initial, ev.max(floor, decay(v)))
      }

    def T[V](initial: V, decay: V => V)
            (implicit ev: Numeric[V]): V =
      T(initial, ev.zero, decay)

    def T[V](initial: V)
            (implicit ev: Numeric[V]): V =
      T(initial, (t: V) => ev.minus(t, ev.one))

    def T[V](initial: V, dt: V)
            (implicit ev: Numeric[V]): V =
      T(initial, (t: V) => ev.minus(t, dt))

    def timer[V: Numeric](length: V): V =
      T(length)

    def limitedMemory[V, T](value: V, expValue: V, timeout: T)
                           (implicit ev: Numeric[T]): (V, T) = {
      val t = timer[T](timeout)
      (if (ev.gt(t, ev.zero)) value else expValue, t)
    }

    /**
    * Timer synchronized within a neighborhood
    */
    def sharedTimerWithDecay[T](period: T, dt: T)(implicit ev: Numeric[T]): T =
      rep(ev.zero) { clock =>
        val clockPerceived = neighbourhoodFold(clock)(ev.max)(clock)
        branch(ev.compare(clockPerceived, clock) <= 0) {
          // I'm currently as fast as the fastest device in the neighborhood, so keep on counting time
          ev.plus(clock, (if (cyclicTimerWithDecay(period, dt)) {
            ev.one
          } else {
            ev.zero
          }))
        } {
          // Someone else's faster, take his time, and restart counting
          clockPerceived
        }
      }

    /**
      * Cyclic timer.
      *
      * @param length timeout
      * @param decay  decay rate
      * @return true if the timeout is expired, false otherwise
      */
    def cyclicTimerWithDecay[T](length: T, decay: T)(implicit ev: Numeric[T]): Boolean =
      rep(length) { left =>
        branch(left == ev.zero) {
          length
        } {
          T(length, decay)
        }
      } == length

    /**
      * Cyclic timer with a default unitary decay
      *
      * @param length timeout
      * @return true if the timeout is expired, false otherwise
      */
    def cyclicTimer[T](length: T)(implicit ev: Numeric[T]): Boolean = {
      cyclicTimerWithDecay(length, ev.one)
    }

    def clock[T](length: T, decay: T)
                (implicit ev: Numeric[T]): Long =
      rep((0L, length)) { case (k, left) =>
        branch(left == ev.zero) {
          (k + 1, length)
        } {
          (k, T(length, decay))
        }
      }._1

    def impulsesEvery[T: Numeric](d: T): Boolean =
      rep(false) { impulse =>
        branch(impulse) {
          false
        } {
          timer(d) == 0
        }
      }

    /**
      * Exponential back-off filter.
      *
      * @param signal T, signal to be filtered
      * @param a      T, alpha value
      * @return T, filtered signal
      */
    def exponentialBackoffFilter[T](signal: T, a: T)(implicit ev: Numeric[T]): T =
      rep(signal)(s => ev.plus(ev.times(s, a), ev.times(s, ev.minus(ev.one, a))))

  }

  trait TimeUtilsInterface extends BlockTInterface {
    self: ScafiBaseLanguage with StandardSensors with LanguageDependant =>

    def sharedTimer(period: FiniteDuration): FiniteDuration =
      sharedTimerWithDecay(period.toMillis, deltaTime().toMillis).seconds

    def timerLocalTime(dur: Duration): Long =
      T(initial = dur.toNanos, dt = deltaTime().toNanos)

    def impulsesEvery(d: FiniteDuration): Boolean =
      rep(false){ impulse =>
        branch(impulse) { false } { timerLocalTime(d)==0 }
      }

    def recentlyTrue(dur: Duration, cond: => Boolean): Boolean =
      rep(false) { happened =>
        branch(cond) {
          true
        } {
          branch(!happened) {
            false
          } {
            timerLocalTime(dur) > 0
          }
        }
      }

    /**
      * Evaporation pattern.
      * Starting from [lenght, info] descends to [0, info] with a custom decay
      * (The floor values depends on length's type)
      *
      * @param length T, duration
      * @param info   V, information
      * @param decay  T => T, decay rate
      * @return [V, T]
      */
    def evaporation[T, V](length: T, decay: T => T, info: V)(implicit ev: Numeric[T]): (T, V) =
      (T(length, decay), info)

    /**
      * Evaporation pattern.
      * Starting from [lenght, info] descends to [0, info] with a predefined unitary decay
      * (The floor values depends on length's type)
      *
      * @param length T, duration
      * @param info   V, information
      * @return [V, T]
      */
    def evaporation[T, V](length: T, info: V)(implicit ev: Numeric[T]): (T, V) = {
      (T(length), info)
    }

    /**
      * Periodically invoke a function.
      *
      * @param length FiniteDuratrion, timeout
      * @param f      () -> T, function to be invoked
      * @param NULL   T, default value
      * @return       T, apply f if the timeout is expired, NULL otherwise
      */
    def cyclicFunction[T](length: FiniteDuration, f:  () => T, NULL: T): T =
      cyclicFunctionWithDecay(length.toNanos, deltaTime().toNanos, f, NULL)

    /**
      * Periodically invoke a function.
      *
      * @param length  T, timeout
      * @param decay   T, decay rate
      * @param f       () -> V, function to be invoked
      * @param NULL V, default value
      * @return V, apply f if the timeout is expired, NULL otherwise
      */
    def cyclicFunctionWithDecay[T, V](length: T, decay: T, f: () => V, NULL: V)(implicit ev: Numeric[T]): V =
      branch(cyclicTimerWithDecay(length, decay)) {
        f()
      } {
        NULL
      }

  }

  trait BlockT_ScafiStandard extends BlockTInterface with LanguageDependant_ScafiStandard {
    self: ScafiStandardLanguage =>
  }
  trait TimeUtils_ScafiStandard extends TimeUtilsInterface with LanguageDependant_ScafiStandard {
    self: ScafiStandardLanguage with StandardSensors =>
  }

  trait BlockT_ScafiFC extends BlockTInterface with LanguageDependant_ScafiFC {
    self: ScafiFCLanguage =>
  }
  trait TimeUtils_ScafiFC extends TimeUtilsInterface with LanguageDependant_ScafiFC {
    self: ScafiFCLanguage with StandardSensors =>
  }
}
