package ca.nengo.ui.config.panels;

import ca.nengo.ui.config.Property;
import ca.nengo.ui.config.PropertyInputPanel;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public abstract class PropertyTextPanel extends PropertyInputPanel {
	
	protected enum TextError {
		NoError, ValueNotSet, InvalidFormat
	}
	
	protected final String valueNotSetMessage = "Value not set";
	protected String invalidFormatMessage = "Invalid number format";
	
    /**
     * Text field component
     */
	protected final JTextField textField;

	public PropertyTextPanel(Property property, int columns) {
		super(property);
		
		textField = new JTextField(columns);
		textField.addFocusListener(new TextFieldFocusListener());
		add(textField);
	}
	
	protected String getText() {
		return textField.getText();
	}
	
	/**
	 * Check if a string is valid as the value for this property, and set
	 * the appropriate status message.
	 * @param value the current text
	 * @return true if the text is valid, false otherwise
	 */
	protected abstract TextError checkValue(String value);
	
	protected void valueUpdated() {
		TextError error = checkValue(getText());
		switch (error) {
		case ValueNotSet:
			setStatusMsg(valueNotSetMessage);
			break;
		case InvalidFormat:
			setStatusMsg(invalidFormatMessage);
			break;
		default:
			setStatusMsg("");
		}
	}
	
	public boolean isValueSet() {
		return (checkValue(getText()) == TextError.NoError);
	}
	
    @Override
    public void setValue(Object value) {
        textField.setText(value.toString());
        checkValue(getText());
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
    }
	
	protected class TextFieldFocusListener implements FocusListener {

		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			valueUpdated();
		}
	}
	

}
