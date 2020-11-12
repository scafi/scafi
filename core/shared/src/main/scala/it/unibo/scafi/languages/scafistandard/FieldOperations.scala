package it.unibo.scafi.languages.scafistandard

import it.unibo.scafi.core.Core
import it.unibo.scafi.languages.Languages_FieldOperations

trait FieldOperations extends Languages_FieldOperations {
  self: Core with Language with RichLanguage =>

  trait ScafiStandard_FieldOperations extends FieldOperationsInterface {
    self: ScafiStandard_Constructs with ScafiStandard_Builtins =>

    override type FieldType[A] = A

    override def mapField[A, B](field: => A)(mappingFunction: A => B): B =
      mappingFunction(field)

    override def zipFields[A, B](fieldA: => A, fieldB: => B): (A, B) =
      (fieldA, fieldB)

    override def zipFieldWithID[T](field: => T): (ID, T) =
      (nbr{ mid() }, field)

    override def includingSelf: IncludingSelfInterface =
      IncludingSelf

    override def excludingSelf: ExcludingSelfInterface =
      ExcludingSelf

    object IncludingSelf extends IncludingSelfInterface {
      override def foldhoodTemplate[T](init: T)(acc: (T, T) => T)(field: => T): T =
        foldhood(init)(acc)(field)
    }

    object ExcludingSelf extends ExcludingSelfInterface {
      override def foldhoodTemplate[T](init: T)(acc: (T, T) => T)(field: => T): T =
        foldhoodPlus(init)(acc)(field)
    }
  }

}
