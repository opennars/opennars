package nars.nal;

import nars.Global;
import nars.meta.TaskRule;
import nars.narsese.NarseseParser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Holds an array of derivation rules
 */
public class DerivationRules {

    public final TaskRule[] rules;

    public DerivationRules() throws IOException, URISyntaxException {
        this("NAL_Definition.logic");
    }

    public DerivationRules(String ruleFile) throws IOException, URISyntaxException {

        this(Files.readAllLines(Paths.get(
                Deriver.class.getResource(ruleFile).toURI()
        )));
    }

    public DerivationRules(Iterable<String> ruleStrings) {
        Collection<TaskRule> r = parseRules(loadRuleStrings(ruleStrings));
        rules = r.toArray(new TaskRule[r.size()]);
    }

    static List<String> loadRuleStrings(Iterable<String> lines) {

        List<String> unparsed_rules = Global.newArrayList();
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
     * @param rules
     * @param ruleString
     */
    static void preAdd(NarseseParser meta,
                       Collection<TaskRule> rules /* results collection */,
                       String ruleString,
                       Set<String> variations /* temporary */) {

        variations.clear();

        if(ruleString.contains("Order:ForAllSame")) {

            List<String> equs = Global.newArrayList(3);
            equs.add("<=>");
            if(ruleString.contains("<=>")) {
                equs.add("</>");
                equs.add("<|>");
            }

            List<String> impls = Global.newArrayList(3);
            impls.add("==>");
            if(ruleString.contains("==>")) {
                impls.add("=/>");
                impls.add("=|>");
            }

            List<String> conjs = Global.newArrayList(3);
            conjs.add("&&");
            if(ruleString.contains("&&")) {
                conjs.add("&|");
                conjs.add("&/");
            }

            variations.add(ruleString);

            for(String equ : equs) {

                String p1 = ruleString.replace("<=>",equ);

                for(String imp : impls) {

                    String p2 = p1.replace("==>",imp);

                    for(String conj : conjs) {

                        String p3 = p2.replace("&&",conj);

                        variations.add( p3 );

                    }
                }
            }

        }
        else {
            variations.add(ruleString);
        }

        for (final String v : variations) {
            TaskRule r = meta.term(v);
            if (r == null)
                System.err.println("parse error: " + v);
            else {
                rules.add(r); //try to parse it
                addReverseQuestions( rules, r );
            }
        }
    }

    static void addReverseQuestions(
            Collection<TaskRule> rules /* results collection */,
            TaskRule r) {

        r.forEachReverseQuestion( R -> {
            rules.add(R);
        });

    }


        static Collection<TaskRule> parseRules(final Collection<String> not_yet_parsed_rules) {
        //2. ok we have our unparsed rules, lets parse them to terms now
        final NarseseParser meta = NarseseParser.the();
        final Collection<TaskRule> rules
                //= new ArrayList<>(not_yet_parsed_rules.size() /* approximately */);
                = new LinkedHashSet<>(not_yet_parsed_rules.size() /* approximately */);

        /*
        List<String> fails = new ArrayList();

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

        final Set<String> temp = Global.newHashSet(16);

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
                                preAdd(meta, rules, parsable_unrolled, temp);
                            }
                        } else {
                            String parsable_unrolled = p.replace("A_1..n", str).replace("B_1..n", str2);
                            preAdd(meta, rules, parsable_unrolled, temp);
                        }

                        str+=", A_"+String.valueOf(i+2);
                        str2+=", B_"+String.valueOf(i+2);
                    }
                }
                else {
                    preAdd(meta,rules,p, temp);
                }
            } catch (Exception ex) {
                System.err.println("Ignoring invalid input rule: ");
                System.err.print("  ");
                System.err.println(p);
                System.err.println("  " + ex);
                //ex.printStackTrace();
            }
        }

        return rules;
    }


    /** default set of rules, statically available */
    public static DerivationRules standard;

    static {
        try {
            standard = new DerivationRules();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
