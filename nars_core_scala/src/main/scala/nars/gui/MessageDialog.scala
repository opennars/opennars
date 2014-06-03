package nars.gui

import java.awt._
import java.awt.event._
//remove if not needed
import scala.collection.JavaConversions._

/**
 * Pop-up message for the user to accept
 */
class MessageDialog(parent: Frame, message: String) extends Dialog(parent, "Message", false) with ActionListener with WindowListener {

  protected var button: Button = new Button(" OK ")

  protected var text: TextArea = new TextArea(message)

//  super(parent, "Message", false)

  setLayout(new BorderLayout(5, 5))

  setBackground(NarsFrame.SINGLE_WINDOW_COLOR)

  setFont(NarsFrame.NarsFont)

  text.setBackground(NarsFrame.DISPLAY_BACKGROUND_COLOR)

  this.add("Center", text)

  button.addActionListener(this)

  val p = new Panel()

  p.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5))

  p.add(button)

  this.add("South", p)

  setModal(true)

  setBounds(200, 250, 400, 180)

  addWindowListener(this)

  setVisible(true)

  /**
   * Handling button click
   * @param e The ActionEvent
   */
  def actionPerformed(e: ActionEvent) {
    if (e.getSource == button) {
      close()
    }
  }

  private def close() {
    this.setVisible(false)
    this.dispose()
  }

  override def windowActivated(arg0: WindowEvent) {
  }

  override def windowClosed(arg0: WindowEvent) {
  }

  override def windowClosing(arg0: WindowEvent) {
    close()
  }

  override def windowDeactivated(arg0: WindowEvent) {
  }

  override def windowDeiconified(arg0: WindowEvent) {
  }

  override def windowIconified(arg0: WindowEvent) {
  }

  override def windowOpened(arg0: WindowEvent) {
  }
}
