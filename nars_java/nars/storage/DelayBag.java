
package nars.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import nars.core.Attention;
import nars.core.Attention.AttentionAware;
import nars.core.EventEmitter.Observer;
import nars.core.Events.CycleEnd;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.Concept;
import nars.entity.Item;
import nars.io.Texts;
import nars.storage.Bag.MemoryAware;

/**
 * Bag which uses time-since-last-activation and priority to decide which items are eligible for firing.
 */
public class DelayBag<E extends Item<K>,K> extends Bag<E,K> implements MemoryAware, AttentionAware, Observer {

    private final int capacity;
    private Map<K,E> items;
    private Deque<E> pending;
    private List<K> toRemove;
    private float activityThreshold = 0.8f;
    private float latencyMin = 200; /* in cycles */
    private float forgetThreshold = 0.01f;
    
    private int targetActivations;
    private int maxActivations;
    
    private int skippedPerSample = 0;
    
    
    float mass;
    private Memory memory;
    private long now;
    private Attention attention;

    public DelayBag(int capacity) {
        this.capacity = capacity;
        this.items = 
                Parameters.THREADS > 1 ? 
                    new ConcurrentHashMap(capacity) :
                    new HashMap(capacity);
        
        this.pending = new ConcurrentLinkedDeque<E>();
        this.targetActivations = (int)(0.1f * capacity);
        this.maxActivations = (int)(0.2f * capacity);
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

    protected E removeItem(final K k) {        
        E x = items.remove(k);
        if ((attention!=null) && (x instanceof Concept) && (x != null)) {
            attention.conceptRemoved((Concept)x);
        }
        return x;
    }
    
    //TODO use some other locking mechanism, synchronized may be restrictive
    protected void reload() {
        
        this.now = memory.time();
        int j = 0;
        for (final Map.Entry<K, E> s : items.entrySet()) {
            E e = s.getValue();                       
                
            //remove concepts
            if (size()-toRemove.size() > capacity) {
                if (e.getPriority() <= forgetThreshold) {
                    toRemove.add(e.name());                        
                } 
            }                            
            else if (ready(e)) {
                //ACTIVATE

                //shuffle
                if (j++ % 2 == 0)
                    pending.addFirst(e);
                else
                    pending.addLast(e);                
            }
        }
        
        for (final K k : toRemove) {
            removeItem(k);
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
            
            skippedPerSample = (int)Math.ceil(activated / maxActivations) - 1;
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
        
        
        if (activated > 0)
            System.out.println(Texts.n2(activityThreshold) + "(" + skippedPerSample + ") " + pending.size() + " / " + size());
        
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
    
    private AtomicBoolean busyReloading = new AtomicBoolean(false);
    
    protected boolean ensureLoaded() {
        if (pending.size() == 0) {
            
            //allow only one thread to reload, while the others try again later
            if (busyReloading.compareAndSet(false, true)) {
                
                reload();
                
                busyReloading.set(false);
                return true;
            }
            else {
                return false;
            }
        }
        return true;
    }
    
    
    @Override
    public E takeNext() {
        if (items.size() == 0) return null;

        if (!ensureLoaded())
            return null;
                
        for (int i = 0; i < skippedPerSample; i++) {
            pending.pollFirst();
        }
        E n = pending.pollFirst();
        if (n!=null)
            return take(n.name());
        else
            return null;
    }

    @Override
    public E peekNext() {
        if (size() == 0) return null;
        
        if (pending.isEmpty())
            return null;
        
        return pending.peekFirst();
    }

    @Override
    protected E addItem(E x) {                    
        E previous = items.put(x.name(), x);
        return null;
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
        this.memory.event.on(CycleEnd.class, this);
    }

    @Override
    public void setAttention(Attention a) {
        this.attention = a;
    }

    @Override
    public void event(Class event, Object[] arguments) {
        if (event == CycleEnd.class) {            
            //ensure loaded for the next cycle
            //ensureLoaded();
        }
    }
    
    
    
}
