package it.unibo.scafi.simulation.gui.view

import javax.swing._
import javax.swing.border.LineBorder
import java.awt._
import java.awt.event.{ActionEvent, InputEvent, MouseEvent}

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum

/**
  * This is the most important panel in which the simulation will be executed.
  * Created by Varini on 19/10/16.
  * Converted/refactored to Scala by Casadei on 04/02/17
  */
class SimulationPanel() extends JDesktopPane {
  final private val neighborsPanel: NeighborsPanel = new NeighborsPanel //pannello visualizzazione vicini
  private var bkgImage: Image = null
  final private val captureRect: Rectangle = new Rectangle //rettangolo di selezione
  final private val popup: MyPopupMenu = new MyPopupMenu //menu tasto destro

  this.setBackground(Color.decode("#9EB3C2")) //azzurro
  setBorder(new LineBorder(Color.black))
  this.add(neighborsPanel, 1)
  val motion: SimulationPanelMouseListener = new SimulationPanelMouseListener(this)
  this.addMouseListener(motion) //gestisco quando appare il pannello delle opzioni
  this.addMouseMotionListener(motion) //creo e gestisco l'era di selezione

  val imap = this.getInputMap()
  val amap = this.getActionMap()
  val ctrl = Controller.getIstance
  imap.put(KeyStroke.getKeyStroke('1'), SensorEnum.SENS1.name)
  amap.put(SensorEnum.SENS1.name, createSensorAction[Boolean](SensorEnum.SENS1.name, default = false, map = !_))
  imap.put(KeyStroke.getKeyStroke('2'), SensorEnum.SENS2.name)
  amap.put(SensorEnum.SENS2.name, createSensorAction[Boolean](SensorEnum.SENS2.name, default = false, map = !_))
  imap.put(KeyStroke.getKeyStroke('3'), SensorEnum.SENS3.name)
  amap.put(SensorEnum.SENS3.name, createSensorAction[Boolean](SensorEnum.SENS3.name, default = false, map = !_))
  imap.put(KeyStroke.getKeyStroke("DOWN"), "Quicker")
  imap.put(KeyStroke.getKeyStroke("UP"), "Slower")
  amap.put("Quicker", createAction((e: ActionEvent)=>{
    val currVal = ctrl.simManager.simulation.getDeltaRound()
    val newVal = if(currVal-10 < 0) 0 else currVal-10
    println(s"Setting delta round = $newVal")
    ctrl.simManager.simulation.setDeltaRound(newVal)
  }))
  amap.put("Slower", createAction((e: ActionEvent)=>{
    val currVal = ctrl.simManager.simulation.getDeltaRound()
    val newVal = currVal+10
    println(s"Setting delta round = $newVal")
    ctrl.simManager.simulation.setDeltaRound(newVal)
  }))

  override def paintComponent(g: Graphics) {
    if (bkgImage != null) {
      // Shows background image
      g.drawImage(bkgImage, 0, 0, this.getWidth, this.getHeight, this)
    }
    if (captureRect != null) {
      // Shows selection area
      g.setColor(Color.lightGray)
      g.drawRect(captureRect.getX.toInt, captureRect.getY.toInt, captureRect.getWidth.toInt, captureRect.getHeight.toInt)
      g.setColor(new Color(255, 255, 255, 150))
      g.fillRect(captureRect.getX.toInt, captureRect.getY.toInt, captureRect.getWidth.toInt, captureRect.getHeight.toInt)
    }
  }

  /**
    * Set the background image
    *
    * @param bkgImage
    */
  def setBackgroundImage(bkgImage: Image) {
    this.bkgImage = bkgImage
  }

  /**
    * Shows the panel representing the neighbourhood
    *
    * @param show
    */
  def showNeighbours(show: Boolean) {
    //mostro il pannello che visualizza i collegamenti con i vicini
    neighborsPanel.setVisible(show)
    this.revalidate()
    this.repaint()
  }

  def setRectSelection(r: Rectangle) {
    this.captureRect.setRect(r)
  }

  def getCaptureRect: Rectangle = {
    return captureRect
  }

  def maybeShowPopup(e: MouseEvent) {
    if (e.isPopupTrigger) {
      popup.show(e.getComponent, e.getX, e.getY)
    }
  }

  def getPopUpMenu: MyPopupMenu = {
    return this.popup
  }

  private def createAction(f: ActionEvent => Unit): Action ={
    new AbstractAction() {
      override def actionPerformed(e: ActionEvent) = f(e)
    }
  }

  private def createSensorAction[T](sensorName: String, default: T, map: T=>T) = {
    createAction((e: ActionEvent) => {
      val currVal = ctrl.getSensor(sensorName).getOrElse(default).asInstanceOf[T]
      val newVal = map(currVal)
      println(s"Setting '$sensorName' to ${newVal}")
      ctrl.setSensor(sensorName, newVal)
    })
  }
}