package it.unibo.scafi.languages.scafifc

import it.unibo.scafi.core.ExecutionEnvironment
import it.unibo.scafi.languages.FieldCalculusLanguage
import it.unibo.scafi.languages.scafibase.{Semantics => BaseSemantics}

import scala.language.implicitConversions

trait Semantics extends FieldCalculusLanguage with Language with BaseSemantics {
  self: ExecutionEnvironment =>

  final case class NbrField[A](index: Int) extends Slot
  final case class NbrFieldLocal[A](index: Int) extends Slot


  trait ScafiFC_ConstructSemantics extends LanguageSemantics with NeighbourhoodSensorReader
    with ScafiFC_Constructs with ScafiBase_ConstructSemantics {
    self: ExecutionTemplate =>

    override def nbrField[A](expr: => A): Field[A] =
      neighbouringField[A](
        vm.nest(NbrFieldLocal[A](vm.index))(write = vm.onlyWhenFoldingOnSelf) {
          vm.neighbourValOrExpr(expr)
        }
      )

    override def nbrFieldVar[A](name: CNAME): Field[A] =
      neighbouringField[A](vm.neighbourSense[A](name))

    //TODO based on foldhood in ScafiStandard, but maybe we can remove nesting?
    private def neighbouringField[A](expr: => A): Field[A] = vm.nest(NbrField[A](vm.index))(write = true){
      Field[A](
        vm.alignedNeighbours
          .flatMap(id => vm.foldedEval(expr)(id).map((id, _)))
          .toMap
      )
    }

    override type NbrSensorRead[A] = Field[A]
    override def readNbrSensor[A](name: CNAME): Field[A] = nbrFieldVar(name)

    override implicit def withOps[A](base: NbrSensorRead[A]): FCNbrSensorReadOps[A] =
      new FCNbrSensorReadOps[A](base)

    class FCNbrSensorReadOps[A](base: Field[A]) extends NbrSensorReadWithOps[A] {
      override def map[B](mappingFunction: A => B): Field[B] =
        base.map(mappingFunction)
    }
  }
}
