package it.unibo.scafi.languages

import it.unibo.scafi.core.ExecutionEnvironment

trait FieldCalculusLanguage {
  self: ExecutionEnvironment =>

  trait LanguageSemantics {
    self: ExecutionTemplate =>
  }
}

trait FieldCalculusLanguages
  extends scafistandard.Semantics with scafistandard.RichLanguage
  with scafifc.Semantics {
  self: ExecutionEnvironment =>

  trait ScafiStandardLanguage extends ScafiStandard_ConstructSemantics with ScafiStandard_Builtins {
    self: ExecutionTemplate =>
  }

  trait ScafiFCLanguage extends ScafiFC_ConstructSemantics with ScafiBase_Builtins {
    self: ExecutionTemplate =>
  }
}