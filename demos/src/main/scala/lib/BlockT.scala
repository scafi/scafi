package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

import scala.concurrent.duration.Duration

/**
  * @author Roberto Casadei
  *
  */
trait BlockT { self: AggregateProgram =>
  def T[V](initial: V, floor: V, decay: V => V)
          (implicit ev: Numeric[V]): V = {
    rep(initial) { v =>
      ev.min(initial, ev.max(floor, decay(v)))
    }
  }

  def T[V](initial: V, decay: V => V)
          (implicit ev: Numeric[V]): V = {
    T(initial, ev.zero, decay)
  }

  def T[V](initial: V)
          (implicit ev: Numeric[V]): V = {
    T(initial, (t: V) => ev.minus(t, ev.one))
  }

  def timer[V](length: V)
              (implicit ev: Numeric[V]) =
    T[V](length)

  def limitedMemory[V, T](value: V, expValue: V, timeout: T)
                         (implicit ev: Numeric[T]) = {
    val t = timer[T](timeout)
    (if (ev.gt(t, ev.zero)) value else expValue, t)
  }

  def timer(dur: Duration): Long = {
    val ct = System.nanoTime() // Current time
    val et = ct + dur.toNanos // Time-to-expire (bootstrap)

    rep((et, dur.toNanos)) { case (expTime, remaining) =>
      if (remaining <= 0) (et, 0)
      else (expTime, expTime - ct)
    }._2 // Selects the component expressing remaining time
  }

  def recentlyTrue(dur: Duration, cond: => Boolean): Boolean =
    rep(false){ happened =>
      branch(cond){ true } { branch(!happened){ false }{ timer(dur)>0 } }
    }

}
