package nars.gui

import java.awt._
import java.awt.event._
//remove if not needed
import scala.collection.JavaConversions._
import NarsFrame._

/**
 * Window displaying a system parameter that can be adjusted in run time
 */
class ParameterWindow(title: String, var defaultValue: Int) extends NarsFrame(title) with ActionListener with AdjustmentListener {

  /**
   Display label
   */
  private var valueLabel: Label = new Label(String.valueOf(defaultValue), Label.CENTER)

  /**
   Control buttons
   */
  private var hideButton: Button = new Button("Hide")

  private var undoButton: Button = new Button("Undo")

  private var defaultButton: Button = new Button("Default")

  /**
   Adjusting bar
   */
  private var valueBar: Scrollbar = new Scrollbar(Scrollbar.HORIZONTAL, defaultValue, 0, 0, 101)

  /**
   parameter values
   */
  private var previousValue: Int = _

  private var currentValue: Int = _

//  super(title)

  setLayout(new GridLayout(3, 3, 8, 4))

  setBackground(NarsFrame.SINGLE_WINDOW_COLOR)

  val sp1 = new Label("")

  sp1.setBackground(SINGLE_WINDOW_COLOR)

  add(sp1)

  valueLabel.setBackground(SINGLE_WINDOW_COLOR)

  add(valueLabel)

  val sp2 = new Label("")

  sp2.setBackground(SINGLE_WINDOW_COLOR)

  add(sp2)

  add(new Label("0", Label.RIGHT))

  valueBar.addAdjustmentListener(this)

  add(valueBar)

  add(new Label("100", Label.LEFT))

  undoButton.addActionListener(this)

  add(undoButton)

  defaultButton.addActionListener(this)

  add(defaultButton)

  hideButton.addActionListener(this)

  add(hideButton)

  this.setBounds(300, 300, 250, 120)

  /**
   * Get the value of the parameter
   * @return The current value
   */
  def value(): Int = currentValue

  /**
   * Handling button click
   * @param e The ActionEvent
   */
  def actionPerformed(e: ActionEvent) {
    val s = e.getSource
    if (s == defaultButton) {
      currentValue = defaultValue
      valueBar.setValue(currentValue)
      valueLabel.setText(String.valueOf(currentValue))
    } else if (s == undoButton) {
      currentValue = previousValue
      valueBar.setValue(currentValue)
      valueLabel.setText(String.valueOf(currentValue))
    } else if (s == hideButton) {
      close()
    }
  }

  private def close() {
    previousValue = currentValue
    setVisible(false)
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
      currentValue = v
    }
  }
}
