package nars.nal;

import com.google.common.base.Predicates;
import nars.Global;
import nars.meta.TaskRule;
import nars.narsese.NarseseParser;
import org.infinispan.util.concurrent.ConcurrentHashSet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds an array of derivation rules
 */
public class DerivationRules {

    @Deprecated static final int maxVarArgsToMatch = 5;

    public final TaskRule[] rules;

    public DerivationRules() throws IOException, URISyntaxException {
        this("NAL_Definition.logic");
    }

    public DerivationRules(String ruleFile) throws IOException, URISyntaxException {

        this(Files.readAllLines(Paths.get(
                Deriver.class.getResource(ruleFile).toURI()
        )));
    }

    public DerivationRules(final Iterable<String> ruleStrings) {
        this(parseRules(loadRuleStrings(ruleStrings)));
    }

    public DerivationRules(Stream<List<TaskRule>> r) {
        super();

        //remove duplicates again?
        final Set<TaskRule> ruleSet = r.flatMap(x -> x.stream()).collect(Collectors.toSet());

        rules = ruleSet.toArray(new TaskRule[ruleSet.size()]);//collect(Collectors.toList());
    }

//    public DerivationRules(Stream<TaskRule[]> r, Predicate<TaskRule[]> filter) {
//        rules = r.filter(filter).toArray(n -> new TaskRule[n]);
//    }

//    public DerivationRules(DerivationRules master, Predicate<TaskRule> filter) {
//        rules = Stream.of(master.rules).filter(filter).toArray(n -> new TaskRule[n]);
//    }

//    public DerivationRules(DerivationRules master, int nalLevel) {
//        this(master, tr -> tr.levelValid(nalLevel));
//    }



    static List<String> loadRuleStrings(Iterable<String> lines) {

        List<String> unparsed_rules = Global.newArrayList(2048);

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

        if (current_rule.length()> 0) {
            unparsed_rules.add(current_rule.toString());
        }

        return unparsed_rules;
    }

    @Deprecated /* soon */ static String preprocess(String rule) //minor things like Truth.Comparison -> Truth_Comparison
    {                                     //A_1..n ->  "A_1_n" and adding of "<" and ">" in order to be parsable

        String ret = '<' + rule + '>';

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
    static void addAndPermuteTenses(NarseseParser meta,
                                    Collection<String> rules /* results collection */,
                                    String ruleString) {

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

            rules.add(ruleString);

            for(String equ : equs) {

                String p1 = ruleString.replace("<=>",equ);

                for(String imp : impls) {

                    String p2 = p1.replace("==>",imp);

                    for(String conj : conjs) {

                        String p3 = p2.replace("&&",conj);

                        rules.add( p3 );

                    }
                }
            }

        }
        else {
            rules.add(ruleString);
        }


    }




    static Stream<List<TaskRule>> parseRules(final Collection<String> rawRules) {



        final Set<String> expanded = Global.newHashSet(1); //new ConcurrentSkipListSet<>();

        final NarseseParser parser = NarseseParser.the();

        rawRules.stream().forEach(rule -> {

            final String p = preprocess(rule);


            //there might be now be A_1..maxVarArgsToMatch in it, if this is the case we have to add up to maxVarArgsToMatch rules
            if (p.contains("A_1..n") || p.contains("A_1..A_i.substitute(_)..A_n")) {
                addUnrolledVarArgs(parser, expanded, p, maxVarArgsToMatch);
            } else {
                addAndPermuteTenses(parser, expanded, p);
            }


        });//.forEachOrdered(s -> expanded.addAll(s));



        //accumulate these in a set to eliminate duplicates
        return expanded.stream().map(s -> {
            try {


                final TaskRule rUnnorm = parser.term(s);

                final TaskRule rNorm = rUnnorm.normalize();
                if (rNorm == null)
                    throw new RuntimeException("invalid rule, detected after normalization: " + s);

                List<TaskRule> rules = new ArrayList();
                boolean added = rules.add(rNorm);
                if (added) {

                    /*System.out.println(s);
                    System.out.println("  " + rNorm);
                    System.out.println("    " +
                                    Integer.toBinaryString(
                            ((MatchTaskBeliefPattern)rNorm.preconditions[0])
                                    .pattern.structure()
                                    )
                    );*/

                    //add reverse questions
                    rUnnorm.forEachQuestionReversal(q -> {

                        q = q.normalize();

                        //normalize may be returned null if the rearranging produced an invalid result
                        //so do not add null

                        if (q!=null && rules.add(q)) {
                            //System.out.println("  " + q);
                        }
                    });
                }

                /*String s2 = rUnnorm.toString();
                if (!s2.equals(s1))
                    System.err.println("rUnnorm modified");*/

                return rules;

            } catch (Exception ex) {
                System.err.println("Ignoring invalid input rule:  " + s);
                ex.printStackTrace();//ex.printStackTrace();
            }

            return Collections.EMPTY_LIST;


        });


    }

    private static void addUnrolledVarArgs(NarseseParser parser,
                                           Set<String> expanded,
                                           String p,

                                           @Deprecated int maxVarArgs
                                           //TODO replace this with a var arg matcher, to reduce # of rules that would need to be created
    )

    {
        String str = "A_1";
        for (int i = 0; i < maxVarArgs; i++) {
            if (p.contains("A_i")) {
                for (int j = 0; j <= i; j++) {
                    if(p.contains("B_1..m")) {
                        String str2 = "B_1";
                        for (int k = 0; k < maxVarArgs; k++) {
                            String A_i = "A_" + String.valueOf(j + 1);
                            String strrep = str;
                            if (p.contains("A_1..A_i.substitute(")) { //todo maybe allow others than just _ as argument
                                strrep = str.replace(A_i, "_");
                            }
                            String parsable_unrolled = p.replace("A_1..A_i.substitute(_)..A_n", strrep).replace("A_1..n", str).replace("B_1..m", str2).replace("A_i", A_i);
                            addAndPermuteTenses(parser, expanded, parsable_unrolled);
                            str2 += ", B_" + (k+2);
                        }
                    }
                    else {
                        String A_i = "A_" + String.valueOf(j + 1);
                        String strrep = str;
                        if (p.contains("A_1..A_i.substitute(")) { //todo maybe allow others than just _ as argument
                            strrep = str.replace(A_i, "_");
                        }
                        String parsable_unrolled = p.replace("A_1..A_i.substitute(_)..A_n", strrep).replace("A_1..n", str).replace("A_i", A_i);
                        addAndPermuteTenses(parser, expanded, parsable_unrolled);
                    }
                }
            } else {
                if(p.contains("B_1..m")) {
                    String str2 = "B_1";
                    for (int k = 0; k < maxVarArgs; k++) {
                        str2 += ", B_" + (k+2);
                        String parsable_unrolled = p.replace("A_1..n", str+" ").replace("B_1..m", str2+" ");
                        addAndPermuteTenses(parser, expanded, parsable_unrolled);
                    }
                }
                else {
                    String parsable_unrolled = p.replace("A_1..n", str);
                    addAndPermuteTenses(parser, expanded, parsable_unrolled);
                }
            }

            final int iPlus2 = i + 2;
            str += ", A_" + iPlus2;

        }
    }



}
