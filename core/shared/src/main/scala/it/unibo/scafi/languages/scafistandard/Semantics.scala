package it.unibo.scafi.languages.scafistandard

import it.unibo.scafi.core.{Core, ExecutionEnvironment}
import it.unibo.scafi.languages.FieldCalculusLanguage

trait Semantics extends FieldCalculusLanguage with Language {
  self: ExecutionEnvironment =>

  trait ScafiStandard_ConstructSemantics extends LanguageSemantics with ScafiStandard_Constructs {
    self: ExecutionTemplate =>

    override def mid(): ID = vm.self

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

    override def aggregate[T](f: => T): T =
      vm.nest(FunCall[T](vm.index, vm.elicitAggregateFunctionTag()))(write = vm.unlessFoldingOnOthers) {
        vm.neighbour match {
          case Some(nbr) if nbr != vm.self => vm.loadFunction()()
          case Some(nbr) if nbr == vm.self => vm.saveFunction(f); f
          case _ => f
        }
      }

    override def align[K,V](key: K)(proc: K => V): V =
      vm.nest[V](Scope[K](key))(write = vm.unlessFoldingOnOthers, inc = false){
        proc(key)
      }

    def sense[A](name: CNAME): A = vm.localSense(name)

    def nbrvar[A](name: CNAME): A = vm.neighbourSense(name)
  }
}
