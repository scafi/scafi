/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

import it.unibo.scafi.incarnations.BasicAbstractIncarnation

class LibExtTypeClasses(val incarnation: BasicAbstractIncarnation) {
  import incarnation._

  object BoundedTypeClasses {
    import shapeless.{::, Generic, HList, HNil}
    import incarnation.Builtins._

    implicit val hnilBounded: Bounded[HNil] = new Bounded[HNil] {
      override def top: HNil = HNil
      override def bottom: HNil = HNil
      override def compare(x: HNil, y: HNil): Int = 0
    }

    implicit def hlistBounded[H, T <: HList](
      implicit hb: Bounded[H],
      tb: Bounded[T]
    ): Bounded[H :: T] = new Bounded[H :: T] {
      override def top: ::[H, T] = hb.top :: tb.top
      override def bottom: ::[H, T] = hb.bottom :: tb.bottom
      override def compare(x: ::[H, T], y: ::[H, T]): Int = if (hb.compare(x.head, y.head) != 0) {
        hb.compare(x.head, y.head)
      } else {
        tb.compare(x.tail, y.tail)
      }
    }

    implicit def genericBounded[A, R](
      implicit gen: Generic.Aux[A,R],
      reprb: Bounded[R]
    ): Bounded[A] = new Bounded[A] {
      override def top = gen.from(reprb.top)
      override def bottom = gen.from(reprb.bottom)
      override def compare(x: A, y: A) = reprb.compare(gen.to(x), gen.to(y))
    }
  }
}
