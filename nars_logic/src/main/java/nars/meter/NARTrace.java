package nars.meter;

import nars.Events;
import nars.Events.InferenceEvent;
import nars.NAR;
import nars.event.MemoryReaction;
import nars.concept.Concept;
import nars.task.Task;

import java.io.PrintStream;
import java.util.*;


/**
 * Records all sensors, output, and trace events in an indexed data structure for runtime or subsequent analysis of a NAR's execution telemetry.
 */
public class NARTrace extends MemoryReaction {

    /**
     * utility method for diagnosing stack overflow errors caused by unbounded
     * recursion or other phenomena
     */
    public static boolean guardStack(int alertDepth, CharSequence methodname, Object... args) {
        StackTraceElement[] st = new Exception().getStackTrace();
        if (st.length < 1 + alertDepth) {
            return false;
        }
        for (int i = 1; i < alertDepth; i++) {
            //look for a series of equal (TODO: or cyclic) method names
            if (!st[i].getMethodName().contains(methodname)) {
                return false;
            }
        }
        return true;
    }

    
    //public final Map<Concept, List<InferenceEvent>> concept = new HashMap();
    public final TreeMap<Long, List<InferenceEvent>> time = new TreeMap();
        

    

    private long t;
    public final NAR nar;




    public static class OutputEvent extends InferenceEvent {

        public final Class channel;
        public final Object[] signal;

        public OutputEvent(long when, Class channel, Object... signal) {
            super(when);
            this.channel = channel;
            this.signal = signal;
        }

        @Override
        public String toString() {
            return channel.getSimpleName() + ": " +
                    (signal.length > 1 ? Arrays.toString(signal) : signal[0]);
        }
        
        @Override
        public Class getType() {
            return channel;
        }
        

    }

    public static interface HasLabel {
        public String toLabel();
    }
    
    public static enum AddOrRemove {
        Add, Remove
    }
    
    public static class TaskEvent extends InferenceEvent implements HasLabel {

        public final Task task;
        public final AddOrRemove type;
        public final float priority;

        public TaskEvent(Task t, long when, AddOrRemove type) {
            super(when);
            this.task = t;
            this.type = type;
            this.priority = t.getPriority();
        }

        @Override
        public String toString() {
            return "Task " + type + " (" + task.getHistory() + "): " + task.toStringWithBudget();
        }
        @Override
        public String toLabel() {
            return "Task " + type + " (" + task.getHistory()  + "):\n" + task.name();
        }
    }

    

    public NARTrace(NAR n) {
        super(n, true);
        this.nar = n;
    }



    public void addEvent(InferenceEvent e) {
        List<InferenceEvent> timeslot = time.get(t);
        if (timeslot == null) {
            timeslot = new ArrayList();
            time.put(t, timeslot);
        }
        timeslot.add(e);
    }

  

    public void reset() {
        time.clear();
        //concept.clear();
    }

    //TODO use MemoryObserver's event dispatcher
    @Override
    @Deprecated public void event(final Class event, final Object[] arguments) {
        if (event == Events.OUT.class) {
            onTaskAdd((Task)arguments[0]);
        }
        else if (event == Events.TaskRemove.class) {
            onTaskRemove((Task)arguments[0], (String)arguments[1]);
        }
        else
            super.event(event, arguments);

    }
    
    @Override
    public void onConceptActive(Concept concept) {
//        ConceptNew cc = new ConceptNew(concept, t);
//        addEvent(cc);
//
//        List<InferenceEvent> lc = new ArrayList(1);
//        lc.add(cc);
//
//        this.concept.put(concept, lc);
    }

    
    @Override
    public void onCycleStart(long clock) {
        this.t = clock;
    }

    @Override
    public void onCycleEnd(long time) {
    }

    @Override
    public void onTaskAdd(Task task) {
        TaskEvent ta = new TaskEvent(task, t, AddOrRemove.Add);
        addEvent(ta);
    }

    @Override
    public void onTaskRemove(Task task, String reason) {
        TaskEvent tr = new TaskEvent(task, t, AddOrRemove.Remove);
        addEvent(tr);
    }

    @Override
    public void output(Class channel, Object... signal) {
        addEvent(new OutputEvent(t, channel, signal));
    }
    
    public void printTime() {
        printTime(System.out);
    }

    public void printTime(PrintStream out) {
        for (Long w : time.keySet()) {
            List<InferenceEvent> events = time.get(w);
            if (events.isEmpty()) {
                continue;
            }

            out.println(w + " ---------\\");
            for (InferenceEvent e : events) {
                System.out.println("  " + e);
            }
            out.println(w + " ---------/\n");

        }
    }


//    abstract public static class CycleTreeMLData extends TreeMLData implements Reaction {
//
//        private final NAR nar;
//
//        public CycleTreeMLData(NAR n, String theName, int historySize) {
//            super(theName, Color.WHITE  /*Video.getColor(name, 0.9f, 1f)*/, historySize);
//            this.nar = n;
//            n.on(CycleEnd.class, this);
//        }
//
//        public CycleTreeMLData(NAR n, String theName, float min, float max, int historySize) {
//            this(n, theName, historySize);
//            setRange(min, max);
//        }
//
//        @Override
//        public void event(Class event, Object[] arguments) {
//            long time = nar.time();
//            setData((int)nar.time(), next(time, nar));
//        }
//
//        public abstract float next(long time, NAR nar);
//
//    }
//
//    public static class ConceptBagTreeMLData extends CycleTreeMLData {
//
//        public final Mode mode;
//        private final Iterable<Concept> concepts;
//
//        public static enum Mode {
//
//            ConceptPriorityTotal, TaskLinkPriorityMean, TermLinkPriorityMean /* add others */ }
//
//        public ConceptBagTreeMLData(NAR n, Iterable<Concept> concepts, int historySize, Mode mode) {
//            super(n, "Concepts: " + mode, historySize);
//            this.mode = mode;
//            this.concepts = concepts;
//
//        }
//
//        @Override
//        public float next(long time, NAR nar) {
//            float r = 0;
//            int numConcepts = 0;
//            for (Concept c : concepts) {
//                switch (mode) {
//                    case ConceptPriorityTotal:
//                        r += c.getPriority();
//                        break;
//                    case TermLinkPriorityMean:
//                        r += c.termLinks.getTotalPriority();
//                        break;
//                    case TaskLinkPriorityMean:
//                        r += c.taskLinks.getTotalPriority();
//                        break;
//                }
//                numConcepts++;
//            }
//
//            switch (mode) {
//                case TermLinkPriorityMean:
//                case TaskLinkPriorityMean:
//                    if (numConcepts > 0) r /= numConcepts;
//                    break;
//            }
//
//            return r;
//        }
//
//    }
//
//    public static class ConceptTreeMLData extends CycleTreeMLData {
//
//        public final Mode mode;
//        private final String conceptString;
//        private final Term conceptTerm;
//        private Concept concept;
//
//        public static enum Mode {
//
//            Priority, Duration, BeliefConfidenceMax /* add others */ }
//
//        public ConceptTreeMLData(NAR n, String concept, int historySize, Mode mode) throws InvalidInputException {
//            super(n, concept + ": " + mode, 0, 1, historySize);
//            this.mode = mode;
//            this.conceptString = concept;
//            this.conceptTerm = new Narsese(n).parseTerm(conceptString);
//
//            this.concept = null;
//        }
//
//        @Override
//        public float next(final long time, final NAR nar) {
//            if (concept == null) {
//                concept = nar.memory.concept(conceptTerm);
//                if (concept == null) {
//                    return 0;
//                }
//            }
//            switch (mode) {
//                case Priority:
//                    return concept.getPriority();
//                case Duration:
//                    return concept.getDurability();
//                case BeliefConfidenceMax:
//                    if (concept.beliefs.size() > 0) {
//                        return concept.beliefs.get(0).truth.getConfidence();
//                    }
//                    return 0;
//            }
//            return 0f;
//        }
//
//    }

}
