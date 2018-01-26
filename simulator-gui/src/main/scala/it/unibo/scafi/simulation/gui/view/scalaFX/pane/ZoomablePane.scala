package it.unibo.scafi.simulation.gui.view.scalaFX.pane

import scalafx.scene.input.ScrollEvent
import scalafx.scene.layout.Pane

/**
  * a pane that allow zoom with the whels of mouse
  */
class ZoomablePane extends Pane {
  private val SCALE_DELTA = 1.1;
  // SCROLLING TO REMOVE HERE
  import scalafx.Includes._
  this.onScroll = (e : ScrollEvent) => {
    e.consume();
    val scaleFactor = if(e.getDeltaY() > 0) SCALE_DELTA else 1 / SCALE_DELTA
    this.setScaleX(this.getScaleX() * scaleFactor);
    this.setScaleY(this.getScaleY() * scaleFactor);
  }
}
