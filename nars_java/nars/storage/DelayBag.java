
package nars.storage;

import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
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
import nars.inference.BudgetFunctions;
import nars.storage.Bag.MemoryAware;

/**
 * Bag which uses time-since-last-activation and priority to decide which items are eligible for firing.
 * 
 *
 *
 * From: tony_lofthouse@btinternet.com  *
 * Concepts are defined as agents, so can run in parallel. To keep the number of
 * ‘active’ agents manageable I do a few things; 1) concepts have a latency
 * period and can’t be activated whilst they are latent. 2) The activation
 * threshold is dynamic and is adjusted to keep the number of ‘active’ concepts
 * within a manageable range. 3) Some concepts are inhibited (using negative
 * truth values (0.5) as inhibitor). So each cycle all ‘eligible’ concepts are
 * activated, cycles have a fixed time unit (parameter adjustable, currently 13
 * cycles/sec (Human alpha wave freq!)), latency is 8 cycles. Another difference
 * is that I process more than one task and belief per concept per cycle. My
 * system functions more like a neural net than NARS so has different dynamics.
 *
 * Forgetting is a separate process which runs in parallel. When memory starts
 * to get low, the process removes low ‘durability’ items. (My attention
 * allocation is different to Pei’s).  *
 * The latency period simulates the recovery period for neurons whereby a neuron
 * cannot fire after previously firing for the specified recovery period.
 *
 * Each concept records the systemTime when it was last activated. So a concept
 * is latent if the current systemTime(in cycles) minus the last activation time
 * (in cycles) is less then the latency period (e.g. 8 cycles). The latency
 * period is a parameter so can be adjusted. 8 cycles just happen to have given
 * me the best results on my current test data.
 *
 * Each cycle 'All' concepts that are active and not latent fire. By adjusting
 * the activation threshold this number can be quite small even in a very large
 * network.
 *
 * In an earlier version of the system I did use this approach with ConceptBags
 * - however, extracting a non-latent concept was not very efficient. I had to
 * TakeOut() then check the latency - this lead to quite a few misses before
 * getting a usable concept.
 *
 * The agent based approach in place now is much more efficient. Because every
 * concept is an agent it decides whether it needs to fire. Concepts only have
 * the potential to fire if they have received a new task. So again, this limits
 * the number of concepts to fire each cycle.
 *
 * In summary, each cycle, all new tasks are 'dispatched’’ to the relevant
 * concepts and ‘all’ the concepts that are not latent and have an activation
 * level above the dynamic threshold are fired. There is a final check on each
 * concept so that it only fires once it has processed all of its agent messages
 * (Tasks)
 *
 *
 */
public class DelayBag<E extends Item<K>,K> extends Bag<E,K> implements MemoryAware, AttentionAware, Observer {

    private final int capacity;
    
    private Map<K,E> items;
    private Deque<E> pending;
    private List<K> toRemove;
    
    private float activityThreshold = 0.8f;
    private float latencyMin = 0; /* in cycles */
    private float forgetThreshold = 0.01f;
    
    private int targetActivations;
    private int maxActivations;
    
    private int skippedPerSample = 0;
    
    
    private double numPriorityThru = 0; //TODO
    private double totalPriorityThru = 0; //TODO
    float mass; //TODO
    
    private Memory memory;
    private long now;
    private Attention attention;
    private final Iterator<Map.Entry<K, E>> itemIterator;

    public DelayBag(int capacity) {
        this.capacity = capacity;
        this.items = 
                //Parameters.THREADS > 1 ? 
                    new ConcurrentHashMap(capacity);// :
                  //  new HashMap(capacity);
        this.itemIterator = Iterators.cycle(items.entrySet());
        this.pending = new ConcurrentLinkedDeque<E>();
        this.targetActivations = (int)(0.1f * capacity);
        this.maxActivations = (int)(0.2f * capacity);
        this.toRemove = new ArrayList();
        this.mass = 0;
    }
    
    @Override
    public void setMemory(Memory m) {
        this.memory = m;
        this.memory.event.on(CycleEnd.class, this);
        
        //This assumes the bag is used for concepts:
        this.latencyMin = m.param.cycles(memory.param.conceptForgetDurations);
    }

    @Override
    public synchronized void clear() {
        items.clear();
        pending.clear();
        mass = 0;
        numPriorityThru = 0;
        totalPriorityThru = 0;
    }

    public void setLatencyMin(float latencyMin) {
        this.latencyMin = latencyMin;
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
        int iterations = 0;
        int maxIterations = size();
        float forgetCycles = memory.param.cycles(memory.param.beliefForgetDurations);
        int originalSize = size();
        
        numPriorityThru = 0;
        totalPriorityThru = 0;
                
        while (itemIterator.hasNext() && (pending.size() < targetActivations) && (iterations < maxIterations)) {
            Map.Entry<K, E> ee = itemIterator.next();            
            E e = ee.getValue();
                           
            BudgetFunctions.forgetPeriodic(e.budget, forgetCycles, Parameters.BAG_THRESHOLD, now);
            
            
            totalPriorityThru += e.getPriority();
            numPriorityThru++;
            
            //remove concepts
            if (originalSize-toRemove.size() > capacity) {
                if (e.getPriority() <= forgetThreshold) {
                    toRemove.add(e.name());                        
                } 
            }                            
            if (ready(e)) {
                //ACTIVATE

                //shuffle
                if (j++ % 2 == 0)
                    pending.addFirst(e);
                else
                    pending.addLast(e);                
            }
            
            iterations++;
        }
        
        for (final K k : toRemove) {
            removeItem(k);
        }
        
        toRemove.clear();
    
        //ADJUST FIRING THRESHOLD
        int activated = pending.size();        
        
        //TODO use rate that items iterated were selected
        float percentActivated = activated / iterations;
        
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
            if (forgetThreshold < 0.01f) forgetThreshold = 0.01f;
        }
        
        
        /*
        if (activated > 0)
            System.out.println(Texts.n2(activityThreshold) + "(" + skippedPerSample + ") " + pending.size() + " / " + size());
        */
        
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
                
        if ((firingAge >= latencyMin*latencyScale) || (activity >= activityThreshold )) {
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
               
        for (int i = 0; i < skippedPerSample; i++)
            pending.pollFirst();
        
        E n = pending.pollFirst();
        if (n!=null) {
            return take(n.name());
        }
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
        x.budget.setLastForgetTime(now);
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
        //quick way to calculate this passively while iterating
        if (numPriorityThru!=0)
            return (float)(totalPriorityThru / numPriorityThru);
        return 0;
    }

    @Override
    public Iterator<E> iterator() {
        return items.values().iterator();
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

    @Override
    public String toString() {
        return super.toString() + "[" + size() + "|" + pending.size() + "|" + this.forgetThreshold + ".." + this.activityThreshold + "]";
    }
    
    

    
    
}
