package nars.nal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.meta.TaskRule;
import nars.narsese.NarseseParser;
import nars.process.ConceptProcess;
import nars.process.NAL;
import nars.process.concept.ConceptFireTaskTerm;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by patrick.hammer on 30.07.2015.
 */
public class NALExecuter extends ConceptFireTaskTerm {


    public final TaskRule[] rules;

    public static final NALExecuter defaults;

    Multimap<Term, TaskRule> ruleByPreconditions = HashMultimap.create();

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
        Collection<TaskRule> r = parseRules(loadRuleStrings(ruleStrings));
        rules = r.toArray(new TaskRule[r.size()]);
    }

    @Override
    public final boolean apply(final ConceptProcess f, final TermLink bLink) {
        final TaskLink tLink = f.getTaskLink();
        final Task belief = f.getBelief();
        return reason(tLink.getTask(), belief, bLink.getTerm(), f);
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

        /*while (ret.contains("  ")) {
            ret = ret.replace("  ", " ");
        }*/

        return ret;//.replace("\n", "")/*.replace("A_1..n","\"A_1..n\"")*/; //TODO: implement A_1...n notation, needs dynamic term construction before matching
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
                System.err.println("Ignoring Invalid rule:");
                System.err.print("  ");
                System.err.println(p);
                System.err.println();
                //ex.printStackTrace();
            }
        }

        return rules;
    }


    public boolean reason(final Task task, final Sentence belief, Term beliefterm, final NAL nal) {


        if (task.isJudgment() || task.isGoal()) {

            //forward inference
            for (TaskRule r : rules) {
                r.forward(task, belief, beliefterm,  nal);
            }

            //TODO also allow backward inference by traversing
        }

        return true;
    }

}