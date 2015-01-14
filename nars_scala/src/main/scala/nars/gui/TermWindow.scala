package nars.gui

import java.awt._
import java.awt.event._
import nars.logic.entity.Concept
import nars.storage.Memory
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Window accept a Term, then display the content of the corresponding Concept
 */
class TermWindow(var memory: Memory) extends NarsFrame("Term Window") with ActionListener {

  /**
   Display label
   */
  private var termLabel: Label = new Label("Term:", Label.RIGHT)

  /**
   Input field for term name
   */
  private var termField: TextField = new TextField("")

  /**
   Control buttons
   */
  private var playButton: Button = new Button("Show")

  private var hideButton: Button = new Button("Hide")

//  super("Term Window")

  setBackground(NarsFrame.SINGLE_WINDOW_COLOR)

  val gridbag = new GridBagLayout()

  val c = new GridBagConstraints()

  setLayout(gridbag)

  c.ipadx = 3

  c.ipady = 3

  c.insets = new Insets(5, 5, 5, 5)

  c.fill = GridBagConstraints.BOTH

  c.gridwidth = 1

  c.weightx = 0.0

  c.weighty = 0.0

  termLabel.setBackground(NarsFrame.SINGLE_WINDOW_COLOR)

  gridbag.setConstraints(termLabel, c)

  add(termLabel)

  c.weightx = 1.0

  gridbag.setConstraints(termField, c)

  add(termField)

  c.weightx = 0.0

  playButton.addActionListener(this)

  gridbag.setConstraints(playButton, c)

  add(playButton)

  hideButton.addActionListener(this)

  gridbag.setConstraints(hideButton, c)

  add(hideButton)

  setBounds(400, 0, 400, 100)

  /**
   * Handling button click
   * @param e The ActionEvent
   */
  def actionPerformed(e: ActionEvent) {
    val b = e.getSource.asInstanceOf[Button]
    if (b == playButton) {
      val concept = memory.nameToConcept(termField.getText.trim())
      if (concept != null) {
        concept.startPlay(true)
      }
    } else if (b == hideButton) {
      close()
    }
  }

  private def close() {
    setVisible(false)
  }

  override def windowClosing(arg0: WindowEvent) {
    close()
  }
}
