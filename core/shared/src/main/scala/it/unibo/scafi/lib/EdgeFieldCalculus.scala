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
    self: FieldCalculusSyntax with ExecutionTemplate =>

    case class ExchangeParams[T](old: EdgeField[T], neigh: EdgeField[T])
    object ExchangeParams {
      implicit def fromTuple[T](tuple: (EdgeField[T], EdgeField[T])): ExchangeParams[T] =
        ExchangeParams(tuple._1, tuple._2)
    }

    private val EXCHANGE_SLOT_NAME = Scope("exchange")
    private val EXCHANGE_SLOT = Scope(EXCHANGE_SLOT_NAME)

    def exchange[A](init: EdgeField[A])(f: EdgeField[A] => EdgeField[A]): EdgeField[A] = {
      val rvm = vm.asInstanceOf[RoundVMImpl]
      vm.nest(EXCHANGE_SLOT.copy(index = vm.index))(write = true) {
        val nbrs = vm.alignedNeighbours
        def nbrEdgeValue[A](id: ID): Option[A] = rvm.context.readSlot(id, rvm.status.path)
        val inputEdgeField = new EdgeField[A](
          nbrs.map(nbrId => { // create the edgevalue by getting the contributions from all the neighbours (`nbrs`)
            nbrId -> nbrEdgeValue[EdgeField[A]](nbrId) // get edgevalue received from device `nbrId`
              .getOrElse(init).m // if there is not an aligned export from `nbrId`, use `init`
              .getOrElse(vm.self, // from the neighbour's edgevalue, get the value sent to the current device (`self`)
                nbrEdgeValue[EdgeField[A]](nbrId).getOrElse(init).default // otherwise, provide the default
              )
          }).toMap,
          init.default)
        val outputEdgeField = f(inputEdgeField)
        //println(s"$mid ${rvm.status.path} \n ${rvm.context} \n \t\t -> apply exchange to $inputEdgeField ==> $outputEdgeField")
        outputEdgeField
      }
    }

    def exchangeFull[A](init: EdgeField[A])(f: ExchangeParams[A] => EdgeField[A]): EdgeField[A] = {
      val rvm = vm.asInstanceOf[RoundVMImpl]
      vm.nest(EXCHANGE_SLOT.copy(index = vm.index))(write = true) {
        val nbrs = vm.alignedNeighbours
        def nbrEdgeValue[A](id: ID): Option[A] = rvm.context.readSlot(id, rvm.status.path)
        val oldEdgeField = rvm.context.readSlot(vm.self, rvm.status.path).getOrElse(init)
        val inputEdgeField = new EdgeField[A](
          nbrs.map(nbrId =>
            nbrId -> nbrEdgeValue[EdgeField[A]](nbrId)
              .getOrElse(init).m
              .getOrElse(vm.self, nbrEdgeValue[EdgeField[A]](nbrId).getOrElse(init).default)
          ).toMap,
          init.default)
        val outputEdgeField = f(ExchangeParams(oldEdgeField, inputEdgeField))
        // println(s"$mid -> apply exchange to $inputEdgeField ==> $outputEdgeField")
        outputEdgeField
      }
    }

    def defSubs[T](ef: EdgeField[T], defaultValue: T): EdgeField[T] =
      EdgeField(vm.alignedNeighbours().map(id => id -> ef.m.getOrElse(id, ef.default)).toMap, defaultValue)

    def selfSubs[T](ef: EdgeField[T], selfValue: T): EdgeField[T] =
      EdgeField(ef.m ++ Map[ID,T](mid -> selfValue), ef.default)

    def nbrByExchange[A](e: => EdgeField[A]): EdgeField[A] =
      exchange[(A,A)](e.map2(e)((_,_)))(n => n.map2(e){ case (n,e) => (e, n._1) }).map(_._2) // NB: exchange(..)(..)._2 would compile but doesn't work


    def nbrLocalByExchange[A](e: => A): EdgeField[A] =
      exchange[(A,EdgeField[A])]((e,EdgeField.localToField(e)))(n => (e, n.map(_._1)))._2

    def repByExchange[A](init: => A)(f: (A) => A): A =
      exchangeFull(init)(p => f(p.old))

    def shareByExchange[A](init: A)(f: A => A): A =
      exchange(init)(n => f(n))
      // exchangeFull(init)(p => f(p.neigh))

    def branchByExchange[A](cond: EdgeField[Boolean])(th: => EdgeField[A])(el: => EdgeField[A]): EdgeField[A] =
      mux(cond)(() => aggregate{ th })(() => aggregate{ el })()

    def fsns[A](e: => A, defaultA: A): EdgeField[A] =
      EdgeField[A](includingSelf.reifyField(e), defaultA)

    def pair[A,B](a: EdgeField[A], b: EdgeField[B]): EdgeField[(A,B)] =
      a.map2(b)((_,_))

    def muxEdgeField[A](c: EdgeField[Boolean])(thEF: => EdgeField[A])(elEF: => EdgeField[A]): EdgeField[A] = {
      val (th, el) = (thEF, elEF)
      c.restricted.mapWithId((optId,v) => optId.map(id => if(v) th.m.getOrElse(id, th.default) else el.m.getOrElse(id, el.default))
        .getOrElse(if(v) th.default else el.default)
      )
    }

    /**
      * Basic Field type
      * @param m map from devices to corresponding values
      * @tparam T type of field values
      */
    class EdgeField[T](private[lib] val m: Map[ID,T], override val default: T) extends Builtins.Defaultable[T] {
      implicit val defaultable: Defaultable[T] = this

      def defSubs(defaultValue: T): EdgeField[T] =
        EdgeField[T](this.m, defaultValue)

      def selfSubs(selfValue: T): EdgeField[T] =
        EdgeField[T](this.m ++ Map[ID,T](mid -> selfValue), this.default)

      def restricted: EdgeField[T] = { // TODO: contains on list {
        val nbrsSet = vm.alignedNeighbours().toSet
        EdgeField(this.m.filter(el => nbrsSet.contains(el._1)), this.default)
      }

      def flatMap[R](f: T => EdgeField[R]): EdgeField[R] =
        EdgeField(this.m.map { case (id,v) => id -> {
          val newEdgeField = f(v)
          newEdgeField.m.getOrElse(id, newEdgeField.default)
        } }, f(this.default))

      def mapWithId[R](o: (Option[ID],T)=>R): EdgeField[R] =
        EdgeField(this.m.map(tp => tp._1 -> o(Some(tp._1), tp._2)), o(None, default))

      def map[R](o: T=>R): EdgeField[R] =
        EdgeField(this.m.map(tp => tp._1 -> o(tp._2)), o(default))

      def map[R](defaultr: R, o: T=>R): EdgeField[R] =
        EdgeField(this.m.map(tp => tp._1 -> o(tp._2)), defaultr)

      def map2[R,S](f: EdgeField[R])(o: (T,R)=>S): EdgeField[S] =
        EdgeField((this.m.keySet ++ f.m.keySet).map(i => i -> o(this.m.getOrElse(i, this.default), f.m.getOrElse(i, f.default)) ).toMap,
          o(default, f.default))

      def map2l[R,S](f: EdgeField[R])(o: (T,R)=>S): EdgeField[S] =
        EdgeField(this.m.map { case (i,v) => i -> o(v, f.m.getOrElse(i, f.default)) }, o(default, f.default))

      def map2r[R,S](f: EdgeField[R])(o: (T,R)=>S): EdgeField[S] =
        EdgeField(f.m.map { case (i,v) => i -> o(this.m.getOrElse(i, this.default), v) }, o(default, f.default))

      def map2i[R,S](f: EdgeField[R])(o: (T,R)=>S): EdgeField[S] =
        map2u(f)(this.default, f.default)(o)

      def map2d[R,S](f: EdgeField[R])(defaultr: R)(o: (T,R)=>S): EdgeField[S] =
        EdgeField(this.m.map { case (i,v) => i -> o(v,f.m.getOrElse(i,defaultr)) }, o(default, defaultr))

      def map2u[R,S](f: EdgeField[R])(dl: T, dr: R)(o: (T,R)=>S): EdgeField[S] =
        EdgeField((this.m.keys ++ f.m.keys).map { k => k -> o(this.m.getOrElse(k, dl), f.m.getOrElse(k, dr)) }.toMap,
          o(dl, dr))

      def fold[V>:T](z:V)(o: (V,V)=>V): V =
        restricted.m.values.fold(z)(o)

      def reduce[V>:T](o: (V,V)=>V): V =
        restricted.m.values.reduce(o)

      def minHood[V>:T](implicit ev: Bounded[V]): V  =
        fold[V](ev.top) { case (a, b) => ev.min(a, b) }

      def minHoodPlus[V>:T](implicit ev: Bounded[V]): V =
        withoutSelf.minHood(ev)

      def withoutSelf: EdgeField[T] = EdgeField[T](this.m - mid, this.default)

      def toLocal: T = EdgeField.fieldToLocal(this)

      def toMap: Map[ID,T] = this.m

      override def toString: String = s"EdgeField[default=$default, exceptions=$m]"
    }

    object EdgeField {

      def apply[T](m: Map[ID,T])(implicit defaultable: Builtins.Defaultable[T]): EdgeField[T] =
        apply(m, defaultable.default)

      def apply[T](m: Map[ID,T], defaultValue: T): EdgeField[T] =
        new EdgeField(m, defaultValue)


      implicit def localToField[T](lv: T): EdgeField[T] =
        EdgeField(Map.empty, lv)

      implicit def fieldToLocal[T](fv: EdgeField[T]): T = {
        fv.m.getOrElse(mid, fv.default)
      }

      /*
      implicit def tupleToFieldOfTuples[T,U](tp: (EdgeField[T],EdgeField[U])): EdgeField[(T,U)] =
        tp._1.map2(tp._2)((_,_))
       */
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

      def +(f2: EdgeField[T]): EdgeField[T] = f.map2(f2)(ev.plus(_,_))
      def -(f2: EdgeField[T]): EdgeField[T] = f.map2(f2)(ev.minus(_,_))
      def *(f2: EdgeField[T]): EdgeField[T] = f.map2(f2)(ev.times(_,_))
      def +/[U](lv: U)(implicit uev: Numeric[U]): EdgeField[Double] = f.map[Double](ev.toDouble(_:T) + uev.toDouble(lv))

      def foldSum(init: T): EdgeField[T] = f.fold(init)(ev.plus)
      def foldSum(): EdgeField[T] = foldSum(ev.zero)

      def <(g: EdgeField[T]): EdgeField[Boolean] = f.map2(g)(ev.lt(_, _))
      def <=(g: EdgeField[T]): EdgeField[Boolean] = f.map2(g)(ev.lteq(_, _))
      def >(g: EdgeField[T]): EdgeField[Boolean] = f.map2(g)(ev.gt(_, _))
      def >=(g: EdgeField[T]): EdgeField[Boolean] = f.map2(g)(ev.gteq(_, _))
    }

    implicit class BooleanField(f: EdgeField[Boolean]) {
      def &&(g: EdgeField[Boolean]): EdgeField[Boolean] = f.map2(g)(_ && _)
      def ||(g: EdgeField[Boolean]): EdgeField[Boolean] = f.map2(g)(_ || _)
    }

    implicit class FieldOfTuples[A, B](f: EdgeField[Tuple2[A, B]]) {
      def _1: EdgeField[A] = f.map(_._1)

      def _2: EdgeField[B] = f.map(_._2)
    }
  }

}
