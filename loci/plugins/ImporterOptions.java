//
// ImporterOptions.java
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

import ij.*;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import loci.formats.Location;

/**
 * Helper class for managing Bio-Formats Importer options.
 * Gets parameter values through a variety of means, including
 * preferences from IJ_Prefs.txt, plugin argument string, macro options,
 * and user input from dialog boxes.
 */
public class ImporterOptions implements ItemListener {

  // -- Constants --

  // enumeration for status
  public static final int STATUS_OK = 0;
  public static final int STATUS_CANCELED = 1;
  public static final int STATUS_FINISHED = 2;

  // enumeration for stackFormat
  public static final String VIEW_NONE = "Metadata only";
  public static final String VIEW_STANDARD = "Standard ImageJ";
  public static final String VIEW_BROWSER = "4D Data Browser";
  public static final String VIEW_IMAGE_5D = "Image5D";
  public static final String VIEW_VIEW_5D = "View5D";

  // class to check for each viewing option
  private static final String CLASS_BROWSER =
    "loci.plugins.browser.LociDataBrowser";
  private static final String CLASS_IMAGE_5D = "i5d.Image5D";
  private static final String CLASS_VIEW_5D = "View5D_";

  // enumeration for location
  public static final String LOCATION_LOCAL = "Local machine";
  public static final String LOCATION_HTTP = "Internet";
  public static final String LOCATION_OME = "OME server";
  public static final String[] LOCATIONS = {
    LOCATION_LOCAL, LOCATION_HTTP, LOCATION_OME
  };

  // keys for use in IJ_Prefs.txt
  public static final String PREF_STACK = "bioformats.stackFormat";
  public static final String PREF_MERGE = "bioformats.mergeChannels";
  public static final String PREF_COLORIZE = "bioformats.colorize";
  public static final String PREF_SPLIT = "bioformats.splitWindows";
  public static final String PREF_METADATA = "bioformats.showMetadata";
  public static final String PREF_GROUP = "bioformats.groupFiles";
  public static final String PREF_CONCATENATE = "bioformats.concatenate";
  public static final String PREF_RANGE = "bioformats.specifyRanges";

  // labels for user dialog; when trimmed these double as argument & macro keys
  public static final String LABEL_STACK = "View stack with: ";
  public static final String LABEL_MERGE = "Merge channels to RGB";
  public static final String LABEL_COLORIZE = "Colorize channels";
  public static final String LABEL_SPLIT =
    "Open each channel in its own window";
  public static final String LABEL_METADATA = "Display associated metadata";
  public static final String LABEL_GROUP = "Group files with similar names";
  public static final String LABEL_CONCATENATE =
    "Concatenate compatible series";
  public static final String LABEL_RANGE = "Specify range for each series";

  public static final String LABEL_LOCATION = "Location: ";
  public static final String LABEL_ID = "Open";

  // -- Fields - GUI components --

  private Choice stackChoice;
  private Checkbox mergeBox;
  private Checkbox colorizeBox;
  private Checkbox splitBox;
  private Checkbox metadataBox;
  private Checkbox groupBox;
  private Checkbox concatenateBox;
  private Checkbox rangeBox;

  // -- Fields - core options --

  private String stackFormat;
  private boolean mergeChannels;
  private boolean colorize;
  private boolean splitWindows;
  private boolean showMetadata;
  private boolean groupFiles;
  private boolean concatenate;
  private boolean specifyRanges;

  private String location;
  private String id;
  private boolean quiet;

  private Location idLoc;
  private String idName;
  private String idType;

  // -- ImporterOptions API methods - accessors --

  public String getStackFormat() { return stackFormat; }
  public boolean isMergeChannels() { return mergeChannels; }
  public boolean isColorize() { return colorize; }
  public boolean isSplitWindows() { return splitWindows; }
  public boolean isShowMetadata() { return showMetadata; }
  public boolean isGroupFiles() { return groupFiles; }
  public boolean isConcatenate() { return concatenate; }
  public boolean isSpecifyRanges() { return specifyRanges; }

  public boolean isViewNone() { return VIEW_NONE.equals(stackFormat); }
  public boolean isViewStandard() { return VIEW_STANDARD.equals(stackFormat); }
  public boolean isViewImage5D() { return VIEW_IMAGE_5D.equals(stackFormat); }
  public boolean isViewBrowser() { return VIEW_BROWSER.equals(stackFormat); }
  public boolean isViewView5D() { return VIEW_VIEW_5D.equals(stackFormat); }

  public String getLocation() { return location; }
  public String getId() { return id; }
  public boolean isQuiet() { return quiet; }

  public boolean isLocal() { return LOCATION_LOCAL.equals(location); }
  public boolean isHTTP() { return LOCATION_HTTP.equals(location); }
  public boolean isOME() { return LOCATION_OME.equals(location); }

  public Location getIdLocation() { return idLoc; }
  public String getIdName() { return idName; }
  public String getIdType() { return idType; }

  // -- ImporterOptions API methods - mutators --

  /** Loads default option values from IJ_Prefs.txt. */
  public void loadPreferences() {
    stackFormat = Prefs.get(PREF_STACK, VIEW_STANDARD);
    mergeChannels = Prefs.get(PREF_MERGE, false);
    colorize = Prefs.get(PREF_COLORIZE, false);
    splitWindows = Prefs.get(PREF_SPLIT, true);
    showMetadata = Prefs.get(PREF_METADATA, false);
    groupFiles = Prefs.get(PREF_GROUP, false);
    concatenate = Prefs.get(PREF_CONCATENATE, false);
    specifyRanges = Prefs.get(PREF_RANGE, false);
  }

  /** Saves option values to IJ_Prefs.txt as the new defaults. */
  public void savePreferences() {
    Prefs.set(PREF_STACK, stackFormat);
    Prefs.set(PREF_MERGE, mergeChannels);
    Prefs.set(PREF_COLORIZE, colorize);
    Prefs.set(PREF_SPLIT, splitWindows);
    Prefs.set(PREF_METADATA, showMetadata);
    Prefs.set(PREF_GROUP, groupFiles);
    Prefs.set(PREF_CONCATENATE, concatenate);
    Prefs.set(PREF_RANGE, specifyRanges);
  }

  /** Parses the plugin argument for parameter values. */
  public void parseArg(String arg) {
    if (arg == null || arg.length() == 0) return;
    if (new Location(arg).exists()) {
      // old style arg: entire argument is a file path

      // this style is used by the HandleExtraFileTypes plugin

      // NB: This functionality must not be removed, or the plugin
      // will stop working correctly with HandleExtraFileTypes.

      location = LOCATION_LOCAL;
      id = arg;
      quiet = true; // suppress obnoxious error messages and such
    }
    else {
      // new style arg: split up similar to a macro options string, but
      // slightly different than macro options, in that boolean arguments
      // must be of the form "key=true" rather than just "key"

      // only the core options are supported for now

      // NB: This functionality enables multiple plugin entries to achieve
      // distinct behavior by calling the LociImporter plugin differently.

      mergeChannels = getMacroValue(arg, LABEL_MERGE, mergeChannels);
      colorize = getMacroValue(arg, LABEL_COLORIZE, colorize);
      splitWindows = getMacroValue(arg, LABEL_COLORIZE, splitWindows);
      showMetadata = getMacroValue(arg, LABEL_METADATA, showMetadata);
      groupFiles = getMacroValue(arg, LABEL_GROUP, groupFiles);
      concatenate = getMacroValue(arg, LABEL_CONCATENATE, concatenate);
      specifyRanges = getMacroValue(arg, LABEL_RANGE, specifyRanges);
      stackFormat = Macro.getValue(arg, LABEL_STACK, stackFormat);

      location = Macro.getValue(arg, LABEL_LOCATION, location);
      id = Macro.getValue(arg, LABEL_ID, id);
    }
  }

  /**
   * Gets the location (type of data source) from macro options,
   * or user prompt if necessary.
   * @return status of operation
   */
  public int promptLocation() {
    if (location == null) {
      // Open a dialog asking the user what kind of dataset to handle.
      // Ask only if the location was not already specified somehow.
      // ImageJ will grab the value from the macro options, when possible.
      GenericDialog gd = new GenericDialog("Bio-Formats Dataset Location");
      gd.addChoice(LABEL_LOCATION, LOCATIONS, LOCATION_LOCAL);
      gd.showDialog();
      if (gd.wasCanceled()) return STATUS_CANCELED;
      location = gd.getNextChoice();
    }

    // verify that location is valid
    boolean isLocal = LOCATION_LOCAL.equals(location);
    boolean isHTTP = LOCATION_HTTP.equals(location);
    boolean isOME = LOCATION_OME.equals(location);
    if (!isLocal && !isHTTP && !isOME) {
      if (!quiet) IJ.error("Bio-Formats", "Invalid location: " + location);
      return STATUS_FINISHED;
    }
    return STATUS_OK;
  }

  /**
   * Gets the id (e.g., filename or URL) to open from macro options,
   * or user prompt if necessary.
   * @return status of operation
   */
  public int promptId() {
    if (id == null) {
      if (isLocal()) {
        // prompt user for the filename (or grab from macro options)
        OpenDialog od = new OpenDialog(LABEL_ID, id);
        String dir = od.getDirectory();
        String name = od.getFileName();
        if (dir == null || name == null) return STATUS_CANCELED;
        id = dir + name;
      }
      else if (isHTTP()) {
        // prompt user for the URL (or grab from macro options)
        GenericDialog gd = new GenericDialog("Bio-Formats URL");
        gd.addStringField("URL: ", "http://", 30);
        gd.showDialog();
        if (gd.wasCanceled()) return STATUS_CANCELED;
        id = gd.getNextString();
      }
      else { // isOME
        IJ.runPlugIn("loci.plugins.OMEPlugin", "");
        return STATUS_FINISHED;
      }
    }

    // verify that id is valid
    if (isLocal()) {
      if (id != null) idLoc = new Location(id);
      if (idLoc == null || !idLoc.exists()) {
        if (!quiet) {
          IJ.error("Bio-Formats", idLoc == null ?
            "No file was specified." :
            "The specified file (" + id + ") does not exist.");
        }
        return STATUS_FINISHED;
      }
      idName = idLoc.getName();
      idType = "Filename";
    }
    else if (isHTTP()) {
      if (id == null) {
        if (!quiet) IJ.error("Bio-Formats", "No URL was specified.");
        return STATUS_FINISHED;
      }
      idName = id;
      idType = "URL";
    }
    else { // isOME
      idType = "OME address";
    }
    return STATUS_OK;
  }

  /**
   * Gets option values from macro options, or user prompt if necessary.
   * @return status of operation
   */
  public int promptOptions() {
    Vector stackTypes = new Vector();
    stackTypes.add(VIEW_NONE);
    stackTypes.add(VIEW_STANDARD);
    if (Checker.checkClass(CLASS_BROWSER)) stackTypes.add(VIEW_BROWSER);
    if (Checker.checkClass(CLASS_IMAGE_5D)) stackTypes.add(VIEW_IMAGE_5D);
    if (Checker.checkClass(CLASS_VIEW_5D)) stackTypes.add(VIEW_VIEW_5D);
    final String[] stackFormats = new String[stackTypes.size()];
    stackTypes.copyInto(stackFormats);

    // prompt user for parameters (or grab from macro options)
    GenericDialog gd = new GenericDialog("Bio-Formats Import Options");
    gd.addChoice(LABEL_STACK, stackFormats, stackFormat);
    gd.addCheckbox(LABEL_MERGE, mergeChannels);
    gd.addCheckbox(LABEL_COLORIZE, colorize);
    gd.addCheckbox(LABEL_SPLIT, splitWindows);
    gd.addCheckbox(LABEL_METADATA, showMetadata);
    gd.addCheckbox(LABEL_GROUP, groupFiles);
    gd.addCheckbox(LABEL_CONCATENATE, concatenate);
    gd.addCheckbox(LABEL_RANGE, specifyRanges);

    // extract GUI components from dialog and add listeners
    Vector choices = gd.getChoices();
    if (choices != null) {
      stackChoice = (Choice) choices.get(0);
      for (int i=0; i<choices.size(); i++) {
        ((Choice) choices.get(i)).addItemListener(this);
      }
    }
    Vector boxes = gd.getCheckboxes();
    if (boxes != null) {
      mergeBox = (Checkbox) boxes.get(0);
      colorizeBox = (Checkbox) boxes.get(1);
      splitBox = (Checkbox) boxes.get(2);
      metadataBox = (Checkbox) boxes.get(3);
      groupBox = (Checkbox) boxes.get(4);
      concatenateBox = (Checkbox) boxes.get(5);
      rangeBox = (Checkbox) boxes.get(6);
      for (int i=0; i<boxes.size(); i++) {
        ((Checkbox) boxes.get(i)).addItemListener(this);
      }
    }

    gd.showDialog();
    if (gd.wasCanceled()) return STATUS_CANCELED;

    stackFormat = stackFormats[gd.getNextChoiceIndex()];
    mergeChannels = gd.getNextBoolean();
    colorize = gd.getNextBoolean();
    splitWindows = gd.getNextBoolean();
    showMetadata = gd.getNextBoolean();
    groupFiles = gd.getNextBoolean();
    concatenate = gd.getNextBoolean();
    specifyRanges = gd.getNextBoolean();

    return STATUS_OK;
  }

  // -- ItemListener API methods --

  /** Handles toggling of mutually exclusive options. */
  public void itemStateChanged(ItemEvent e) {
    Object src = e.getSource();
    if (src == stackChoice) {
      String s = stackChoice.getSelectedItem();
      if (s.equals(VIEW_NONE)) {
        metadataBox.setState(true);
        rangeBox.setState(false);
      }
      if (s.equals(VIEW_STANDARD)) {
      }
      else if (s.equals(VIEW_BROWSER)) {
        colorizeBox.setState(false); // NB: temporary
        splitBox.setState(false);
        rangeBox.setState(false);
      }
      else if (s.equals(VIEW_IMAGE_5D)) {
        mergeBox.setState(false);
      }
      else if (s.equals(VIEW_VIEW_5D)) {
      }
    }
    else if (src == mergeBox) {
      if (mergeBox.getState()) {
        colorizeBox.setState(false);
        splitBox.setState(false);
        String s = stackChoice.getSelectedItem();
        if (s.equals(VIEW_IMAGE_5D)) stackChoice.select(VIEW_STANDARD);
      }
    }
    else if (src == colorizeBox) {
      if (colorizeBox.getState()) {
        mergeBox.setState(false);
        splitBox.setState(true);
        // NB: temporary
        String s = stackChoice.getSelectedItem();
        if (s.equals(VIEW_BROWSER)) stackChoice.select(VIEW_STANDARD);
      }
    }
    else if (src == splitBox) {
      if (splitBox.getState()) {
        mergeBox.setState(false);
        String s = stackChoice.getSelectedItem();
        if (s.equals(VIEW_BROWSER)) stackChoice.select(VIEW_STANDARD);
      }
    }
    else if (src == metadataBox) {
      if (!metadataBox.getState()) {
        String s = stackChoice.getSelectedItem();
        if (s.equals(VIEW_NONE)) stackChoice.select(VIEW_STANDARD);
      }
    }
    else if (src == groupBox) {
    }
    else if (src == concatenateBox) {
    }
    else if (src == rangeBox) {
      if (rangeBox.getState()) {
        String s = stackChoice.getSelectedItem();
        if (s.equals(VIEW_NONE) || s.equals(VIEW_BROWSER)) {
          stackChoice.select(VIEW_STANDARD);
        }
      }
    }
  }

  // -- Helper methods --

  private boolean getMacroValue(String options,
    String key, boolean defaultValue)
  {
    String s = Macro.getValue(options, key, null);
    return s == null ? defaultValue : s.equalsIgnoreCase("true");
  }

}
