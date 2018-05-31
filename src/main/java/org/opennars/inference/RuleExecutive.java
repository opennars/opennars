/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.opennars.inference;

import org.opennars.control.DerivationContext;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import org.opennars.io.Symbols;
import org.opennars.language.*;
import org.opennars.main.Parameters;

import java.util.HashMap;
import java.util.Map;

import static org.opennars.inference.TemporalRules.ORDER_INVALID;
import static org.opennars.inference.TemporalRules.analogyOrder;

class RuleExecutive {
    public static Map<String, Rule> rules = new HashMap<>();

    public static Term nullContent = Term.get(-1); // special term used to indicate that the content is valid but not initialized

    static {
        rules.put("structuralCompose1", new Rule(
            true,

            new Budgeting(BudgetFunctions.EnumBudgetType.COMPOUND),

            // precondition
            ctx -> ctx.nal.getCurrentTask().sentence.isJudgment(), // allow only forward inference

            // preamble
            (compound, index, nal, ctx) -> {
                ctx.component = compound.term[index];
                //final Task task = nal.getCurrentTask();   implicit
                //final Sentence sentence = task.sentence;   implicit
                //ctx.order = sentence.getTemporalOrder();   implicit
                //ctx.truth = sentence.truth;  implicit

                final float reliance = Parameters.reliance;
                ctx.truthDed = TruthFunctions.deduction(ctx.truth, reliance);
                ctx.truthNDed = TruthFunctions.negation(ctx.truthDed);
            },

            // content
            ctx -> nullContent, // not used

            // conclusions
            new Conclusion[]{
                // from structuralCompose1() in legacy
                //  {<S --> P>, P@(P|Q)} |- <S --> (P|Q)>

                new Conclusion(
                    ctx -> ctx.component.equals(ctx.subject) && (ctx.compound instanceof IntersectionExt),
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.compound, ctx.predicate, ctx.truthDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),
                new Conclusion(
                    ctx -> ctx.component.equals(ctx.subject) && ctx.compound instanceof DifferenceExt && ctx.index == 0,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.compound, ctx.predicate, ctx.truthDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),
                new Conclusion(
                    ctx -> ctx.component.equals(ctx.subject) && ctx.compound instanceof DifferenceInt && ctx.index != 0,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.compound, ctx.predicate, ctx.truthNDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),

                new Conclusion(
                    ctx -> ctx.component.equals(ctx.predicate) && ctx.compound instanceof IntersectionInt,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.compound, ctx.truthDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),
                new Conclusion(
                    ctx -> ctx.component.equals(ctx.predicate) && ctx.compound instanceof DifferenceExt && ctx.index != 0,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.compound, ctx.truthNDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),
                new Conclusion(
                    ctx -> ctx.component.equals(ctx.predicate) && ctx.compound instanceof DifferenceInt && ctx.index == 0,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.compound, ctx.truthDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                )
            },

            // post-conclusion
            ctx -> {
                return null;
                /* we do this already in assignStatementWithOldCopula()
                final Task task = ctx.nal.getCurrentTask();
                final Term oldContent = task.getTerm();
                if (oldContent instanceof Statement) {
                    return Statement.make((Statement) oldContent, ctx.subject, ctx.predicate, ctx.order);
                }
                else {
                    return null;
                }
                */
            }
        ));

        rules.put("structuralDecompose1", new Rule(
            true,

            new Budgeting(BudgetFunctions.EnumBudgetType.COMPOUND),

            // precondition
            ctx -> ctx.index < ctx.compound.term.length,

            // preamble
            (compound, index, nal, ctx) -> {
                ctx.component = compound.term[index];
                //final Task task = nal.getCurrentTask();   implicit
                //final Sentence sentence = task.sentence;   implicit
                //ctx.order = sentence.getTemporalOrder();   implicit
                //ctx.truth = sentence.truth;  implicit

                final float reliance = Parameters.reliance;
                ctx.truthDed = TruthFunctions.deduction(ctx.truth, reliance);
                ctx.truthNDed = TruthFunctions.negation(ctx.truthDed);
            },

            // content
            ctx -> nullContent, // not used

            // conclusions
            new Conclusion[]{
                // from structuralDecompose1() in legacy
                //  {<(S|T) --> P>, S@(S|T)} |- <S --> P> {<S --> (P&T)>, P@(P&T)} |- <S --> P>

                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.subject) && ctx.compound instanceof IntersectionInt,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.component, ctx.predicate, ctx.truthDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.subject) && ctx.compound instanceof SetExt && (ctx.compound.size() > 1),
                    (content, ctx, budgeting) -> {
                        final Term[] t1 = new Term[]{ctx.component};
                        ctx.assignStatementWithOldCopula(new SetExt(t1), ctx.predicate, ctx.truthDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting);
                    }
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.subject) && ctx.compound instanceof DifferenceInt && ctx.index == 0,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.component, ctx.predicate, ctx.truthDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.subject) && ctx.compound instanceof DifferenceInt && ctx.index != 0,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.component, ctx.predicate, ctx.truthNDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),

                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.predicate) && ctx.compound instanceof IntersectionExt,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.component, ctx.truthDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.predicate) && (ctx.compound instanceof SetInt) && (ctx.compound.size() > 1),
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.subject, new SetInt(ctx.component), ctx.truthDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.predicate) && ctx.compound instanceof DifferenceExt && ctx.index == 0,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.component, ctx.truthDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.predicate) && ctx.compound instanceof DifferenceExt && ctx.index != 0,
                    (content, ctx, budgeting) -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.component, ctx.truthNDed, BudgetFunctions.EnumBudgetDirection.FORWARD, budgeting)
                ),
            },

            // post-conclusion
            ctx -> null
        ));

        /**
         * from transformSetRelation() in legacy
         * {<S --> {P}>} |- <S <-> {P}>
         */
        rules.put("transformSetRelation", new Rule(
            true,

            new Budgeting(BudgetFunctions.EnumBudgetType.COMPOUND),

            // precondition
            ctx -> {
                if (ctx.compound.size() > 1) {
                    return false;
                }
                if (ctx.statement instanceof Inheritance && ((ctx.compound instanceof SetExt) && (ctx.side == 0)) || ((ctx.compound instanceof SetInt) && (ctx.side == 1))) {
                    return false;
                }
                return true;
            },

            // preamble
            (compound, index, nal, ctx) -> {},

            // content
            ctx -> {
                if (ctx.statement instanceof Inheritance) {
                    return Similarity.make(ctx.subject, ctx.predicate);
                } else {
                    if (((ctx.compound instanceof SetExt) && (ctx.side == 0)) || ((ctx.compound instanceof SetInt) && (ctx.side == 1))) {
                        return Inheritance.make(ctx.predicate, ctx.subject);
                    } else {
                        return Inheritance.make(ctx.subject, ctx.predicate);
                    }
                }
            },

            // conclusions
            // TODO< abstract Budget function >
            new Conclusion[]{
                new Conclusion(
                    ctx -> true,
                    (content, ctx, budgeting) -> {
                        final BudgetValue budget;
                        if (ctx.taskSentence.isJudgment()) {
                            budget = BudgetFunctions.compoundForward(ctx.truth, content, ctx.nal);
                        } else {
                            budget = BudgetFunctions.compoundBackward(content, ctx.nal);
                        }
                        ctx.nal.singlePremiseTask(content, ctx.truth, budget);
                    }
                )
            },

            // post-conclusion
            ctx -> null
        ));

        /**
         * from contraposition() in legacy
         * {<A ==> B>, A@(--, A)} |- <(--, B) ==> (--, A)>
         */
        rules.put("contraposition", new Rule(
            false,

            new Budgeting(BudgetFunctions.EnumBudgetType.COMPOUND),

            // precondition
            ctx -> true,

            // preamble
            (compound, index, nal, ctx) -> {},

            // content
            ctx ->
                Statement.make(ctx.statement,
                    Negation.make(ctx.predicate),
                    Negation.make(ctx.subject),
                    TemporalRules.reverseOrder(ctx.statement.getTemporalOrder())),

            // conclusions
            // TODO< abstract Budget function >
            new Conclusion[]{
                new Conclusion(
                    ctx -> true,
                    (content, ctx, budgeting) -> {
                        final BudgetValue budget;
                        if (ctx.taskSentence.isQuestion() || ctx.taskSentence.isQuest()) {
                            if (content instanceof Implication) {
                                budget = BudgetFunctions.compoundBackwardWeak(content, ctx.nal);
                            } else {
                                budget = BudgetFunctions.compoundBackward(content, ctx.nal);
                            }
                            ctx.nal.singlePremiseTask(content, Symbols.QUESTION_MARK, ctx.truth, budget);
                        } else {
                            if (content instanceof Implication) {
                                ctx.truth = TruthFunctions.contraposition(ctx.truth);
                            }
                            budget = BudgetFunctions.compoundForward(ctx.truth, content, ctx.nal);
                            ctx.nal.singlePremiseTask(content, Symbols.JUDGMENT_MARK, ctx.truth, budget);
                        }
                    }
                )
            },

            // post-conclusion
            ctx -> null
        ));

        // from transformNegation() in legacy
        // {A, A@(--, A)} |- (--, A)
        rules.put("transformNegation", new Rule(
            false,

            new Budgeting(BudgetFunctions.EnumBudgetType.COMPOUND),

            // precondition
            ctx -> true,

            // preamble
            (compound, index, nal, ctx) -> {},

            // content
            ctx -> ctx.compound,

            // conclusions
            new Conclusion[]{
                new Conclusion(
                    ctx -> true,
                    (content, ctx, budgeting) -> {
                        final BudgetValue budget;

                        if (ctx.taskSentence.isJudgment() || ctx.taskSentence.isGoal()) {
                            ctx.truth = TruthFunctions.negation(ctx.truth);
                            budget = BudgetFunctions.compoundForward(ctx.truth, content, ctx.nal);
                        } else {
                            budget = BudgetFunctions.compoundBackward(content, ctx.nal);
                        }
                        ctx.nal.singlePremiseTask(content, ctx.truth, budget);
                    }

                )
            },

            // post-conclusion
            ctx -> null
        ));


        // from analogy() in legacy
        // {<S ==> P>, <M <=> P>} |- <S ==> P>
        rules.put("analogy", new Rule(
            false,

            new Budgeting(BudgetFunctions.EnumBudgetType.COMPOUND), // TODO< change to non-compound >

            // precondition
            ctx -> {
                final Term subject = ctx.termParameter[0];
                final Term predicate = ctx.termParameter[1];

                if (Statement.invalidStatement(subject, predicate)) {
                    return false;
                }

                final Sentence asym = ctx.sentencesParameter[0];
                final Sentence sym = ctx.sentencesParameter[1];

                final int order1 = asym.term.getTemporalOrder();
                final int order2 = sym.term.getTemporalOrder();
                final int order = analogyOrder(order1, order2, ctx.figure);
                if (order == ORDER_INVALID) {
                    return false;
                }

                if (ctx.taskSentence.isQuestion() || ctx.taskSentence.isQuest()) {
                    if(asym.truth==null) { //a question for example
                        return false;
                    }
                }

                return true;
            },

            // preamble
            (compound, index, nal, ctx) -> {},

            // content
            ctx -> null,

            // conclusions
            new Conclusion[]{
                new Conclusion(
                    ctx -> true,
                    (content, ctx, budgeting) -> {
                        final Term subject = ctx.termParameter[0];
                        final Term predicate = ctx.termParameter[1];

                        final Sentence asym = ctx.sentencesParameter[0];
                        final Sentence sym = ctx.sentencesParameter[1];

                        final Statement st = (Statement) asym.term;
                        TruthValue truth = null;
                        final BudgetValue budget;
                        //final Sentence sentence = ctx.nal.getCurrentTask().sentence;
                        final CompoundTerm taskTerm = (CompoundTerm)ctx.taskSentence.term;
                        if (ctx.taskSentence.isQuestion() || ctx.taskSentence.isQuest()) {
                            if (taskTerm.isCommutative()) {
                                budget = BudgetFunctions.backwardWeak(asym.truth, ctx.nal);
                            } else {
                                budget = BudgetFunctions.backward(sym.truth, ctx.nal);
                            }
                        } else {
                            if (ctx.taskSentence.isGoal()) {
                                truth = TruthFunctions.lookupTruthFunctionByBoolAndCompute(taskTerm.isCommutative(), TruthFunctions.EnumType.DESIREWEAK, TruthFunctions.EnumType.DESIRESTRONG, asym.truth, sym.truth);
                            } else {
                                truth = TruthFunctions.analogy(asym.truth, sym.truth);
                            }

                            budget = BudgetFunctions.forward(truth, ctx.nal);
                        }

                        final int order1 = asym.term.getTemporalOrder();
                        final int order2 = sym.term.getTemporalOrder();
                        final int order = analogyOrder(order1, order2, ctx.figure);

                        final Term content2 = Statement.make(st, subject, predicate, order);
                        ctx.nal.doublePremiseTask(content2, truth, budget,false, false); //(allow overlap) but not needed here, isn't detachment
                    }

                )
            },

            // post-conclusion
            ctx -> null
        ));
    }

    public static void executeByRuleName(final String ruleName, final CompoundTerm compoundTerm, final int index, final Statement statement, final int side, final Sentence[] sentencesParameter, final Term[] termParameter, final int figure, final DerivationContext nal) {
        new RuleExecutive().execute(rules.get(ruleName), compoundTerm, index, statement, side, sentencesParameter, termParameter, figure, nal);
    }

    // TODO< complete documentation >
    /**
     *
     * @param sentencesParameter sentence to be passed into the inference rule - can be null if the rule doesn't need to pass it as a parameter - it will be fetched from the nal instead
     * @param figure the configuration of the sides of the statements, can be -1 for invalid or 11 or 12 or 21 or 22
     */
    public void execute(final Rule rule, final CompoundTerm compoundTerm, final int index, final Statement statement, final int side, final Sentence[] sentencesParameter, final Term[] termParameter, final int figure, final DerivationContext nal) {
        Context ctx = new Context();
        ctx.nal = nal;

        ctx.figure = figure;
        ctx.index = index;
        ctx.side = side;
        ctx.compound = compoundTerm;
        ctx.statement = statement;

        ctx.termParameter = termParameter;

        // Some rules need to pass a explicit sentence parameter
        // NOTE< the source of the sentence parameters is not coming from task.sentence, so we need this special handling here >
        ctx.sentencesParameter = sentencesParameter;

        // TODO< remove and pull into rules! >
        if( statement != null ) { // we need to test for this special case because some rules don't have a statement
            ctx.subject = statement.getSubject();
            ctx.predicate = statement.getPredicate();
        }

        if (!rule.precondition.test(ctx)) {
            // we ignore the rule if the precondition fails
            return;
        }

        // this is always the same functionality for all rules
        final Task task = nal.getCurrentTask();
        ctx.taskSentence = task.sentence;

        ctx.truth = ctx.taskSentence.truth;

        // ASK< is this really necessary? >
        // some rules implemented this to prevent accessing null values
        if (rule.checkNullTruth && ctx.truth == null) {
            return;
        }




        // execute preamble to initialize variables for the rule
        rule.preamble.apply(compoundTerm, index, nal, ctx);

        // we have to derive the conclusions if the preconditions of the conclusions are true
        for (final Conclusion iteratorConclusion : rule.conclusions) {
            if (iteratorConclusion.test.apply(ctx)) {
                final Term content = rule.contentLambda.apply(ctx);
                if (content == null) {
                    return;
                }
                iteratorConclusion.action.apply(content, ctx, rule.budgeting);

                rule.postConclusion.apply(ctx);
                return;
            }
        }
    }

    public static class Rule {
        public final boolean checkNullTruth;

        public final Budgeting budgeting;

        public final CheckPrecondition precondition;
        public final Preamble preamble;
        public final ContentLambda contentLambda;
        public final Conclusion[] conclusions;
        public final PostConclusion postConclusion;

        public Rule(
            final boolean checkNullTruth,

            final Budgeting budgeting,

            final CheckPrecondition precondition,
            final Preamble preamble,
            final ContentLambda contentLambda,
            final Conclusion[] conclusions,
            final PostConclusion postConclusion
        ) {
            this.checkNullTruth = checkNullTruth;

            this.budgeting = budgeting;

            this.precondition = precondition;
            this.preamble = preamble;
            this.contentLambda = contentLambda;
            this.conclusions = conclusions;
            this.postConclusion = postConclusion;
        }
    }

    public static class Context {
        // never changed directly by preamble
        public DerivationContext nal;
        public TruthValue truth;
        public int order; // temporal order
        public int figure;

        public Term component;
        public CompoundTerm compound;
        public Statement statement;

        public Term[] termParameter;
        public Sentence[] sentencesParameter; // parameters of the rule
        public Sentence taskSentence; // the sentence of the task

        public Term subject, predicate;
        public int index = -1; // -1 is invalid
        public int side = -1; // -1 is invalid

        // computed truths
        public TruthValue truthDed;
        public TruthValue truthNDed;

        /**
         * creates a new task for a statement with the copula of te current task
         *
         * @param subject the subject of the new created Statement
         * @param predicate the predicate of the new created Statement
         * @param truth the truth of the resultSentence
         */
        public void assignStatementWithOldCopula(final Term subject, final Term predicate, final TruthValue truth, BudgetFunctions.EnumBudgetDirection budgetingDirection, final Budgeting budgeting) {
            final Task task = nal.getCurrentTask();
            final Term oldContent = task.getTerm();
            if (oldContent instanceof Statement) {
                final Statement content = Statement.make((Statement)oldContent, subject, predicate, order);
                if (content != null) {
                    final BudgetValue budget = BudgetFunctions.compute(budgetingDirection, budgeting.budgetType, BudgetFunctions.EnumBudgetStrength.STRONG, truth, content, nal);
                    nal.singlePremiseTask(content, truth, budget);
                }
            }
        }
    }

    public static class Conclusion {
        public ContextTest test;
        public ApplyConclusion action;

        public Conclusion(final ContextTest test, final ApplyConclusion action) {
            this.test = test;
            this.action = action;
        }
    }

    interface ContextTest {
        boolean apply(final Context ctx);
    }

    interface Preamble {
        void apply(final CompoundTerm compoundTerm, final int index, final DerivationContext nal, final Context ctx);
    }

    interface PostConclusion {
        Term apply(final Context ctx);
    }

    /**
     * Functionality used to compute the content term of the result of the applied rule
     */
    interface ContentLambda {
        /**
         *
         * @return can be null if the rule doesn't need to generate content before it executes the conclusions
         */
        Term apply(final Context ctx);
    }

    interface ApplyConclusion {
        /**
         *
         * @param content content as calculated by the content lambda
         * @param ctx Rule executive context which is used to transfer states and variables
         * @param budgeting carries all budgeting related information
         */
        void apply(final Term content, final Context ctx, final Budgeting budgeting);
    }

    interface CheckPrecondition {
        boolean test(final Context ctx);
    }

    static public class Budgeting {
        public final BudgetFunctions.EnumBudgetType budgetType;

        public Budgeting(BudgetFunctions.EnumBudgetType budgetType) {
            this.budgetType = budgetType;
        }
    }
}

