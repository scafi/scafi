/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
