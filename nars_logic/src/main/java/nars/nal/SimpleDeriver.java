package nars.nal;

import nars.Global;
import nars.Op;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.meta.TaskRule;
import nars.task.Task;
import nars.term.Term;

import java.util.EnumMap;
import java.util.List;

/** separates rules according to task/belief term type but otherwise involves significant redundancy we'll eliminate in other Deriver implementations */
public class SimpleDeriver extends Deriver {

    private final EnumMap<Op, EnumMap<Op, List<TaskRule>>> taskTypeMap;
    private final EnumMap<Op, List<TaskRule>> beliefTypeMap;


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

    public void forEachRule(final RuleMatch match) {

        final Term taskTerm = match.premise.getTask().getTerm();

        EnumMap<Op, List<TaskRule>> taskSpecific = taskTypeMap.get(taskTerm.op());

        final Task belief = match.premise.getBelief();
        final Term beliefTerm = belief!=null ? belief.getTerm() : null;

        if (taskSpecific!=null) {

            if (beliefTerm != null) {
                // <T>,<B>
                List<TaskRule> u = taskSpecific.get(beliefTerm.op());
                if (u != null)
                    match.run(u);
            }

            // <T>,%
            List<TaskRule> taskSpecificBeliefAny = taskSpecific.get(Op.VAR_PATTERN);
            if (taskSpecificBeliefAny != null)
                match.run(taskSpecificBeliefAny);
        }

        if (beliefTerm!=null) {
            // %,<B>
            List<TaskRule> beliefSpecific = beliefTypeMap.get(Op.VAR_PATTERN);
            if (beliefSpecific!=null)
                match.run(beliefSpecific);
        }

        // %,%
        List<TaskRule> bAny = beliefTypeMap.get(Op.VAR_PATTERN);
        if (bAny!=null)
            match.run(bAny);


    }

}
