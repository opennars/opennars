package objenome.solution.dependency;

/**
 * A simple implementation of the Dependency interface.
 *
 * @author sergio.oliveira.jr@gmail.com
 */
public class ConstructorDependency {

    private final String sourceFromContainer;

    private final Class<?> sourceType;
    private Object containerKey;

    public ConstructorDependency(String sourceFromContainer, Class<?> sourceType) {

        this.sourceFromContainer = sourceFromContainer;

        this.sourceType = sourceType;
    }

    
            
    public String getSource() {

        return sourceFromContainer;
    }

    public Class<?> getSourceType() {

        return sourceType;
    }

    @Override
    public int hashCode() {

        return sourceFromContainer.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof ConstructorDependency)) {
            return false;
        }

        ConstructorDependency d = (ConstructorDependency) obj;

        return d.sourceFromContainer.equals(sourceFromContainer);
    }

    @Override
    public String toString() {
        return "[ConstructorDependency: sourceFromContainer=" + sourceType + ';' + sourceFromContainer + ';' + containerKey + ']';
    }

    public void setContainerKey(Object sourceFromContainer) {
        containerKey = sourceFromContainer;
    }

    public Object getContainerKey() {
        return containerKey;
    }
    
}
