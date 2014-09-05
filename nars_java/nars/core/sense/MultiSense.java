package nars.core.sense;

import java.util.HashSet;
import java.util.Set;
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

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet();
        for (AbstractSense s : senses) {
            keys.addAll(s.keySet());
        }
        return keys;
    }
 
    
}
