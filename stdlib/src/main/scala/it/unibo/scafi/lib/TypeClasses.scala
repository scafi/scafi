/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.lib

trait StdLib_TypeClasses {
  self: StandardLibrary.Subcomponent =>

  object BoundedTypeClasses {
    import shapeless.{::, Generic, HList, HNil}
    import Builtins.Bounded

    implicit val hnilBounded: Bounded[HNil] = new Bounded[HNil] {
      override def top: HNil = HNil
      override def bottom: HNil = HNil
      override def compare(x: HNil, y: HNil): Int = 0
    }

    implicit def hlistBounded[H, T <: HList](implicit hb: Bounded[H],
                                             tb: Bounded[T]): Bounded[H :: T] = new Bounded[H :: T] {
      override def top: ::[H, T] = hb.top :: tb.top
      override def bottom: ::[H, T] = hb.bottom :: tb.bottom
      override def compare(x: ::[H, T], y: ::[H, T]): Int = if (hb.compare(x.head, y.head) != 0) {
        hb.compare(x.head, y.head)
      } else {
        tb.compare(x.tail, y.tail)
      }
    }

    implicit def genericBounded[A, R](implicit gen: Generic[A] {type Repr = R},
                                      reprb: Bounded[R]): Bounded[A] = new Bounded[A] {
      override def top = gen.from(reprb.top)
      override def bottom = gen.from(reprb.bottom)
      override def compare(x: A, y: A) = reprb.compare(gen.to(x), gen.to(y))
    }
  }
}
