package nars.util.graph;

import nars.NAR;
import nars.event.MemoryReaction;
import nars.concept.Concept;
import nars.task.Task;
import nars.util.data.CuckooMap;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;


public class TaskGraph  {

    private final MemoryReaction reaction;
    private final NAR nar;
    private boolean started;


    final Deque<Task> log = new ArrayDeque();
    public final Map<Task, Float> y = new CuckooMap<>(); //TODO use ObjectDoubleMap
    final DefaultGrapher extraGrapher = new DefaultGrapher(true, true, true, false, 1, true, true);

    int maxItems = 32;
    private float earliestCreationTime = -1;

    public TaskGraph(NAR n) {

        this.nar = n;
        reaction = new MemoryReaction(n.memory, false) {

            @Override
            public void output(Class channel, Object... args) {

            }

            @Override
            public void onConceptActive(Concept concept) {

            }

            @Override
            public void onCycleStart(long clock) {

            }

            @Override
            public void onCycleEnd(long clock) {

            }

            @Override
            public void onTaskAdd(Task task) {
                next(task);
            }

            @Override
            public void onTaskRemove(Task task, String reason) {

            }
        };

        start();
    }


    public void next(Task o) {

        if (!log.isEmpty())
            if (log.getLast().equals(o))
                return; //duplicate at the same time

        while (1 + log.size() >= maxItems) {
            log.removeFirst();
        }

        log.addLast(o);


        //TODO check all elements, in case they are out of order
        earliestCreationTime = log.getFirst().getCreationTime();
    }

    public float getEarliestCreationTime() {
        return earliestCreationTime;
    }

    public boolean contains(Object t) {
        if ((t instanceof Task) && (y.containsKey(t))) return true;
        return false;
    }

    public static class TaskSequenceEdge extends DefaultEdge {

        private final Task from;
        private final Task to;

        public TaskSequenceEdge(Task from, Task to) {
            super();
            this.from = from;
            this.to = to;
        }

        @Override
        protected Task getSource() {
            return from;
        }

        @Override
        protected Task getTarget() {
            return to;
        }
    }

    public static class TaskConceptEdge extends DefaultEdge {

        private final Task from;
        private final Concept to;

        public TaskConceptEdge(Task from, Concept to) {
            super();
            this.from = from;
            this.to = to;
        }

        @Override
        protected Task getSource() {
            return from;
        }

        @Override
        protected Concept getTarget() {
            return to;
        }
    }

    public NARGraph get() {
        NARGraph g = new NARGraph();
        Task previous = null;

        y.clear();
        float cy = 0;

        //iterate in reverse to display the newest copy of a task and not an older one
        Iterator<Task> ii = log.descendingIterator();
        while (ii.hasNext()) {

            Task o = ii.next();

            if (y.containsKey(o))
                continue;

            g.addVertex(o);
            y.put(o, cy);

            if ((previous!=null) && (!previous.equals(o))) {
                g.addEdge(previous, o, new TaskSequenceEdge(previous, o));
            }

            Concept c = nar.concept(o.getTerm());
            if (c!=null) {
                extraGrapher.onConcept(c, true);
                g.addEdge(o, c, new TaskConceptEdge(o, c));
            }

            previous = o;

            cy++;
        }



        return g;
    }

    public void start() {
        if (started) return;
        reaction.setActive(true);
        started = true;
    }
    public void stop() {
        if (!started) return;
        started = false;
        reaction.setActive(false);
    }

}
