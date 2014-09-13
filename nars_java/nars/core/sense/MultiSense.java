package nars.core.sense;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import nars.core.Memory;



public class MultiSense extends AbstractSense {
    private final AbstractSense[] senses;

    public MultiSense(AbstractSense... senses) {
        super(new TreeMap() /* alphabetic order */);
        this.senses = senses;
        setActive(active); //refresh after setting this.senses
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
 
    @Override public void setActive(final boolean b) {        
        this.active = b;
        if (senses!=null)
            for (AbstractSense s : senses) {
                s.setActive(active);
            }
    }

}
