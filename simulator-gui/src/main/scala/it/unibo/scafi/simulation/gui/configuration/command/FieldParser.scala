package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandParser
import it.unibo.scafi.simulation.gui.configuration.command.FieldParser.{Field, FieldValue}

trait FieldParser extends CommandParser[Iterable[FieldValue]]{
  def fields : Set[Field]
}

object FieldParser {
  case class FieldValue(name: String, value : Any)

  case class Field(name: String, fieldType: FieldType)

  sealed trait FieldType

  final case object IntField extends FieldType

  final case object StringField extends FieldType

  final case class MultipleField(v: Set[Any]) extends FieldType
}