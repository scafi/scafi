/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
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
      def restricted: Field[T] = {
        val alignedField = fnbr{1}
        Field(m.filter(el => alignedField.m.contains(el._1)))
      }

      def map[R](o: T=>R): Field[R] =
        Field(m.mapValues(o))

      def map2[R,S](f: Field[R])(o: (T,R)=>S): Field[S] =
        Field(m.map { case (i,v) => i -> o(v,f.m(i)) })

      def map2i[R,S](f: Field[R])(o: (T,R)=>S): Field[S] =
        Field(restricted.m.collect { case (i,v) if f.m.contains(i) => i -> o(v,f.m(i)) })

      def map2d[R,S](f: Field[R])(default: R)(o: (T,R)=>S): Field[S] =
        Field(m.map { case (i,v) => i -> o(v,f.m.getOrElse(i,default)) })

      def map2u[R,S](f: Field[R])(dl: T, dr: R)(o: (T,R)=>S): Field[S] =
        Field((m.keys ++ f.m.keys).map { k => k -> o(m.getOrElse(k, dl), f.m.getOrElse(k, dr)) } toMap)

      def fold[V>:T](z:V)(o: (V,V)=>V): V =
        restricted.m.values.fold(z)(o)

      def reduce[V>:T](o: (V,V)=>V): V =
        restricted.m.values.reduce(o)

      def minHood[V>:T](implicit ev: Bounded[V]): V  =
        fold[V](ev.top) { case (a, b) => ev.min(a, b) }

      def minHoodPlus[V>:T](implicit ev: Bounded[V]): V =
        withoutSelf.minHood(ev)

      def withoutSelf: Field[T] = Field[T](m - mid)

      def toMap: Map[ID,T] = m

      override def toString: String = s"Field[$m]"
    }

    object Field {
      def apply[T](m: Map[ID,T]): Field[T] = new Field(m)

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

      def +(f2: Field[T]): Field[T] = f.map2i(f2)(ev.plus(_,_))
      def -(f2: Field[T]): Field[T] = f.map2i(f2)(ev.minus(_,_))
      def *(f2: Field[T]): Field[T] = f.map2i(f2)(ev.times(_,_))
      def +/[U](lv: U)(implicit uev: Numeric[U]): Field[Double] = f.map[Double](ev.toDouble(_) + uev.toDouble(lv))
    }
  }

}
