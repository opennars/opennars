package nars.nal;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import nars.Global;
import nars.Op;
import nars.nal.meta.PostCondition;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TaskBeliefPair;
import nars.term.Term;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

/** separates rules according to task/belief term type but otherwise involves significant redundancy we'll eliminate in other Deriver implementations */
@Deprecated class SimpleDeriver extends Deriver  {

    /** maps rule patterns to one or more rules which involve it */
    public final Multimap<TaskBeliefPair, TaskRule> ruleIndex;


    protected final EnumMap<Op, EnumMap<Op, List<TaskRule>>> taskTypeMap;
    protected final EnumMap<Op, List<TaskRule>> beliefTypeMap;

    public SimpleDeriver() {
        this(Deriver.standard);
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
    public void forEachRule(RuleMatch match) {


        final TaskBeliefPair taskBelief = match.taskBelief;
        final Term taskTerm = taskBelief.term(0);
        final Term beliefTerm = taskBelief.term(1);


        final int n = match.premise.nal();


        EnumMap<Op, List<TaskRule>> taskSpecific = taskTypeMap.get(taskTerm.op());
        if (taskSpecific!=null) {


            // <T>,<B>
            List<TaskRule> taskSpecificBeliefSpecific = taskSpecific.get(beliefTerm.op());
            if (taskSpecificBeliefSpecific != null)
                run(match, taskSpecificBeliefSpecific, n);


            // <T>,%
            List<TaskRule> taskSpecificBeliefAny = taskSpecific.get(Op.VAR_PATTERN);
            if (taskSpecificBeliefAny != null)
                run(match, taskSpecificBeliefAny, n);
        }


        // %,<B>
        List<TaskRule> beliefSpecific = beliefTypeMap.get(beliefTerm.op());
        if (beliefSpecific!=null)
            run(match, beliefSpecific, n);


        // %,%
        List<TaskRule> any = beliefTypeMap.get(Op.VAR_PATTERN);
        if (any!=null)
            run(match, any, n);

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


    final static void run(RuleMatch m, List<TaskRule> rules, int level) {

//        Consumer<Task> t = m.receiver;
//
//        final int nr = rules.size();
//        for (int i = 0; i < nr; i++) {
//
//            TaskRule r = rules.get(i);
//            if (r.minNAL > level) continue;
//
//            PostCondition[] pc = run(r, m);
//            if (pc != null) {
//                for (PostCondition p : pc) {
//                    if (p.minNAL > level) continue;
//                    ArrayList<Task> Lx = m.apply(p);
//                    if(Lx!=null) {
//                        for (Task x : Lx) {
//                            if (x != null)
//                                t.accept(x);
//                        }
//                    }
//                    /*else
//                        System.out.println("Post exit: " + r + " on " + m.premise);*/
//                }
//            }
//        }
    }

    /** return null if no postconditions match (equivalent to an empty array)
     *  or an array of matching PostConditions to apply */
    public static PostCondition[] run(TaskRule rule, RuleMatch m) {

        m.start(rule);

        //stage 1 (early)
        for (final PreCondition p : rule.prePreconditions) {
            if (!p.test(m))
                return null;
        }

        //stage 2 (task/belief term match + late)
        for (final PreCondition p : rule.postPreconditions) {
            if (!p.test(m))
                return null;
        }

        return rule.postconditions;
    }

}
