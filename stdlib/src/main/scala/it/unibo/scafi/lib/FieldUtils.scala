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

      def unionHood[T](expr: => T): Set[T] =
        foldhoodTemplate[Set[T]](Set())(_.union(_))(Set(expr))

      def anyHood(expr: => Boolean): Boolean =
        foldhoodTemplate[Boolean](false)(_||_)(expr)
    }

    object includingSelf extends FieldOps {
      override def foldhoodTemplate[T]: (T) => ((T, T) => T) => ( => T ) => T = foldhood[T](_)
    }

    object excludingSelf extends FieldOps {
      override def foldhoodTemplate[T]: (T) => ((T, T) => T) => ( => T ) => T = foldhoodPlus[T](_)
    }
  }

}
