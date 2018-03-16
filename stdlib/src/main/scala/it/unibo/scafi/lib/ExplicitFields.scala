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

    def fsns[A](e: => A): Field[A] =
      Field[A](includingSelf.reifyField(e))

    /**
      * Basic Field type
      * @param m map from devices to corresponding values
      * @tparam T type of field values
      */
    class Field[T](private[Field] val m: Map[ID,T]) {
      def map[R](o: T=>R): Field[R] =
        Field(m.mapValues(o))

      def map2[R,S](o: (T,R)=>S)(f: Field[R]): Field[S] =
        Field(m.map { case (i,v) => i -> o(v,f.m(i)) })

      def fold[V>:T](z:V)(o: (V,V)=>V): V =
        m.values.fold(z)(o)

      def reduce[V>:T](o: (V,V)=>V): V =
        m.values.reduce(o)

      def minHood(implicit ev: Ordering[T]): T =
        reduce((ev.min(_,_)))

      def minHoodPlus[V>:T](implicit ev: Bounded[V]): V =
        withoutSelf.fold[V](ev.top) { case (a: V, b: V) => ev.min(a, b) }

      def withoutSelf = Field[T](m - mid)
    }

    object Field {
      def apply[T](m: Map[ID,T]) = new Field(m)

      implicit def localToField[T](lv: T): Field[T] =
        fnbr(mid).map(_ => lv)

      implicit def fieldToLocal[T](fv: Field[T]): T =
        fv.m(mid)
    }

    /**
      * Syntactic sugar for numeric fields.
      */
    implicit class NumericField[T:Numeric](f: Field[T]){
      private val ev = implicitly[Numeric[T]]

      def +(f2: Field[T]): Field[T] = f.map2[T,T](ev.plus(_,_))(f2)
      def -(f2: Field[T]): Field[T] = f.map2[T,T](ev.minus(_,_))(f2)
      def *(f2: Field[T]): Field[T] = f.map2[T,T](ev.times(_,_))(f2)
      def +[U](lv: U)(implicit uev: Numeric[U]): Field[Double] = f.map[Double](ev.toDouble(_) + uev.toDouble(lv))
    }
  }

}
