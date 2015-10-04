package nars.meta;

import nars.NAR;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.nal.DerivationRules;
import nars.nal.SimpleDeriver;
import nars.nar.Default;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * for testing specific rules in isolation
 */
abstract public class RuleTest {

    final DerivationRules d;

    public RuleTest(String task, String... rules) {
        d = new DerivationRules(rules);
        onRulesCreated(d);

        SimpleDeriver sd = new SimpleDeriver(d);

        NAR n = new Default() {
            @Override
            protected SimpleDeriver newDeriver() {
                return sd;
            }
        };

        Task b = n.inputTask(task);
        n.frame(1);


        Concept c = n.concept(b.getTerm());

        setupAfterTaskInput(n);


        RuleMatch rm = new RuleMatch(new XorShift1024StarRandom(1));

        List<Task> derivations = new ArrayList();

        for (TermLink tl : c.getTermLinks().values()) {
            rm.start(new ConceptProcess(null,null,null) {

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
                public Stream<Task> derive(Function<ConceptProcess, Stream<Task>> p) {
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
                public void updateBelief(Task revised) {

                }
            });
            sd.forEachRule(rm);
        }

        onDerivations(derivations);

    }

    /** additional setup after the task has been input */
    protected void setupAfterTaskInput(NAR n) {

    }

    abstract public void onDerivations(List<Task> derivations);

    abstract public void onRulesCreated(DerivationRules d);
}
