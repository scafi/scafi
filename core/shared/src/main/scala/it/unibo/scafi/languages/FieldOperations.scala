/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.languages

import it.unibo.scafi.core.Core

trait Languages_FieldOperations {
  self: Core =>

  import it.unibo.scafi.languages.TypesInfo.Bounded

  trait FieldOperationsInterface {
    type FieldType[T]

    def mapField[A, B](field: => FieldType[A])(mappingFunction: A => B): FieldType[B]

    def zipFields[A, B](fieldA: => FieldType[A], fieldB: => FieldType[B]): FieldType[(A, B)]

    def zipFieldWithID[T](field: => FieldType[T]): FieldType[(ID, T)]

    trait NeighbourhoodOperationsInterface {
      type M[T]
      def wrap[T](t: Option[T]): M[T]

      def foldhoodTemplate[T](init: T)(acc: (T,T) => T)(field: => FieldType[T]): T

      def mapNbrs[T](expr: => FieldType[T]): Map[ID, T] = reifyField(expr)

      def reifyField[T](expr: => FieldType[T]): Map[ID, T] =
        foldhoodTemplate[Map[ID,T]](Map[ID, T]())(_ ++ _) {
          mapField(zipFieldWithID(expr)){case (id, v) => Map(id -> v)}
        }

      def sumHood[T](expr: => FieldType[T])(implicit numEv: Numeric[T]): T =
        foldhoodTemplate[T](numEv.zero)(numEv.plus)(expr)

      def unionHood[T](expr: => FieldType[T]): Set[T] =
        unionHoodSet(mapField(expr)(Set(_)))

      def unionHoodSet[T](expr: => FieldType[Iterable[T]]): Set[T] =
        foldhoodTemplate[Set[T]](Set())(_.union(_))(mapField(expr)(_.toSet))

      def mergeHoodFirst[K,V](expr: => FieldType[Map[K,V]]): Map[K,V] =
        mergeHood(expr)((x, _) => x)

      def mergeHood[K,V](expr: => FieldType[Map[K,V]])(overwritePolicy: (V,V) => V): Map[K,V] = {
        foldhoodTemplate[Map[K,V]](Map()) { case (m1, m2) =>
          var newMap = m1
          m2.keys.foreach(k => newMap += k -> m1.get(k).map(v => overwritePolicy(v, m2(k))).getOrElse(m2(k)))
          newMap
        }(expr)
      }

      def anyHood(expr: => FieldType[Boolean]): Boolean =
        foldhoodTemplate[Boolean](false)(_||_)(expr)

      def everyHood(expr: => FieldType[Boolean]): Boolean =
        foldhoodTemplate(true)(_&&_)(expr)

      def minHoodSelector[T, V](toMinimize: => FieldType[T])(data: => FieldType[V])
                               (implicit ord1: TypesInfo.Bounded[T], ord2: TypesInfo.Bounded[ID]): M[V] = wrap[V]{
        foldhoodTemplate[(T, ID, Option[V])]((ord1.top, ord2.top, None))( (x,y) =>
          if(x._3.isDefined && (ord1.compare(x._1,y._1) < 0 || ord1.compare(x._1,y._1) == 0 && ord2.compare(x._2,y._2) <= 0)) x else y
        )(mapField(zipFields(zipFieldWithID(toMinimize), data)){case ((id, p), d) => (p, id, Some(d))})._3
      }

      def maxHoodSelector[T, V](toMaximize: => FieldType[T])(data: => FieldType[V])
                               (implicit ord1: TypesInfo.Bounded[T], ord2: TypesInfo.Bounded[ID]): M[V] = wrap[V]{
        foldhoodTemplate[(T,ID,Option[V])]((ord1.bottom, ord2.bottom, None))( (x,y) =>
          if(x._3.isDefined && (ord1.compare(x._1,y._1) > 0 || ord1.compare(x._1,y._1) == 0 && ord2.compare(x._2,y._2) >= 0)){ x } else y
        )(mapField(zipFields(zipFieldWithID(toMaximize), data)){case ((id, p), d) => (p, id, Some(d))})._3
      }

      def minHoodLoc[T: Bounded](default: T)(expr: => FieldType[T]): T =
        foldhoodTemplate[T](default)(implicitly[Bounded[T]].min)(expr)

      def maxHoodLoc[T: Bounded](default: T)(expr: => FieldType[T]): T =
        foldhoodTemplate[T](default)(implicitly[Bounded[T]].max)(expr)

      //similar to minHoodSelector where toMinimize == data, but doesn't use ids as tie-breakers (in line with the original minHood syntax)
      def minHood[T](expr: => FieldType[T])(implicit of: Bounded[T]): M[T] =
        wrap { foldhoodTemplate[Option[T]](None)((x, y) =>
          if (x.isDefined && of.compare(x.get, y.get) <= 0) x else y
        ) { mapField(expr)(Some(_)) } }

      //similar to maxHoodSelector where toMaximize == data, but doesn't use ids as tie-breakers (in line with the original maxHood syntax)
      def maxHood[T](expr: => FieldType[T])(implicit of: Bounded[T]): M[T] =
        wrap { foldhoodTemplate[Option[T]](None)((x, y) =>
          if (x.isDefined && of.compare(x.get, y.get) > 0) x else y
        ) { mapField(expr)(Some(_)) } }
    }

    def includingSelf: IncludingSelfInterface

    def excludingSelf: ExcludingSelfInterface

    trait IncludingSelfInterface extends NeighbourhoodOperationsInterface {
      override type M[T] = T

      override def wrap[T](t: Option[T]): M[T] = t.get
    }

    trait ExcludingSelfInterface extends NeighbourhoodOperationsInterface {
      override type M[T] = Option[T]

      override def wrap[T](t: Option[T]): M[T] = t

      def maxHoodPlus[T](expr: => FieldType[T])(implicit of: Bounded[T]): T =
        maxHood(expr).getOrElse(of.bottom)

      def minHoodPlus[T](expr: => FieldType[T])(implicit of: Bounded[T]): T =
        minHood(expr).getOrElse(of.top)
    }
  }
}
