package nars.nal.nal5;

import com.google.common.collect.Lists;
import nars.NAR;
import nars.budget.Budget;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.meta.pre.PairMatchingProduct;
import nars.nal.DerivationRules;
import nars.nal.SimpleDeriver;
import nars.nar.Default;
import nars.process.ConceptTaskTermLinkProcess;
import nars.task.Task;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 9/5/15.
 */
public class NAL5RuleTest {

    @Test
    public void testNAL5MostCriticalRule() {
        /* ((&&,M,A,B) ==> C), ((&&,A,B) ==> C) |- M, (Truth:Abduction) */
        DerivationRules d = new DerivationRules( Lists.newArrayList(
                "((&&,M,A,B) ==> C), ((&&,A,B) ==> C) |- M, (Truth:Abduction)"
        ));
        //System.out.println(Arrays.toString(d.rules));
        assertEquals("original rule + some derived",
                3, d.size());

        SimpleDeriver sd = new SimpleDeriver(d);

        NAR n = new Default();

        /*n.input("<(&&, m, a, b) ==> c>.");
        n.input("<(&&, a, b) ==> c>.");*/

        //
        {
            //Compound taskBelief = n.term("(<(&&, a, b, m) ==> c>, <(&&, a, b) ==> c>)");
            PairMatchingProduct taskBelief = new PairMatchingProduct(
                n.term("<(&&, a, b, m) ==> c>"),
                n.term("<(&&, a, b) ==> c>")
            );

            PairMatchingProduct pattern = new PairMatchingProduct(
                n.term("<(&&, %1, %2, %3) ==> %4>"),
                n.term("<(&&, %1, %2) ==> %4>")
            );

            assertTrue(taskBelief.substitutesMayExist(pattern));

        }

        Task t = n.inputTask("<(&&, <m<->n>, a, b) ==> c>.");
        Task b = n.inputTask("<(&&, a, b) ==> c>.");


        n.frame(1);

        /** dummy task and termlinks until they arent needed */
        TaskLink tl = new TaskLink(t, new Budget(1,1,1) );


//        PremiseProcessor rules = new PremiseProcessor(
//
//                new LogicStage[]{
//                        //new FilterEqualSubtermsAndSetPremiseBelief(),
//                        //new QueryVariableExhaustiveResults(),
//                        sd
//                        //---------------------------------------------
//                },
//
//                new DerivationFilter[]{
//                        new FilterBelowConfidence(0.01),
//                        new FilterDuplicateExistingBelief()
//                        //param.getDefaultDerivationFilters().add(new BeRational());
//                }
//
//        );

        ConceptTaskTermLinkProcess cp = new ConceptTaskTermLinkProcess(n, n.concept(t.getTerm()), tl,
                new TermLink(b.getTerm(), new Budget(1, 1, '.'))
                ) {

            @Override
            public Task getTask() {
                return t;
            }

            @Override
            public Task getBelief() {
                return b;
            }


        };


        sd.accept(cp);




        assertEquals(
            "[$1.00;0.33;0.24$ <m <-> n>. %1.00;0.45% {?: 1;2}]",
            cp.getCached().toString()
        );


    }
}
