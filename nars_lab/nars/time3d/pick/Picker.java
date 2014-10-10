package nars.time3d.pick;

import processing.core.PApplet;
import processing.core.PGraphics;

public class Picker {
  public final static String VERSION = "##library.prettyVersion##";

  /**
   * Processing applet
   */
  protected PApplet parent;

  /**
   * Main picking buffer Direct access to the buffer is allowed so you can
   * draw shapes that you wouldn't like to draw on the main screen (like
   * bounding boxes).
   */
  public Buffer buffer;

  public Picker(PApplet parent) {
    this.parent = parent;
    buffer = (Buffer) parent.createGraphics(parent.width, parent.height, "picking.Buffer");
    buffer.callCheckSettings();
    buffer.background(0);

    parent.registerMethod("pre", this);
    parent.registerMethod("draw", this);
    welcome();
  }

  public void pre() {
    parent.beginRecord(buffer);
  }

  public void draw() {
    // make sure recorder is there before shutting it down
    if (parent.recorder == null) {
      parent.recorder = buffer;
    }
    parent.endRecord();
  }

  /**
   * Begins recording object(s)
   * 
   * @param i Object ID
   */
  public void start(int i) {
    if (i < 0 || i > 16777214) {
      PApplet.println("[Picking error] start(): ID out of range");
      return;
    }

    if (parent.recorder == null) {
      parent.recorder = buffer;
    }
    buffer.setCurrentId(i);
  }

  /**
   * Stops/pauses recording object(s)
   */
  public void stop() {
    parent.recorder = null;
  }

  /**
   * Resumes recording object(s)
   */
  public void resume() {
    if (parent.recorder == null) {
      parent.recorder = buffer;
    }
  }

  /**
   * Reads the ID of the object at point (x, y) -1 means there is no object at
   * this point
   * 
   * @param x X coordinate
   * @param y Y coordinate
   * @return Object ID
   */
  public int get(int x, int y) {
    return buffer.getId(x, y);
  }

  /**
   * Get the buffer
   * 
   * @return Buffer
   */
  public PGraphics getBuffer() {
    return buffer;
  }

  public static String version() {
    return VERSION;
  }

  private void welcome() {
    System.out.println("##library.name## ##library.prettyVersion## by ##author##");
  }
}