/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.view

import java.awt.event.ActionEvent
import java.awt.{Color, GridBagConstraints, GridBagLayout, Insets}
import java.text.NumberFormat

import it.unibo.scafi.simulation.frontend.controller.GeneralController
import it.unibo.scafi.simulation.frontend.utility.{ImageFilter, Utils}
import javax.swing._

/**
  * This class represent the Application menu
  */
class MenuBarNorth(controller: GeneralController) extends JMenuBar {
  private var menus = Vector[JMenu]()

  val file: JMenu = new JMenu("File")
  val newFile: JMenu = new JMenu("New")
  val simulation: JMenuItem = new JMenuItem("Scafi Simulation")
  simulation.addActionListener((e:ActionEvent) => { new ConfigurationPanel(controller); () })
  newFile.add(simulation)
  val open: JMenuItem = new JMenuItem("Open")
  val save: JMenuItem = new JMenuItem("Save")
  file.add(newFile)
  val simConfig: JMenu = new JMenu("Simulation")
  val close: JMenuItem = new JMenuItem("Close")
  close.addActionListener((e:ActionEvent) => {
    controller.clearSimulation()
  })
  val addImage: JMenuItem = new JMenuItem("Add Image")
  val removeImage: JMenuItem = new JMenuItem("Remove Image")
  removeImage.setEnabled(false)
  addImage.addActionListener((e:ActionEvent) => {
    val choose = new JFileChooser()
    choose.addChoosableFileFilter(new ImageFilter())
    choose.setAcceptAllFileFilterUsed(false)
    choose.showOpenDialog(this.getParent())
    val selectedFile = choose.getSelectedFile()
    if(selectedFile != null){
      controller.showImage(new ImageIcon(selectedFile.getPath()).getImage(), true)
      removeImage.setEnabled(true)
      addImage.setEnabled(false)
    }
  })
  removeImage.addActionListener((e:ActionEvent)  => {
    controller.showImage(new ImageIcon("").getImage(), false)
    removeImage.setEnabled(false)
    addImage.setEnabled(true)
  })
  val dim: Int = Utils.getIconMenuDim.getWidth.toInt
  //dimensione icone
  val start: JMenuItem = new JMenuItem("Start", Utils.getScaledImage("start.png", dim, dim))
  val pause: JMenuItem = new JMenuItem("Pause", Utils.getScaledImage("pause.png", dim, dim))
  start.addActionListener((e:ActionEvent)  => {
    controller.resumeSimulation()
    start.setEnabled(false)
    pause.setEnabled(true)
  })
  pause.addActionListener((e:ActionEvent)  => {
    controller.pauseSimulation()
    start.setEnabled(true)
    pause.setEnabled(false)
  })
  start.setEnabled(false)
  val step: JMenuItem = new JMenuItem("Step", Utils.getScaledImage("step.png", dim, dim))
  step.addActionListener((e:ActionEvent) => { new StepDialog(); () })
  val stop: JMenuItem = new JMenuItem("Stop", Utils.getScaledImage("stop.png", dim, dim))
  stop.addActionListener((e:ActionEvent)  => {start.setEnabled(false); controller.stopSimulation()})
  simConfig.add(close)
  simConfig.add(addImage)
  simConfig.add(removeImage)
  simConfig.addSeparator()
  simConfig.add(start)
  simConfig.add(step)
  simConfig.add(pause)
  simConfig.add(stop)
  simConfig.setEnabled(false)
  menus = menus :+ file
  menus = menus :+ simConfig
  menus.foreach(m => add(m))

  override def setEnabled(enabled: Boolean): Unit = {
    super.setEnabled(enabled)
    menus.foreach(m => m.setEnabled(enabled) )
  }

  /**
    * This is a JDialog for request the number of step
    * that user want to skip in the simulation
    */
  private class StepDialog private[view]() extends JDialog {
    val cancel: JButton = new JButton("Cancel")
    val enter: JButton = new JButton("Ok")
    val errMsg: JLabel = new JLabel("Error! Insert an integer number")
    val panel: JPanel = new JPanel(new GridBagLayout)
    val gb: GridBagConstraints = new GridBagConstraints
    val n_step: JFormattedTextField = new JFormattedTextField(NumberFormat.getIntegerInstance)

    setTitle("Enter the number of steps that you want do")
    setSize(400, 200)
    setLocationRelativeTo(null)
    n_step.setColumns(10)

    enter.addActionListener((e:ActionEvent) => {
      try {
        controller.stepSimulation(n_step.getText.toInt)
        dispose()
      } catch {
        case ex: Throwable =>
          errMsg.setVisible(true)
          panel.add(errMsg, gb)
      }
    })

    cancel.addActionListener((e:ActionEvent) => dispose())
    gb.insets = new Insets(0, 0, 0, 10)
    gb.gridx = 0
    gb.gridy = 1
    gb.gridwidth = 3
    panel.add(n_step, gb)
    gb.anchor = GridBagConstraints.LINE_END
    gb.gridwidth = 1
    gb.gridx = 1
    gb.gridy = 2
    panel.add(cancel, gb)
    gb.anchor = GridBagConstraints.LINE_START
    gb.gridx = 2
    gb.gridy = 2
    panel.add(enter, gb)
    gb.gridx = 1
    gb.gridy = 0
    gb.gridwidth = 3
    errMsg.setForeground(Color.red)
    errMsg.setVisible(false)
    panel.add(errMsg, gb)
    setContentPane(panel)
    setVisible(true)
  }

}
