/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_EdgeFields {
  self: StandardLibrary.Subcomponent =>
  import Builtins.Bounded
  import Builtins.Defaultable

  trait EdgeFields extends FieldUtils {
    self: FieldCalculusSyntax =>

    case class ExchangeParams[T](old: EdgeField[T], neigh: EdgeField[T])

    def exchange[A](init: EdgeField[A])(f: ExchangeParams[A] => EdgeField[A]): EdgeField[A] = {
      branch(true) {
        val params: ExchangeParams[A] = ExchangeParams(
          old = ???,
          neigh = ???)
        val outEdgeField = f(params)
        outEdgeField
      }{ init }
    }

    def fnbr[A](e: => A): EdgeField[A] =
      EdgeField[A](e, includingSelf.reifyField(nbr(e)))

    def fsns[A](e: => A, defaultA: A): EdgeField[A] =
      EdgeField[A](defaultA, includingSelf.reifyField(e))

    /**
      * Basic Field type
      * @param m map from devices to corresponding values
      * @tparam T type of field values
      */
    class EdgeField[T](private[EdgeField] val m: Map[ID,T], val defaultValue: T) extends Builtins.Defaultable[T] {
      override implicit val default: T = defaultValue
      implicit val defaultable: Defaultable[T] = this

      def restricted: EdgeField[T] = {
        val alignedField = fnbr{1}
        EdgeField(m.filter(el => alignedField.m.contains(el._1)))
      }

      def map[R](o: T=>R): EdgeField[R] =
        EdgeField(o(default), m.mapValues(o))

      def map[R](defaultr: R, o: T=>R): EdgeField[R] = {
        EdgeField(defaultr, m.mapValues(o).toMap)
      }

      def map2[R,S](f: EdgeField[R])(o: (T,R)=>S): EdgeField[S] =
        EdgeField(o(default, f.default),
          m.map { case (i,v) => i -> o(v,f.m(i)) })

      def map2i[R,S](f: EdgeField[R])(o: (T,R)=>S): EdgeField[S] =
        EdgeField(o(default, f.default),
          restricted.m.collect { case (i,v) if f.m.contains(i) => i -> o(v,f.m(i)) })

      def map2d[R,S](f: EdgeField[R])(defaultr: R)(o: (T,R)=>S): EdgeField[S] =
        EdgeField(o(default, defaultr), m.map { case (i,v) => i -> o(v,f.m.getOrElse(i,defaultr)) })

      def map2u[R,S](f: EdgeField[R])(dl: T, dr: R)(o: (T,R)=>S): EdgeField[S] =
        EdgeField(o(dl, dr), (m.keys ++ f.m.keys).map { k => k -> o(m.getOrElse(k, dl), f.m.getOrElse(k, dr)) }.toMap)

      def fold[V>:T](z:V)(o: (V,V)=>V): V =
        restricted.m.values.fold(z)(o)

      def reduce[V>:T](o: (V,V)=>V): V =
        restricted.m.values.reduce(o)

      def minHood[V>:T](implicit ev: Bounded[V]): V  =
        fold[V](ev.top) { case (a, b) => ev.min(a, b) }

      def minHoodPlus[V>:T](implicit ev: Bounded[V]): V =
        withoutSelf.minHood(ev)

      def withoutSelf: EdgeField[T] = EdgeField[T](m - mid)

      def toMap: Map[ID,T] = m

      override def toString: String = s"Field[$m]"
    }

    object EdgeField {
      def apply[T](m: Map[ID,T])(implicit defaultable: Builtins.Defaultable[T]): EdgeField[T] =
        apply(defaultable.default, m)

      def apply[T](defaultValue: T, m: Map[ID,T]): EdgeField[T] =
        new EdgeField(m, defaultValue)

      implicit def localToField[T](lv: T): EdgeField[T] = {
        implicit val defaultablet = new Defaultable[T] {
          override def default: T = lv
        }
        fnbr(mid).map(_ => lv)
      }

      implicit def fieldToLocal[T](fv: EdgeField[T]): T =
        fv.m(mid)
    }

    /**
      * Syntactic sugar for numeric fields.
      */
    implicit class NumericField[T:Numeric](f: EdgeField[T]) extends Defaultable[T] {
      private val ev = implicitly[Numeric[T]]

      override def default: T = f.default
      implicit val defaultable: Defaultable[T] = this
      implicit val defaultableDouble: Defaultable[Double] = new Defaultable[Double] {
        override def default: Double = 0.0
      }

      def +(f2: EdgeField[T]): EdgeField[T] = f.map2i(f2)(ev.plus(_,_))
      def -(f2: EdgeField[T]): EdgeField[T] = f.map2i(f2)(ev.minus(_,_))
      def *(f2: EdgeField[T]): EdgeField[T] = f.map2i(f2)(ev.times(_,_))
      def +/[U](lv: U)(implicit uev: Numeric[U]): EdgeField[Double] = f.map[Double](ev.toDouble(_:T) + uev.toDouble(lv))
    }
  }

}
