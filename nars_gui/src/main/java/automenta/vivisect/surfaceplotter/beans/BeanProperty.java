package automenta.vivisect.surfaceplotter.beans;

import java.beans.*;

/** Observe a standard Bean's property, and provide easy, type safe access to the get and set.
 * 
 * @author Eric
 *
 */
public abstract class BeanProperty<BEAN, PROP> {

	BEAN bean;
	String propertyName;
	PropertyChangeListener propertyObserver = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (propertyName!=null && propertyName.equals(evt.getPropertyName() ) )
				onPropertyChanged(evt);
		}
	};

	transient BeanInfo beaninfo;
	transient EventSetDescriptor eventSetDescriptor;
	transient PropertyDescriptor propertyDescriptor;
	
	
	
	
	/** Sets the actual bean object
	 * 
	 * @param model
	 */
	public void setBean(BEAN model) {
		Object old = bean;
		bean = model;
		bind(old);
		
	}
	
	protected abstract void onPropertyChanged(PropertyChangeEvent evt);
	
	/* (non-Javadoc)
	 * @see javax.swing.DefaultButtonModel#isSelected()
	 */
	public PROP getProperty() {
		if (propertyDescriptor == null || bean == null)
			return null;
		try {
			return (PROP) propertyDescriptor.getReadMethod().invoke(bean);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.DefaultButtonModel#setPressed(boolean)
	 */
	public void setProperty(PROP value) {
		if (propertyDescriptor == null)
			return; // simple security
		try {
			propertyDescriptor.getWriteMethod().invoke(bean, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the propertyName
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/** attribute to bind to.
	 * @param propertyName usual lowercased property name
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
		bind(bean);
	}

	/**
	 * @return the bean
	 */
	public BEAN getBean() {
		return bean;
	}
	
	
	
	private Object unregister(Object bean) {
		if (bean != null && propertyObserver !=null) {
			try {
				eventSetDescriptor.getRemoveListenerMethod().invoke(bean, propertyObserver);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return getProperty(); // return the old value
	}
	
	private Object register() {
		beaninfo = null;
		eventSetDescriptor = null;
		propertyDescriptor = null;

		if (bean != null) {
			try {
				// introspect the bean
				beaninfo = Introspector.getBeanInfo(bean.getClass());

				// extract the property
				for (PropertyDescriptor prop : beaninfo.getPropertyDescriptors())
					if (prop.getName().equals(propertyName)) {
						propertyDescriptor = prop;
						break;
					}
				// extract the events
				for (EventSetDescriptor ev : beaninfo.getEventSetDescriptors())
					if (ev.getListenerType() == PropertyChangeListener.class) {
						eventSetDescriptor = ev;
						break;
					}
				// now I know the event, and the property, I can use it 
				if (propertyObserver!=null) 
					eventSetDescriptor.getAddListenerMethod().invoke(bean, propertyObserver);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return getProperty();
	}
	
	private void bind(Object oldBean) {
		Object oldValue = unregister(oldBean);
		Object newValue = register() ;
		if (bean!=null && propertyName !=null) onPropertyChanged(new PropertyChangeEvent(bean, propertyName, oldValue, newValue)); 
		
	}
	
	
	
	
	
	
}