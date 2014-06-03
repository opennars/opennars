package nars.gui

import java.awt._
import java.awt.event._
import nars.io._
//remove if not needed
import scala.collection.JavaConversions._
import NarsFrame._

/**
 * Window displaying inference log
 */
class InferenceWindow(var recorder: InferenceRecorder) extends NarsFrame("Inference log") with ActionListener with ItemListener {

  /**
   Control buttons
   */
  private var playButton: Button = new Button("Play")

  private var stopButton: Button = new Button("Stop")

  private var hideButton: Button = new Button("Hide")

  /**
   Display area
   */
  private var text: TextArea = new TextArea("")

  /**
   String to be caught
   */
  private var watchText: TextField = new TextField(20)

  /**
   Type of caught text
   */
  private var watchType: Choice = new Choice()

  /**
   Type of caught text
   */
  private var watched: String = ""

//  super("Inference log")

  setBackground(NarsFrame.SINGLE_WINDOW_COLOR)

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

  gridbag.setConstraints(watchText, c)

  add(watchText)

  watchType.add("No Watch")

  watchType.add("Watch Term")

  watchType.add("Watch String")

  gridbag.setConstraints(watchType, c)

  watchType.addItemListener(this)

  add(watchType)

  gridbag.setConstraints(playButton, c)

  playButton.addActionListener(this)

  add(playButton)

  gridbag.setConstraints(stopButton, c)

  stopButton.addActionListener(this)

  add(stopButton)

  gridbag.setConstraints(hideButton, c)

  hideButton.addActionListener(this)

  add(hideButton)

  setBounds(400, 200, 400, 400)

  /**
   * Clear display
   */
  def clear() {
    text.setText("")
  }

  /**
   * Append a new line to display
   * @param str Text to be added into display
   */
  def append(str: String) {
    text.append(str)
    if (watched != "" && (str.indexOf(watched) != -1)) {
      recorder.stop()
    }
  }

  /**
   * Handling button click
   * @param e The ActionEvent
   */
  def actionPerformed(e: ActionEvent) {
    val s = e.getSource
    if (s == playButton) {
      recorder.play()
    } else if (s == stopButton) {
      recorder.stop()
    } else if (s == hideButton) {
      close()
    }
  }

  def itemStateChanged(event: ItemEvent) {
    val request = watchText.getText.trim()
    if (request != "") {
      watched = request
    }
  }

  private def close() {
    recorder.stop()
    dispose()
  }

  override def windowClosing(arg0: WindowEvent) {
    close()
  }

  /**
   * Change background color to remind the on-going file saving
   */
  def switchBackground() {
    text.setBackground(SAVING_BACKGROUND_COLOR)
  }

  /**
   * Reset background color after file saving
   */
  def resetBackground() {
    text.setBackground(DISPLAY_BACKGROUND_COLOR)
  }
}
