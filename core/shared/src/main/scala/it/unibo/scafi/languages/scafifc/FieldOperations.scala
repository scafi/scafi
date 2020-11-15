package it.unibo.scafi.languages.scafifc

import it.unibo.scafi.core.Core
import it.unibo.scafi.languages.Languages_FieldOperations

trait FieldOperations extends Languages_FieldOperations {
  self: Core with Language =>

  trait ScafiFC_FieldOperations extends FieldOperationsInterface {
    self: ScafiFC_Constructs =>
    override type FieldType[T] = Field[T]

    override def mapField[A, B](field: => Field[A])(mappingFunction: A => B): Field[B] =
      field.map(mappingFunction)

    override def zipFields[A, B](fieldA: => Field[A], fieldB: => Field[B]): Field[(A, B)] =
      fieldA.zip(fieldB)

    override def zipFieldWithID[T](field: => Field[T]): Field[(ID, T)] =
      Field(field.toMap.map(x => (x._1, (x._1, x._2))))

    override def includingSelf: IncludingSelfInterface =
      IncludingSelf

    override def excludingSelf: ExcludingSelfInterface =
      ExcludingSelf

    object IncludingSelf extends IncludingSelfInterface {
      override def foldhoodTemplate[T](init: T)(acc: (T, T) => T)(field: => Field[T]): T =
        field.fold(init)(acc)
    }

    object ExcludingSelf extends ExcludingSelfInterface {
      override def foldhoodTemplate[T](init: T)(acc: (T, T) => T)(field: => Field[T]): T =
        field.withoutSelf.fold(init)(acc)
    }
  }
}
