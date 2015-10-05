package nars.nal.nal6;

import nars.Global;
import nars.NAR;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkTemplate;
import nars.meter.MemoryBudget;
import nars.meter.TestNAR;
import nars.nal.SimpleDeriver;
import nars.nar.Default;
import nars.nar.SingleStepNAR;
import nars.process.ConceptProcess;
import nars.process.ConceptTaskTermLinkProcess;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.util.graph.TermLinkGraph;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.jgroups.util.Util.assertNotNull;
import static org.jgroups.util.Util.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by me on 10/4/15.
 */
public class PremiseExhaustion {

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
            assertNotNull("conceptualized", c);

            System.out.println("ExhaustiveLinkage:TaskProcess(" + tp + ")   ----------");
            c.print(System.out);

            List<TermLinkTemplate> templates = c.getTermLinkTemplates();
            HashSet tempSet = new HashSet(templates);
            assertEquals("termlink templates all unique",
                    templates.size(), tempSet.size());
            assertTrue("termlink templates contains a self-reference",
                    tempSet.contains(new TermLink(tt.getTerm(), new Budget())));

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

            }
            else {
                System.out.println("termlink growth stabilized.");
                current.print(System.out);
                stable = true;
            }
        } while (!current.equals(prev));

        System.out.println("\n");

        assertTrue("connected graph", current.isConnected());
        assertTrue("struture of termlink graph stabilized", stable);

        return current;
    }

    public static class PremiseExhauster extends TestNAR {


        public PremiseExhauster(NAR nar, String stask1, String stask2) {
            super(nar);

            Global.DEBUG = true;

            assertNotNull(nar.task(stask1));
            assertNotNull(nar.task(stask2));

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

            System.out.println("--  " + stask1 + ",  " + stask2 + "  -------------");
            premiseMatrixAnalysis(c, nar.task(stask1), nar.task(stask2));

            System.out.println("--  " + stask2 + ",  " + stask1 + "  -------------");
            premiseMatrixAnalysis(c, nar.task(stask2), nar.task(stask1));

            System.out.println();

            //4. use this TestNAR to test the original unit test
            this.nar.input(nar.task(stask1));
            this.nar.input(nar.task(stask2));

        }

        private void premiseMatrixAnalysis(NAR c, Task task, Task belief) {

            SimpleDeriver sd = new SimpleDeriver();

            // iterate all premises:  task, {termlinks of task}, belief
            c.concept(task).getTermLinks().forEach(tl -> {
                ConceptProcess p = new ConceptTaskTermLinkProcess(c,
                        c.concept(task),
                        new TaskLink(task, new Budget(1,1,1)), tl, belief);

                List<Task> s = p.derive(sd).collect(Collectors.toList());
                System.out.println("\t" + s);

            });
        }

        @Override
        protected void report(Report report, boolean success) {
            super.report(report, success);
        }
    }

    @Test
    public void variable_unification1_exhaustive() {

        long cycles =32;

        PremiseExhauster pe = new PremiseExhauster(
            new SingleStepNAR(),
            "<<$x --> a> ==> <$x --> b>>.",
            "<<$y --> a> ==> <$y --> b>>. %0.00;0.70%");

        //tester.believe(); //en("If something is a a, then it is a B.");
        //tester.believe(,0.00f,0.70f); //en("If something is a a, then it is not a B.");
        pe.mustBelieve(cycles, "<<$1 --> a> ==> <$1 --> b>>", 0.79f, 0.92f); //en("If something is A, then usually, it is a B.");
        pe.run();

    }

}
