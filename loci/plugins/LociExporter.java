//
// LociExporter.java
//

/*
LOCI Plugins for ImageJ: a collection of ImageJ plugins including the
4D Data Browser, Bio-Formats Importer, Bio-Formats Exporter and OME plugins.
Copyright (C) 2006-@year@ Melissa Linkert, Christopher Peterson,
Curtis Rueden, Philip Huettl and Francis Wong.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Library General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Library General Public License for more details.

You should have received a copy of the GNU Library General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.plugins;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.HashSet;

/**
 * ImageJ plugin for writing files using the LOCI Bio-Formats package.
 * Wraps core logic in {@link loci.plugins.Exporter}, to avoid direct
 * references to classes in the external Bio-Formats library.
 *
 * @author Melissa Linkert linkert at wisc.edu
 */
public class LociExporter implements PlugInFilter {

  // -- Fields --

  /** Argument passed to setup method. */
  public String arg;

  private Exporter exporter;

  // -- PlugInFilter API methods --

  /** Sets up the writer. */
  public int setup(String arg, ImagePlus imp) {
    this.arg = arg;
    exporter = new Exporter(this, imp);
    return DOES_ALL + NO_CHANGES;
  }

  /** Executes the plugin. */
  public void run(ImageProcessor ip) {
    if (!Checker.checkVersion()) return;
    HashSet missing = new HashSet();
    Checker.checkLibrary(Checker.BIO_FORMATS, missing);
    Checker.checkLibrary(Checker.OME_JAVA_XML, missing);
    if (!Checker.checkMissing(missing)) return;
    if (exporter != null) exporter.run(ip);
  }

}
