package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.utility.Utils
import javax.swing._
import java.awt._
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

/**
  * This is the general frame that contains all panel
  * Created by Varini on 19/10/16.
  * Converted/refactored to Scala by Casadei on 04/02/17
  */
class SimulatorUI() extends JFrame("SCAFI Simulator") {
  private var center: SimulationPanel = new SimulationPanel //JDesktopPane per visualizzare le simulazioni
  final private val menuBarNorth: JMenuBar = new MenuBarNorth //barra del menù in alto
  private var oldDim: Dimension = null //utilizzato per la riposizione dei nodi quando il frame viene rimpicciolito

  setSize(Utils.getFrameDimension)
  oldDim = Utils.getFrameDimension
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  val panel: JPanel = new JPanel(new BorderLayout)
  panel.add(this.center, BorderLayout.CENTER)
  setContentPane(panel)
  this.setJMenuBar(menuBarNorth)

  this.addComponentListener(new ComponentAdapter() {
    override def componentResized(e: ComponentEvent) {
      super.componentResized(e)
      Utils.setDimensionFrame(getSize)
      for (i <- center.getAllFrames) {
        i.setSize(Utils.getSizeGuiNode) //ridimensionamento
        i.setLocation((i.getLocation().getX * getWidth / oldDim.getWidth().round).toInt,
          (i.getLocation().getY * getHeight / oldDim.getHeight.round).toInt)
      }
      center.getCaptureRect.setSize((center.getCaptureRect.getWidth * getWidth / oldDim.getWidth().round).toInt,
        (center.getCaptureRect.getHeight * getHeight / oldDim.getHeight.round).toInt)
      center.getCaptureRect.setLocation((center.getCaptureRect.getLocation.getX * getWidth / oldDim.getWidth().round).toInt,
        (center.getCaptureRect.getLocation.getY * getHeight / oldDim.getHeight.round).toInt)
      oldDim = getSize
    }
  })

  setVisible(true)

  /**
    * @return center panel
    */
  def getSimulationPanel: SimulationPanel = {
    return center
  }

  def setSimulationPanel(simPanel: SimulationPanel) {
    this.remove(center)
    this.add(simPanel, BorderLayout.CENTER)
    center = simPanel
    this.revalidate()
    this.repaint()
  }

  /**
    * @return application menu
    */
  def getMenuBarNorth: JMenuBar = {
    return menuBarNorth
  }
}