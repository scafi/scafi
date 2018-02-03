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

trait StdLib_FieldUtils {
  self: StandardLibrary.Subcomponent =>

  trait FieldUtils {
    self: FieldCalculusSyntax =>

    trait FieldOps {
      def foldhoodTemplate[T]: T => ((T,T) => T) => ( => T ) => T

      def mapNbrs[T](expr: => T): Map[ID, T] = reifyField(expr)

      def reifyField[T](expr: => T): Map[ID, T] = {
        foldhoodTemplate[Seq[(ID, T)]](Seq[(ID, T)]())(_ ++ _) {
          Seq(nbr { mid() } -> expr)
        }.toMap
      }

      def sumHood[T](expr: => T)(implicit numEv: Numeric[T]) =
        foldhoodTemplate[T](numEv.zero)(numEv.plus(_,_))(nbr(expr))

      def unionHood[T](expr: => T): Set[T] =
        unionHoodSet(Set(expr))

      def unionHoodSet[T](expr: => Iterable[T]): Set[T] =
        foldhoodTemplate[Set[T]](Set())(_.union(_))(expr.toSet)

      def mergeHoodFirst[K,V](expr: => Map[K,V]): Map[K,V] =
        mergeHood(expr)((x,y) => x)

      def mergeHood[K,V](expr: => Map[K,V])(overwritePolicy: (V,V) => V): Map[K,V] = {
        foldhoodTemplate[Map[K,V]](Map()) { case (m1, m2) =>
          var newMap = m1
          m2.keys.foreach(k => newMap += k -> m1.get(k).map(v => overwritePolicy(v, m2(k))).getOrElse(m2(k)))
          newMap
        }(expr)
      }

      def anyHood(expr: => Boolean): Boolean =
        foldhoodTemplate[Boolean](false)(_||_)(expr)

      def minHoodSelector[T: Builtins.Bounded, V](toMinimize: => T)(data: => V): Option[V] = {
        val ord = implicitly[Builtins.Bounded[T]]
        foldhoodTemplate[(T,Option[V])]((ord.top, None))( (x,y) => if(ord.compare(x._1,y._1) <= 0) x else y )((toMinimize, Some(data)))._2
      }
    }

    object includingSelf extends FieldOps {
      override def foldhoodTemplate[T]: (T) => ((T, T) => T) => ( => T ) => T = foldhood[T](_)
    }

    object excludingSelf extends FieldOps {
      override def foldhoodTemplate[T]: (T) => ((T, T) => T) => ( => T ) => T = foldhoodPlus[T](_)
    }
  }

}
