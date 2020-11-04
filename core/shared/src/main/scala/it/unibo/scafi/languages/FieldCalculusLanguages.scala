package it.unibo.scafi.languages

import it.unibo.scafi.core.ExecutionEnvironment

trait FieldCalculusLanguage {
  self: ExecutionEnvironment =>

  trait LanguageSemantics {
    self: ExecutionTemplate =>
  }
}

trait FieldCalculusLanguages
  extends scafistandard.Semantics with scafistandard.RichLanguage {
  self: ExecutionEnvironment =>

  trait ScafiStandardLanguage extends ScafiStandard_ConstructSemantics with ScafiStandard_Builtins {
    self: ExecutionTemplate =>
  }
}