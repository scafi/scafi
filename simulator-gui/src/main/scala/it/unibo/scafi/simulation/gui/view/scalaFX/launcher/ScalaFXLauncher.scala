package it.unibo.scafi.simulation.gui.view.scalaFX.launcher

import javafx.scene.control.{Tab, TabPane, TitledPane}
import javafx.scene.layout.{StackPane => JFXStackPane}

import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory
import it.unibo.scafi.simulation.gui.configuration.parser.VirtualMachine
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.view.WindowConfiguration
import it.unibo.scafi.simulation.gui.view.scalaFX.LogoStage
import it.unibo.scafi.simulation.gui.view.scalaFX.common.Help
import it.unibo.scafi.simulation.gui.view.scalaFX.pane.Logo

import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.TabPane.TabClosingPolicy
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{Pane, VBox}
import scalafx.stage.Stage
class ScalaFXLauncher(factories : List[CommandFactory],
                      subsection : Map[String,List[CommandFactory]],
                      unixMachine : VirtualMachine[String])
                     (implicit val window : WindowConfiguration) extends LogoStage(window) {
  import ScalaFXLauncher._
  import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
  //collections used to save field box created
  private var tabPaneSections : Map[TabPane,List[FieldBox]] = Map.empty
  private var sections : List[FieldBox] = List.empty

  //create a window with the width and height selected
  val windowRect : Rectangle = window
  this.width = windowRect.w
  this.height = windowRect.h
  this.title = window.name
  //create the main content

  private val mainContent = new VBox
  //used to scroll main content
  private val scrollPane = new ScrollPane {
    content = mainContent
  }
  mainContent.children.add(new JFXStackPane(logo))
  this.scene = new Scene {
    content = new ScrollPane{
      content = scrollPane
    }
  }

  //show always scroll bar
  scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.Never
  scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.Never
  scrollPane.padding = Insets { FieldBoxSpacing }
  scrollPane.prefHeight.bind(this.scene.value.heightProperty())
  scrollPane.prefWidth.bind(this.scene.value.widthProperty())
  //main content change the size with the scrollPane with
  mainContent.prefWidth.bind(scrollPane.prefWidth - RightScrollPanePadding)
  //create subsection pane
  subsection foreach (x => {
    //each subsection is a tab, the parent of tabs is a tab pane
    val tabPane = new TabPane
    //the tab can't be closed
    tabPane.setTabClosingPolicy(TabClosingPolicy.Unavailable);
    //foreach subsection create a field box with the factory selected
    var fieldBoxes : List[FieldBox] = List.empty
    x._2 foreach(y => {
      //create a tab with the name selected
      val tab = new Tab(international(y.name)(KeyFile.CommandName))
      //find the factory associated with the name
      val factory = y
      //create a tooltip to show factory description
      tab.setGraphic(Help(new Tooltip(factory.description)))
      //put the fieldbox as the tab content
      val box : FieldBox = FieldBox(factory)
      tab.setContent(box)
      fieldBoxes = fieldBoxes ::: box :: Nil
      //add tab created
      tabPane.getTabs.add(tab)
    })
    tabPaneSections += tabPane -> fieldBoxes
    //add subsection into section ( a titled pane)
    mainContent.children.add(new TitledPane(x._1,tabPane))

  })
  //here i put other factory in the scene, added is a set of all pane added
  private val added = subsection.values.flatten.toSet
  factories foreach (x => {
    //if the factory isn't added, i create the new field box and add into scene
    if(!added.contains(x)) {
      val box = FieldBox(x)
      val pane = new TitledPane(international(x.name)(KeyFile.CommandName),box)
      pane.setGraphic(Help(new Tooltip(x.description)))
      mainContent.children.add(pane)
      sections = box :: sections
    }
  })
  //create a button used to launch scafi simulation
  val button = new Button(Launch)
  import scalafx.Includes._
  button.onMouseClicked = (e : MouseEvent) => {
    sections.foreach(x => {unixMachine.process(x.toUnix)})
    for(tabSection <- this.tabPaneSections) {
      val tab = tabSection._1.getSelectionModel.getSelectedIndex
      unixMachine.process(tabSection._2(tab).toUnix)
    }
    unixMachine.process(Launch)
    this.close()
  }

  mainContent.children.add(new JFXStackPane(button))
}

object ScalaFXLauncher {
  val FieldBoxSpacing = 10
  val RightScrollPanePadding = 20
  val Launch = "launch"
  val FontName = "Verdana"
  val FontSize = 40
}




