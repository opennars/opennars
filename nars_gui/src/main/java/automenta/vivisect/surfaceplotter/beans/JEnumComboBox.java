/*
       ____  _____  ___  ____    __      ____  _____  _    _  ____  ____ 
      (  _ \(  _  )/ __)( ___)  /__\    (  _ \(  _  )( \/\/ )( ___)(  _ \
       )(_) ))(_)(( (__  )__)  /(__)\    )___/ )(_)(  )    (  )__)  )   /
      (____/(_____)\___)(____)(__)(__)  (__)  (_____)(__/\__)(____)(_)\_)

* Created 20 mai 2011 by : eric@doceapower.com
* Copyright Docea Power 2011
* Any reproduction or distribution prohibited without express written permission from Docea Power
***************************************************************************
*/
package automenta.vivisect.surfaceplotter.beans;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;


/**
 * @author eric
 *
 */
public abstract class JEnumComboBox<T extends Enum<T>> extends JComboBox {

	
	



	ModelBindedBeanProperty<T> property = new ModelBindedBeanProperty<T>("surfaceModel") {
		@Override protected void onPropertyChanged(PropertyChangeEvent evt) {
			Object newValue = evt.getNewValue();
			if (newValue != null)
				setSelectedItem(newValue);
		}
	};
	
	
	/**
	 * 
	 */
	@SuppressWarnings("ConstructorNotProtectedInAbstractClass")
	public JEnumComboBox(T[] values, String property) {
		super(values);
		this.property.setPropertyName(property);
		setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText( getEnumLabel((T) value)  );
				return c;
				
			}

		});
	}

	protected abstract String getEnumLabel(T value) ;
	
	/* intercept the state changed to fire my own
	 */
	@Override protected void fireItemStateChanged(ItemEvent e) {
		T old = property.getProperty();
		if (old !=null && !old.equals(e.getItem()))
			property.setProperty((T) e.getItem());
		super.fireItemStateChanged(e);
	}

	// ##########################################################################
	// DELEGATED SECTION BEGIN
	// ##########################################################################
	

	/**
	 * @return
	 * @see net.ericaro.surfaceplotter.beans.BeanProperty#getProperty()
	 */
	public T getProperty() {
		return property.getProperty();
	}

	

	
	/**
	 * @param value
	 * @see net.ericaro.surfaceplotter.beans.BeanProperty#setProperty(Object)
	 */
	public void setProperty(T value) {
		property.setProperty(value);
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
