package automenta.vivisect.swing.property.sheet.editor;

import java.awt.BorderLayout;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.UIManager;

import automenta.vivisect.swing.property.beans.editor.AbstractPropertyEditor;
import automenta.vivisect.swing.property.sheet.ResizeLayout;


/**
 * Editor containing spinner inside. It can be used for various models such as
 * date, numbers, chars, etc.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class SpinnerEditor extends AbstractPropertyEditor {

	protected class ContainerPanel extends JPanel implements KeyListener, FocusListener {

		private static final long serialVersionUID = 11429722436474288L;

		private JComponent component = null;
		private boolean active = false;

		public ContainerPanel(JComponent component) {

			this.component = component;

			addKeyListener(this);
			addFocusListener(this);

			setBorder(null);
			setFocusCycleRoot(true);
			setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy());

			add(component);

			if (getUI().getClass().getSimpleName().equals("SubstancePanelUI")) {
				setLayout(new ResizeLayout());
			} else {
				setLayout(new BorderLayout());
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			component.dispatchEvent(e);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			component.dispatchEvent(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			component.dispatchEvent(e);
		}

		@Override
		public void focusGained(FocusEvent e) {
			if (!active) {
				// received focus for the first time, mark component as active
				active = true;
				component.transferFocusDownCycle();
			} else {
				// received focus back from cycle, finish edit
				SpinnerEditor.this.firePropertyChange(oldValue, spinner.getValue());
			}
		}

		@Override
		public void focusLost(FocusEvent e) {
		}
	}

	private Object oldValue;

	protected JSpinner spinner = null;
	protected JPanel panel = null;

	public SpinnerEditor() {

		spinner = new JSpinner() {

			private static final long serialVersionUID = 6795837270307274730L;

			@Override
			public void setValue(Object value) {
				super.setValue(value);
			}

			@Override
			public void paint(Graphics g) {
				super.paint(g);
			}
		};

		spinner.setBorder(BorderFactory.createEmptyBorder());
		spinner.setOpaque(false);
		spinner.setFont(UIManager.getFont("Table.font"));
		spinner.setLocation(new Point(-1, -1));

		editor = new ContainerPanel(spinner);

		formatSpinner();
	}

	protected void formatSpinner() {
		DefaultEditor ne = (DefaultEditor) spinner.getEditor();
		ne.setFont(UIManager.getFont("Table.font"));
		ne.getTextField().setHorizontalAlignment(JTextField.LEFT);
		ne.getTextField().setAlignmentX(JTextField.LEFT_ALIGNMENT);
		ne.getTextField().setFont(UIManager.getFont("Table.font"));
	}

	@Override
	public Object getValue() {
		Object value = spinner.getValue();
		if (value instanceof ObjectWrapper) {
			return ((ObjectWrapper) value).value;
		} else {
			return value;
		}
	}

	@Override
	public void setValue(Object value) {
		if (value != spinner.getValue()) {
			spinner.setValue(value);
		}
	}

	public static final class ObjectWrapper {

		private Object value;

		public ObjectWrapper(Object value, Object visualValue) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (value == o || (value != null && value.equals(o))) {
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return value == null ? 0 : value.hashCode();
		}
	}
}
