package nars.nal;

import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.meta.TaskRule;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.narsese.NarseseParser;
import nars.process.ConceptProcess;
import nars.process.concept.ConceptFireTaskTerm;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variables;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by patrick.hammer on 30.07.2015.
 */
public class NALExecuter extends ConceptFireTaskTerm {


    public final TaskRule[] rules;

    public static final NALExecuter defaults;

    static {

        NALExecuter r;

        try {
            r = new NALExecuter();
        } catch (Exception e) {
            r = null;
            e.printStackTrace();
            System.exit(1);
        }

        defaults = r;
    }


    public NALExecuter() throws IOException, URISyntaxException {
        this("NAL_Definition.logic");
    }

    public NALExecuter(String ruleFile) throws IOException, URISyntaxException {

        this(Files.readAllLines(Paths.get(
                NALExecuter.class.getResource(ruleFile).toURI()
        )));

    }

    public NALExecuter(Iterable<String> ruleStrings) {
        List<TaskRule> r = parseRules(loadRuleStrings(ruleStrings));
        rules = r.toArray(new TaskRule[r.size()]);
    }

    @Override
    public final boolean apply(final ConceptProcess f, final TermLink bLink) {
        final TaskLink tLink = f.getTaskLink();
        final Task belief = f.getBelief();
        return reason(tLink.getTask(), belief, f);
    }

    static List<String> loadRuleStrings(Iterable<String> lines) {

        List<String> unparsed_rules = new ArrayList<>();
        StringBuilder current_rule = new StringBuilder();

        for (String s : lines) {
            boolean currentRuleEmpty = current_rule.length() == 0;

            if (s.startsWith("//") || s.replace(" ", "").isEmpty()) {

                if (!currentRuleEmpty) {
                    unparsed_rules.add(current_rule.toString().trim()); //rule is finished, add it
                    current_rule.setLength(0); //start identifying a new rule
                }

            } else {
                //note, it can also be that the current_rule is not empty and this line contains |- which means
                //its already a new rule, in which case the old rule has to be added before we go on
                if (!currentRuleEmpty && s.contains("|-")) {

                    unparsed_rules.add(current_rule.toString().trim()); //rule is finished, add it
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

        return ret.replace("\n", "")/*.replace("A_1..n","\"A_1..n\"")*/; //TODO: implement A_1...n notation, needs dynamic term construction before matching
    }

    public static void AddWithPotentialForAllSameOrder(NarseseParser meta,List<TaskRule> uninterpreted_rules,String parsable) {
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
                impls.add("=>");
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


    static List<TaskRule> parseRules(Collection<String> not_yet_parsed_rules) {
        //2. ok we have our unparsed rules, lets parse them to terms now
        NarseseParser meta = NarseseParser.the();
        List<TaskRule> uninterpreted_rules = new ArrayList<>(not_yet_parsed_rules.size() /* approximately */);

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

            String parsable = preprocess(rule);
            try {

                //there might be now be A_1..n in it, if this is the case we have to add up to n rules
                int n=5;
                if(parsable.contains("A_1..n")) {
                    String str="A_1";
                    String str2="B_1";
                    for(int i=0;i<n;i++) {

                        String parsable_unrolled = parsable.replace("A_1..n",str).replace("B_1..n", str2);
                        AddWithPotentialForAllSameOrder(meta,uninterpreted_rules,parsable_unrolled);

                        str+=", A_"+String.valueOf(i+2);
                        str2+=", B_"+String.valueOf(i+2);
                    }
                }
                else {
                    AddWithPotentialForAllSameOrder(meta,uninterpreted_rules,parsable);
                }
            } catch (Exception ex) {
                System.err.println("Ignoring Invalid rule:");
                System.err.print("  ");
                System.err.println(parsable);
                System.err.println();
                //ex.printStackTrace();
            }
        }

        return uninterpreted_rules;
    }


    public boolean reason(final Task task, final Sentence belief, final ConceptProcess nal) {

        if (task.isJudgment() || task.isGoal()) {

            //forward inference
            for (TaskRule r : rules) {
                r.forward(task, belief, nal);
            }

            //TODO also allow backward inference by traversing
        }

        return true;
    }

}
