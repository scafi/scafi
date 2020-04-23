package it.unibo.scafi.js

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, _}
import WebIncarnation._
import it.unibo.scafi.incarnations.BasicSimulationIncarnation

import scala.collection.mutable.ArrayBuffer

/**
  * p = new GradientProgram();
  * s = scafi.simulator(p);
  * c = scafi.context("0", {}, {"source":true}, { "nbrRange": { "0": 0.0, "1": 1.0 } });
  * e = scafi.round(p, c)
  * e.toString()
  */
@JSExportTopLevel("scafi")
object ScafiFacade {

  @JSExport
  def context(id: ID, exports: js.Dictionary[EXPORT], lsens: js.Dictionary[Any], nbsens: js.Dictionary[js.Dictionary[Any]]): CONTEXT =
    factory.context(id, exports.toMap, lsens.toMap, nbsens.mapValues(_.toMap).toMap)

  @JSExport
  def export(paths: js.Array[(Path,Any)]): EXPORT =
    factory.`export`(paths.toArray:_*)

  @JSExport
  def path(slots: js.Array[Slot]): Path =
    factory.path(slots.toArray:_*)

  @JSExport def slotNbr(index: Int) = Nbr[Any](index)
  @JSExport def slotRep(index: Int) = Rep[Any](index)
  @JSExport def slotFold(index: Int) = FoldHood[Any](index)
  @JSExport def slotAlign(index: Int) = Scope[Any](index)
  @JSExport def slotFunCall(index: Int, funId: String) = FunCall[Any](index,funId)

  @JSExport
  def simulator(program: AggregateProgram): NETWORK = {
    val nodes = ArrayBuffer((0 to 100):_*).map(_.toString)
    val net = simulatorFactory.basicSimulator(nodes)
    net
  }

  @JSExport
  def round(program: AggregateProgram, context: CONTEXT): EXPORT =
    program.round(context)


  @JSExport
  def round(program: AggregateProgram, context: CONTEXT, expr: js.Function0[Any]): EXPORT = {
    val scalaExpr: () => Any = expr
    program.round(context, scalaExpr())
  }

  @JSExportTopLevel("GradientProgram")
  // NOTE: trying to extend a non-JS class exported from Scala.js is undefined behaviour
  class GradientProgram extends AggregateProgram with StandardSensors {
    override def main(): Any = rep(Double.PositiveInfinity){ case g =>
      mux(sense[Boolean]("source")){ 0.0 }{
        minHoodPlus { nbr(g) + nbrRange() }
      }
    }
  }
}

/**
  * dsl = new ScafiDsl(); f = () => { with(dsl){ return rep(0, (d) => d+1) } }; e1 = dsl.run(scafi.context("1",[],[],[]), f);
  * e2 = dsl.run(scafi.context("1", { 1: e1 } ,[],[]), f)
  * e3 = eval("dsl.run(scafi.context('1', { 1: e2 } ,[],[]), f)")
  */
@JSExportTopLevel("ScafiDsl")
@JSExportAll
class ScafiDsl(val p: AggregateProgram = new AggregateProgram {
  override def main(): Any =
    throw new IllegalStateException("This method should not be called as the aggregate program only works as API provider.")
}) extends (CONTEXT => EXPORT) {
  var programExpression: js.Function0[Any] = _

  def run(c: CONTEXT, f: js.Function0[Any]): EXPORT = {
    val fScala: () => Any = f
    p.round(c, fScala())
  }

  @JSExport("apply")
  override def apply(ctx: CONTEXT): EXPORT = {
    run(ctx, programExpression)
  }

  def nbr[A](expr: js.Function0[A]): A = {
    val exprFun: () => A = expr
    p.nbr(exprFun())
  }

  def mux[A](cond: Boolean, ifTrue: A, ifFalse: A) =
    p.mux(cond){ ifTrue }{ ifFalse }

  // TODO: init should by by-name
  def rep[A](init: A, fun: js.Function1[A,A]): A = {
    val funS: A => A = fun
    p.rep(init)(funS)
  }

  def foldhood[A](init: js.Function0[A], aggr: js.Function2[A,A,A], expr: js.Function0[A]): A = {
    val initS: () => A = init
    val aggrS: (A,A) => A = aggr
    val exprS: () => A = expr
    p.foldhood(initS())(aggrS)(exprS())
  }

  def foldhoodPlus[A](init: js.Function0[A], aggr: js.Function2[A,A,A], expr: js.Function0[A]): A = {
    val initS: () => A = init
    val aggrS: (A,A) => A = aggr
    val exprS: () => A = expr
    p.foldhoodPlus(initS())(aggrS)(exprS())
  }

  def aggregate[A](f: js.Function0[A]): A = {
    val scalaF: () => A = f
    p.aggregate(scalaF())
  }

  def align[K, V](key: K, comp: K => V): V = p.align(key)(comp)

  def mid(): String = p.mid()

  def sense[A](name: String): A = p.sense(name)

  def nbrvar[A](name: String): A = p.nbrvar(name)
}

class FromJavaScriptFunctionToAggregateProgram(jsf: js.Function1[AggregateProgram,Any]) extends AggregateProgram {
  override def main(): Any = {
    val f: (AggregateProgram) => Any = jsf
    f(this)
  }
}