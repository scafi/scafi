package it.unibo.scafi.simulation.frontend.view

import it.unibo.scafi.simulation.frontend.configuration.SensorName

import scalafx.scene.paint.Color
/**
  * a setting using to display nodes
  */
object ViewSetting {
  /**
    * device name that view can show correctly
   */
  lazy val deviceName : List[String] = SensorName.inputSensor ++ SensorName.outputSensor
  /**
    * a circular list of color associated to device name
    */
  var deviceColor : List[Color] = List(Color.Red, Color.Yellow, Color.Blue, Color.LimeGreen, Color.MediumOrchid)
  /**
    * the node color
    */
  var nodeColor : Color = Color.Black
  /**
    * a image path of node
    */
  var nodeImagePath : String = "icon.png"
  /**
    * color of neighbour line
   */
  var lineColor : Color = Color(0,0,0,0.2)
  /**
    * max size of label
    */
  var maxTextLength = 200
  /**
    * the value of world background
    */
  var backgroundColor : Color = Color.Transparent
  /**
    * background image (if it is present)
     */
  var backgroundImage : Option[String] = None
}