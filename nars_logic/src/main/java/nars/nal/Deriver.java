package nars.nal;

import nars.Memory;
import nars.Premise;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.util.db.TemporaryCache;
import org.infinispan.commons.marshall.jboss.GenericJBossMarshaller;

import java.util.function.Consumer;

/**
 *
 * Implements a strategy for managing submitted derivation processes
 * ( via the run() methods )
 *
 * Created by patrick.hammer on 30.07.2015.
 */
abstract public class Deriver  {

    public static final Deriver standardDeriver;
    /**
     * default set of rules, statically available
     */
    public static DerivationRules standard;
    public final DerivationRules rules;


    static {
        loadRules();
        //standardDeriver = new SimpleDeriver(SimpleDeriver.standard);
        standardDeriver = new TrieDeriver(Deriver.standard);
    }

    public Deriver(DerivationRules rules) {
        this.rules = rules;
    }

    //not ready yet
    static void loadCachedRules() {
        final String key = "derivation_rules:standard";
        Deriver.standard = TemporaryCache.computeIfAbsent(
                key, new GenericJBossMarshaller(),
                () -> {
                    try {
//                        standard = new DerivationRules();

                        return new DerivationRules();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                        return null;
                    }
                }
//                //TODO compare hash/checksum of the input file
//                //to what is stored in cached file
//                (x) -> {
//                    //this disables entirely and just creates a new one each time:
//                    return  ...
//                }
        );
    }

    static void loadRules() {
        try {
            Deriver.standard = new DerivationRules();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    abstract protected void forEachRule(final RuleMatch match);


    /** runs a ConceptProcess (premise) and supplies
     *  a consumer with all resulting derived tasks.
     *  this method does not provide a way to stop or interrupt
     *  the process once it begins.
     */
    public final void run(Premise premise, Consumer<Task> t) {
        premise.memory().eventConceptProcess.emit((ConceptProcess)premise);

        RuleMatch m = RuleMatch.matchers.get();
        m.start(premise, t);

        forEachRule(m);
    }


    public synchronized void load(Memory memory) {
        for (int i = 0; i < rules.size(); i++) {
            rules.get(i).normalized(memory.terms);
        }
    }
}
