package nars.core;

import nars.Global;
import nars.Memory;
import nars.io.qa.Answered;
import nars.io.LibraryInput;
import nars.testing.TestNAR;
import nars.nal.Sentence;
import nars.nal.term.Term;
import nars.model.impl.Solid;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;


public class SolidTest {

    @Test
    public void testDetective() throws Exception {

        int time = 256; //should solve the example in few cycles

        Memory.resetStatic(1);
        Global.DEBUG = true;

        Solid s = new Solid(1, 800, 1, 1, 2, 5);
        s.setInternalExperience(null);

        TestNAR n = new TestNAR(s);

        //TextOutput.out(n).setOutputPriorityMin(1.0f);

        Set<Term> solutionTerms = new HashSet();
        Set<Sentence> solutions = new HashSet();

        n.input(LibraryInput.get(n, "app/detective.nal"));

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

        n.frame(time);

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
