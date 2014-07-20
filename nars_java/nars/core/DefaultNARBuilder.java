package nars.core;

/**
 * Default set of NAR parameters.
 * @author me
 */
public class DefaultNARBuilder extends NARBuilder {

    final static int DefaultLevels = 100;
    
    public DefaultNARBuilder() {
        super();
        
        setConceptBagLevels(DefaultLevels);
        setConceptBagSize(1000);        
        
        setTaskLinkBagLevels(DefaultLevels);        
        setTaskLinkBagSize(20);

        setTermLinkBagLevels(DefaultLevels);
        setTermLinkBagSize(100);
    }

    @Override
    public NARParams newInitialParams() {
        NARParams p = new NARParams();
        p.setSilenceLevel(0);
        return p;
    }
 
    
    
}
