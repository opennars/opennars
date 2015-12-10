
package automenta.vivisect.surfaceplotter.beans;

import automenta.vivisect.surfaceplotter.surface.AbstractSurfaceModel;

import java.beans.PropertyChangeEvent;




/** bind the "bean" attribute to a "source" property of a model provider.
 * @author eric
 * 
 */
public abstract class ModelBindedBeanProperty<PROP> extends BeanProperty<AbstractSurfaceModel, PROP>{
	
	
	
	
	BeanProperty<ModelSource, AbstractSurfaceModel> sourceBeanProperty = new BeanProperty<ModelSource, AbstractSurfaceModel>() {
		
		@Override protected void onPropertyChanged(PropertyChangeEvent evt) {
			ModelBindedBeanProperty.this.setBean((AbstractSurfaceModel) evt.getNewValue());
		}
	};
	
	
	@SuppressWarnings("ConstructorNotProtectedInAbstractClass")
	public ModelBindedBeanProperty(String sourcePropertyName) {
		setSourcePropertyName(sourcePropertyName);
	}
	
	/**
	 * @return the modelSource
	 */
	public ModelSource getSourceBean() {
		return sourceBeanProperty.getBean();
	}
	/**
	 * @param modelSource the modelSource to set
	 */
	public void setSourceBean(ModelSource modelSource) {
		sourceBeanProperty.setBean(modelSource);
	}
	/**
	 * @return the modelSourcePropertyName
	 */
	public String getSourcePropertyName() {
		return sourceBeanProperty.getPropertyName();
	}
	/**
	 * @param modelSourcePropertyName the modelSourcePropertyName to set
	 */
	public void setSourcePropertyName(String modelSourcePropertyName) {
		sourceBeanProperty.setPropertyName(modelSourcePropertyName);
	}
	
	
	
}
