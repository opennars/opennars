package nars.core;

/**
 * Default set of NAR parameters.
 * @author me
 */
public class DefaultNARBuilder extends NARBuilder {

    public DefaultNARBuilder() {
        super();
        
        setConceptBagSize(1000);
        setBagLevels(100);
    }

    @Override
    public NARParams newInitialParams() {
        NARParams p = new NARParams();
        p.setSilenceLevel(0);
        return p;
    }
 
    
    
}
