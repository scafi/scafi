/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.utility

import java.io.File
import javax.swing.filechooser.FileFilter

/* ImageFilter.java is used by FileChooserDemo2.java. */
class ImageFilter extends FileFilter {
  //Accept all directories and all gif, jpg, tiff, or png files.
  def accept(f: File): Boolean = {
    if (f.isDirectory) {
      return true
    }
    val extension: String = Utils.getExtension(f)
    if (extension != null) {
      if (extension == Utils.tiff || extension == Utils.tif || extension == Utils.gif || extension == Utils.jpeg || extension == Utils.jpg || extension == Utils.png) {
        return true
      }
      else {
        return false
      }
    }
    return false
  }

  //The description of this filter
  def getDescription: String = {
    return "Just Images"
  }
}
