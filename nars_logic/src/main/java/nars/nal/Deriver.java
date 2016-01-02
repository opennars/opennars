package nars.nal;

import nars.process.ConceptProcess;
import nars.task.Task;

import java.util.function.Consumer;

/**
 *
 * Implements a strategy for managing submitted derivation processes
 * ( via the run() methods )
 *
 * Created by patrick.hammer on 30.07.2015.
 */
public abstract class Deriver  {

    //@Deprecated public static final TermIndex terms = TermIndex.memory(16384);
    private static Deriver defaultDeriver = null;
    private static PremiseRuleSet defaultRules = null;

    public static synchronized PremiseRuleSet getDefaultRules() {
        if (defaultRules == null) {
            try {
                defaultRules = new PremiseRuleSet();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);  //e.printStackTrace();
            }

        }
        return defaultRules;
    }

    public static synchronized Deriver getDefaultDeriver() {
        if (defaultDeriver == null) {
            defaultDeriver = new TrieDeriver( getDefaultRules());
            //defaultDeriver = new SimpleDeriver( getDefaultRules() );
        }
        return defaultDeriver;
    }

    /**
     * default set of rules, statically available
     */
    public final PremiseRuleSet rules;


    public Deriver() {
        this.rules = null;
    }

    public Deriver(PremiseRuleSet rules) {
        this.rules = rules;
    }


//    //not ready yet
//    static void loadCachedRules() {
//        final String key = "derivation_rules:standard";
//        Deriver.standard = TemporaryCache.computeIfAbsent(
//                key, new GenericJBossMarshaller(),
//                () -> {
//                    try {
////                        standard = new DerivationRules();
//
//                        return new DerivationRules();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        System.exit(1);
//                        return null;
//                    }
//                }
////                //TODO compare hash/checksum of the input file
////                //to what is stored in cached file
////                (x) -> {
////                    //this disables entirely and just creates a new one each time:
////                    return  ...
////                }
//        );
//    }

    /** run an initialized rule matcher */
    protected abstract void run(PremiseMatch matcher);


    /** initialize a rule matcher with a Premise to supply
     *  a consumer with zero or more derived tasks.
     *  this method does not provide a way to stop or interrupt
     *  the process once it begins.
     */
    public final void run(ConceptProcess premise, PremiseMatch m, Consumer<Task> t) {
        premise.memory().eventConceptProcess.emit(premise);

        m.start(premise, t, this);
    }


//    public void load(Memory memory) {
//        DerivationRules r = this.rules;
//        int s = r.size();
//        for (int i = 0; i < s; i++) {
//            r.get(i).index(memory.index);
//        }
//    }
}
