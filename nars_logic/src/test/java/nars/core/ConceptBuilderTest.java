package nars.core;

import nars.NAR;
import nars.nal.Task;
import nars.nal.concept.AxiomaticConcept;
import nars.model.impl.Default;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Created by me on 4/17/15.
 */
public class ConceptBuilderTest {

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
        assertEquals(0.78f, n.concept(term).beliefs.get(0).truth.getFrequency(), 0.001); //the original truthvalue, unchanged
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

