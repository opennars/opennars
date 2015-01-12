
package automenta.vivisect.surfaceplotter.beans;

import automenta.vivisect.surfaceplotter.surface.AbstractSurfaceModel;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;


/** A Bean that "handles" an DefaultSurfaceModel, editors  get ginded to this source to display an attribute.
 * @author eric
 *
 */
public class ModelSource {

	SwingPropertyChangeSupport event = new SwingPropertyChangeSupport(this);
	AbstractSurfaceModel surfaceModel;
	
	
	/**
	 * @return the surfaceModel
	 */
	public AbstractSurfaceModel getSurfaceModel() {
		return surfaceModel;
	}

	/**
	 * @param surfaceModel the surfaceModel to set
	 */
	public void setSurfaceModel(AbstractSurfaceModel surfaceModel) {
		Object old = this.surfaceModel ;
		this.surfaceModel = surfaceModel;
		event.firePropertyChange("surfaceModel", old, surfaceModel);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		event.addPropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		event.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		event.removePropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		event.removePropertyChangeListener(propertyName, listener);
	}
	
	
	
	
}
