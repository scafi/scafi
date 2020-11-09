package it.unibo.scafi.lib

import it.unibo.scafi.languages.ScafiLanguages
import it.unibo.scafi.languages.TypesInfo.Bounded

/**
 * Defines some language-dependant functions that can be used by libraries
 */
trait StdLib_LanguageDependant {
  selfcomp: StandardLibrary.Subcomponent with ScafiLanguages =>

  private[lib] trait LanguageDependant {
    def neighbourhoodMin[A](expr: => A)(implicit of: Bounded[A]): A
    def neighbourhoodMax[A](expr: => A)(implicit of: Bounded[A]): A
    def neighbourhoodFold[A](init: => A)(aggr: (A, A) => A)(expr: => A): A
  }

  private[lib] trait LanguageDependant_ScafiStandard extends LanguageDependant {
    self: ScafiStandardLanguage =>
    override def neighbourhoodMin[A](expr: => A)(implicit of: Bounded[A]): A =
      minHood(nbr(expr))
    override def neighbourhoodMax[A](expr: => A)(implicit of: Bounded[A]): A =
      maxHood(nbr(expr))
    override def neighbourhoodFold[A](init: => A)(aggr: (A, A) => A)(expr: => A): A =
      foldhood(init)(aggr)(nbr(expr))
  }

  private[lib] trait LanguageDependant_ScafiFC extends LanguageDependant {
    self: ScafiFCLanguage =>

    override def neighbourhoodMin[A](expr: => A)(implicit of: Bounded[A]): A =
      nbrField(expr).minHood
    override def neighbourhoodMax[A](expr: => A)(implicit of: Bounded[A]): A =
      nbrField(expr).maxHood
    override def neighbourhoodFold[A](init: => A)(aggr: (A, A) => A)(expr: => A): A =
      nbrField(expr).fold(init)(aggr)
  }
}
