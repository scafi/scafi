package it.unibo.scafi.simulation.frontend.view.scalaFX.common

import java.util.function.UnaryOperator
import java.util.regex.Pattern
import javafx.scene.control.TextFormatter
import javafx.util.converter.IntegerStringConverter

import scalafx.scene.control.TextField

/**
  * a text field that accept only integer
  */
private [scalaFX] class IntField extends TextField {
  private val integerFilter = new  UnaryOperator[TextFormatter.Change] {
    private val DIGIT_PATTERN = Pattern.compile("\\d*")
    private val FIRST_DIGIT = Pattern.compile("[1-9]")
    override def apply(aT: TextFormatter.Change): TextFormatter.Change = {
      if (DIGIT_PATTERN.matcher(aT.getText).matches) {
        val text = aT.getControlText
        if(text.isEmpty && FIRST_DIGIT.matcher(aT.getText).matches) {
          aT
        } else if (text.nonEmpty) {
          aT
        }else {
          null
        }
      } else {
        null
      }
    }
  }
  private val formatter = new TextFormatter[Integer](new IntegerStringConverter,IntField.DefaultValue,integerFilter)
  this.setTextFormatter(formatter)
}

object IntField {
  val DefaultValue = 0
}
