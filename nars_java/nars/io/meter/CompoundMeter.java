package nars.io.meter;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import nars.core.Memory;



public class CompoundMeter extends AbstractMeter {
    public final AbstractMeter[] senses;

    public CompoundMeter(AbstractMeter... senses) {
        super(new TreeMap() /* alphabetic order */);
        this.senses = senses;
        setActive(active); //refresh after setting this.senses
    }

    
    @Override public void sense(final Memory memory) {
        
        for (AbstractMeter s : senses) {
            s.update(memory);
            dataMap.putAll(s.dataMap);
        }
        
    
    }

    
    @Override
    public Set<String> keySet() {
        Set<String> keys = new TreeSet();
        for (AbstractMeter s : senses) {
            keys.addAll(s.keySet());
        }
        return keys;
    }
    
 
    @Override public void setActive(final boolean b) {        
        this.active = b;
        if (senses!=null)
            for (AbstractMeter s : senses) {
                s.setActive(active);
            }
    }

}
