package nars.nal;

import com.gs.collections.api.list.MutableList;
import com.gs.collections.impl.multimap.list.FastListMultimap;
import nars.Global;
import nars.Op;
import nars.link.TermLink;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.meta.TaskRule;
import nars.meta.pre.BeliefTermType;
import nars.meta.pre.TaskTermMinVolume;
import nars.meta.pre.TaskTermType;
import nars.narsese.NarseseParser;
import nars.premise.Premise;
import nars.process.ConceptProcess;
import nars.process.concept.ConceptFireTaskTerm;
import nars.task.Task;
import nars.term.Term;
import nars.util.data.random.XORShiftRandom;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by patrick.hammer on 30.07.2015.
 */
public class Deriver extends ConceptFireTaskTerm {


    public final TaskRule[] rules;

    public static final Deriver defaults;
    private final EnumMap<Op, EnumMap<Op, List<TaskRule>>> taskTypeMap;
    private final EnumMap<Op, List<TaskRule>> beliefTypeMap;

    //Multimap<Term, TaskRule> ruleByPreconditions = HashMultimap.create();

    FastListMultimap<PreCondition,TaskRule> dependencies = new FastListMultimap<>();

    static {

        Deriver r;

        try {
            r = new Deriver();
        } catch (Exception e) {
            r = null;
            e.printStackTrace();
            System.exit(1);
        }

        defaults = r;
    }


    public Deriver() throws IOException, URISyntaxException {
        this("NAL_Definition.logic");
    }

    public Deriver(String ruleFile) throws IOException, URISyntaxException {

        this(Files.readAllLines(Paths.get(
                Deriver.class.getResource(ruleFile).toURI()
        )));

    }



    public Deriver(Iterable<String> ruleStrings) {
        Collection<TaskRule> r = parseRules(loadRuleStrings(ruleStrings));
        rules = r.toArray(new TaskRule[r.size()]);

        taskTypeMap = new EnumMap(Op.class);
        beliefTypeMap = new EnumMap(Op.class);

        for (final TaskRule tr : rules) {

            final PreCondition[] trpre = tr.preconditions;
            for (int i = 0; i < trpre.length; i++) {
                dependencies.put(trpre[i], tr);
            }

            Op o1 = tr.getTaskTermType();
            if (o1!=Op.VAR_PATTERN) {
                dependencies.put(new TaskTermType(o1), tr);
                int o1v = tr.getTaskTermVolumeMin();
                dependencies.put(new TaskTermMinVolume(o1v), tr);
            }


            Op o2 = tr.getBeliefTermType();
            if (o2!=Op.VAR_PATTERN) {
                dependencies.put(new BeliefTermType(o2), tr);
                int o2v = tr.getBeliefTermVolumeMin();
                dependencies.put(new TaskTermMinVolume(o2v), tr);
            }


            if (o1!=Op.VAR_PATTERN) {
                EnumMap<Op, List<TaskRule>> subtypeMap = taskTypeMap.computeIfAbsent(o1, op -> {
                    return new EnumMap(Op.class);
                });

                List<TaskRule> lt = subtypeMap.computeIfAbsent(o2, x -> {
                    return Global.newArrayList();
                });
                lt.add(tr);
            }
            else {
                List<TaskRule> lt = beliefTypeMap.computeIfAbsent(o2, x -> {
                    return Global.newArrayList();
                });
                lt.add(tr);
            }
        }

        MutableList<PreCondition> sortedDeps = dependencies.keysView().toSortedListBy(p -> {
            return -dependencies.get(p).size();
        });
        /*for (int i = 0; i < sortedDeps.size(); i++) {
            PreCondition k = sortedDeps.get(i);
            System.out.println(dependencies.get(k).size() + "\t" + k);
        }*/



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

    public void forEachRule(final Term taskTerm, final RuleMatch match) {
        EnumMap<Op, List<TaskRule>> taskSpecific = taskTypeMap.get(taskTerm.operator());

        final Task belief = match.premise.getBelief();
        final Term beliefTerm = belief!=null ? belief.getTerm() : null;

        if (taskSpecific!=null) {

            if (beliefTerm != null) {
                // <T>,<B>
                List<TaskRule> u = taskSpecific.get(beliefTerm.operator());
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


        //match.run();
    }

    final ThreadLocal<RuleMatch> matchers = ThreadLocal.withInitial(() -> {
        //TODO use the memory's RNG for complete deterministic reproducibility
        return new RuleMatch(new XORShiftRandom());
    });

    @Override
    public final boolean apply(final ConceptProcess f, final TermLink bLink) {
        return reason(f);
    }

    static List<String> loadRuleStrings(Iterable<String> lines) {

        List<String> unparsed_rules = new ArrayList<>();
        StringBuilder current_rule = new StringBuilder();
        boolean single_rule_test = false;

        for (String s : lines) {
            if(s.startsWith("try:")) {
                single_rule_test=true;
                break;
            }
        }

        for (String s : lines) {
            boolean currentRuleEmpty = current_rule.length() == 0;

            if (s.startsWith("//") || s.replace(" ", "").isEmpty()) {

                if (!currentRuleEmpty) {

                    if(!single_rule_test || (single_rule_test && current_rule.toString().contains("try:"))) {
                        unparsed_rules.add(current_rule.toString().trim().replace("try:","")); //rule is finished, add it
                    }
                    current_rule.setLength(0); //start identifying a new rule
                }

            } else {
                //note, it can also be that the current_rule is not empty and this line contains |- which means
                //its already a new rule, in which case the old rule has to be added before we go on
                if (!currentRuleEmpty && s.contains("|-")) {

                    if(!single_rule_test || (single_rule_test && current_rule.toString().contains("try:"))) {
                        unparsed_rules.add(current_rule.toString().trim().replace("try:","")); //rule is finished, add it
                    }
                    current_rule.setLength(0); //start identifying a new rule

                }
                current_rule.append(s).append('\n');
            }
        }

        return unparsed_rules;
    }

    @Deprecated /* soon */ static String preprocess(String rule) //minor things like Truth.Comparison -> Truth_Comparison
    {                                     //A_1..n ->  "A_1_n" and adding of "<" and ">" in order to be parsable

        String ret = "<" + rule + ">";

        while (ret.contains("  ")) {
            ret = ret.replace("  ", " ");
        }

        return ret.replace("\n", "");/*.replace("A_1..n","\"A_1..n\"")*/ //TODO: implement A_1...n notation, needs dynamic term construction before matching
    }



    /**
     * //TODO do this on the parsed rule, because string contents could be unpredictable:
     * permute(rule, Map<Op,Op[]> alternates)
     * @param meta
     * @param uninterpreted_rules
     * @param parsable
     */
    public static void AddWithPotentialForAllSameOrder(NarseseParser meta, Collection<TaskRule> uninterpreted_rules,String parsable) {
        if(parsable.contains("Order:ForAllSame")) {

            ArrayList<String> equs = new ArrayList<>();
            equs.add("<=>");
            if(parsable.contains("<=>")) {
                equs.add("</>");
                equs.add("<|>");
            }

            ArrayList<String> impls = new ArrayList<>();
            impls.add("==>");
            if(parsable.contains("==>")) {
                impls.add("=/>");
                impls.add("=|>");
                impls.add("==>");
            }

            ArrayList<String> conjs = new ArrayList<>();
            conjs.add("&&");
            if(parsable.contains("&&")) {
                conjs.add("&|");
                conjs.add("&/");
            }

            for(String equ : equs) {
                for(String imp : impls) {
                    for(String conj : conjs) {
                        String variation = parsable.replace("<=>",equ).replace("==>",imp).replace("&&",conj);
                        TaskRule r = meta.term(variation);
                        uninterpreted_rules.add(r); //try to parse it
                    }
                }
            }

        }
        else {
            TaskRule r = meta.term(parsable);
            uninterpreted_rules.add(r); //try to parse it
        }
    }


    static Collection<TaskRule> parseRules(final Collection<String> not_yet_parsed_rules) {
        //2. ok we have our unparsed rules, lets parse them to terms now
        final NarseseParser meta = NarseseParser.the();
        final Collection<TaskRule> rules
                //= new ArrayList<>(not_yet_parsed_rules.size() /* approximately */);
                = new LinkedHashSet<>(not_yet_parsed_rules.size() /* approximately */);

        /*
        ArrayList<String> fails = new ArrayList();

        ListeningParseRunner lpr = new ListeningParseRunner(meta.Term());

        lpr.registerListener(new ParseRunnerListener() {


            @Override
            public void matchSuccess(MatchSuccessEvent event) {
                fails.clear();
            }

            @Override
            public void matchFailure(MatchFailureEvent event) {

                String es = event.getContext().toString();
                es = es.replace("Term/firstOf/sequence", "term");
                es = es.replace("Term/firstOf/Variable", "var");


                if (!fails.isEmpty()) {
                    //replace the last one if it leads an extension of the current one
                    final int f = fails.size() - 1;
                    String last = fails.get(f);
                    boolean contained = (last.indexOf(es) == 0);
                    if (contained) {
                        if (last.length() < es.length())
                            fails.set(f, es);
                        return;
                    }
                }
                fails.add(es);
            }

        });
        */

        for (String rule : not_yet_parsed_rules) {

            final String p = preprocess(rule);
            try {

                //there might be now be A_1..n in it, if this is the case we have to add up to n rules
                int n=5;
                if(p.contains("A_1..n") || p.contains("A_1..A_i.substitute(_)..A_n")) {
                    String str="A_1";
                    String str2="B_1";
                    for(int i=0; i < n; i++) {
                        if(p.contains("A_i")) {
                            for(int j=0; j <= i; j++) {
                                String A_i = "A_" + String.valueOf(j + 1);
                                String strrep = str;
                                if(p.contains("A_1..A_i.substitute(")) { //todo maybe allow others than just _ as argument
                                    strrep = str.replace(A_i,"_");
                                }
                                String parsable_unrolled = p.replace("A_1..A_i.substitute(_)..A_n", strrep).replace("A_1..n", str).replace("B_1..n", str2).replace("A_i",A_i);
                                AddWithPotentialForAllSameOrder(meta, rules, parsable_unrolled);
                            }
                        } else {
                            String parsable_unrolled = p.replace("A_1..n", str).replace("B_1..n", str2);
                            AddWithPotentialForAllSameOrder(meta, rules, parsable_unrolled);
                        }

                        str+=", A_"+String.valueOf(i+2);
                        str2+=", B_"+String.valueOf(i+2);
                    }
                }
                else {
                    AddWithPotentialForAllSameOrder(meta,rules,p);
                }
            } catch (Exception ex) {
                System.err.println("Ignoring Invalid rule: ");
                System.err.print("  ");
                System.err.println(p);
                System.err.println("  " + ex);
                //ex.printStackTrace();
            }
        }

        return rules;
    }


    public boolean reason(final Premise premise) {
        ///final Task task, final Sentence belief, Term beliefterm,
        //tLink.getTask(), belief, bLink.getTerm(),

        RuleMatch m = matchers.get();
        m.start(premise);

        final Task task = premise.getTask();
        if (task.isJudgment() || task.isGoal()) {

            forEachRule(task.getTerm(), m);

            //TODO also allow backward inference by traversing
        }

        return true;
    }

}
