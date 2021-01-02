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

    // STEP 1) CONSIDER HOW REP/NBR ARE IMPLEMENTED
    /*
    override def rep[A](init: =>A)(fun: (A) => A): A = {
      vm.nest(Rep[A](vm.index))(write = vm.unlessFoldingOnOthers) {
        vm.locally {
          fun(vm.previousRoundVal.getOrElse(init))
        }
      }
    }

    override def foldhood[A](init: => A)(aggr: (A, A) => A)(expr: => A): A = {
      vm.nest(FoldHood[A](vm.index))(write = true) { // write export always for performance reason on nesting
        val nbrField = vm.alignedNeighbours
          .map(id => vm.foldedEval(expr)(id).getOrElse(vm.locally { init }))
        vm.isolate { nbrField.fold(vm.locally { init })((x,y) => aggr(x,y) ) }
      }
    }

    override def nbr[A](expr: => A): A =
      vm.nest(Nbr[A](vm.index))(write = vm.onlyWhenFoldingOnSelf) {
        vm.neighbour match {
          case Some(nbr) if (nbr != vm.self) => vm.neighbourVal
          case _  => expr
        }
      }
    */

    // STEP 1B: POSSIBLY CONSIDER ALSO THE IMPL OF SHARE
    /*
    def nbrpath[A](path: Path)(expr: => A): A = {
      val tvm = vm.asInstanceOf[RoundVMImpl]
      vm.nest(Nbr[A](vm.index))(vm.neighbour.map(_ == vm.self).getOrElse(false)) {
        vm.neighbour match {
          case Some(nbr) if (nbr != vm.self) => tvm.context
            .readSlot[A](vm.neighbour.get, path)
            .getOrElse(throw new OutOfDomainException(tvm.context.selfId, vm.neighbour.get, path))
          case _ => expr
        }
      }
    }

    def share[A](init: => A)(f: (A, () => A) => A): A = {
      rep(init){ oldRep =>
        val repp = vm.asInstanceOf[RoundVMImpl].status.path
        f(oldRep, () => nbrpath(repp)(oldRep))
      }
    }
    */

    // STEP 1C: SLOTS ARE SEALED, SO, UNLESS WORKAROUNDS ARE USED, EXTEND THOSE IN `SEMANTICS`
    private val EXCHANGE_SLOT = Scope("exchange")

    // STEP 2: IMPL exchange
    def exchange[A](init: EdgeField[A])(f: EdgeField[A] => EdgeField[A]): EdgeField[A] = {
      ???
    }

    def exchangeFull[A](init: EdgeField[A])(f: ExchangeParams[A] => EdgeField[A]): EdgeField[A] = {
      /*
      branch(true) {
        val thisPath = vm.asInstanceOf[RoundVMImpl].status.path
        val exchangePath = thisPath.push(EXCHANGE_SLOT)

        val params: ExchangeParams[A] = ExchangeParams(
          old = ???,
          neigh = ???)
        val outEdgeField = f(params)
        outEdgeField
      }{ init }
       */

      // EdgeField[A](includingSelf.reifyField(nbr(e)), )
      ???
    }

    // STEP 3: IMPL fnbr (and the other constructs) IN TERMS OF EXCHANGE?
    def fnbr[A](e: => A): EdgeField[A] =
      //EdgeField[A](e, includingSelf.reifyField(nbr(e)))
      exchangeFull((e,e))(p => (e, p.neigh._1))._2

    def fsns[A](e: => A, defaultA: A): EdgeField[A] =
      EdgeField[A](includingSelf.reifyField(e), defaultA)

    /**
      * Basic Field type
      * @param m map from devices to corresponding values
      * @tparam T type of field values
      */
    class EdgeField[T](private[EdgeField] val m: Map[ID,T], override val default: T) extends Builtins.Defaultable[T] {
      implicit val defaultable: Defaultable[T] = this

      def defSubs(defaultValue: T): EdgeField[T] =
        EdgeField[T](this.m, defaultValue)

      def selfSubs(selfValue: T): EdgeField[T] =
        EdgeField[T](this.m ++ Map(mid -> selfValue), this.default)

      def restricted: EdgeField[T] = {
        val alignedField = fnbr{1}
        EdgeField(this.m.filter(el => alignedField.m.contains(el._1)), this.default)
      }

      def map[R](o: T=>R): EdgeField[R] =
        EdgeField(this.m.mapValues(o), o(default))

      def map[R](defaultr: R, o: T=>R): EdgeField[R] = {
        EdgeField(this.m.mapValues(o).toMap, defaultr)
      }

      def map2[R,S](f: EdgeField[R])(o: (T,R)=>S): EdgeField[S] =
        EdgeField(this.m.map { case (i,v) => i -> o(v,f.m(i)) }, o(default, f.default))

      def map2i[R,S](f: EdgeField[R])(o: (T,R)=>S): EdgeField[S] =
        EdgeField(restricted.m.collect { case (i,v) if f.m.contains(i) => i -> o(v,f.m(i)) }, o(default, f.default))

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

      def toMap: Map[ID,T] = this.m

      override def toString: String = s"Field[$m]"
    }

    object EdgeField {

      def apply[T](m: Map[ID,T])(implicit defaultable: Builtins.Defaultable[T]): EdgeField[T] =
        apply(m, defaultable.default)

      def apply[T](m: Map[ID,T], defaultValue: T): EdgeField[T] =
        new EdgeField(m, defaultValue)

      implicit def localToField[T](lv: T): EdgeField[T] =
        EdgeField(Map.empty, lv)

      implicit def fieldToLocal[T](fv: EdgeField[T]): T =
        fv.m.getOrElse(mid, fv.default)
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
