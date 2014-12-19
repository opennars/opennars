/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.inference;

import automenta.vivisect.swing.NWindow;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import nars.core.NAR;
import nars.core.build.Default;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.gui.output.JGraphXGraphPanel;
import nars.io.ExampleFileInput;
import nars.io.condition.OutputCondition;
import nars.io.condition.OutputContainsCondition;
import nars.operator.Operation;
import nars.util.NARGraph;

/**
 * Graph analysis of reasoning processes to determine essential and non-essential
 * activity
 */
public class LogicPerformance {
    
    /**
     * contains tasks/sentences, time cycles, etc to explain a task's formation */     
    public static class TaskReasonGraph extends NARGraph {

        public TaskReasonGraph() {
            super();
        }
        
        public TaskReasonGraph(Task[] results, int maxLevels) {
            this();

            for (Task result : results)
                explain(result, maxLevels);
        }
        
        public static class TimeNode extends AtomicLong {

            public TimeNode(long t) {
                super(t);
            }        
            
        }
        
        public static class TimeEdge {
            public TimeEdge() {
                
            }

            @Override
            public String toString() {
                return "@";
            }
            
        }
                
        public void at(Object x, long t) {            
            String timeNode = "t" + t;
            addVertex(timeNode);
            addEdge(timeNode, x, new TimeEdge());
        }
        
        public void explain(Task t, int maxLevels) {
            //String x = toString() + "\n";
            Operation cause = t.getCause();
            Sentence bestSolution = t.getBestSolution();
            Sentence parentBelief = t.getParentBelief();
            Task parentTask = t.getParentTask();
            
            addVertex(t);
            at(t, t.getCreationTime());
            
            if (cause!=null) {
                //x += "  cause=" + cause + "\n";
                Task causeTask = cause.getTask();
                addVertex(causeTask);
                addEdge(causeTask, t, "cause");
                explain(causeTask, maxLevels-1);
            }
            if (bestSolution!=null) {
                if (!t.getTerm().equals(bestSolution.term)) {
                    //x += "  solution=" + bestSolution + "\n";
                    addVertex(bestSolution);
                    addEdge(t, bestSolution, "bestSolution");
                    
                    at(bestSolution, bestSolution.getCreationTime());
                }
            }
            if (parentBelief!=null) {
                //x += "  parentBelief=" + parentBelief + " @ " + parentBelief.getCreationTime() + "\n";
                addVertex(parentBelief);
                addEdge(parentBelief, t, "belief");
                at(parentBelief, parentBelief.getCreationTime());
            }
            if (parentTask!=null) {
                //x += "  parentTask=" + parentTask + " @ " + parentTask.getCreationTime() + "\n";
                addVertex(parentTask);
                addEdge(parentTask, t, "parent");
                
                if (maxLevels > 0)
                    explain(parentTask, maxLevels-1);
                
            }
        
                    
        }
        
    }
    
    public static void main(String[] args) throws Exception {
        
        NAR n = new NAR(new Default());
        
        ExampleFileInput example = ExampleFileInput.getExample("test/nal2.4");
                
        List<OutputCondition> conditions = example.getConditions(n, 5);

        n.addInput(example);
        
        //new TextOutput(n, System.out);
        
        
        n.run(1);
        
        TaskReasonGraph result = new TaskReasonGraph();
        
        for (OutputCondition o : conditions) {
            if (o instanceof OutputContainsCondition) {
                OutputContainsCondition c = (OutputContainsCondition)o;
                if (c.isTrue()) {
                    List<Task> t = c.getTrueReasons();                

                    for (Task task : t) {                        
                        //System.out.println(task.getExplanation());
                        result.explain(task, 4);
                    }
                }
                else {
                    System.err.println("FAIL: " + o.getFalseReason());
                }
            }
                
            
        }
        
        System.out.println(result);
        new NWindow("x", new JGraphXGraphPanel(result)).show(500,400, true);
        
    }
}
