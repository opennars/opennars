package automenta.vivisect.swing.property.sheet.editor;

import automenta.vivisect.swing.property.beans.editor.AbstractPropertyEditor;
import automenta.vivisect.swing.property.sheet.ResizeLayout;

import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;


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

			if ("SubstancePanelUI".equals(getUI().getClass().getSimpleName())) {
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
		return value instanceof ObjectWrapper ? ((ObjectWrapper) value).value : value;
	}

	@Override
	public void setValue(Object value) {
		if (value != spinner.getValue()) {
			spinner.setValue(value);
		}
	}

	public static final class ObjectWrapper {

		private final Object value;

		public ObjectWrapper(Object value, Object visualValue) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			return Objects.equals(value, o);
		}

		@Override
		public int hashCode() {
			return value == null ? 0 : value.hashCode();
		}
	}
}
