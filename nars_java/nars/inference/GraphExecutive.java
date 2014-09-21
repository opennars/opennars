package nars.inference;

import java.util.Comparator;
import nars.core.EventEmitter.Observer;
import nars.core.Events.ConceptGoalAdd;
import nars.core.Events.ConceptGoalRemove;
import nars.core.Events.CycleEnd;
import nars.core.Memory;
import nars.entity.Task;
import nars.io.buffer.PriorityBuffer;
import nars.util.graph.ImplicationGraph;



public class GraphExecutive implements Observer {

    ImplicationGraph implication;
    PriorityBuffer<Task> tasks;
    int numTasks = 32;
    
    public GraphExecutive(Memory memory) {        
        super();
        
        tasks = new PriorityBuffer(new Comparator<Task>() {
            @Override public final int compare(final Task a, final Task b) {
                float bp = b.getPriority();
                float ap = a.getPriority();
                if (bp!=ap)
                    return Float.compare(bp, ap);
                else {
                    float ad = a.getDurability();
                    float bd = b.getDurability();
                    return Float.compare(bd, ad);
                }
                     
            }            
        }, numTasks);
                
        implication = new ImplicationGraph(memory);
        memory.event.on(CycleEnd.class, this);
        memory.event.on(ConceptGoalAdd.class, this);
        memory.event.on(ConceptGoalRemove.class, this);
    }

    @Override
    public void event(Class event, Object[] a) {
        
        if (event == ConceptGoalAdd.class) {
            Task t = (Task)a[2];
            if (!t.isInput()) {
                System.out.println("Goal add: " + a[0] + " " + a[1] +  " " + t.budget.getPriority());
                tasks.add(t.clone());
            }
        }
        else if (event == ConceptGoalRemove.class) {
            Task t = (Task)a[2];
            System.out.println("Goal rem: " + a[0] + " " + a[1] +  " " + t.budget);
            tasks.remove(t);
        }
        else if (event == CycleEnd.class) {
            if (tasks.size() > 0) {
                plan();
            }
        }
    }

    protected void plan() {
        System.out.println("Goals");
        for (Task t : tasks) {
            System.out.println(t + " " + t.getParentBelief());
            //System.out.println(getImplicationPath(t.getParentBelief()));
        }
        System.out.println();        
    }
            
    
//    public String getImplicationPath(Sentence s) {
//        Term t = s.content;
//        if (t instanceof Implication) {
//            return getImplicationPath(((Implication)t).getPredicate());                   
//        }
//        else {
//            return getImplicationPath(t);
//        }
//        //return "";
//    }
//    public String getImplicationPath(Term t) {
//        KShortestPaths ksp = new KShortestPaths(implication, t, 5);
//        return ksp.getPaths(t).toString();
//        
//    }
//    
    
}
