
package nars.storage;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import nars.core.Attention;
import nars.core.Attention.AttentionAware;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.Concept;
import nars.entity.Item;
import nars.inference.BudgetFunctions;
import nars.util.sort.ArraySortedIndex;

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
 * TODO make this abstract and derive ThresholdDelayBag subclass
 */
public class DelayBag<E extends Item<K>,K> extends Bag<E,K> implements AttentionAware {

    private final int capacity;
    
    public Map<K,E> items;
    private Deque<E> pending;
    private ArraySortedIndex<E> toRemove;
    
    private float activityThreshold = 0f;
    private float forgetThreshold = 1f;
    private float latencyMin = 0; /* in cycles */
    
    private int targetActivations;
    
    private float numPriorityThru = 0;
    private float mass = 0;
    
    
    private Memory memory;
    private long now;
    private Attention attention;

    private AtomicBoolean busyReloading = new AtomicBoolean(false);
    private final AtomicDouble forgetRate;
    protected int reloadIteration;
    private boolean overcapacity;
    private float avgPriority;

    /** size below which to return items in flat, sequential, cyclic iterative order. */
    int flatThreshold = 2;
    
    public DelayBag(AtomicDouble forgetRate, int capacity) {
        this(forgetRate, capacity, (int)(0.25f * capacity));
    }
    
    public DelayBag(AtomicDouble forgetRate, int capacity, int targetPendingBufferSize) {
        this.capacity = capacity;
        this.forgetRate = forgetRate;

        if (Parameters.THREADS == 1) {
             //this.items = new LinkedHashMap(capacity);
             this.items = new ConcurrentHashMap(capacity);
             this.pending = new ArrayDeque(targetPendingBufferSize);
        }
        else {
            //find a solution to make a concurrent analog of the LinkedHashMap, if cyclical balance of iteration order (reinsertion appends to end) is necessary
            this.items = new ConcurrentHashMap(capacity);
            this.pending = new ConcurrentLinkedDeque<E>();            
        }
        
        this.targetActivations = targetPendingBufferSize;
        this.toRemove = new ArraySortedIndex(capacity);
        avgPriority = 0.5f;
        mass = 0;
    }
    
    @Override
    public synchronized void clear() {
        items.clear();
        pending.clear();
        avgPriority = 0.5f;
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
    
    protected void reload() {

        if (memory == null)
            throw new RuntimeException("Memory not set");
        
        this.now = memory.time();

        this.latencyMin = memory.param.cycles(forgetRate);
        float forgetCycles = memory.param.cycles(forgetRate);

        int j = 0;
        int originalSize = size();
        
                
        numPriorityThru = mass = 0;
                
        overcapacity = originalSize >= capacity;
                
        int numToRemove = originalSize - capacity;
        if (originalSize >= capacity) {
            toRemove.clear();
            toRemove.setCapacity(numToRemove + 1);
        }
        
            
        E e = null;
        for (final Map.Entry<K, E> ee : items.entrySet()) {
        
            e = ee.getValue();
                           
            if (forgettable(e))
                BudgetFunctions.forgetPeriodic(e.budget, forgetCycles, Parameters.BAG_THRESHOLD, now);
            
            float p = e.getPriority();
            
            mass += p;
            numPriorityThru++;
            
            if (fireable(e)) {
                //ACTIVATE

                //shuffle
                if (j++ % 2 == 0)
                    pending.addFirst(e);
                else
                    pending.addLast(e);                
            }
            else if (removeable(e)) {
                toRemove.add(e);                
            }                            
            
            reloadIteration++;
        }
        
        avgPriority = numPriorityThru / mass;
        
        //remove lowest priority items until the capacity is maintained        
        if (numToRemove > 0) {
            int rj = 0;
            for (final E r : toRemove) {
                E removed = removeItem(r.name());
                if (removed == null)
                    throw new RuntimeException("Unable to remove item: " + r);
                if (rj++ == numToRemove)
                    break;
            }        
        }
            
        adjustActivationThreshold();
        adjustForgettingThreshold();                
        
        //in case the iteration added nothing to pending, use the last item
        if (pending.isEmpty() && (e!=null))
            pending.add(e); 
        
    }
    

    protected boolean forgettable(E e) {
        return true;
    }
    
    protected boolean removeable(E e) {
        return overcapacity && (e.getPriority() <= forgetThreshold);
    }
    
    protected boolean fireable(final E c) {
        
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
       
        int s = items.size();
        if (s == 0) 
            return null;
        else if (s <= flatThreshold) {
            K nn = items.keySet().iterator().next();
            return take(nn);
        }
        
        /* this doesnt seem to be necessary:
        
        
        if (!pending.isEmpty()) {
            //discard removed items at the head of the pending queue
            while (!items.containsKey(pending.peekFirst().name())) {
                E r = pending.removeFirst();
                System.out.println("removed stale item from pending: " + r);                
            }
        }
        */
        
        if (!ensureLoaded()) {            
            //TODO throw exception if not threading and this happens
            return null;
        }
               
        E n = pending.pollFirst();
        if (n!=null) {
            return take(n.name());
        }
        else {
            if (s!= 0)
                throw new RuntimeException("Bag did not find an item although it is not empty; " + pending.size() + " " + s );
            return null;
        }
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
        
        if (x.budget.getLastForgetTime() == -1)
            x.budget.setLastForgetTime(now);

        /* return null since nothing was actually displaced yet */
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
    public Iterable<E> values() {
        return items.values();
    }

    @Override
    public float getAveragePriority() {
        //quick way to calculate this passively while iterating
        return avgPriority;
    }

    @Override
    public Iterator<E> iterator() {
        return values().iterator();
    }


    @Override
    public void setAttention(Attention a) {
        this.attention = a;
        this.memory = a.getMemory();        
    }
    
    public void setMemory(Memory m) {
        this.memory = m;
    }


    @Override
    public String toString() {
        return super.toString() + "[" + size() + "|" + pending.size() + "|" + this.forgetThreshold + ".." + this.activityThreshold + "]";
    }

    public void setTargetActivated(int i) {
        this.targetActivations = i;
    }

    protected void adjustActivationThreshold() {
        //ADJUST FIRING THRESHOLD
        int activated = pending.size();                
        if (activated < targetActivations) {
            //too few activated, reduce threshold
            activityThreshold *= 0.99f;
            if (activityThreshold < 0.01) activityThreshold = 0.01f;            
        }
        else if (activated > targetActivations) {
            //too many, increase threshold
            activityThreshold *= 1.1f;
            if (activityThreshold > 0.99) activityThreshold = 0.99f;            
        }
    }

    protected void adjustForgettingThreshold() {
        //ADJUST FORGET THRESHOLD
        int s = size();
        if (s > capacity) {
            //forgetThreshold *= 1.1f;
            forgetThreshold = 1.0f - ((s - capacity)/capacity);
            if (forgetThreshold > 0.99f) forgetThreshold = 0.99f;
        }
        else if (s < capacity) {
            forgetThreshold *= 0.98f;
            if (forgetThreshold < 0.01f) forgetThreshold = 0.01f;
        }
    }

  
}
