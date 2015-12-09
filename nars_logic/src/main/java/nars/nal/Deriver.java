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

    static void loadRules() {
        try {
            Deriver.standard = new DerivationRules();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** run an initialized rule matcher */
    protected abstract void run(final RuleMatch matcher);


    /** initialize a rule matcher with a Premise to supply
     *  a consumer with zero or more derived tasks.
     *  this method does not provide a way to stop or interrupt
     *  the process once it begins.
     */
    public final void run(ConceptProcess premise, Consumer<Task> t) {
        premise.memory().eventConceptProcess.emit(premise);

        RuleMatch m = RuleMatch.matchers.get();

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
