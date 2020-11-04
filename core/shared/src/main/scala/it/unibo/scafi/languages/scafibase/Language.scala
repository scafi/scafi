package it.unibo.scafi.languages.scafibase

import it.unibo.scafi.core.Core

trait Language {
  self: Core =>

  trait ScafiBase_Constructs {
    def rep[A](init: => A)(fun: (A) => A): A

    def aggregate[A](f: => A): A

    def align[K, V](key: K)(comp: K => V): V

    // Contextual, but foundational
    def mid(): ID

    def sense[A](name: CNAME): A
  }
}
