package it.unibo.scafi.lib

import it.unibo.scafi.languages.ScafiLanguages
import it.unibo.scafi.languages.TypesInfo.Bounded

/**
 * Defines some language-dependant functions that can be used by libraries
 */
trait StdLib_LanguageDependant {
  selfcomp: StandardLibrary.Subcomponent with ScafiLanguages =>

  private[lib] trait LanguageDependant {
    def neighbourhoodMin[A](expr: => A, includingSelf: Boolean = true)(implicit of: Bounded[A]): A
    def neighbourhoodMax[A](expr: => A, includingSelf: Boolean = true)(implicit of: Bounded[A]): A
    def neighbourhoodFold[A](init: => A, includingSelf: Boolean = true)(aggr: (A, A) => A)(expr: => A): A
  }

  private[lib] trait LanguageDependant_ScafiStandard extends LanguageDependant {
    self: ScafiStandardLanguage =>
    override def neighbourhoodMin[A](expr: => A, includingSelf: Boolean)(implicit of: Bounded[A]): A =
      if (includingSelf)
        minHood(nbr(expr))
      else
        minHoodPlus(nbr(expr))

    override def neighbourhoodMax[A](expr: => A, includingSelf: Boolean)(implicit of: Bounded[A]): A =
      if (includingSelf)
        maxHood(nbr(expr))
      else
        maxHoodPlus(nbr(expr))
    override def neighbourhoodFold[A](init: => A, includingSelf: Boolean)(aggr: (A, A) => A)(expr: => A): A =
      if (includingSelf)
        foldhood(init)(aggr)(nbr(expr))
      else
        foldhoodPlus(init)(aggr)(nbr(expr))
  }

  private[lib] trait LanguageDependant_ScafiFC extends LanguageDependant {
    self: ScafiFCLanguage =>

    override def neighbourhoodMin[A](expr: => A, includingSelf: Boolean)(implicit of: Bounded[A]): A =
      makeField(expr, includingSelf).minHood
    override def neighbourhoodMax[A](expr: => A, includingSelf: Boolean)(implicit of: Bounded[A]): A =
      makeField(expr, includingSelf).maxHood
    override def neighbourhoodFold[A](init: => A, includingSelf: Boolean)(aggr: (A, A) => A)(expr: => A): A =
      makeField(expr, includingSelf).fold(init)(aggr)

    private def makeField[A](expr: => A, includeSelf: Boolean): Field[A] = {
      val field = nbrField(expr)
      if (includeSelf)
        field
      else
        field.withoutSelf
    }
  }
}
