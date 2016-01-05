package nars.nal;

import nars.Global;
import nars.util.data.list.FasterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nars.$.$;

/**
 * Holds an array of derivation rules
 */
public class PremiseRuleSet {

    private static final Pattern twoSpacePattern = Pattern.compile("  ", Pattern.LITERAL);
    private static final Pattern equivOperatorPattern = Pattern.compile("<=>", Pattern.LITERAL);
    private static final Pattern implOperatorPattern = Pattern.compile("==>", Pattern.LITERAL);
    private static final Pattern conjOperatorPattern = Pattern.compile("&&", Pattern.LITERAL);
    private final List<PremiseRule> premiseRules = new FasterList<>();


    public PremiseRuleSet() throws IOException, URISyntaxException {
        this(Paths.get(Deriver.class.getResource("default.meta.nal").toURI()));
    }

    public PremiseRuleSet(Path path) throws IOException {
        this(Files.readAllLines(path));
    }
    private final PatternIndex patterns = new PatternIndex();


    private static final Logger logger = LoggerFactory.getLogger(PremiseRuleSet.class);


    public PremiseRuleSet(boolean normalize, PremiseRule... rules) {
        for (PremiseRule p : rules) {
            if (normalize)
                p = p.normalizeRule(getPatterns());
            premiseRules.add(p);
        }
    }

    public PremiseRuleSet(Collection<String> ruleStrings) {
        int[] errors = {0};

        parse(load(ruleStrings), getPatterns()).forEach(s -> premiseRules.add(s));


        getLogger().info("indexed " + premiseRules.size() + " total rules, consisting of " + getPatterns().size() + " unique pattern components terms");
        if (errors[0] > 0) {
            getLogger().warn("\trule errors: " + errors[0]);
        }
    }


    static List<String> load(Iterable<String> lines) {

        List<String> unparsed_rules = Global.newArrayList(1024);

        StringBuilder current_rule = new StringBuilder();
        boolean single_rule_test = false;

        for (String s : lines) {
            if (s.startsWith("try:")) {
                single_rule_test = true;
                break;
            }
        }

        for (String s : lines) {
            boolean currentRuleEmpty = current_rule.length() == 0;

            if (s.startsWith("//") || getSpacePattern().matcher(s).replaceAll(Matcher.quoteReplacement("")).isEmpty()) {

                if (!currentRuleEmpty) {

                    if (!single_rule_test || single_rule_test && current_rule.toString().contains("try:")) {
                        unparsed_rules.add(current_rule.toString().trim().replace("try:", "")); //rule is finished, add it
                    }
                    current_rule.setLength(0); //start identifying a new rule
                }

            } else {
                //note, it can also be that the current_rule is not empty and this line contains |- which means
                //its already a new rule, in which case the old rule has to be added before we go on
                if (!currentRuleEmpty && s.contains("|-")) {

                    if (!single_rule_test || single_rule_test && current_rule.toString().contains("try:")) {
                        unparsed_rules.add(current_rule.toString().trim().replace("try:", "")); //rule is finished, add it
                    }
                    current_rule.setLength(0); //start identifying a new rule

                }
                current_rule.append(s).append('\n');
            }
        }

        if (current_rule.length() > 0) {
            if (!single_rule_test || single_rule_test && current_rule.toString().contains("try:")) {
                unparsed_rules.add(current_rule.toString());
            }
        }

        return unparsed_rules;
    }

    @Deprecated /* soon */ static String preprocess(String rule) //minor things like Truth.Comparison -> Truth_Comparison
    {

        String ret = '<' + rule + '>';

        while (ret.contains("  ")) {
            ret = getTwoSpacePattern().matcher(ret).replaceAll(Matcher.quoteReplacement(" "));
        }

        ret = ret.replace("A..", "%A.."); //add var pattern manually to ellipsis
        ret = ret.replace("%A..B=_", "%A..%B=_"); //add var pattern manually to ellipsis
        ret = ret.replace("B..", "%B.."); //add var pattern manually to ellipsis
        ret = ret.replace("%A.._=B", "%A.._=%B"); //add var pattern manually to ellipsis

        return ret.replace("\n", "");/*.replace("A_1..n","\"A_1..n\"")*/ //TODO: implement A_1...n notation, needs dynamic term construction before matching
    }


    private static final String[] equFull = {"<=>", "</>", "<|>"};
    private static final String[] implFull = {"==>", "=/>", "=|>", "=\\>"};
    private static final String[] conjFull = {"&&", "&|", "&/"};
    private static final String[] unchanged = {null};

    /**
     * //TODO do this on the parsed rule, because string contents could be unpredictable:
     * permute(rule, Map<Op,Op[]> alternates)
     *
     * @param rules
     * @param ruleString
     */
    static void permuteTenses(Collection<String> rules /* results collection */,
                              String ruleString) {

        //Original version which permutes in different tenses

        if (!ruleString.contains("Order:ForAllSame")) {
            rules.add(ruleString);
            return;
        }

        String[] equs =
                ruleString.contains("<=>") ?
                        getEquFull() :
                        getUnchanged();


        String[] impls =
                ruleString.contains("==>") ?
                        getImplFull() :
                        getUnchanged();

        String[] conjs =
                ruleString.contains("&&") ?
                        getConjFull() :
                        getUnchanged();


        rules.add(ruleString);


        for (String equ : equs) {

            String p1 = equ != null ? getEquivOperatorPattern().matcher(ruleString).replaceAll(Matcher.quoteReplacement(equ)) : ruleString;

            for (String imp : impls) {

                String p2 = imp != null ? getImplOperatorPattern().matcher(p1).replaceAll(Matcher.quoteReplacement(imp)) : p1;

                for (String conj : conjs) {

                    String p3 = conj != null ? getConjOperatorPattern().matcher(p2).replaceAll(Matcher.quoteReplacement(conj)) : p2;

                    rules.add(p3);
                }
            }
        }


    }


    static Set<PremiseRule> parse(Collection<String> rawRules, PatternIndex index) {


        Set<String> expanded = new HashSet(rawRules.size() * 4); //Global.newHashSet(1); //new ConcurrentSkipListSet<>();


        rawRules/*.parallelStream()*/.forEach(rule -> {

            String p = preprocess(rule);


            //there might be now be A_1..maxVarArgsToMatch in it, if this is the case we have to add up to maxVarArgsToMatch ur
            /*if (p.contains("A_1..n") || p.contains("A_1..A_i.substitute(_)..A_n")) {
                addUnrolledVarArgs(expanded, p, maxVarArgsToMatch);
            } else {*/
            permuteTenses(expanded, p);



        });//.forEachOrdered(s -> expanded.addAll(s));


        Set<PremiseRule> ur = Global.newHashSet(rawRules.size()*4);
        //ListMultimap<TaskRule, TaskRule> ur = MultimapBuilder.linkedHashKeys().arrayListValues().build();


        //accumulate these in a set to eliminate duplicates
        expanded.forEach(src -> {
            try {


                PremiseRule preNorm = new PremiseRule($(src));

                PremiseRule r = add(ur, preNorm, src, index);

                if (r.allowBackward)
                    addQuestions(ur, r, src, index);

                PremiseRule f = r.forwardPermutation();
                if (r.allowBackward)
                    addQuestions(ur, f, src, index);
                add(ur, f, src, index);


            } catch (Exception ex) {
                getLogger().error("Invalid TaskRule: {}", ex);
                ex.printStackTrace();
            }
        });

        return ur;
    }

    private static void addQuestions(Collection<PremiseRule> target, PremiseRule r, String src, PatternIndex patterns) {

        r.forEachQuestionReversal((q,reason) -> add(target, q, src + "//" + reason, patterns));

     }

    static PremiseRule add(Collection<PremiseRule> target, PremiseRule q, String src, PatternIndex index) {
        if (q == null)
            throw new RuntimeException("null: " + q + " " + src);

        q = q.normalizeRule(index).setup(index);
        q.setSource(src);
        target.add(q);
        return q;
    }
    private static final Pattern spacePattern = Pattern.compile(" ", Pattern.LITERAL);

    public static Pattern getSpacePattern() {
        return spacePattern;
    }

    public static Pattern getTwoSpacePattern() {
        return twoSpacePattern;
    }

    public static Pattern getEquivOperatorPattern() {
        return equivOperatorPattern;
    }

    public static Pattern getImplOperatorPattern() {
        return implOperatorPattern;
    }

    public static Pattern getConjOperatorPattern() {
        return conjOperatorPattern;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static String[] getEquFull() {
        return equFull;
    }

    public static String[] getImplFull() {
        return implFull;
    }

    public static String[] getConjFull() {
        return conjFull;
    }

    public static String[] getUnchanged() {
        return unchanged;
    }

    /** for compiling and de-duplicating pattern term components */
    public PatternIndex getPatterns() {
        return patterns;
    }

    public List<PremiseRule> getPremiseRules() {
        return Collections.unmodifiableList(premiseRules);
    }


}

