
package automenta.vivisect.surfaceplotter.beans;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;


/** A {@link JCheckBox} binded to a boolean property of a ModelSource bean
 * @author Eric
 *
 */
public class JBindedCheckBox extends JCheckBox {

	
	ModelBindedBeanProperty<Boolean> property = new ModelBindedBeanProperty<Boolean>("surfaceModel") {
		
		@Override protected void onPropertyChanged(PropertyChangeEvent evt) {
			Object newValue = evt.getNewValue() ;
			if (newValue!=null) setSelected((Boolean) newValue);
		}
	};
	
	/**
	 * 
	 */
	public JBindedCheckBox() {
	}

	/**
	 * @param a
	 */
	public JBindedCheckBox(Action a) {
		super(a);
	}

	/**
	 * @param icon
	 * @param selected
	 */
	public JBindedCheckBox(Icon icon, boolean selected) {
		super(icon, selected);
	}

	/**
	 * @param icon
	 */
	public JBindedCheckBox(Icon icon) {
		super(icon);
	}

	/**
	 * @param text
	 * @param selected
	 */
	public JBindedCheckBox(String text, boolean selected) {
		super(text, selected);
	}

	/**
	 * @param text
	 * @param icon
	 * @param selected
	 */
	public JBindedCheckBox(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
	}

	/**
	 * @param text
	 * @param icon
	 */
	public JBindedCheckBox(String text, Icon icon) {
		super(text, icon);
	}

	/**
	 * @param text
	 */
	public JBindedCheckBox(String text) {
		super(text);
	}

	
	
	
	
	/* intercept the actionperformed to fire my own
	 */
	@Override protected void fireActionPerformed(ActionEvent event) {
		// toogles the property
		Object old= property.getProperty() ;
		if (old !=null) property.setProperty(! (Boolean) old);
		super.fireActionPerformed(event);
	}

	// ##########################################################################
// DELEGATE TO THE MODELBINDEDBEAN BEGIN
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
// DELEGATE TO THE MODELBINDEDBEAN END
// ##########################################################################


	
	
	
}
