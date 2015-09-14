package nars.nal.nal1;

import nars.NAR;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.meta.RuleMatch;
import nars.meta.TaskRule;
import nars.nal.DerivationRules;
import nars.nal.SimpleDeriver;
import nars.nar.Default;
import nars.premise.Premise;
import nars.task.Task;
import nars.util.data.random.XorShift1024StarRandom;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 9/5/15.
 */
public class NAL1SinglePremiseConversionTest {

    @Test
    public void test1() {
        DerivationRules d = new DerivationRules(
            "(S --> P), S |- (P --> S), (Truth:Conversion)"
        );

        assertEquals("original rule + some derived",
                3, d.size());



        NAR n = new Default();


//        test.believe("<bird --> swimmer>")
//                .ask("<swimmer --> bird>") //.en("Is swimmer a type of bird?");
//                .mustOutput(time, "<swimmer --> bird>. %1.00;0.47%")
//                .run();


        Task b = n.inputTask("<bird --> swimmer>.");
        n.frame(1);
        Concept c = n.concept(b.getTerm());

        //Task q = n.inputTask("<swimmer --> bird>?");

        SimpleDeriver sd = new SimpleDeriver(d);
        //sd.printSummary();

        RuleMatch rm = new RuleMatch(new XorShift1024StarRandom(1)) {
            @Override
            public void run(List<TaskRule> u) {
                super.run(u);
            }
        };

        List<Task> derivations = new ArrayList();

        for (TermLink tl : c.getTermLinks().values() ) {
            rm.start(new Premise() {

                @Override
                public Concept getConcept() {
                    return c;
                }

                @Override
                public TermLink getTermLink() {
                    return tl;
                }

                @Override
                public Task getBelief() {
                    return null;
                }

                @Override
                public Task getTask() {
                    return b;
                }

                @Override
                public NAR nar() {
                    return n;
                }

                @Override
                public void accept(Task derivedTask) {
                    derivations.add(derivedTask);
                }
            });
            sd.forEachRule(rm);
        }

        assertTrue(!derivations.isEmpty());
        System.out.println("derived: " + derivations);



    }
}
