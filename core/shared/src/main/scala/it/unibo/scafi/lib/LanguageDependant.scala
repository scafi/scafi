package it.unibo.scafi.lib

import it.unibo.scafi.languages.ScafiLanguages
import it.unibo.scafi.languages.TypesInfo.Bounded

/**
 * Defines some language-dependant functions that can be used by libraries
 */
trait StdLib_LanguageDependant {
  selfcomp: StandardLibrary.Subcomponent with ScafiLanguages =>

  private[lib] trait LanguageDependant {
    self: NeighbourhoodSensorReader with FieldOperationsInterface =>

    def makeField[A](expr: => A): FieldType[A]

    def combineFields[A](condition: FieldType[Boolean])(th: FieldType[A])(el: FieldType[A]): FieldType[A]

    def combineWithRead[F, R, T](field: FieldType[F])(read: NbrSensorRead[R])(combine: (F, R) => T): FieldType[T]

    def constantField[A](expr: A): FieldType[A]

    def fieldsAnd(fields: FieldType[Boolean]*): FieldType[Boolean] =
      fields.tail.fold(fields.head){(a,b) =>
        mapField(zipFields(a, b)){case (x,y) => x && y}
      }
  }

  private[lib] trait LanguageDependant_ScafiStandard extends LanguageDependant {
    self: ScafiStandardLanguage =>

    override def makeField[A](expr: => A): A = nbr(expr)

    override def combineFields[A](condition: Boolean)(th: A)(el: A): A =
      mux(condition){th}{el}

    override def combineWithRead[F, R, T](field: F)(read: R)(combine: (F, R) => T): T =
      combine(field, read)

    override def constantField[A](expr: A): A = expr
  }

  private[lib] trait LanguageDependant_ScafiFC extends LanguageDependant {
    self: ScafiFCLanguage =>

    override def makeField[A](expr: => A): Field[A] = nbrField(expr)

    override def combineFields[A](condition: Field[Boolean])(th: Field[A])(el: Field[A]): Field[A] =
      condition.compose(th)(el)

    override def combineWithRead[F, R, T](field: Field[F])(read: Field[R])(combine: (F, R) => T): Field[T] =
      field.zip(read).map{case (f, r) => combine(f, r)}

    override def constantField[A](expr: A): Field[A] = nbrField{1}.map(_ => expr)
  }
}
