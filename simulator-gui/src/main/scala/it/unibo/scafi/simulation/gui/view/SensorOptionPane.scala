package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Controller
import javax.swing._
import java.awt._
import java.awt.event.ActionEvent

import it.unibo.scafi.simulation.gui.utility.Utils

/**
  * Created by Varini on 12/11/16.
  * Converted/refactored to Scala by Casadei on 04/02/17
  */
class SensorOptionPane(title: String) extends JDialog {
  final private val sensorsChoice: JComboBox[String] = new JComboBox[String]
  final private val operators: JComboBox[String] = new JComboBox[String]
  final private val controller: Controller = Controller.getIstance

  val sensorNameField: JTextField = new JTextField(20)
  val valueField: JTextField = new JTextField(10)
  val enter: JButton = new JButton("OK")
  val cancel: JButton = new JButton("Cancel")

  setTitle(title)
  setSize(600, 300)
  this.setLocationRelativeTo(null)

  enter.addActionListener((e:ActionEvent) => {
    /*
    if (operators.getItemCount() > 1) {
      controller.checkSensor(sensorsChoice.getSelectedItem().toString,
        operators.getSelectedItem().toString,
        valueField.getText()
      )
    } else {
      controller.setSensor(sensorsChoice.getSelectedItem().toString,
        valueField.getText()
      )
    }
    */
    val sensorName = sensorNameField.getText

    controller.setSensor(sensorName, Utils.parseSensorValue(valueField.getText))

    this.dispose()
  })
  cancel.addActionListener((e:ActionEvent) => this.dispose())
  val panel: JPanel = new JPanel(new GridBagLayout)
  val c: GridBagConstraints = new GridBagConstraints
  c.insets = new Insets(5, 5, 5, 15)
  c.fill = GridBagConstraints.HORIZONTAL
  c.gridx = 0
  c.gridy = 0
  //panel.add(sensorsChoice, c)
  panel.add(sensorNameField, c)
  c.anchor = GridBagConstraints.CENTER
  c.gridx = 1
  c.gridy = 0
  panel.add(operators, c)
  c.fill = GridBagConstraints.HORIZONTAL
  c.gridx = 2
  c.gridy = 0
  panel.add(valueField, c)
  c.insets = new Insets(10, 0, 0, 0)
  c.fill = GridBagConstraints.NONE
  c.anchor = GridBagConstraints.LINE_END
  c.gridx = 2
  c.gridy = 1
  panel.add(cancel, c)
  c.anchor = GridBagConstraints.LINE_START //bottom of space
  c.gridx = 3 //aligned with button 2
  c.gridy = 1 //third row
  panel.add(enter, c)
  setContentPane(panel)
  setVisible(true)

  def addSensor(sensorName: String) {
    sensorsChoice.addItem(sensorName)
  }

  def addOperator(operator: String) {
    operators.addItem(operator)
  }
}