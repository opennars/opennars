package nars.meter;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import nars.NAR;
import nars.event.NARReaction;

import java.io.PrintStream;

/**
 *
 * @author me
 */
public class ItemCounter<E> extends NARReaction {

    public final Multiset<E> items = HashMultiset.create();
    
    public ItemCounter(NAR n, Class... event) {
        super(n, true, event);
    }

    @Override
    public void event(Class event, Object[] arguments) {
        if (arguments.length > 0) {
                E item = (E)arguments[0];

                int alreadyExisted;
                synchronized (items) {
                    alreadyExisted = items.add(item, 1);
                }
                
                if (alreadyExisted == 0) {
                    onFirstAdd(item);
                }
            
        }
    }

    
    @Override
    public String toString() {
        return items.toString();
    }

    public void clear() { items.clear(); }
    
    public void report(PrintStream out) {        
        synchronized (items) {
            System.out.println(items.size() + " total, " + items.elementSet().size() + " unique");
            for (E e : items.elementSet()) {
                System.out.println(items.count(e) + ": "  + e);            
            }
            System.out.println();
        }
    }

    protected void onFirstAdd(E item) {
    }

    
    
}
