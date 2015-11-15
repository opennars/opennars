package nars.nal;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import nars.Global;
import nars.Op;
import nars.nal.meta.PostCondition;
import nars.nal.meta.TaskBeliefPair;
import nars.task.Task;
import nars.term.Term;
import nars.util.db.TemporaryCache;
import org.infinispan.commons.marshall.jboss.GenericJBossMarshaller;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

/** separates rules according to task/belief term type but otherwise involves significant redundancy we'll eliminate in other Deriver implementations */
public class SimpleDeriver extends Deriver  {

    /** maps rule patterns to one or more rules which involve it */
    public final Multimap<TaskBeliefPair, TaskRule> ruleIndex;



    //not ready yet
    static void loadCachedRules() {
        final String key = "derivation_rules:standard";
        SimpleDeriver.standard = TemporaryCache.computeIfAbsent(
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
            SimpleDeriver.standard = new DerivationRules();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final SimpleDeriver standardDeriver;

    static {
        loadRules();
        standardDeriver = new SimpleDeriver(SimpleDeriver.standard);
    }

    /**
     * default set of rules, statically available
     */
    public static DerivationRules standard;
    protected final EnumMap<Op, EnumMap<Op, List<TaskRule>>> taskTypeMap;
    protected final EnumMap<Op, List<TaskRule>> beliefTypeMap;

    public SimpleDeriver() {
        this(SimpleDeriver.standard);
    }

    public SimpleDeriver(DerivationRules rules) {
        super(rules);


        this.ruleIndex = MultimapBuilder.treeKeys()
                .arrayListValues()
                //.treeSetValues()
                .build();

        {
            rules.forEach((Consumer<TaskRule>) ((r) -> {
                ruleIndex.put(r.pattern, r);
            }));

            if (rules.size() != ruleIndex.size()) {
                //this could indicate that rules are not identified properly so that different rules alias to the same equal/hash identity or something
                throw new RuntimeException("duplicate rules detected");
            }
        }


        taskTypeMap = new EnumMap(Op.class);
        beliefTypeMap = new EnumMap(Op.class);

        {

            rules.forEach((Consumer<TaskRule>)r -> {

                //final PreCondition[] p = r.preconditions;

                final Op o1 = r.getTaskTermType();
                final Op o2 = r.getBeliefTermType();

                if (o1 != Op.VAR_PATTERN) {
                    EnumMap<Op, List<TaskRule>> subtypeMap = taskTypeMap.computeIfAbsent(o1, op -> {
                        return new EnumMap(Op.class);
                    });

                    List<TaskRule> lt = subtypeMap.computeIfAbsent(o2, x -> {
                        return Global.newArrayList();
                    });
                    lt.add(r);

                } else {
                    List<TaskRule> lt = beliefTypeMap.computeIfAbsent(o2, x -> {
                        return Global.newArrayList();
                    });
                    lt.add(r);
                }
            });

        }

        //printSummary();

    }

    @Override
    public void forEachRule(RuleMatch match, Consumer<Task> receiver) {


        final TaskBeliefPair taskBelief = match.taskBelief;
        final Term taskTerm = taskBelief.term(0);
        final Term beliefTerm = taskBelief.term(1);


        final int n = match.premise.nal();


        EnumMap<Op, List<TaskRule>> taskSpecific = taskTypeMap.get(taskTerm.op());
        if (taskSpecific!=null) {


            // <T>,<B>
            List<TaskRule> taskSpecificBeliefSpecific = taskSpecific.get(beliefTerm.op());
            if (taskSpecificBeliefSpecific != null)
                run(match, taskSpecificBeliefSpecific, n, receiver);


            // <T>,%
            List<TaskRule> taskSpecificBeliefAny = taskSpecific.get(Op.VAR_PATTERN);
            if (taskSpecificBeliefAny != null)
                run(match, taskSpecificBeliefAny, n, receiver);
        }


        // %,<B>
        List<TaskRule> beliefSpecific = beliefTypeMap.get(beliefTerm.op());
        if (beliefSpecific!=null)
            run(match, beliefSpecific, n, receiver);


        // %,%
        List<TaskRule> any = beliefTypeMap.get(Op.VAR_PATTERN);
        if (any!=null)
            run(match, any, n, receiver);

    }

    public void printSummary() {
        taskTypeMap.entrySet().forEach(k -> {
            k.getValue().entrySet().forEach(m -> {
                System.out.println(k.getKey() + "," + m.getKey() + ": " + m.getValue().size());
            });
        });
        beliefTypeMap.entrySet().forEach(k -> {
            System.out.println("%," + k.getKey() + ": " + k.getValue().size());
        });
    }


    final static void run(RuleMatch m, List<TaskRule> rules, int level, Consumer<Task> t) {

        final int nr = rules.size();
        for (int i = 0; i < nr; i++) {

            TaskRule r = rules.get(i);
            if (r.minNAL > level) continue;

            PostCondition[] pc = m.run(r);
            if (pc != null) {
                for (PostCondition p : pc) {
                    if (p.minNAL > level) continue;
                    Task x = m.apply(p);
                    if (x != null)
                        t.accept(x);
                    /*else
                        System.out.println("Post exit: " + r + " on " + m.premise);*/
                }
            }
        }
    }


}
