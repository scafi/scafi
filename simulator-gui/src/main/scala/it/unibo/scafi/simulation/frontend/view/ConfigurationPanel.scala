/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.view

import java.awt._
import java.awt.event.ActionEvent
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import java.text.NumberFormat
import javax.swing._

import it.unibo.scafi.simulation.frontend.Settings
import it.unibo.scafi.simulation.frontend.controller.Controller
import it.unibo.scafi.simulation.frontend.utility.Utils

/**
  * This class represent the panel where the user can configure a new simulation.
  */
class ConfigurationPanel(controller: Controller) extends JDialog(controller.getUI) with PropertyChangeListener {
  final private var err: JLabel = null
  final private var gbc: GridBagConstraints = null

  private var yCellLayout: Int = 0 // used in insertRow method

  final private var nodeNumberField: JFormattedTextField = null
  final private var neinghborsAreaField: JFormattedTextField = null
  final private var deltaRoundField: JFormattedTextField = null
  final private var topologyField: JComboBox[String] = null
  final private var runProgram: JTextField = null
  final private var strategy: JTextField = null
  final private var sensors: JTextArea = null

  final private var addFile: JButton = null
  private var submitButton: JButton = null //Button for starting the simulation

  setTitle("Configuration")
  setSize(Utils.getConfPanelDim)
  setLocationRelativeTo(null)
  setAlwaysOnTop(true)

  nodeNumberField = new JFormattedTextField(NumberFormat.getIntegerInstance)
  nodeNumberField.setValue(Settings.Sim_NumNodes)
  nodeNumberField.setColumns(10)
  nodeNumberField.addPropertyChangeListener(this)
  import it.unibo.scafi.simulation.frontend.SettingsSpace.Topologies._
  var vtop = Vector[String](Random, Grid, Grid_LoVar, Grid_MedVar, Grid_HighVar)

  topologyField = new JComboBox[String](vtop.toArray)
  topologyField.setSelectedItem(Settings.Sim_Topology)
  topologyField.addPropertyChangeListener(this)

  deltaRoundField = new JFormattedTextField(NumberFormat.getNumberInstance)
  deltaRoundField.setValue(Settings.Sim_DeltaRound)
  deltaRoundField.setColumns(10)
  deltaRoundField.addPropertyChangeListener(this)

  neinghborsAreaField = new JFormattedTextField(NumberFormat.getNumberInstance)
  neinghborsAreaField.setValue(Settings.Sim_NbrRadius)
  neinghborsAreaField.setColumns(10)
  neinghborsAreaField.addPropertyChangeListener(this)

  runProgram = new JTextField
  runProgram.setText(Settings.Sim_ProgramClass)
  runProgram.setColumns(10)
  runProgram.addPropertyChangeListener(this)

  strategy = new JTextField
  strategy.setColumns(10)
  strategy.addPropertyChangeListener(this)

  sensors = new JTextArea(Settings.Sim_Sensors, 4,20)

  addFile = new JButton("File")
  addFile.addActionListener((e: ActionEvent) => {
    val choose = new JFileChooser()
    choose.showOpenDialog(this)
    addFile.setText(choose.getSelectedFile().getName())
    //controller.startSimulation(choose.getSelectedFile())
  })

  err = new JLabel("Error! Invalid input")
  err.setForeground(Color.red)
  err.setVisible(false)

  val p1: JPanel = new JPanel(new GridBagLayout)
  gbc = new GridBagConstraints {
    gridwidth = 1
    insets = new Insets(5, 0, 0, 10)
  }

  insertRow("Run program: ", runProgram, p1)
  insertRow("Number of nodes: ", nodeNumberField, p1)
  insertRow("Topology", topologyField, p1)
  insertRow("Neighborhood radius: ", neinghborsAreaField, p1)
  insertRow("∆ round: ", deltaRoundField, p1)
  //insertRow("Strategy: ", strategy, p1);
  //insertRow("Add configuration file: ", addFile, p1);
  //insertRow("Sensors", sensors, p1)

  submitButton = new JButton("Start")
  gbc.gridx = 2
  gbc.gridy = yCellLayout
  gbc.insets = new Insets(20, 10, 0, 0)
  gbc.anchor = GridBagConstraints.CENTER
  p1.add(submitButton, gbc)
  yCellLayout += 1
  //error label
  gbc.gridx = 2
  p1.add(err, gbc)
  setContentPane(p1)

  submitButton.addActionListener((e: ActionEvent) => {
    try {
      Settings.Sim_NumNodes = nodeNumberField.getText.toInt
      Settings.Sim_NbrRadius = neinghborsAreaField.getText.replaceAll(",", ".").toDouble
      Settings.Sim_DeltaRound = deltaRoundField.getText.toInt
      Settings.Sim_ProgramClass = runProgram.getText()
      Settings.Sim_ExecStrategy = strategy.getText()
      if(topologyField.getSelectedItem() != null) Settings.Sim_Topology = topologyField.getSelectedItem().toString()
      //Settings.Sim_Sensors = sensors.getText
      controller.startSimulation()
      dispose()
    } catch {
      case ex: Throwable => ex.printStackTrace(); showErr(0)
    }
  })
  setVisible(true)

  /**
    * Checks if the field's input is valid.
    *
    * @param evt
    */
  def propertyChange(evt: PropertyChangeEvent) {
    val source: Any = evt.getSource
    if (source == nodeNumberField) {
      if (!nodeNumberField.isEditValid) {
        showErr(0)
      }
      else {
        err.setVisible(false)
      }
    }
    else if (source == deltaRoundField) {
      if (!deltaRoundField.isEditValid) {
        showErr(1)
      }
      else {
        err.setVisible(false)
      }
    }
    else if (source == neinghborsAreaField) {
      if (!neinghborsAreaField.isEditValid) {
        showErr(2)
      }
      else {
        err.setVisible(false)
      }
    }
  }

  /**
    * Put String on left and JComponent on right of p.
    *
    * @param name
    * @param comp
    * @param p
    */
  private def insertRow(name: String, comp: JComponent, p: JPanel) {
    gbc.gridx = 0
    gbc.gridy = yCellLayout
    gbc.anchor = GridBagConstraints.LINE_END
    p.add(new JLabel(name), gbc)
    gbc.gridx = 1
    gbc.gridy = yCellLayout
    gbc.anchor = GridBagConstraints.LINE_START
    p.add(comp, gbc)
    yCellLayout += 1
  }

  /**
    * Show the "error" label
    *
    * @param y
    */
  private def showErr(y: Int) {
    gbc.gridx = 2
    gbc.gridy = y
    err.setVisible(true)
    getContentPane.add(err, gbc)
  }
}
