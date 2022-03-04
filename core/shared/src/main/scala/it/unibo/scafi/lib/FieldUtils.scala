/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLibFieldUtils {
  self: StandardLibrary.Subcomponent =>

  import Builtins.Bounded

  trait FieldUtils {
    self: FieldCalculusSyntax =>

    trait FieldOps {
      type M[T]
      def wrap[T](t: Option[T]): M[T]

      def foldhoodTemplate[T](init: T)(acc: (T,T) => T)(expr: => T ): T

      def mapNbrs[T](expr: => T): Map[ID, T] = reifyField(expr)

      def reifyField[T](expr: => T): Map[ID, T] =
        foldhoodTemplate[Map[ID,T]](Map.empty[ID, T])(_ ++ _) {
          Map(nbr { mid() } -> expr)
        }

      def sumHood[T](expr: => T)(implicit numEv: Numeric[T]): T =
        foldhoodTemplate[T](numEv.zero)(numEv.plus)(expr)

      def unionHood[T](expr: => T): Set[T] =
        unionHoodSet(Set(expr))

      def unionHoodSet[T](expr: => Iterable[T]): Set[T] =
        foldhoodTemplate[Set[T]](Set.empty)(_.union(_))(expr.toSet)

      def mergeHoodFirst[K,V](expr: => Map[K,V]): Map[K,V] =
        mergeHood(expr)((x, _) => x)

      def mergeHood[K,V](expr: => Map[K,V])(overwritePolicy: (V,V) => V): Map[K,V] = {
        foldhoodTemplate[Map[K,V]](Map.empty) { case (m1, m2) =>
          var newMap = m1
          m2.keys.foreach(k => newMap += k -> m1.get(k).map(v => overwritePolicy(v, m2(k))).getOrElse(m2(k)))
          newMap
        }(expr)
      }

      def anyHood(expr: => Boolean): Boolean =
        foldhoodTemplate[Boolean](false)(_||_)(expr)

      def everyHood(expr: => Boolean): Boolean =
        foldhoodTemplate(true)(_&&_)(expr)

      def minHoodSelector[T, V](toMinimize: => T)(data: => V)
                               (implicit ord1: Builtins.Bounded[T], ord2: Builtins.Bounded[ID]): M[V] = wrap[V]{
        foldhoodTemplate[(T,ID,Option[V])]((ord1.top, ord2.top, None))( (x,y) =>
          if(x._3.isDefined && (ord1.compare(x._1,y._1) < 0 || ord1.compare(x._1,y._1) == 0 && ord2.compare(x._2,y._2) <= 0)){ x } else y
        )((toMinimize, ord2.top, Some(data)))._3
      }

      def maxHoodSelector[T, V](toMaximize: => T)(data: => V)
                               (implicit ord1: Builtins.Bounded[T], ord2: Builtins.Bounded[ID]): M[V] = wrap[V]{
        foldhoodTemplate[(T,ID,Option[V])]((ord1.bottom, ord2.bottom, None))( (x,y) =>
          if(x._3.isDefined && (ord1.compare(x._1,y._1) > 0 || ord1.compare(x._1,y._1) == 0 && ord2.compare(x._2,y._2) >= 0)){ x } else y
        )((toMaximize, ord2.bottom, Some(data)))._3
      }

      def minHoodLoc[T: Bounded](default: T)(expr: => T): T =
        foldhoodTemplate[T](default)(implicitly[Bounded[T]].min)(expr)
    }

    object includingSelf extends FieldOps {
      override type M[T] = T

      override def foldhoodTemplate[T](init: T)(acc: (T,T) => T)(expr: => T ): T = foldhood[T](init)(acc)(expr)

      override def wrap[T](t: Option[T]): M[T] = t.get
    }

    object excludingSelf extends FieldOps {
      override type M[T] = Option[T]

      override def foldhoodTemplate[T](init: T)(acc: (T,T) => T)(expr: => T ): T = foldhoodPlus[T](init)(acc)(expr)

      override def wrap[T](t: Option[T]): M[T] = t
    }
  }

}
