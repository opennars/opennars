package automenta.vivisect.swing.property.sheet;

import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.UIManager;

import automenta.vivisect.swing.property.model.DefaultBeanInfoResolver;
import automenta.vivisect.swing.property.propertysheet.Property;
import automenta.vivisect.swing.property.propertysheet.PropertySheetPanel;


/**
 * Binds a bean object to a PropertySheet.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class DefaultBeanBinder implements PropertyChangeListener {

	private Object object = null;
	private BeanInfo info = null;
	private PropertySheetPanel sheet = null;

	public DefaultBeanBinder(Object object, PropertySheetPanel sheet) {
		this(object, sheet, new DefaultBeanInfoResolver().getBeanInfo(object));
	}

	public DefaultBeanBinder(Object object, PropertySheetPanel sheet, BeanInfo info) {

		if (info == null) {
			throw new IllegalArgumentException(String.format("Cannot find %s for %s", BeanInfo.class.getSimpleName(), object.getClass()));
		}

		this.object = object;
		this.sheet = sheet;
		this.info = info;

		bind();
	}

	public void bind() {
		sheet.setProperties(info.getPropertyDescriptors());
		sheet.readFromObject(object);
		sheet.addPropertySheetChangeListener(this);
	}

	public void unbind() {
		sheet.removePropertyChangeListener(this);
		sheet.setProperties(new Property[0]);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {

		Property prop = (Property) event.getSource();

		try {
			prop.writeToObject(object);
		} catch (RuntimeException e) {

			if (e.getCause() instanceof PropertyVetoException) {
				UIManager.getLookAndFeel().provideErrorFeedback(sheet);
				prop.setValue(event.getOldValue());
			} else {
				throw e;
			}
		}
	}
}
