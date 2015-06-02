package nars.core;

import nars.Memory;
import nars.NAR;
import nars.model.impl.Default;
import nars.nal.DefaultTruth;
import nars.nal.Truth;
import nars.nal.concept.ConstantConceptBuilder;
import nars.nal.concept.PatternConceptBuilder;
import nars.nal.term.Term;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


//UNTESTED

public class ConceptBuilderTest {


    @Test
    public void testConstantConcept() {
        NAR n = new NAR(new Default());
        n.input("lessThan(4,3).");
        n.frame(1);

        assertTrue("although lessThan(4,3) is asserted, its ConceptBuilder will have set the correct value and locked it",
                n.concept("lessThan(4,3)").getStrongestBelief().getTruth().isNegative());
    }

    @Test
    public void testEval() {
        eq(0, "lessThan(eval(add(1,1)), 1)");
        eq(1, "lessThan(eval(add(1,1)), 3)");
    }

    public void eq(float expectedValue, String term) {
        NAR n = new NAR(new Default());
        n.input(term + "?");
        n.frame(1);
        assertEquals(expectedValue, n.concept(term).getStrongestBelief().getTruth().getFrequency(), 0.01);
    }

    @Test
    public void testConceptBuilder() {

        final AtomicInteger count = new AtomicInteger(0);

        NAR n = new NAR(new Default());
        n.on(new PatternConceptBuilder("(\\*, pos, *)", new ConstantConceptBuilder() {

            @Override
            protected Truth truth(Term t, Memory m) {
                count.getAndIncrement();
                return new DefaultTruth(0.75f, 0.99f);
            }

        }));

        n.input("(*, pos, noun)?"); //matches pattern, should create
        n.input("(*, notpos, noun)?"); //doesnt match pattern, should not create
        n.frame(3);

        assertEquals(1, count.get());

    }

    @Test
    public void testConceptBuilderOp() {

        final AtomicInteger count = new AtomicInteger(0);

        NAR n = new NAR(new Default());
        n.on(new PatternConceptBuilder("pos(*,*)", new ConstantConceptBuilder() {

            @Override
            protected Truth truth(Term t, Memory m) {
                count.getAndIncrement();
                return new DefaultTruth(0.75f, 0.99f);
            }

        }));

        //TextOutput.out(n);

        n.input("pos(noun, N)?"); //matches pattern, should create
        n.input("notpos(noun, N)?"); //doesnt match pattern, should not create
        n.frame(3);

        assertEquals(1, count.get());

    }

//    @Test
//    public void testAxiomaticConceptBeliefAndGoal() {
//        String term = "<google --> [deleted]>";
//
//
//        NAR n = new NAR(new Default());
//
//        Task belief = n.task(term + ". %0.78;0.60%");
//        Task goal = n.task(term + "! %0.78;0.60%");
//
//        AxiomaticConcept.add(n.memory,
//                belief,
//                goal
//        );
//
//        n.run(1);
//
//        assertNotNull(n.concept(term));
//        assertEquals(1, n.concept(term).beliefs.size());
//        assertEquals(1, n.concept(term).goals.size());
//
//        n.input(term + ". %0.10;0.10%"); //alternate belief
//        n.run(10);
//
//        assertEquals(1, n.concept(term).beliefs.size());
//        assertEquals(0.78f, n.concept(term).beliefs.get(0).getTruth().getFrequency(), 0.001); //the original truthvalue, unchanged
//    }
//
//    @Test
//    public void testAxiomaticConceptGoalOnlyAndAllowBeliefs() {
//        String term = "<google --> [deleted]>";
//
//
//        NAR n = new NAR(new Default());
//
//        Task belief = n.task(term + ". %0.78;0.60%");
//        Task goal = n.task(term + "! %0.78;0.60%");
//
//        AxiomaticConcept.add(n.memory,
//                goal
//        );
//
//        n.run(1);
//
//        assertNotNull(n.concept(term));
//        assertEquals(0, n.concept(term).beliefs.size());
//        assertEquals(1, n.concept(term).goals.size());
//
//
//        n.input(belief);
//        n.run(10);
//
//        assertEquals(1, n.concept(term).beliefs.size());
//
//    }

}

