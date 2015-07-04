package automenta.vnc.viewer.swing.gui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * @author dime at tightvnc.com
 *
 * Using idea by Thomas Bierhance from http://www.orbital-computer.de/JComboBox/
 */
public class AutoCompletionComboEditorDocument extends PlainDocument {

	private final ComboBoxModel model;
	private boolean selecting;
	private final JComboBox comboBox;
	private final boolean hidePopupOnFocusLoss;
	private final JTextComponent editor;

	public AutoCompletionComboEditorDocument(final JComboBox comboBox) {
		this.comboBox = comboBox;
		this.model = comboBox.getModel();
		this.editor = (JTextComponent)comboBox.getEditor().getEditorComponent();
		editor.setDocument(this);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!selecting) highlightCompletedText(0);
			}
		});

		Object selectedItem = comboBox.getSelectedItem();
		if (selectedItem!=null) {
			setText(selectedItem.toString());
			highlightCompletedText(0);
		}
		hidePopupOnFocusLoss = System.getProperty("java.version").startsWith("1.5");
		editor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (hidePopupOnFocusLoss) comboBox.setPopupVisible(false);
			}
		});
	}

	@Override
	public void remove(int offs, int len) throws BadLocationException {
		if (selecting) return;
		super.remove(offs, len);
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		if (selecting) return;
		super.insertString(offs, str, a);
		Object item = lookupItem(getText(0, getLength()));
		if (item != null) {
			setSelectedItem(item);
			setText(item.toString());
			highlightCompletedText(offs + str.length());
			if (comboBox.isDisplayable()) comboBox.setPopupVisible(true);
		}
	}

	private void setText(String text) {
		try {
			super.remove(0, getLength());
			super.insertString(0, text, null);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	private void setSelectedItem(Object item) {
		selecting = true;
		model.setSelectedItem(item);
		selecting = false;
	}

	private void highlightCompletedText(int offs) {
		JTextComponent editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
		editor.setSelectionStart(offs);
		editor.setSelectionEnd(getLength());
	}

	private Object lookupItem(String pattern) {
		Object selectedItem = model.getSelectedItem();
		if (selectedItem != null && startsWithIgnoreCase(selectedItem, pattern)) {
			return selectedItem;
		} else {
			for (int i = 0, n = model.getSize(); i < n; i++) {
				Object currentItem = model.getElementAt(i);
				if (startsWithIgnoreCase(currentItem, pattern)) {
					return currentItem;
				}
			}
		}
		return null;
	}

	private boolean startsWithIgnoreCase(Object currentItem, String pattern) {
		return currentItem.toString().toLowerCase().startsWith(pattern.toLowerCase());
	}


}
