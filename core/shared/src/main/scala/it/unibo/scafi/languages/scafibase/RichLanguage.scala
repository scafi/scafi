package it.unibo.scafi.languages.scafibase

import it.unibo.scafi.core.Core

trait RichLanguage extends Language {
  self: Core =>

  trait ScafiBase_Builtins {
    this: ScafiBase_Constructs =>

    def branch[A](cond: => Boolean)(th: => A)(el: => A): A =
      mux(cond)(() => aggregate {
        th
      })(() => aggregate {
        el
      })()

    def mux[A](cond: Boolean)(th: A)(el: A): A = if (cond) th else el
  }

}
