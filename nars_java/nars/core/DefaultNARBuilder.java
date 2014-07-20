package nars.core;

/**
 * Default set of NAR parameters.
 * @author me
 */
public class DefaultNARBuilder extends NARBuilder {

    public DefaultNARBuilder() {
        super();
        
        setSilenceLevel(0);
        setConceptBagSize(1000);
        setBagLevels(100);
    }
    
    
}
