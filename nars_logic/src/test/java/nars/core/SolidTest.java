package nars.core;

import nars.Global;
import nars.io.in.LibraryInput;
import nars.io.qa.Answered;
import nars.model.impl.Solid;
import nars.nal.Sentence;
import nars.nal.task.filter.ConstantDerivationLeak;
import nars.nal.term.Term;
import nars.testing.TestNAR;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;


public class SolidTest {

    @Test
    public void testDetective() throws Exception {

        int time = 256; //should solve the example in few cycles
        int timePeriod = 8;


        Global.DEBUG = true;

        final int numConcepts = 800;
        final float leakRate = 0.3f;
        Solid s = new Solid(3, numConcepts, 1, 4, 1, 3) {

            @Override
            protected void initDerivationFilters() {
                //TODO tune this based on # concepts fired, termlnks etc
                final float DERIVATION_PRIORITY_LEAK = leakRate; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
                final float DERIVATION_DURABILITY_LEAK = leakRate; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
                getLogicPolicy().derivationFilters.add(new ConstantDerivationLeak(DERIVATION_PRIORITY_LEAK, DERIVATION_DURABILITY_LEAK));
            }
        };

        s.level(6);
        s.setInternalExperience(null);
        //s.setMaxTasksPerCycle(numConcepts);

        TestNAR n = new TestNAR(s);
        n.memory.randomSeed(1);

        //TextOutput.out(n).setOutputPriorityMin(1.0f);

        Set<Term> solutionTerms = new HashSet();
        Set<Sentence> solutions = new HashSet();


        new Answered(n) {

            @Override
            public void onSolution(Sentence belief) {
                solutions.add(belief);
                solutionTerms.add(belief.getTerm());
                if ((solutionTerms.size() >= 2) && (solutions.size() >= 2)) {
                    n.stop();
                }
            }

        };
        for (int i = 0; i < time; i+=timePeriod) {
            n.input(LibraryInput.get(n, "app/detective.nal"));
            n.frame(timePeriod);
            if (solutionTerms.size() >= 2)
                break;
        }



        //System.out.println(solutions);
        assertTrue("at least 2 unique solutions: " + solutions.toString(), 2 <= solutions.size());
        assertTrue("at least 2 unique terms: " + solutionTerms.toString(), 2 <= solutionTerms.size());

    }


        /*
        n.input("<a --> b>. %1.00;0.90%\n" +
                "<b --> c>. %1.00;0.90%\n"+
                "<c --> d>. %1.00;0.90%\n" +
                "<a --> d>?");*/
        //''outputMustContain('<a --> d>. %1.00;0.27%')


}
