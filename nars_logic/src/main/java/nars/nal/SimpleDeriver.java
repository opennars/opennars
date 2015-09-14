package nars.nal;

import nars.Global;
import nars.Op;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.meta.TaskRule;
import nars.term.Term;
import nars.util.db.TemporaryCache;
import org.infinispan.commons.marshall.jboss.GenericJBossMarshaller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.List;

/** separates rules according to task/belief term type but otherwise involves significant redundancy we'll eliminate in other Deriver implementations */
public class SimpleDeriver extends Deriver  {

    public static final String key = "derivation_rules:standard";

    static void loadCachedRules() {
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    static {
        loadRules();
    }

    /**
     * default set of rules, statically available
     */
    public static DerivationRules standard;
    private final EnumMap<Op, EnumMap<Op, List<TaskRule>>> taskTypeMap;
    private final EnumMap<Op, List<TaskRule>> beliefTypeMap;

    public SimpleDeriver() {
        this(SimpleDeriver.standard);
    }

    public SimpleDeriver(DerivationRules rules) {
        super(rules);

        taskTypeMap = new EnumMap(Op.class);
        beliefTypeMap = new EnumMap(Op.class);

        int rs = rules.size();
        for (int i = 0; i < rs; i++) {
            final TaskRule r = rules.get(i);

            final PreCondition[] p = r.preconditions;

            final Op o1 = r.getTaskTermType();
            final Op o2 = r.getBeliefTermType();

            if (o1!=Op.VAR_PATTERN) {
                EnumMap<Op, List<TaskRule>> subtypeMap = taskTypeMap.computeIfAbsent(o1, op -> {
                    return new EnumMap(Op.class);
                });

                List<TaskRule> lt = subtypeMap.computeIfAbsent(o2, x -> {
                    return Global.newArrayList();
                });
                lt.add(r);

            }
            else {
                List<TaskRule> lt = beliefTypeMap.computeIfAbsent(o2, x -> {
                    return Global.newArrayList();
                });
                lt.add(r);
            }
        }

        //printSummary();

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

    public void forEachRuleExhaustive(final RuleMatch match) {
        match.run(rules);
    }

    public void forEachRule(final RuleMatch match) {

        //final Term taskTerm = match.premise.getTask().getTerm();

        final Term taskTerm = match.taskBelief.term(0);
        final Term beliefTerm = match.taskBelief.term(1); //belief!=null ? belief.getTerm() : null;

        EnumMap<Op, List<TaskRule>> taskSpecific = taskTypeMap.get(taskTerm.op());

        //final Task belief = match.premise.getBelief();

        if (taskSpecific!=null) {


            // <T>,<B>
            List<TaskRule> u = taskSpecific.get(beliefTerm.op());
            if (u != null)
                match.run(u);


            // <T>,%
            List<TaskRule> taskSpecificBeliefAny = taskSpecific.get(Op.VAR_PATTERN);
            if (taskSpecificBeliefAny != null)
                match.run(taskSpecificBeliefAny);
        }


        // %,<B>
        List<TaskRule> beliefSpecific = beliefTypeMap.get(beliefTerm.op());
        if (beliefSpecific!=null)
            match.run(beliefSpecific);


        // %,%
        List<TaskRule> bAny = beliefTypeMap.get(Op.VAR_PATTERN);
        if (bAny!=null)
            match.run(bAny);


    }



//    public void fire(final Premise fireConcept) {
//        final List<LogicStage<Premise>> rules = this.rules;
//        final int n = rules.size();
//        for (int l = 0; l < n; l++) {
//            if (!rules.get(l).test(fireConcept))
//                break;
//        }
//    }



}
