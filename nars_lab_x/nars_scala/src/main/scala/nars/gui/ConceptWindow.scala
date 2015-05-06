package nars.gui

import java.awt._
import java.awt.event._
import nars.logic.entity.Concept
import ConceptWindow._
//remove if not needed
import scala.collection.JavaConversions._

object ConceptWindow {

  /**
   Used to adjust the screen position
   */
  private var instanceCount: Int = 0
}

/**
 * Window displaying the content of a Concept, such as beliefs, goals, and questions
 */
class ConceptWindow(var concept: Concept) extends NarsFrame(concept.getKey) with ActionListener {

  /**
   Control buttons
   */
  private var playButton: Button = new Button("Play")

  private var stopButton: Button = new Button("Stop")

  private var playInNewWindowButton: Button = new Button("Play in New Window")

  private var closeButton: Button = new Button("Close")

  /**
   Display area
   */
  private var text: TextArea = new TextArea("")

//  super(concept.getKey)

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

  text.setBackground(NarsFrame.DISPLAY_BACKGROUND_COLOR)

  text.setEditable(false)

  gridbag.setConstraints(text, c)

  add(text)

  c.weighty = 0.0

  c.gridwidth = 1

  gridbag.setConstraints(playButton, c)

  playButton.addActionListener(this)

  add(playButton)

  gridbag.setConstraints(stopButton, c)

  stopButton.addActionListener(this)

  add(stopButton)

  gridbag.setConstraints(playInNewWindowButton, c)

  playInNewWindowButton.addActionListener(this)

  add(playInNewWindowButton)

  gridbag.setConstraints(closeButton, c)

  closeButton.addActionListener(this)

  add(closeButton)

  setBounds(400 + (instanceCount % 10) * 10, 60 + (instanceCount % 10) * 20, 400, 270)

  instanceCount

  setVisible(true)

  /**
   * Display the content of the concept
   * @param str The text to be displayed
   */
  def post(str: String) {
    text.setText(str)
  }

  /**
   * This is called when Concept removes this as its window.
   */
  def detachFromConcept() {
    playButton.setEnabled(false)
    stopButton.setEnabled(false)
  }

  /**
   * Handling button click
   * @param e The ActionEvent
   */
  def actionPerformed(e: ActionEvent) {
    val s = e.getSource
    if (s == playButton) {
      concept.play()
    } else if (s == stopButton) {
      concept.stop()
    } else if (s == playInNewWindowButton) {
      concept.stop()
      concept.startPlay(false)
    } else if (s == closeButton) {
      close()
    }
  }

  private def close() {
    concept.stop()
    dispose()
  }

  override def windowClosing(e: WindowEvent) {
    close()
  }
}
