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

trait StdLib_ExplicitFields {
  self: StandardLibrary.Subcomponent =>
  import Builtins.Bounded

  trait ExplicitFields extends FieldUtils {
    self: FieldCalculusSyntax =>

    def fnbr[A](e: => A): Field[A] =
      Field[A](includingSelf.reifyField(nbr(e)))

    def fsns[A](e: =>A):Field[A] =
      Field[A](includingSelf.reifyField(e))

    case class Field[T](m: Map[ID,T]) {
      def map[R](o: T=>R): Field[R] = Field(for ((i,t)<-m) yield (i,o(t)))
      def map2[R,S](o: (T,R)=>S)(f: Field[R]): Field[S] = Field(for ((i,t)<-m) yield (i,o(t,f.m(i))))
      def fold(z:T)(o: (T,T)=>T): T = m.values.fold(z)(o)
      def minHood(implicit ev: Ordering[T]): T =
        m.values.reduce((ev.min(_,_)))
      def minHoodPlus[V>:T](implicit ev: Bounded[V]): V =
        withoutSelf.m.values.fold[V](ev.top) { case (a: V, b: V) => ev.min(a, b) }

      def withoutSelf = Field[T](m - mid)
    }

    implicit class RichField[T:Numeric](f: Field[T]){
      def +(f2: Field[T]): Field[T] = f.map2[T,T](implicitly[Numeric[T]].plus(_,_))(f2)
    }
  }

}
