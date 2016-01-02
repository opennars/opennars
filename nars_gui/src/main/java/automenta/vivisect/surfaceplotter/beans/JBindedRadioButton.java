
package automenta.vivisect.surfaceplotter.beans;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**A {@link JRadioButton} binded to a boolean property of a ModelSource bean
 * @author Eric
 * 
 */
public class JBindedRadioButton extends JRadioButton {

	ModelBindedBeanProperty<Boolean> property = new ModelBindedBeanProperty<Boolean>("surfaceModel") {
		@Override protected void onPropertyChanged(PropertyChangeEvent evt) {
			Object newValue = evt.getNewValue();
			if (newValue != null)
				setSelected((Boolean) newValue);
		}
	};

	/**
	 * 
	 */
	public JBindedRadioButton() {}

	/**
	 * @param icon
	 */
	public JBindedRadioButton(Icon icon) {
		super(icon);
	}

	/**
	 * @param a
	 */
	public JBindedRadioButton(Action a) {
		super(a);
	}

	/**
	 * @param text
	 */
	public JBindedRadioButton(String text) {
		super(text);
	}

	/**
	 * @param icon
	 * @param selected
	 */
	public JBindedRadioButton(Icon icon, boolean selected) {
		super(icon, selected);
	}

	/**
	 * @param text
	 * @param selected
	 */
	public JBindedRadioButton(String text, boolean selected) {
		super(text, selected);
	}

	/**
	 * @param text
	 * @param icon
	 */
	public JBindedRadioButton(String text, Icon icon) {
		super(text, icon);
	}

	/**
	 * @param text
	 * @param icon
	 * @param selected
	 */
	public JBindedRadioButton(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
	}

	/* intercept the actionperformed to fire my own
	 */
	@Override protected void fireActionPerformed(ActionEvent event) {
		// toogles the property 
		Boolean old = property.getProperty();
		if (old != null && !old)
			property.setProperty(true);
		super.fireActionPerformed(event);
	}

	// ##########################################################################
	// DELEGATED SECTION BEGIN
	// ##########################################################################
	

	/**
	 * @return
	 * @see net.ericaro.surfaceplotter.beans.BeanProperty#getProperty()
	 */
	public Boolean getProperty() {
		return property.getProperty();
	}

	

	
	/**
	 * @param value
	 * @see net.ericaro.surfaceplotter.beans.BeanProperty#setProperty(Object)
	 */
	public void setProperty(Boolean value) {
		property.setProperty(value);
	}

	/**
	 * @return
	 * @see net.ericaro.surfaceplotter.beans.BeanProperty#getPropertyName()
	 */
	public String getPropertyName() {
		return property.getPropertyName();
	}

	/**
	 * @param propertyName
	 * @see net.ericaro.surfaceplotter.beans.BeanProperty#setPropertyName(String)
	 */
	public void setPropertyName(String propertyName) {
		property.setPropertyName(propertyName);
	}

	/**
	 * @return
	 * @see net.ericaro.surfaceplotter.beans.ModelBindedBeanProperty#getSourceBean()
	 */
	public ModelSource getSourceBean() {
		return property.getSourceBean();
	}
	
	/**
	 * @param modelSource
	 * @see net.ericaro.surfaceplotter.beans.ModelBindedBeanProperty#setSourceBean(Object)
	 */
	public void setSourceBean(ModelSource modelSource) {
		property.setSourceBean(modelSource);
	}


	// ##########################################################################
	// DELEGATED SECTION END
	// ##########################################################################

}
