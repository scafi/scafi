package it.unibo.scafi.languages

import it.unibo.scafi.core.ExecutionEnvironment

trait ScafiLanguage {
  self: ExecutionEnvironment =>

  trait LanguageSemantics {
    self: ExecutionTemplate =>
  }
}

trait ScafiLanguages
  extends scafistandard.Semantics with scafistandard.RichLanguage
  with scafifc.Semantics {
  self: ExecutionEnvironment =>

  trait ScafiStandardLanguage extends ScafiBaseLanguage with ScafiStandard_ConstructSemantics with ScafiStandard_Builtins {
    self: ExecutionTemplate =>
  }

  trait ScafiFCLanguage extends ScafiBaseLanguage with  ScafiFC_ConstructSemantics {
    self: ExecutionTemplate =>
  }

  trait ScafiBaseLanguage extends ScafiBase_ConstructSemantics with ScafiBase_Builtins {
    self: ExecutionTemplate =>
  }
}