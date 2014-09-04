package nars.core.sense;

import nars.core.Memory;

/**
 *
 * @author me
 */


public class MultiSense extends AbstractSense {
    private final AbstractSense[] senses;

    public MultiSense(AbstractSense... senses) {
        super();
        this.senses = senses;
    }

    
    @Override public void sense(final Memory memory) {
        
        for (AbstractSense s : senses) {
            s.update(memory);
            dataMap.putAll(s.dataMap);
        }
        
    }
    
}
