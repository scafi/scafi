package it.unibo.scafi.languages.scafistandard

import it.unibo.scafi.core.Core
import it.unibo.scafi.languages.TypesInfo.{Bounded, PartialOrderingWithGLB}
import it.unibo.scafi.languages.scafibase.{RichLanguage => BaseRichLanguage}

/**
 * This trait defines a component that extends LanguageStructure and requires to be "attached" to Core
 * It defines a trait with additional language mechanisms, in the form of certain builtins, and Ordering implicits
 *
 */
trait RichLanguage extends Language with BaseRichLanguage {
  self: Core with FieldOperations =>

  trait ScafiStandard_Builtins extends ScafiBase_Builtins {
    this: ScafiStandard_Constructs with ScafiStandard_FieldOperations =>

    //slightly different from the one in FieldOperations
    def minHoodLoc[A](default: A)(expr: => A)(implicit poglb: PartialOrderingWithGLB[A]): A =
      foldhood[A](default)((x, y) => if (poglb.equiv(x, y)) poglb.gle(x, y) else if (poglb.lt(x, y)) x else y) {
        expr
      }

    //slightly different from the one in FieldOperations
    def minHoodPlusLoc[A](default: A)(expr: => A)(implicit poglb: PartialOrderingWithGLB[A]): A =
      foldhoodPlus[A](default)((x, y) => if (poglb.equiv(x, y)) poglb.gle(x, y) else if (poglb.lt(x, y)) x else y) {
        expr
      }

    def minHood[A](expr: => A)(implicit of: Bounded[A]): A =
      includingSelf.minHood(expr)

    def maxHood[A](expr: => A)(implicit of: Bounded[A]): A =
      includingSelf.maxHood(expr)

    def minHoodPlus[A](expr: => A)(implicit of: Bounded[A]): A =
      excludingSelf.minHoodPlus(expr)

    def maxHoodPlus[A](expr: => A)(implicit of: Bounded[A]): A =
      excludingSelf.maxHoodPlus(expr)

    def foldhoodPlus[A](init: => A)(aggr: (A, A) => A)(expr: => A): A =
      foldhood(init)(aggr)(mux(mid() == nbr(mid())) {
        init
      } {
        expr
      })
  }
}
