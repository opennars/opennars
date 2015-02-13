package nars.util.graph;

import nars.core.NAR;
import nars.logic.MemoryObserver;
import nars.logic.entity.Concept;
import nars.logic.entity.Task;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by me on 2/12/15.
 */
public class TaskGraph  {

    private final MemoryObserver reaction;
    private boolean started;


    final Deque<Task> log = new ArrayDeque();

    int maxItems = 8;
    int edgeSerial = 0;

    public TaskGraph(NAR n) {

        reaction = new MemoryObserver(n.memory, false) {

            @Override
            public void output(Class channel, Object... args) {

            }

            @Override
            public void onConceptAdd(Concept concept) {

            }

            @Override
            public void onCycleStart(long clock) {

            }

            @Override
            public void onCycleEnd(long clock) {

            }

            @Override
            public void onTaskAdd(Task task) {
                System.out.println("task add: " + task);
                next(task);
                System.out.println(this);
            }

            @Override
            public void onTaskRemove(Task task, String reason) {

            }
        };

        start();
    }


    public void next(Task o) {

        while (1 + log.size() >= maxItems) {
            log.removeFirst();
        }

        log.addLast(o);
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
        protected Object getSource() {
            return from;
        }

        @Override
        protected Object getTarget() {
            return to;
        }
    }

    public NARGraph get() {
        NARGraph g = new NARGraph();
        Task previous = null;

        for (Task o : log) {

            g.addVertex(o);

            if (previous!=null) {
                g.addEdge(previous, o, new TaskSequenceEdge(previous, o));
            }

            previous = o;
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
