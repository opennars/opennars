package nars.gui

import java.awt._
import java.awt.event._
import nars.main.Parameters
import Bag
import BagWindow._
//remove if not needed
import scala.collection.JavaConversions._
import NarsFrame._

object BagWindow {

  /**
   The location of the display area, shifted according to the number of windows opened
   */
  private var counter: Int = _
}

/**
 * Window display the priority distribution of items within a given bag
 */
class BagWindow(var bag: Bag[_], title: String) extends NarsFrame(title) with ActionListener with AdjustmentListener {

  /**
   The lowest level displayed
   */
  var showLevel: Int = Parameters.BAG_THRESHOLD

  /**
   Control buttons
   */
  private var playButton: Button = new Button("Play")

  private var stopButton: Button = new Button("Stop")

  private var closeButton: Button = new Button("Close")

  /**
   Display area
   */
  private var text: TextArea = new TextArea("")

  /**
   Display label
   */
  private var valueLabel: Label = new Label(String.valueOf(showLevel), Label.RIGHT)

  /**
   Adjustable display level
   */
  private var valueBar: Scrollbar = new Scrollbar(Scrollbar.HORIZONTAL, showLevel, 0, 1, Parameters.BAG_LEVEL)

//  super(title)

  setBackground(NarsFrame.MULTIPLE_WINDOW_COLOR)

  val gridbag = new GridBagLayout()

  val c = new GridBagConstraints()

  setLayout(gridbag)

  c.ipadx = 3

  c.ipady = 3

  c.insets = new Insets(5, 5, 5, 5)

  c.fill = GridBagConstraints.BOTH

  c.gridwidth = GridBagConstraints.REMAINDER

  c.weightx = 1.0

  c.weighty = 1.0

  text.setBackground(DISPLAY_BACKGROUND_COLOR)

  text.setEditable(false)

  gridbag.setConstraints(text, c)

  add(text)

  c.weighty = 0.0

  c.gridwidth = 1

  gridbag.setConstraints(valueLabel, c)

  add(valueLabel)

  valueBar.addAdjustmentListener(this)

  gridbag.setConstraints(valueBar, c)

  add(valueBar)

  gridbag.setConstraints(playButton, c)

  playButton.addActionListener(this)

  add(playButton)

  gridbag.setConstraints(stopButton, c)

  stopButton.addActionListener(this)

  add(stopButton)

  gridbag.setConstraints(closeButton, c)

  closeButton.addActionListener(this)

  add(closeButton)

  setBounds(400, 60 + counter * 20, 400, 270)

  counter += 1

  setVisible(true)

  /**
   * Post the bag content
   * @param str The text
   */
  def post(str: String) {
    text.setText(str)
  }

  /**
   * Handling button click
   * @param e The ActionEvent
   */
  def actionPerformed(e: ActionEvent) {
    val source = e.getSource
    if (source == playButton) {
      bag.play()
    } else if (source == stopButton) {
      bag.stop()
    } else if (source == closeButton) {
      close()
    }
  }

  /**
   * Close the window
   */
  private def close() {
    bag.stop()
    dispose()
    counter -= 1
  }

  override def windowClosing(arg0: WindowEvent) {
    close()
  }

  /**
   * Handling scrollbar movement
   * @param e The AdjustmentEvent
   */
  def adjustmentValueChanged(e: AdjustmentEvent) {
    if (e.getSource == valueBar) {
      val v = valueBar.getValue
      valueLabel.setText(String.valueOf(v))
      valueBar.setValue(v)
      showLevel = v
      bag.play()
    }
  }
}
