package nars.util.graph;

import nars.concept.Concept;
import nars.task.Task;
import org.jgrapht.graph.DirectedMultigraph;



/**
 * Generates a set of trees (forest) for derivations of a set of Tasks.
 * These incude all known tasks forming the premises of the ancestry
 * of the target task.
 */
public class DerivationTree extends DirectedMultigraph<Task, String> {

    public DerivationTree() {
        super(String.class);
    }

    public DerivationTree add(Iterable<Task> t, int maxLevels) {
        for (Task x : t) add(x, maxLevels);
        return this;
    }

    public static String edge(String label, Task from, Task to) {
        return label + '[' + from + ',' + to + ']';
    }

    public boolean add(Task t, int maxLevels) {
        if (maxLevels == 0) return false;


        addVertex(t);

        Task parent = t.getParentTask();
        if (parent != null) {
            if (add(parent, maxLevels - 1))
                addEdge(parent, t, edge("Parent", parent, t));
        }

        Task belief = t.getParentBelief();
        if (belief != null) {
            addVertex(belief);
            addEdge(belief, t, edge("Belief", belief, t));
        }

//        Operation cause = t.getCause();
//        if (cause != null) {
//            //TODO?
//            /*Task causeTask = cause.getTask();
//            if (!causeTask.equals(t) && add(causeTask, maxLevels - 1))
//                addEdge(causeTask, t, edge("Cause", causeTask, t));*/
//        }

//            if (includeTermLinks) {
//                for (TermLink t : c.termLinks.values()) {
//                    Term target = t.target;
//                    if (!containsVertex(target)) {
//                        addVertex(target);
//                    }
//                    addEdge(source, target, t);
//                }
//            }

                /*
                if (includeTaskLinks) {
                    for (TaskLink t : c.taskLinks.values()) {
                        Task target = t.targetTask;
                        if (!containsVertex(target)) {
                            addVertex(target);
                        }
                        addEdge(source, target, t);
                    }
                }
                */


        return true;
    }


    public DerivationTree add(Concept c, int maxLevels) {
        add(c.getBeliefs(), maxLevels);
        add(c.getGoals(), maxLevels);
        add(c.getQuestions(), maxLevels);
        add(c.getQuests(), maxLevels);
        return this;
    }

}
