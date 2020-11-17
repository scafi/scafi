package it.unibo.scafi.languages.scafistandard

import it.unibo.scafi.languages.ScafiLanguages
import it.unibo.scafi.languages.scafibase.{Semantics => BaseSemantics}

import scala.language.implicitConversions

trait Semantics extends Language with BaseSemantics {
  self: ScafiLanguages.Language =>

  final case class Nbr[A](index: Int) extends Slot
  final case class FoldHood[A](index: Int) extends Slot

  trait ScafiStandard_ConstructSemantics extends LanguageSemantics with NeighbourhoodSensorReader
    with ScafiStandard_Constructs with ScafiBase_ConstructSemantics {
    self: ExecutionTemplate =>

    override def foldhood[A](init: => A)(aggr: (A, A) => A)(expr: => A): A = {
      vm.nest(FoldHood[A](vm.index))(write = true) { // write export always for performance reason on nesting
        val nbrField = vm.alignedNeighbours
          .map(id => vm.foldedEval(expr)(id).getOrElse(vm.locally { init }))
        vm.isolate { nbrField.fold(vm.locally { init })((x,y) => aggr(x,y) ) }
      }
    }

    override def nbr[A](expr: => A): A =
      vm.nest(Nbr[A](vm.index))(write = vm.onlyWhenFoldingOnSelf) {
        vm.neighbourValOrExpr(expr)
      }

    override def nbrvar[A](name: CNAME): A = vm.neighbourSense(name)

    override type NbrSensorRead[A] = A
    override def readNbrSensor[A](name: CNAME): A = nbrvar(name)
    override def constantRead[A](value: A): A = value

    override implicit def withOps[A](base: NbrSensorRead[A]): StandardNbrSensorReadOps[A] =
      new StandardNbrSensorReadOps[A](base)

    class StandardNbrSensorReadOps[A](base: A) extends NbrSensorReadWithOps[A] {
      override def map[B](mappingFunction: A => B): B = mappingFunction(base)
    }
  }
}
