package nars.core.control;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import nars.core.ConceptProcessor;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.inference.BudgetFunctions;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.ContinuousBag;

/**
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
 */
public class RealTimeFloodCycle implements ConceptProcessor {

    float activityThreshold = 0.5f;
    float maxActivityThreshold = 0.5f;

    final int latencyThreshold = 8;
    private Memory memory;

    @Override
    public Iterator<Concept> iterator() {
        return concepts.values().iterator();
    }
    
    /** Concept for use with Real-time Memory Models */
    public static class RTConcept extends Concept {
        /** activation delay aka latency */
        private long lastFired = -1;
        final Queue<Task> pendingTasks = new ArrayDeque();

        public RTConcept(Term tm, AbstractBag<TaskLink> taskLinks, AbstractBag<TermLink> termLinks, Memory memory) {
            super(tm, taskLinks, termLinks, memory);            
        }

        public long getLastFired() {
            return lastFired;
        }

        @Override
        public void fire() {
            this.lastFired = memory.getTime();
            
            while (!pendingTasks.isEmpty()) {
                super.directProcess(pendingTasks.remove());
            }
            
            super.fire();
            
    
            memory.setCurrentTerm(term);

            //fire all tasklinks
            for (TaskLink t : taskLinks)
                fire(t);
                
        }

        @Override
        public boolean directProcess(Task task) {
            pendingTasks.add(task);
            return true; //may be incorrect to return true automatically, because this assumes the task will be processed
        }
        
        
    }
    
    Map<CharSequence,Concept> concepts = new HashMap();
    List<RTConcept> conceptList = new ArrayList();
    int nextSample = 0;

    public RealTimeFloodCycle() {
    }

    @Override
    public void cycle(Memory m) {
        this.memory = m;
        
        m.processNewTasks();

        m.processNovelTask();

        processConcepts(m);
    }

    public void processConcepts(Memory m) {
        
        final long now = m.getTime();
        int fired = 0;
        for (int i = 0; i < conceptList.size(); i++) {
            final RTConcept c = conceptList.get(i);            
            final float activity = c.budget.getPriority();
            final float firingAge = now - c.getLastFired();
            if (firingAge >= latencyThreshold && (activity >= activityThreshold)) {
                c.fire();
                fired++;
            }
            forget(c);
        }
        
        if (fired == 0) {
            activityThreshold *= 0.9f;
        }
        else if (activityThreshold < maxActivityThreshold) {
            activityThreshold *= 1.1f;
        }
        
        //System.out.println("fired: " + fired + " out of " + conceptList.size());
    }

    @Override
    public Collection<RTConcept> getConcepts() {
        return conceptList;
    }

    @Override
    public void clear() {
        concepts.clear();
        conceptList.clear();
    }

    @Override
    public Concept concept(CharSequence name) {
        return concepts.get(name);
    }

    

    @Override
    public Concept addConcept(Term t, Memory m) {        
        //TODO check for capacity, return null if full
        
        RTConcept concept = new RTConcept(t, 
                new ContinuousBag<TaskLink>(20, m.param.taskCycleForgetDurations,true),
                new ContinuousBag<TermLink>(20, m.param.taskCycleForgetDurations,true),
                m);        
        
        concepts.put(concept.name(), concept);       
        conceptList.add(concept);
        return concept;
    }

    @Override
    public void activate(Concept c, BudgetValue b) {
        BudgetFunctions.activate(c, b);
    }

    @Override
    public Concept sampleNextConcept() {
        if (conceptList.size() == 0) {
            return null;
        }
        if (nextSample >= conceptList.size()) {
            nextSample = 0;
        }

        Concept x = conceptList.get(nextSample++);
        return x;
    }
    
    @Override
    public void forget(Concept x) {
        BudgetFunctions.forget(x.budget, memory.param.conceptForgetDurations.getCycles(), Parameters.BAG_THRESHOLD);        
    }

}
