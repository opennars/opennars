package nars.core;

import nars.Memory;
import nars.NAR;
import nars.NARSeed;
import nars.budget.Budget;
import nars.model.impl.Default;
import nars.nal.Task;
import nars.nal.concept.AxiomaticConcept;
import nars.nal.concept.Concept;
import nars.nal.term.Term;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;


//UNTESTED

public class ConceptBuilderTest {

    @Test
    public void testConceptBuilder() {
        String term = "<google --> [deleted]>";

        NAR n = new NAR(new Default());
        n.memory.on(new NARSeed.ConceptBuilder() {

            @Override
            public Concept newConcept(Term t, Budget b, Memory m) {
                return null;
            }
        });
    }

    @Test
    public void testAxiomaticConceptBeliefAndGoal() {
        String term = "<google --> [deleted]>";


        NAR n = new NAR(new Default());

        Task belief = n.task(term + ". %0.78;0.60%");
        Task goal = n.task(term + "! %0.78;0.60%");

        AxiomaticConcept.add(n.memory,
                belief,
                goal
        );

        n.run(1);

        assertNotNull(n.concept(term));
        assertEquals(1, n.concept(term).beliefs.size());
        assertEquals(1, n.concept(term).goals.size());

        n.input(term + ". %0.10;0.10%"); //alternate belief
        n.run(10);

        assertEquals(1, n.concept(term).beliefs.size());
        assertEquals(0.78f, n.concept(term).beliefs.get(0).getTruth().getFrequency(), 0.001); //the original truthvalue, unchanged
    }

    @Test
    public void testAxiomaticConceptGoalOnlyAndAllowBeliefs() {
        String term = "<google --> [deleted]>";


        NAR n = new NAR(new Default());

        Task belief = n.task(term + ". %0.78;0.60%");
        Task goal = n.task(term + "! %0.78;0.60%");

        AxiomaticConcept.add(n.memory,
                goal
        );

        n.run(1);

        assertNotNull(n.concept(term));
        assertEquals(0, n.concept(term).beliefs.size());
        assertEquals(1, n.concept(term).goals.size());


        n.input(belief);
        n.run(10);

        assertEquals(1, n.concept(term).beliefs.size());

    }

}

