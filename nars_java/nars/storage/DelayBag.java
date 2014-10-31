
package nars.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import nars.core.Attention;
import nars.core.Attention.AttentionAware;
import nars.core.Memory;
import nars.entity.Concept;
import nars.entity.Item;
import nars.io.Texts;
import nars.storage.Bag.MemoryAware;

/**
 * Bag which uses time-since-last-activation and priority to decide which items are eligible for firing.
 */
public class DelayBag<E extends Item<K>,K> extends Bag<E,K> implements MemoryAware, AttentionAware {

    private final int capacity;
    private Map<K,E> items;
    private Deque<E> pending;
    private List<K> toRemove;
    private float activityThreshold = 0.8f;
    private float latencyMin = 300; /* in cycles */
    private float forgetThreshold = 0.01f;
    
    private int targetActivations = 10;

    
    private int skippedPerSample = 0;
    
    
    float mass;
    private Memory memory;
    private long now;
    private Attention attention;

    public DelayBag(int capacity) {
        this.capacity = capacity;
        this.items = new ConcurrentHashMap<K, E>(capacity) {

            @Override
            public E remove(Object key) {
                E x = super.remove(key);
                if ((x instanceof Concept) && (x != null)) {
                    attention.conceptRemoved((Concept)x);
                }
                return x;
            }
          
            
        };
        this.pending = new ConcurrentLinkedDeque<E>();
        this.toRemove = new ArrayList();
        this.mass = 0;
    }

    @Override
    public synchronized void clear() {
        items.clear();
        pending.clear();
        mass = 0;
    }

    @Override
    public E get(K key) {
        return items.get(key);
    }

    @Override
    public Set<K> keySet() {
        return items.keySet();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public float getMass() {
        return mass;
    }

    //TODO use some other locking mechanism, synchronized may be restrictive
    protected synchronized void reload() {
        if (!pending.isEmpty()) return; //prevent other threads from re-attepting reload
        
        this.now = memory.time();
        int j = 0;
        for (Map.Entry<K, E> s : items.entrySet()) {
            E e = s.getValue();
            if (ready(e)) {
                //ACTIVATE
                
                //shuffle
                if (j++ % 2 == 0)
                    pending.addFirst(e);
                else
                    pending.addLast(e);                
            }
            else if (forget(e))
                toRemove.add(e.name());
                
        }
        
        for (final K k : toRemove) {
            E r = items.remove(k);
            if (r instanceof Concept) 
                attention.conceptRemoved((Concept)r);            
        }
        
        toRemove.clear();
    
        //ADJUST FIRING THRESHOLD
        int activated = pending.size();        
        if (activated < targetActivations) {
            //too few activated, reduce threshold
            activityThreshold *= 0.99f;
            if (activityThreshold < 0.01) activityThreshold = 0.01f;
            
            skippedPerSample = 0;
        }
        else if (activated > targetActivations) {
            //too many, increase threshold
            activityThreshold *= 1.1f;
            if (activityThreshold > 0.99) activityThreshold = 0.99f;
            
            skippedPerSample = (int)Math.ceil(activated / targetActivations) - 1;
            if (skippedPerSample < 0) skippedPerSample = 0;
        }
        
        //ADJUST FORGET THRESHOLD
        int s = size();
        if (s > capacity) {
            forgetThreshold *= 1.01f;
            if (forgetThreshold > 0.99f) forgetThreshold = 0.99f;
        }
        else if (s < capacity) {
            forgetThreshold *= 0.99f;
            if (forgetThreshold < 0.0f) forgetThreshold = 0.0f;
        }
        
        /*
        if (activated > 0)
            System.out.println(Texts.n2(activityThreshold) + "(" + skippedPerSample + ") " + pending.size() + " / " + size());
        */
    }
    
    protected boolean forget(E c) {
        if (size() > capacity) {
            return (c.getPriority() < forgetThreshold);
        }
        return false;
    }
    
    protected boolean ready(final E c) {
        
        final float firingAge = now - c.budget.getLastForgetTime();        
        
        float activity = c.budget.getPriority();                
        
        //System.out.println(firingAge + " " + activity + " " + latencyMin);
        
        //increase latency according to activity threshold, so an increase in activitythreshold
        //is matched by increase in latency threshold       
        float latencyScale = (1f + activityThreshold);
        
        /*if (activity < activityThreshold) {
            activity += (activityThreshold - activity) * (firingAge / (latencyCyclesMax*latencyScale));
        }*/
                
        if ((firingAge >= latencyMin*latencyScale) && (activity >= activityThreshold )) {
            return true;
        }

        return false;
    }
    
    @Override
    public E takeNext() {
        if (items.size() == 0) return null;
                
        if (pending.isEmpty())
            reload();        
        
        for (int i = 0; i < skippedPerSample; i++) {
            pending.pollFirst();
        }
        return pending.pollFirst();
    }

    @Override
    public E peekNext() {
        if (size() == 0) return null;
        
        if (pending.isEmpty())
            reload();
        
        return pending.peekFirst();
    }

    @Override
    protected E addItem(E x) {
        return items.put(x.name(), x);
    }

    @Override
    public E take(K key) {
        return items.remove(key);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public Collection<E> values() {
        return items.values();
    }

    @Override
    public float getAveragePriority() {
        int s = size();
        if (s == 0) return 0;
        float t = 0;
        for (E e : values())
            t += e.getPriority();
        return t / s;        
    }

    @Override
    public Iterator<E> iterator() {
        return items.values().iterator();
    }

    @Override
    public void setMemory(Memory m) {
        this.memory = m;
    }

    @Override
    public void setAttention(Attention a) {
        this.attention = a;
    }
    
    
    
}
