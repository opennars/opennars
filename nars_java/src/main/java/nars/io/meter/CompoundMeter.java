package nars.io.meter;

import automenta.vivisect.TreeMLData;
import automenta.vivisect.timeline.Chart;
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

    
    @Override public void commit(final Memory memory) {
        super.commit(memory);
        
        for (AbstractMeter s : senses) {
            s.update(memory); //calls commit for each meter also
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

    /** creates an empty chart for the given chart id, or null if unspecified and use MeterVis default */
    public Chart newDefaultChart(String id, TreeMLData data) {
        return null;
    }

}
