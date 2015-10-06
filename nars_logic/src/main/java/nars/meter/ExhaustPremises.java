package nars.meter;

import nars.Global;
import nars.NAR;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkTemplate;
import nars.nal.SimpleDeriver;
import nars.nar.Default;
import nars.nar.SingleStepNAR;
import nars.process.ConceptProcess;
import nars.process.ConceptTaskTermLinkProcess;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.util.graph.TermLinkGraph;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Executes all possible premises between two tasks
 */
public class ExhaustPremises extends TestNAR {


    public ExhaustPremises(NAR nar, Task task1, Task task2) {
        //hack, untested, ultimately should not involve strings
        this(nar, task1.toString(), task2.toString());
    }

    public ExhaustPremises(NAR nar, String stask1, String stask2) {

        super(nar);

        Global.DEBUG = true;
        setTruthTolerance(0.01f); //strict


        //1. get termlink graph results on each independently
        //  measure cycles to stabilization
        //  assert connected
        termlinkAnalysis(nar.task(stask1));
        termlinkAnalysis(nar.task(stask2));

        //2. get the combined termlink graph resultnig from direct conceptualization of both concepts in this test NAR
        System.out.println("COMBINED PREMISE TERMLINK GRAPH");
        NAR c = new Default();
        termlinkAnalysis(c, nar.task(stask1), nar.task(stask2));


        //3. iterate all possible premises using forward and reverse pair order

        System.out.println("  " + stask1 + ",  " + stask2 + "  -------------");
        premiseMatrixAnalysis(c, nar.task(stask1), nar.task(stask2));

        System.out.println("  " + stask2 + ",  " + stask1 + "  -------------");
        premiseMatrixAnalysis(c, nar.task(stask2), nar.task(stask1));

        System.out.println();

        //4. use this TestNAR to test the original unit test
        this.nar.input(nar.task(stask1));
        this.nar.input(nar.task(stask2));

    }

    public static TermLinkGraph termlinkAnalysis(Task... task) {
        return termlinkAnalysis(null, task);
    }

    public static TermLinkGraph termlinkAnalysis(NAR n, Task... task) {

        if (n == null)
            n = new Default();
        //new Terminal();

        for (Task tt : task) {
            //Concept c = n.conceptualize(task.getTerm(), new Budget(1f,1f,1f));
            TaskProcess tp = new TaskProcess(n, tt);
            tp.run();

            Concept c = tp.getConcept();
            assert(c!=null);

            System.out.println("ExhaustiveLinkage:TaskProcess(" + tp + ")   ----------");
            c.print(System.out);

            List<TermLinkTemplate> templates = c.getTermLinkTemplates();
            HashSet tempSet = new HashSet(templates);


            //"termlink templates all unique"
            assert(templates.size() == tempSet.size());

            //"termlink templates contains a self-reference"
            assert(tempSet.contains(new TermLink(tt.getTerm(), new Budget())));

        }


        TermLinkGraph g0 = new TermLinkGraph(n);
        TermLinkGraph prev = g0, current;
        boolean stable = false;
        do {
            n.frame();

            current = new TermLinkGraph(n);

            System.out.println("@" + n.time() + " numConcepts: " + n.memory.concepts.size());

            System.out.println(new MemoryBudget(n));

            if (!current.equals(prev)) {

            } else {
                System.out.println("\ntermlink growth stabilized:");
                current.print(System.out);
                stable = true;
            }
        } while (!current.equals(prev));

        System.out.println("\n");


        //"connected graph"
        assert(current.isConnected());

        //"structure of termlink graph stabilized"
        assert(stable);

        return current;
    }

    public static ExhaustPremises tryPremise(String task, String belief) {
        ExhaustPremises p = new ExhaustPremises(new SingleStepNAR(), task, belief);
        return p;
    }

    private void premiseMatrixAnalysis(NAR c, Task task, Task belief) {

        SimpleDeriver sd = new SimpleDeriver();

        // iterate all premises:  task, {termlinks of task}, belief
        c.concept(task).getTermLinks().forEach(tl -> {
            ConceptProcess p = new ConceptTaskTermLinkProcess(c,
                    c.concept(task),
                    new TaskLink(task, new Budget(1, 1, 1)), tl, belief);

            List<Task> s = p.derive(sd).collect(Collectors.toList());
            System.out.println("\t" + s);

        });
    }

    @Override
    protected void report(Report report, boolean success) {
        super.report(report, success);
    }
}
