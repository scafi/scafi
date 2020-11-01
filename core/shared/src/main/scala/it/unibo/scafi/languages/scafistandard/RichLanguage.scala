package it.unibo.scafi.languages.scafistandard

import it.unibo.scafi.core.Core
import it.unibo.scafi.languages.TypesInfo.{Bounded, PartialOrderingWithGLB}

/**
 * This trait defines a component that extends LanguageStructure and requires to be "attached" to Core
 * It defines a trait with additional language mechanisms, in the form of certain builtins, and Ordering implicits
 *
 */
trait RichLanguage extends Language {
  self: Core =>

  trait ScafiStandard_Builtins {
    this: ScafiStandard_Constructs =>

    def branch[A](cond: => Boolean)(th: => A)(el: => A): A =
      mux(cond)(() => aggregate {
        th
      })(() => aggregate {
        el
      })()

    def mux[A](cond: Boolean)(th: A)(el: A): A = if (cond) th else el

    def minHoodLoc[A](default: A)(expr: => A)(implicit poglb: PartialOrderingWithGLB[A]): A =
      foldhood[A](default)((x, y) => if (poglb.equiv(x, y)) poglb.gle(x, y) else if (poglb.lt(x, y)) x else y) {
        expr
      }

    def minHoodPlusLoc[A](default: A)(expr: => A)(implicit poglb: PartialOrderingWithGLB[A]): A =
      foldhoodPlus[A](default)((x, y) => if (poglb.equiv(x, y)) poglb.gle(x, y) else if (poglb.lt(x, y)) x else y) {
        expr
      }

    def minHood[A](expr: => A)(implicit of: Bounded[A]): A = foldhood[A](of.top)((x, y) => of.min(x, y)) {
      expr
    }

    def maxHood[A](expr: => A)(implicit of: Bounded[A]): A = foldhood[A](of.bottom)((x, y) => of.max(x, y)) {
      expr
    }

    def foldhoodPlus[A](init: => A)(aggr: (A, A) => A)(expr: => A): A =
      foldhood(init)(aggr)(mux(mid() == nbr(mid())) {
        init
      } {
        expr
      })

    def minHoodPlus[A](expr: => A)(implicit of: Bounded[A]): A = foldhoodPlus[A](of.top)((x, y) => of.min(x, y)) {
      expr
    }

    def maxHoodPlus[A](expr: => A)(implicit of: Bounded[A]): A = foldhoodPlus[A](of.bottom)((x, y) => of.max(x, y)) {
      expr
    }
  }

}
