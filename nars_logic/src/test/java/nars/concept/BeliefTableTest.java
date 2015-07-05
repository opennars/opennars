package nars.concept;

import junit.framework.TestCase;
import nars.NAR;
import nars.nar.Default;
import nars.term.Compound;

/**
 * Created by me on 7/5/15.
 */
public class BeliefTableTest extends TestCase {

    public static class BeliefAnalysis {

        private final NAR nar;
        private final Compound term;

        public BeliefAnalysis(NAR n, String term) {
            this.nar = n;
            this.term = nar.term(term);
        }

        public BeliefAnalysis add(float freq, float conf) {
            nar.believe(term, freq, conf);
            nar.frame();
            return this;
        }

        public Concept concept() {
            return nar.concept(term);
        }

        public BeliefTable beliefs() {
            return concept().getBeliefs();
        }

        public void print() {
            System.out.println("Beliefs");
            beliefs().print(System.out);
            System.out.println();
        }

        public int size() { return beliefs().size(); }

    }

    public void testRevision() {
        NAR n = new NAR(new Default().setInternalExperience(null));


        BeliefAnalysis b = new BeliefAnalysis(n, "<a-->b>")
                .add(1.0f, 0.9f)
                .add(0.0f, 0.9f);

        assertEquals(2, b.size());

        n.frame(1);
        //b.print();

        assertEquals("revised", 3, b.size());

        n.frame(200);
        //b.print();

        assertEquals("no additional revisions", 3, b.size());



    }

}