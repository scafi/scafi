package it.unibo.scafi.languages.scafibase

import it.unibo.scafi.core.ExecutionEnvironment
import it.unibo.scafi.languages.ScafiLanguage

trait Semantics extends ScafiLanguage with Language {
  self: ExecutionEnvironment =>

  final case class Rep[A](index: Int) extends Slot
  final case class Scope[K](key: K) extends Slot


  trait ScafiBase_ConstructSemantics extends LanguageSemantics with LocalSensorReader with ScafiBase_Constructs {
    self: ExecutionTemplate =>

    override def mid(): ID = vm.self

    override def rep[A](init: =>A)(fun: (A) => A): A = {
      vm.nest(Rep[A](vm.index))(write = vm.unlessFoldingOnOthers) {
        vm.locally {
          fun(vm.previousRoundVal.getOrElse(init))
        }
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

    override def sense[A](name: CNAME): A = vm.localSense(name)

    override def readLocalSensor[A](name: CNAME): A = sense(name)
  }
}
