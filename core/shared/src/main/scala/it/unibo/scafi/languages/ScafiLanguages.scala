package it.unibo.scafi.languages

import it.unibo.scafi.core.ExecutionEnvironment

trait ScafiLanguage {
  self: ExecutionEnvironment =>

  trait LanguageSemantics {
    self: ExecutionTemplate =>
  }
}

trait ScafiLanguages
  extends scafistandard.Semantics with scafistandard.RichLanguage with scafistandard.FieldOperations
  with scafifc.Semantics with scafifc.FieldOperations {
  self: ExecutionEnvironment =>

  trait ScafiStandardLanguage
    extends ScafiBaseLanguage
    with ScafiStandard_ConstructSemantics
    with ScafiStandard_Builtins
    with ScafiStandard_FieldOperations {
    self: ExecutionTemplate =>
  }

  trait ScafiFCLanguage
    extends ScafiBaseLanguage
    with ScafiFC_ConstructSemantics
    with ScafiFC_FieldOperations {
    self: ExecutionTemplate =>
  }

  trait ScafiBaseLanguage extends ScafiBase_ConstructSemantics with ScafiBase_Builtins {
    self: ExecutionTemplate =>
  }
}