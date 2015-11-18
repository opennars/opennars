package nars.nal;

import com.google.common.collect.Lists;
import com.gs.collections.impl.list.mutable.FastList;
import nars.Global;
import nars.Narsese;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds an array of derivation rules
 */
public class DerivationRules extends FastList<TaskRule> {

    private static final Pattern spacePattern = Pattern.compile(" ", Pattern.LITERAL);
    private static final Pattern twoSpacePattern = Pattern.compile("  ", Pattern.LITERAL);
    private static final Pattern equivOperatorPattern = Pattern.compile("<=>", Pattern.LITERAL);
    private static final Pattern implOperatorPattern = Pattern.compile("==>", Pattern.LITERAL);
    private static final Pattern conjOperatorPattern = Pattern.compile("&&", Pattern.LITERAL);

    @Deprecated
    public static int maxVarArgsToMatch = 4; //originally 5 but as few as 2 can allow tests to pass

    static final Narsese parser = Narsese.the();


    public DerivationRules() throws IOException, URISyntaxException {
        this(Paths.get(Deriver.class.getResource("default.meta.nal").toURI()));
    }

    public DerivationRules(Path path) throws IOException {
        this(Files.readAllLines(path));
    }

    public DerivationRules(String... ruleStrings) {
        this(Lists.newArrayList(ruleStrings));
    }

    public DerivationRules(final Iterable<String> ruleStrings) {

        this(parseRules(loadRuleStrings(ruleStrings)));
    }

    public DerivationRules(Set<TaskRule> r) {
        super(r);
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
            if (s.startsWith("try:")) {
                single_rule_test = true;
                break;
            }
        }

        for (String s : lines) {
            boolean currentRuleEmpty = current_rule.length() == 0;

            if (s.startsWith("//") || spacePattern.matcher(s).replaceAll(Matcher.quoteReplacement("")).isEmpty()) {

                if (!currentRuleEmpty) {

                    if (!single_rule_test || (single_rule_test && current_rule.toString().contains("try:"))) {
                        unparsed_rules.add(current_rule.toString().trim().replace("try:", "")); //rule is finished, add it
                    }
                    current_rule.setLength(0); //start identifying a new rule
                }

            } else {
                //note, it can also be that the current_rule is not empty and this line contains |- which means
                //its already a new rule, in which case the old rule has to be added before we go on
                if (!currentRuleEmpty && s.contains("|-")) {

                    if (!single_rule_test || (single_rule_test && current_rule.toString().contains("try:"))) {
                        unparsed_rules.add(current_rule.toString().trim().replace("try:", "")); //rule is finished, add it
                    }
                    current_rule.setLength(0); //start identifying a new rule

                }
                current_rule.append(s).append('\n');
            }
        }

        if (current_rule.length() > 0) {
            if (!single_rule_test || (single_rule_test && current_rule.toString().contains("try:"))) {
                unparsed_rules.add(current_rule.toString());
            }
        }

        return unparsed_rules;
    }

    @Deprecated /* soon */ static String preprocess(String rule) //minor things like Truth.Comparison -> Truth_Comparison
    {                                     //A_1..n ->  "A_1_n" and adding of "<" and ">" in order to be parsable

        String ret = '<' + rule + '>';

        while (ret.contains("  ")) {
            ret = twoSpacePattern.matcher(ret).replaceAll(Matcher.quoteReplacement(" "));
        }

        return ret.replace("\n", "");/*.replace("A_1..n","\"A_1..n\"")*/ //TODO: implement A_1...n notation, needs dynamic term construction before matching
    }


    final static String[] equFull = {"<=>", "</>", "<|>"};
    final static String[] implFull = {"==>", "=/>", "=|>"};
    final static String[] conjFull = {"&&", "&|", "&/"};
    final static String[] unchanged = {null};

    /**
     * //TODO do this on the parsed rule, because string contents could be unpredictable:
     * permute(rule, Map<Op,Op[]> alternates)
     *
     * @param rules
     * @param ruleString
     */
    static void addAndPermuteTenses(Collection<String> rules /* results collection */,
                                     String ruleString) {

        //Original version which permutes in different tenses

        if (ruleString.contains("Order:ForAllSame")) {

            final String[] equs =
                    ruleString.contains("<=>") ?
                            equFull :
                            unchanged;


            final String[] impls =
                    ruleString.contains("==>") ?
                            implFull :
                            unchanged;

            final String[] conjs =
                    ruleString.contains("&&") ?
                            conjFull :
                            unchanged;


            rules.add(ruleString);


            for (String equ : equs) {

                String p1 = equ != null ? equivOperatorPattern.matcher(ruleString).replaceAll(Matcher.quoteReplacement(equ)) : ruleString;

                for (String imp : impls) {

                    String p2 = imp != null ? implOperatorPattern.matcher(p1).replaceAll(Matcher.quoteReplacement(imp)) : p1;

                    for (String conj : conjs) {

                        String p3 = conj != null ? conjOperatorPattern.matcher(p2).replaceAll(Matcher.quoteReplacement(conj)) : p2;

//                        String[] premiseConc = p3.split("\\|\\-");
//
//                        boolean premiseTemporal = Narsese.the().term("(" + premiseConc[0] + ")").containsTemporal();
//
//                        String conc = premiseConc[1].substring(0, premiseConc[1].length()-1);
//
//                        boolean conclusionTemporal =
//                                ((Product)Narsese.the().term("(" + conc + ")" )).term(0).containsTemporal();
//
//                        boolean valid = true;
//                        if (conclusionTemporal) {
//
//                            //premise and conclusion have opposite temporality
//                            //so this is invalid
//
//                            //p3 = p3.replace(" |-", ", temporal |-");
//                            System.out.println(premiseTemporal + " " + premiseConc[0]);
//                            valid = false;
//                        }
//                        if (valid)

                        //System.out.println(ruleString + " " + p3);
                            rules.add(p3);
                    }
                }
            }

        } else {
            rules.add(ruleString);
        }


    }

//    /**
//     * //TODO do this on the parsed rule, because string contents could be unpredictable:
//     * permute(rule, Map<Op,Op[]> alternates)
//     *
//     * @param rules
//     * @param ruleString
//     */
//    static void addAndPermuteTensesWrong(Collection<String> rules /* results collection */,
//                                    String ruleString) {
//
//        //add the original, which should be tenseless/eternal
//        rules.add(ruleString);
//
//        if (!ruleString.contains("Order:ForAllSame")) {
//            return;
//        }
//
//        for (int order : new int[] { 0, 1}) {
//            String r = ruleString;
//            r = r.replace("<=>", order == 0 ? "<|>" : "</>");
//            r = r.replace("&&", order == 0 ? "&|" : "&/");
//            r = r.replace("==>", order == 0 ? "=|>" : "=/>");
//            rules.add(r);
//        }
//
//    }


    static Set<TaskRule> parseRules(final Collection<String> rawRules) {


        final Set<String> expanded = new HashSet(); //Global.newHashSet(1); //new ConcurrentSkipListSet<>();


        rawRules/*.parallelStream()*/.forEach(rule -> {

            final String p = preprocess(rule);


            //there might be now be A_1..maxVarArgsToMatch in it, if this is the case we have to add up to maxVarArgsToMatch ur
            if (p.contains("A_1..n") || p.contains("A_1..A_i.substitute(_)..A_n")) {
                addUnrolledVarArgs(expanded, p, maxVarArgsToMatch);
            } else {
                addAndPermuteTenses(expanded, p);
            }


        });//.forEachOrdered(s -> expanded.addAll(s));


        Set<TaskRule> ur = Global.newHashSet(4096);

        //accumulate these in a set to eliminate duplicates
        expanded.forEach(s -> {
            try {


                final TaskRule rUnnorm = parser.taskRule(s);

                final TaskRule rNorm = rUnnorm.normalizeRule();
                if (rNorm == null)
                    throw new RuntimeException("invalid rule, detected after normalization: " + s);

                boolean added = ur.add(rNorm);
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
                    rNorm.forEachQuestionReversal(q -> {

                        q = q.normalizeRule();

                        //normalize may be returned null if the rearranging produced an invalid result
                        //so do not add null

                        if (q != null && ur.add(q)) {
                            //System.out.println("  " + q);
                        }
                    });
                }

                /*String s2 = rUnnorm.toString();
                if (!s2.equals(s1))
                    System.err.println("rUnnorm modified");*/

            } catch (Exception ex) {
                System.err.println("invalid TaskRule:  " + s + " (" + ex + ')');
                //ex.printStackTrace();//ex.printStackTrace();
            }
        });

        return ur;
    }


    private static void addUnrolledVarArgs(Set<String> expanded,
                                           String p,
                                           @Deprecated int maxVarArgs
                                           //TODO replace this with a var arg matcher, to reduce # of rules that would need to be created
    )

    {
        String str = "A_1";
        for (int i = 0; i < maxVarArgs; i++) {
            if (p.contains("A_i")) {
                for (int j = 0; j <= i; j++) {
                    if (p.contains("B_1..m")) {
                        String str2 = "B_1";
                        for (int k = 0; k < maxVarArgs; k++) {
                            String A_i = "A_" + String.valueOf(j + 1);
                            String strrep = str;
                            if (p.contains("A_1..A_i.substitute(")) { //todo maybe allow others than just _ as argument
                                strrep = str.replace(A_i, "_");
                            }
                            String parsable_unrolled = p.replace("A_1..A_i.substitute(_)..A_n", strrep).replace("A_1..n", str).replace("B_1..m", str2).replace("A_i", A_i);
                            addAndPermuteTenses(expanded, parsable_unrolled);
                            str2 += ", B_" + (k + 2);
                        }
                    } else {
                        String A_i = "A_" + String.valueOf(j + 1);
                        String strrep = str;
                        if (p.contains("A_1..A_i.substitute(")) { //todo maybe allow others than just _ as argument
                            strrep = str.replace(A_i, "_");
                        }
                        String parsable_unrolled = p.replace("A_1..A_i.substitute(_)..A_n", strrep).replace("A_1..n", str).replace("A_i", A_i);
                        addAndPermuteTenses(expanded, parsable_unrolled);
                    }
                }
            } else {
                if (p.contains("B_1..m")) {
                    String str2 = "B_1";
                    for (int k = 0; k < maxVarArgs; k++) {
                        str2 += ", B_" + (k + 2);
                        String parsable_unrolled = p.replace("A_1..n", str + ' ').replace("B_1..m", str2 + ' ');
                        addAndPermuteTenses(expanded, parsable_unrolled);
                    }
                } else {
                    String parsable_unrolled = p.replace("A_1..n", str);
                    addAndPermuteTenses(expanded, parsable_unrolled);
                }
            }

            final int iPlus2 = i + 2;
            str += ", A_" + iPlus2;

        }
    }


//    public boolean isValid() {
//        int violations = 0;
//        for (TaskRule r : this) {
//
//            final Op o1 = r.getTaskTermType();
//            if (o1==null) {
//                System.err.println(r + " has null taskterm type");
//                violations++;
//            }
//            final Op o2 = r.getBeliefTermType();
//            if (o2==null) {
//                System.err.println(r + " has null beliefterm type");
//                violations++;
//            }
//        }
//
//        return violations==0;
//    }
}


///*
// * RuleTables.java
// *
// * Copyright (C) 2008  Pei Wang
// *
// * This file is part of Open-NARS.
// *
// * Open-NARS is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 2 of the License, or
// * (at your option) any later version.
// *
// * Open-NARS is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
// */
//package nars.nal;
//
//import nars.Global;
//import nars.Op;
//import nars.link.TLink;
//import nars.link.TaskLink;
//import nars.link.TermLink;
//import nars.nal.nal1.Inheritance;
//import nars.nal.nal1.LocalRules;
//import nars.nal.nal1.Negation;
//import nars.nal.nal2.Similarity;
//import nars.nal.nal3.SetTensional;
//import nars.nal.nal5.*;
//import nars.nal.nal7.TemporalRules;
//import nars.nal.nal8.Operation;
//import nars.premise.Premise;
//import nars.process.NAL;
//import nars.task.Task;
//import nars.term.*;
//
//import java.util.Random;
//
//import static nars.Op.*;
//
///**
// * Table of logic rules, indexed by the TermLinks for the task and the
// * belief. Used in indirective processing of a task, to dispatch logic cases
// * to the relevant logic rules.
// */
//public class RuleTables {
//
//    /*
//
//    private static void temporalInduce(final NAL nal, final Task task, final Sentence taskSentence, final Memory memory) {
//        //usual temporal induction between two events
//        for(int i=0;i<Global.TEMPORAL_INDUCTION_SAMPLES;i++) {
//
//            //prevent duplicate inductions
//            Set<Term> alreadyInducted = new HashSet();
//
//            Concept next=nal.memory.sampleNextConceptNovel(task);
//            if (next == null) continue;
//
//            Term t = next.getTerm();
//
//            if (!alreadyInducted.contains(t)) {
//
//                if (!next.beliefs.isEmpty()) {
//
//                    Sentence s=next.beliefs.get(0);
//
//                    ///SPECIAL REASONING CONTEXT FOR TEMPORAL INDUCTION
//                    Stamp SVSTamp=nal.getNewStamp();
//                    Sentence SVBelief=nal.getCurrentBelief();
//                    NAL.StampBuilder SVstampBuilder=nal.newStampBuilder;
//                    //now set the current context:
//                    nal.setCurrentBelief(s);
//
//                    if(!taskSentence.isEternal() && !s.isEternal()) {
//                        if(s.after(taskSentence, memory.param.duration.get())) {
//                            nal.memory.proceedWithTemporalInduction(s,task,task,nal,false);
//                        } else {
//                            nal.memory.proceedWithTemporalInduction(task,s,task,nal,false);
//                        }
//                    }
//
//                    //RESTORE OF SPECIAL REASONING CONTEXT
//                    nal.setNewStamp(SVSTamp);
//                    nal.setCurrentBelief(SVBelief);
//                    nal.newStampBuilder=SVstampBuilder; //also restore this one
//                    //END
//
//                    alreadyInducted.add(t);
//
//                }
//            }
//        }
//    }
//    */
//
//    /* ----- syllogistic inferences ----- */
//
//    /**
//     * Meta-table of syllogistic rules, indexed by the content classes of the
//     * taskSentence and the belief
//     *
//     * @param tLink      The tlink to task
//     * @param bLink      The tlink to belief
//     * @param task   The content of task
//     * @param beliefTerm The content of belief
//     * @param nal        Reference to the memory
//     */
//    public static void syllogisms(TaskLink tLink, TermLink bLink, Task<Statement> task, Statement beliefTerm, NAL nal) {
//        //final Task task = nal.getCurrentTask();
//
//
//        Task belief = nal.getBelief();
//
//        if (!(belief.getTerm() instanceof Statement)) return;
//
//        final Term taskTerm = task.getTerm();
//
//        int figure;
//        if (taskTerm instanceof Inheritance) {
//            if (beliefTerm instanceof Inheritance) {
//                figure = indexToFigure(tLink, bLink);
//                asymmetricAsymmetric(task, belief, figure, nal);
//            } else if (beliefTerm instanceof Similarity) {
//                figure = indexToFigure(tLink, bLink);
//                asymmetricSymmetric(task, belief, figure, nal);
//            } else {
//                detachmentWithVar(task, belief, bLink.getIndex(0), nal);
//            }
//        } else if (taskTerm instanceof Similarity) {
//
//            if (beliefTerm instanceof Inheritance) {
//                figure = indexToFigure(bLink, tLink);
//                asymmetricSymmetric(belief, task, figure, nal);
//            } else if (beliefTerm instanceof Similarity) {
//                figure = indexToFigure(bLink, tLink);
//                symmetricSymmetric(task, belief, figure, nal);
//            } else if (beliefTerm instanceof Implication) {
//                //Bridge to higher order statements:
//                figure = indexToFigure(tLink, bLink);
//                asymmetricSymmetric(belief, task, figure, nal);
//            } else if (beliefTerm instanceof Equivalence) {
//                //Bridge to higher order statements:
//                figure = indexToFigure(tLink, bLink);
//                symmetricSymmetric(task, belief, figure, nal);
//            }
//
//        } else if (taskTerm instanceof Implication) {
//            if (beliefTerm instanceof Implication) {
//                figure = indexToFigure(tLink, bLink);
//                asymmetricAsymmetric(task, belief, figure, nal);
//            } else if (beliefTerm instanceof Equivalence) {
//                figure = indexToFigure(tLink, bLink);
//                asymmetricSymmetric(task, belief, figure, nal);
//            } else if (beliefTerm instanceof Inheritance) {
//                detachmentWithVar(task, belief, tLink.getIndex(0), nal);
//            } else if (beliefTerm instanceof Similarity) {
//                //Bridge to higher order statements:
//                figure = indexToFigure(tLink, bLink);
//                asymmetricSymmetric(task, belief, figure, nal);
//            }
//        } else if (taskTerm instanceof Equivalence) {
//            if (beliefTerm instanceof Implication) {
//                figure = indexToFigure(bLink, tLink);
//                asymmetricSymmetric(belief, task, figure, nal);
//            } else if (beliefTerm instanceof Equivalence) {
//                figure = indexToFigure(bLink, tLink);
//                symmetricSymmetric(task, belief, figure, nal);
//            } else if (beliefTerm instanceof Inheritance) {
//                detachmentWithVar(task, belief, tLink.getIndex(0), nal);
//            } else if (beliefTerm instanceof Similarity) {
//                //Bridge to higher order statements:
//                figure = indexToFigure(tLink, bLink);
//                symmetricSymmetric(task, belief, figure, nal);
//            }
//        }
//    }
//
//    /**
//     * Decide the figure of syllogism according to the locations of the common
//     * term in the premises
//     *
//     * @param link1 The tlink to the first premise
//     * @param link2 The tlink to the second premise
//     * @return The figure of the syllogism, one of the four: 11, 12, 21, or 22
//     */
//    public static final int indexToFigure(final TLink link1, final TLink link2) {
//        final int i1 = link1.getFigureIndex(0);
//        final int i2 = link2.getFigureIndex(0);
//        return ((i1 + 1) * 10) + (i2 + 1);
//    }
//
//    /**
//     * Syllogistic rules whose both premises are on the same asymmetric relation
//     *
//     * @param taskSentence The taskSentence in the task
//     * @param belief       The judgment in the belief
//     * @param figure       The location of the shared term
//     * @param nal          Reference to the memory
//     */
//    public static void asymmetricAsymmetric(final Task<Statement> taskSentence, final Task<Statement> belief, int figure, final NAL nal) {
//        final Random r = nal.nar.random;
//
//        Statement taskStatement = taskSentence.getTerm();
//        Statement beliefStatement = belief.getTerm();
//
//        Term t1, t2;
//        Term[] u = new Term[]{taskStatement, beliefStatement};
//        switch (figure) {
//            case 11:    // induction
//                if (Variables.unify(VAR_INDEPENDENT, taskStatement.getSubject(), beliefStatement.getSubject(), u, r)) {
//                    taskStatement = (Statement) u[0];
//                    beliefStatement = (Statement) u[1];
//                    if (taskStatement.equals(beliefStatement)) {
//                        return;
//                    }
//                    t1 = beliefStatement.getPredicate();
//                    t2 = taskStatement.getPredicate();
//                    SyllogisticRules.abdIndCom(t1, t2, taskSentence, belief, figure, nal);
//
//                    CompositionalRules.composeCompound(taskStatement, beliefStatement, 0, nal);
//                    //if(taskSentence.getOccurenceTime()==Stamp.ETERNAL && belief.getOccurenceTime()==Stamp.ETERNAL)
//                    CompositionalRules.introVarOuter(taskStatement, beliefStatement, 0, nal);//introVarImage(taskContent, beliefContent, index, memory);
//                    CompositionalRules.eliminateVariableOfConditionAbductive(figure, taskSentence, belief, nal);
//
//                }
//
//                break;
//            case 12:    // deduction
//                if (Variables.unify(VAR_INDEPENDENT, taskStatement.getSubject(), beliefStatement.getPredicate(), u, r)) {
//                    taskStatement = (Statement) u[0];
//                    beliefStatement = (Statement) u[1];
//                    if (taskStatement.equals(beliefStatement)) {
//                        return;
//                    }
//                    t1 = beliefStatement.getSubject();
//                    t2 = taskStatement.getPredicate();
//                    if (Variables.unify(VAR_QUERY, t1, t2, new Term[]{taskStatement, beliefStatement}, r)) {
//                        LocalRules.matchReverse(nal);
//                    } else {
//                        SyllogisticRules.dedExe(t1, t2, taskSentence, belief, nal);
//                    }
//                }
//                break;
//            case 21:    // exemplification
//                if (Variables.unify(VAR_INDEPENDENT, taskStatement.getPredicate(), beliefStatement.getSubject(), u, r)) {
//                    taskStatement = (Statement) u[0];
//                    beliefStatement = (Statement) u[1];
//                    if (taskStatement.equals(beliefStatement)) {
//                        return;
//                    }
//                    t1 = taskStatement.getSubject();
//                    t2 = beliefStatement.getPredicate();
//
//
//                    if (Variables.unify(VAR_QUERY, t1, t2, new Term[]{taskStatement, beliefStatement}, r)) {
//                        LocalRules.matchReverse(nal);
//                    } else {
//                        SyllogisticRules.dedExe(t1, t2, taskSentence, belief, nal);
//                    }
//                }
//                break;
//            case 22:    // abduction
//                if (Variables.unify(VAR_INDEPENDENT, taskStatement.getPredicate(), beliefStatement.getPredicate(), u, r)) {
//                    taskStatement = (Statement) u[0];
//                    beliefStatement = (Statement) u[1];
//
//                    if (taskStatement.equals(beliefStatement)) {
//                        return;
//                    }
//                    t1 = taskStatement.getSubject();
//                    t2 = beliefStatement.getSubject();
//                    if (!SyllogisticRules.conditionalAbd(t1, t2, taskStatement, beliefStatement, nal)) {         // if conditional abduction, skip the following
//                        SyllogisticRules.abdIndCom(t1, t2, taskSentence, belief, figure, nal);
//                        CompositionalRules.composeCompound(taskStatement, beliefStatement, 1, nal);
//                        CompositionalRules.introVarOuter(taskStatement, beliefStatement, 1, nal);// introVarImage(taskContent, beliefContent, index, memory);
//
//                    }
//
//                    CompositionalRules.eliminateVariableOfConditionAbductive(figure, taskSentence, belief, nal);
//
//                }
//                break;
//            default:
//        }
//    }
//
//    public static void goalFromQuestion(final Task task, final Term taskTerm, final Premise p) {
//        if (task.isQuestion() && (taskTerm instanceof Implication || taskTerm instanceof Equivalence)) { //<a =/> b>? |- a!
//            Term goalterm = null;
//            Term goalterm2 = null;
//            if (taskTerm instanceof Implication) {
//                Implication imp = (Implication) taskTerm;
//                if (imp.getTemporalOrder() == TemporalRules.ORDER_FORWARD || imp.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT) {
//                    if (!Global.CURIOSITY_FOR_OPERATOR_ONLY || imp.getSubject() instanceof Operation) {
//                        goalterm = imp.getSubject();
//                    }
//                    if (goalterm instanceof Variable && goalterm.hasVarQuery() && (!Global.CURIOSITY_FOR_OPERATOR_ONLY || imp.getPredicate() instanceof Operation)) {
//                        goalterm = imp.getPredicate(); //overwrite, it is a how question, in case of <?how =/> b> it is b! which is desired
//                    }
//                } else if (imp.getTemporalOrder() == TemporalRules.ORDER_BACKWARD) {
//                    if (!Global.CURIOSITY_FOR_OPERATOR_ONLY || imp.getPredicate() instanceof Operation) {
//                        goalterm = imp.getPredicate();
//                    }
//                    if (goalterm instanceof Variable && goalterm.hasVarQuery() && (!Global.CURIOSITY_FOR_OPERATOR_ONLY || imp.getSubject() instanceof Operation)) {
//                        goalterm = imp.getSubject(); //overwrite, it is a how question, in case of <?how =/> b> it is b! which is desired
//                    }
//                }
//            } else if (taskTerm instanceof Equivalence) {
//                Equivalence qu = (Equivalence) taskTerm;
//                if (qu.getTemporalOrder() == TemporalRules.ORDER_FORWARD || qu.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT) {
//                    if (!Global.CURIOSITY_FOR_OPERATOR_ONLY || qu.getSubject() instanceof Operation) {
//                        goalterm = qu.getSubject();
//                    }
//                    if (!Global.CURIOSITY_FOR_OPERATOR_ONLY || qu.getPredicate() instanceof Operation) {
//                        goalterm2 = qu.getPredicate();
//                    }
//                }
//            }
//
//
//            //TODO run each goalTerm through the same TaskSeed to save memory
//            if (goalterm != null && (goalterm instanceof Compound) && !goalterm.hasVarIndep()) {
//                goalFromTask(task, p, (Compound) goalterm);
//            }
//            if (goalterm2 != null && (goalterm2 instanceof Compound) && !goalterm2.hasVarIndep()) {
//                goalFromTask(task, p, (Compound) goalterm2);
//            }
//        }
//    }
//
//    static void goalFromTask(Task task, Premise p, Compound goalterm) {
//        p.deriveSingle(
//                p.newTask(goalterm)
//                        .goal().truth(1.0f, Global.DEFAULT_GOAL_CONFIDENCE * Global.CURIOSITY_DESIRE_CONFIDENCE_MUL)
//                        .budget(task.getPriority() * Global.CURIOSITY_DESIRE_PRIORITY_MUL, task.getDurability() * Global.CURIOSITY_DESIRE_DURABILITY_MUL)
//                        .parent(task).occurrNow()
//        );
//    }
//
//    /**
//     * Syllogistic rules whose first premise is on an asymmetric relation, and
//     * the second on a symmetric relation
//     *
//     * @param asym   The asymmetric premise
//     * @param sym    The symmetric premise
//     * @param figure The location of the shared term
//     * @param nal    Reference to the memory
//     */
//    public static void asymmetricSymmetric(final Task asym, final Task sym, final int figure, final NAL nal) {
//        final Random r = nal.getRandom();
//
//        Statement asymSt = (Statement) asym.getTerm();
//        Statement symSt = (Statement) sym.getTerm();
//        final Term[] u = new Term[]{asymSt, symSt};
//
//        switch (figure) {
//            case 11:
//                if (Variables.unify(VAR_INDEPENDENT, asymSt.getSubject(), symSt.getSubject(), u, r)) {
//                    asymSt = (Statement) u[0];
//                    symSt = (Statement) u[1];
//
//                    asymmetricSymmetric(asym, sym, figure, nal, r,
//                            asymSt.getPredicate(), symSt.getPredicate(), u);
//                }
//                break;
//            case 12:
//                if (Variables.unify(VAR_INDEPENDENT, asymSt.getSubject(), symSt.getPredicate(), u, r)) {
//                    asymSt = (Statement) u[0];
//                    symSt = (Statement) u[1];
//
//
//                    asymmetricSymmetric(asym, sym, figure, nal, r,
//                            asymSt.getPredicate(), symSt.getSubject(), u);
//                }
//                break;
//            case 21:
//                if (Variables.unify(Op.VAR_INDEPENDENT, asymSt.getPredicate(), symSt.getSubject(), u, r)) {
//                    asymSt = (Statement) u[0];
//                    symSt = (Statement) u[1];
//
//                    asymmetricSymmetric(asym, sym, figure, nal, r,
//                            asymSt.getSubject(), symSt.getPredicate(), u);
//                }
//                break;
//            case 22:
//                if (Variables.unify(Op.VAR_INDEPENDENT, asymSt.getPredicate(), symSt.getPredicate(), u, r)) {
//                    asymSt = (Statement) u[0];
//                    symSt = (Statement) u[1];
//
//                    asymmetricSymmetric(asym, sym, figure, nal, r,
//                            asymSt.getSubject(), symSt.getSubject(), u);
//                }
//                break;
//        }
//    }
//
//    private static void asymmetricSymmetric(Task asym, Task sym, int figure, NAL nal, Random r, Term t1, Term t2, Term[] u) {
//        if (Variables.unify(Op.VAR_QUERY, t1, t2, u, r)) {
//            LocalRules.matchAsymSym(asym, sym, figure, nal);
//        } else {
//            switch (figure) {
//                case 11:
//                case 12:
//                    SyllogisticRules.analogy(t2, t1, asym, sym, figure, nal);
//                    break;
//                case 21:
//                case 22:
//                    SyllogisticRules.analogy(t1, t2, asym, sym, figure, nal);
//                    break;
//            }
//        }
//    }
//
//    /**
//     * Syllogistic rules whose both premises are on the same symmetric relation
//     *  @param taskSentence The premise that comes from a task
//     * @param belief       The premise that comes from a belief
//     * @param figure       The location of the shared term
//     * @param nal          Reference to the memory
//     */
//    public static void symmetricSymmetric(final Task<Statement> taskSentence, final Task<Statement> belief, int figure, final NAL nal) {
//        Statement s1 = belief.getTerm();
//        Statement s2 = taskSentence.getTerm();
//
//        Term ut1, ut2;  //parameters for unify()
//
//        switch (figure) {
//            case 11:
//                ut1 = s1.getSubject();
//                ut2 = s2.getSubject();
//                break;
//            case 12:
//                ut1 = s1.getSubject();
//                ut2 = s2.getPredicate();
//                break;
//            case 21:
//                ut1 = s1.getPredicate();
//                ut2 = s2.getSubject();
//                break;
//            case 22:
//                ut1 = s1.getPredicate();
//                ut2 = s2.getPredicate();
//                break;
//            default:
//                throw new RuntimeException("Invalid figure: " + figure);
//        }
//
//        Term[] u = new Term[]{s1, s2};
//        if (Variables.unify(VAR_INDEPENDENT, ut1, ut2, u, nal.nar.random)) {
//
//            Term rt1, rt2;  //parameters for resemblance()
//
//            //recalculate rt1, rt2 from above:
//            switch (figure) {
//                case 11:
//                    rt1 = s1.getPredicate();
//                    rt2 = s2.getPredicate();
//                    break;
//                case 12:
//                    rt1 = s1.getPredicate();
//                    rt2 = s2.getSubject();
//                    break;
//                case 21:
//                    rt1 = s1.getSubject();
//                    rt2 = s2.getPredicate();
//                    break;
//                case 22:
//                    rt1 = s1.getSubject();
//                    rt2 = s2.getSubject();
//                    break;
//                default:
//                    throw new RuntimeException("Invalid figure: " + figure);
//            }
//
//            SyllogisticRules.resemblance(rt1, rt2, taskSentence, belief, figure, nal);
//
//            CompositionalRules.eliminateVariableOfConditionAbductive(
//                    figure, taskSentence, belief, nal);
//
//        }
//
//    }
//
//    /* ----- conditional inferences ----- */
//
//    /**
//     * The detachment rule, with variable unification
//     *
//     * @param originalMainSentence The premise that is an Implication or
//     *                             Equivalence
//     * @param subSentence          The premise that is the subject or predicate of the
//     *                             first one
//     * @param index                The location of the second premise in the first
//     * @param nal                  Reference to the memory
//     */
//    public static void detachmentWithVar(Task<Statement> mainSentence, Task subSentence, int index, NAL nal) {
//        if (mainSentence == null)
//            return;
//
//        /*if (!(originalMainSentenceTask.getTerm() instanceof Statement)) {
//            return;
//        }*/
//
//        Statement statement = mainSentence.getTerm();
//
//        Term component = statement.term[index];
//        Compound content = subSentence.getTerm();
//
//        if (((component instanceof Inheritance) || (component instanceof Negation)) && (nal.getBelief() != null)) {
//
//            Compound[] u = new Compound[]{statement, content};
//
//            if (!component.hasVarIndep()) {
//                SyllogisticRules.detachment(mainSentence, subSentence, index, nal);
//            } else if (Variables.unify(VAR_INDEPENDENT, component, content, u, nal.nar.random)) {
//                Task<Statement> mainSentenceTask = mainSentence.clone((Statement) u[0]);
//
//                subSentence = ((Task)subSentence).clone(u[1]);
//                if (subSentence != null)
//                    SyllogisticRules.detachment(mainSentenceTask, subSentence, index, nal);
//
//            } else if ((statement instanceof Implication) && (statement.getPredicate() instanceof Statement) && (nal.getTask().isJudgment())) {
//                Statement s2 = (Statement) statement.getPredicate();
//                if ((content instanceof Statement) && (s2.getSubject().equals(((Statement) content).getSubject()))) {
//                    CompositionalRules.introVarInner((Statement) content, s2, statement, nal);
//                }
//
//                CompositionalRules.introVarSameSubjectOrPredicate(mainSentence, subSentence, component, content, index, nal);
//            } else if ((statement instanceof Equivalence) && (statement.getPredicate() instanceof Statement) && (nal.getTask().isJudgment())) {
//                CompositionalRules.introVarSameSubjectOrPredicate(mainSentence, subSentence, component, content, index, nal);
//            }
//        }
//    }
//
//    /**
//     * Conditional deduction or induction, with variable unification
//     *
//     * @param conditional The premise that is an Implication with a Conjunction
//     *                    as condition
//     * @param index       The location of the shared term in the condition
//     * @param statement   The second premise that is a statement
//     * @param side        The location of the shared term in the statement
//     * @param nal         Reference to the memory
//     */
//    public static void conditionalDedIndWithVar(Implication<Compound, ?> conditional, short index, Statement statement, short side, NAL nal) {
//
//        if (!(conditional.getSubject() instanceof Compound))
//            return;
//
//        Compound condition = conditional.getSubject();
//
//        Term component = condition.term[index];
//        Term component2 = null;
//        if (statement instanceof Inheritance) {
//            component2 = statement;
//            side = -1;
//        } else if (statement instanceof Implication) {
//            component2 = statement.term[side];
//        }
//
//
//        if (component2 != null) {
//            Term[] u = new Term[]{conditional, statement};
//            boolean unifiable = nal.unify(VAR_INDEPENDENT, component, component2, u);
//            if (!unifiable) {
//                unifiable = nal.unify(VAR_DEPENDENT, component, component2, u);
//            }
//            if (unifiable) {
//                Implication conditionalUnified = (Implication) u[0];
//                statement = (Statement) u[1];
//                SyllogisticRules.conditionalDedInd(conditionalUnified, index, statement, side, nal);
//            }
//        }
//    }
//
//    /* ----- structural inferences ----- */
//
//    /**
//     * Inference between a compound term and a component of it
//     *
//     * @param compound     The compound term
//     * @param component    The component term
//     * @param compoundTask Whether the compound comes from the task
//     * @param nal          Reference to the memory
//     */
//    public static void compoundAndSelf(Compound compound, Term component, boolean compoundTask, int index, NAL nal) {
//        if (compound instanceof Junction) {
//            if (nal.getBelief() != null) {
//                CompositionalRules.decomposeStatement(compound, component, compoundTask, index, nal);
//            } else if (compound.containsTerm(component)) {
//                StructuralRules.structuralCompound(compound, component, compoundTask, index, nal);
//            }
////        } else if ((compound instanceof Negation) && !memory.getCurrentTask().isStructural()) {
//        } else if (compound instanceof Negation) {
//            if (compoundTask) {
//                if (compound.term[0] instanceof Compound)
//                    StructuralRules.transformNegation((Compound) compound.term[0], nal);
//            } else {
//                StructuralRules.transformNegation(compound, nal);
//            }
//        }
//    }
//
//    /**
//     * Inference between two compound terms
//     *
//     * @param taskTerm   The compound from the task
//     * @param beliefTerm The compound from the belief
//     * @param nal        Reference to the memory
//     */
//    public static void compoundAndCompound(Compound taskTerm, Compound beliefTerm, int index, NAL nal) {
//        if ((taskTerm.op() == beliefTerm.op())) {
//            if (taskTerm.length() >= beliefTerm.length()) {
//                compoundAndSelf(taskTerm, beliefTerm, true, index, nal);
//            } else if (taskTerm.length() < beliefTerm.length()) {
//                compoundAndSelf(beliefTerm, taskTerm, false, index, nal);
//            }
//        }
//    }
//
//    /**
//     * Inference between a compound term and a statement
//     *
//     * @param compound   The compound term
//     * @param index      The location of the current term in the compound
//     * @param statement  The statement
//     * @param side       The location of the current term in the statement
//     * @param beliefTerm The content of the belief
//     * @param nal        Reference to the memory
//     */
//    public static void compoundAndStatement(Compound compound, short index, Statement statement, short side, Term beliefTerm, NAL nal) {
//
//        /*if (index >= compound.term.length) {
//            throw new RuntimeException(index + " index out of bounds for compound " + compound + "( " + compound.getClass() + " = " + Arrays.toString(compound.term) + ") in compoundAndStatement with statement=" + statement);
//        }*/
//        Term component = compound.term[index];
//
//        Task task = nal.getTask();
//        if ((component.op() == statement.op())) {
//            if ((compound instanceof Conjunction) && (nal.getBelief() != null)) {
//                final Random r = nal.nar.random;
//
//                Term[] u = new Term[]{compound, statement};
//                if (Variables.unify(VAR_DEPENDENT, component, statement, u, r)) {
//                    compound = (Compound) u[0];
//                    statement = (Statement) u[1];
//                    SyllogisticRules.elimiVarDep(compound, component,
//                            statement.equals(beliefTerm),
//                            nal);
//                } else if (task.isJudgment()) { // && !compound.containsTerm(component)) {
//                    CompositionalRules.introVarInner(statement, (Statement) component, compound, nal);
//                } else if (Variables.unify(VAR_QUERY, component, statement, u, r)) {
//                    compound = (Compound) u[0];
//                    //statement = (Statement) u[1];
//                    CompositionalRules.decomposeStatement(compound, component, true, index, nal);
//                }
//            }
//        } else {
//            if (task.isJudgment()) {
//                if (statement instanceof Inheritance) {
//                    StructuralRules.structuralCompose1(compound, index, statement, nal);
//                    if (!(compound instanceof SetTensional || compound instanceof Negation)) {
//                        StructuralRules.structuralCompose2(compound, index, statement, side, nal);
//                    }    // {A --> B, A @ (A&C)} |- (A&C) --> (B&C)
//                } else if ((statement instanceof Similarity) && !(compound instanceof Conjunction)) {
//                    StructuralRules.structuralCompose2(compound, index, statement, side, nal);
//                }       // {A <-> B, A @ (A&C)} |- (A&C) <-> (B&C)
//            }
//        }
//    }
//
//    /**
//     * Inference between a component term (of the current term) and a statement
//     *
//     * @param compound  The compound term
//     * @param index     The location of the current term in the compound
//     * @param statement The statement
//     * @param side      The location of the current term in the statement
//     * @param nal       Reference to the memory
//     */
//    public static void componentAndStatement(Compound compound, short index, Statement statement, short side, NAL nal) {
//        if (statement instanceof Inheritance) {
//            StructuralRules.structuralDecompose1(compound, index, statement, nal);
//            if (compound instanceof SetTensional) {
//                StructuralRules.transformSetRelation(compound, statement, side, nal);
//            } else {
//                StructuralRules.structuralDecompose2(statement, index, nal);    // {(C-B) --> (C-A), A @ (C-A)} |- A --> B
//            }
//        } else if (statement instanceof Similarity) {
//            StructuralRules.structuralDecompose2(statement, index, nal);        // {(C-B) --> (C-A), A @ (C-A)} |- A --> B
//            if (compound instanceof SetTensional) {
//                StructuralRules.transformSetRelation(compound, statement, side, nal);
//            }
//        }
//
//       /* else if ((statement instanceof Implication) && (compound instanceof Negation)) {
//            if (index == 0) {
//                StructuralRules.contraposition(statement, nal.getCurrentTask(), nal);
//            } else {
//                StructuralRules.contraposition(statement, nal.getCurrentBelief(), nal);
//            }
//        }*/
//
//    }
//
//
//}
