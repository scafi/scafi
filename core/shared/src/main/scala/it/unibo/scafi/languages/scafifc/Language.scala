package it.unibo.scafi.languages.scafifc

import it.unibo.scafi.core.Core
import it.unibo.scafi.languages.TypesInfo.Bounded
import it.unibo.scafi.languages.scafibase.{Language => BaseLanguage}

trait Language extends BaseLanguage {
  self: Core =>

  trait ScafiFC_Constructs extends ScafiBase_Constructs {
    def nbrField[A](expr: => A): Field[A]

    def nbrFieldVar[A](name: CNAME): Field[A]

    //TODO can we make it +T?
    class Field[T](private[Field] val m: Map[ID,T]) {
      def restricted: Field[T] = {
        val alignedField = nbrField{1}
        Field(m.filter(el => alignedField.m.contains(el._1)))
      }

      def map[R](o: T=>R): Field[R] =
        Field(m.mapValues(o).toMap)

      def map2[R,S](f: Field[R])(o: (T,R)=>S): Field[S] =
        Field(m.map { case (i,v) => i -> o(v,f.m(i)) })

      def map2i[R,S](f: Field[R])(o: (T,R)=>S): Field[S] =
        Field(restricted.m.collect { case (i,v) if f.m.contains(i) => i -> o(v,f.m(i)) })

      def map2d[R,S](f: Field[R])(default: R)(o: (T,R)=>S): Field[S] =
        Field(m.map { case (i,v) => i -> o(v,f.m.getOrElse(i,default)) })

      def map2u[R,S](f: Field[R])(dl: T, dr: R)(o: (T,R)=>S): Field[S] =
        Field((m.keys ++ f.m.keys).map { k => k -> o(m.getOrElse(k, dl), f.m.getOrElse(k, dr)) }.toMap)

      def fold[V>:T](z:V)(o: (V,V)=>V): V =
        restricted.m.values.fold(z)(o)

      def reduce[V>:T](o: (V,V)=>V): V =
        restricted.m.values.reduce(o)

      /**
       * Combines two fields using a condition on this field
       * @param condition a condition on places of this field
       * @param trueField values to use where the condition holds
       * @param falseField values to use where the condition doesn't hold
       * @return the combination of trueField and falseField
       */
      def conditionalMap[B](condition: T => Boolean)(trueField: Field[B])(falseField: Field[B]): Field[B] =
        map(condition).compose(trueField)(falseField)

      def minHood[V>:T](implicit ev: Bounded[V]): V  =
        fold[V](ev.top) { case (a, b) => ev.min(a, b) }

      def minHoodPlus[V>:T](implicit ev: Bounded[V]): Option[V] =
        withoutSelfOption(_.minHood(ev))

      def minimizing[V](toMinimize: T => V)(implicit ev: Bounded[V], idEv: Bounded[ID]): T =
        (this.map(toMinimize) zip this).toMap.fold[(ID, (V, T))]((idEv.top, (ev.top, this.m.values.head))){ case (a,b) =>
          if (ev.compare(a._2._1,b._2._1) < 0 || ev.compare(a._2._1,b._2._1) == 0 && idEv.compare(a._1,b._1) <= 0) a else b
        }._2._2

      def minimizingPlus[V](toMinimize: T => V)(implicit ev: Bounded[V], idEv: Bounded[ID]): Option[T] =
        withoutSelfOption(_.minimizing(toMinimize)(ev, idEv))

      def maxHood[V>:T](implicit ev: Bounded[V]): V  =
        fold[V](ev.bottom) { case (a, b) => ev.max(a, b) }

      def maxHoodPlus[V>:T](implicit ev: Bounded[V]): Option[V] =
        withoutSelfOption(_.maxHood(ev))

      def maximizing[V](toMaximize: T => V)(implicit ev: Bounded[V], idEv: Bounded[ID]): T =
        (this.map(toMaximize) zip this).toMap.fold[(ID, (V, T))]((idEv.bottom, (ev.bottom, this.m.values.head))){ case (a,b) =>
          if (ev.compare(a._2._1,b._2._1) > 0 || ev.compare(a._2._1,b._2._1) == 0 && idEv.compare(a._1,b._1) >= 0) a else b
        }._2._2

      def maximizingPlus[V](toMaximize: T => V)(implicit ev: Bounded[V], idEv: Bounded[ID]): Option[T] =
        withoutSelfOption(_.maximizing(toMaximize)(ev, idEv))

      def withoutSelf: Field[T] = Field[T](m - mid)

      private def withoutSelfOption[V](toDo: Field[T] => V): Option[V] =
        if (this.m.size > 1) {
          Some(toDo(withoutSelf))
        } else {
          None
        }

      /**
       * Zips together the value of two fields on the same area
       * @param other the field to zip with this
       * @tparam V the type of values of the other field
       * @return a field with both this field values and the other field values
       */
      def zip[V](other: Field[V]): Field[(T,V)] = {
        Field.ensureSameArea(this, other)
        Field(this.m.map{ case (id, ownValue) =>
          (id, (ownValue, other.m(id)))
        })
      }

      def toMap: Map[ID,T] = m

      override def toString: String = s"Field[$m]"
    }

    object Field {
      def apply[T](m: Map[ID,T]): Field[T] = new Field(m)

      private def ensureSameArea(fields: Field[_]*): Unit = {
        if (fields.size > 1) {
          val keys = fields.map(_.toMap.keySet)
          if (keys.tail.exists(_ != keys.head))
            throw new Exception("All fields should be defined over the same area")
        }
      }

      implicit def localToField[T](lv: T): Field[T] =
        nbrField(mid).map(_ => lv)

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

    implicit class BooleanField(f: Field[Boolean]) {
      /**
       * Combines two fields using the values of this field
       * @param trueField values to use where this field is true
       * @param falseField values to use where this field is false
       * @return the combination of trueField and falseField
       */
      def compose[A](trueField: Field[A])(falseField: Field[A]): Field[A] =
        f.zip(trueField.zip(falseField)).map{case (condition, (th, el)) =>
          if (condition) th else el
        }
    }
  }
}
