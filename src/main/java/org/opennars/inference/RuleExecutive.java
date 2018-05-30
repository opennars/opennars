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
import org.opennars.language.*;
import org.opennars.main.Parameters;

import java.util.HashMap;
import java.util.Map;

class RuleExecutive {
    public static Map<String, Rule> rules = new HashMap<>();

    static {
        rules.put("structuralCompose1", new Rule(
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

            // conclusions
            new Conclusion[]{
                // from structuralCompose1() in legacy
                //  {<S --> P>, P@(P|Q)} |- <S --> (P|Q)>

                new Conclusion(
                    ctx -> ctx.component.equals(ctx.subject) && (ctx.compound instanceof IntersectionExt),
                    ctx -> ctx.assignStatementWithOldCopula(ctx.compound, ctx.predicate, ctx.truthDed)
                ),
                new Conclusion(
                    ctx -> ctx.component.equals(ctx.subject) && ctx.compound instanceof DifferenceExt && ctx.index == 0,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.compound, ctx.predicate, ctx.truthDed)
                ),
                new Conclusion(
                    ctx -> ctx.component.equals(ctx.subject) && ctx.compound instanceof DifferenceInt && ctx.index != 0,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.compound, ctx.predicate, ctx.truthNDed)
                ),

                new Conclusion(
                    ctx -> ctx.component.equals(ctx.predicate) && ctx.compound instanceof IntersectionInt,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.compound, ctx.truthDed)
                ),
                new Conclusion(
                    ctx -> ctx.component.equals(ctx.predicate) && ctx.compound instanceof DifferenceExt && ctx.index != 0,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.compound, ctx.truthNDed)
                ),
                new Conclusion(
                    ctx -> ctx.component.equals(ctx.predicate) && ctx.compound instanceof DifferenceInt && ctx.index == 0,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.compound, ctx.truthDed)
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

            // conclusions
            new Conclusion[]{
                // from structuralDecompose1() in legacy
                //  {<(S|T) --> P>, S@(S|T)} |- <S --> P> {<S --> (P&T)>, P@(P&T)} |- <S --> P>

                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.subject) && ctx.compound instanceof IntersectionInt,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.component, ctx.predicate, ctx.truthDed)
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.subject) && ctx.compound instanceof SetExt && (ctx.compound.size() > 1),
                    ctx -> {
                        final Term[] t1 = new Term[]{ctx.component};
                        ctx.assignStatementWithOldCopula(new SetExt(t1), ctx.predicate, ctx.truthDed);
                    }
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.subject) && ctx.compound instanceof DifferenceInt && ctx.index == 0,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.component, ctx.predicate, ctx.truthDed)
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.subject) && ctx.compound instanceof DifferenceInt && ctx.index != 0,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.component, ctx.predicate, ctx.truthNDed)
                ),

                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.predicate) && ctx.compound instanceof IntersectionExt,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.component, ctx.truthDed)
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.predicate) && (ctx.compound instanceof SetInt) && (ctx.compound.size() > 1),
                    ctx -> ctx.assignStatementWithOldCopula(ctx.subject, new SetInt(ctx.component), ctx.truthDed)
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.predicate) && ctx.compound instanceof DifferenceExt && ctx.index == 0,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.component, ctx.truthDed)
                ),
                new Conclusion(
                    ctx -> ctx.compound.equals(ctx.predicate) && ctx.compound instanceof DifferenceExt && ctx.index != 0,
                    ctx -> ctx.assignStatementWithOldCopula(ctx.subject, ctx.component, ctx.truthNDed)
                ),
            },

            // post-conclusion
            ctx -> {
                return null;
            }
        ));
    }

    public static void executeByRuleName(final String ruleName, final CompoundTerm compoundTerm, final int index, final Statement statement, final DerivationContext nal) {
        new RuleExecutive().execute(rules.get(ruleName), compoundTerm, index, statement, nal);
    }

    public void execute(final Rule rule, final CompoundTerm compoundTerm, final int index, final Statement statement, final DerivationContext nal) {
        Context ctx = new Context();
        ctx.nal = nal;

        ctx.index = index;
        ctx.compound = compoundTerm;

        if (!rule.precondition.test(ctx)) {
            // we ignore the rule if the precondition fails
            return;
        }

        // this is always the same functionality for all rules
        final Task task = nal.getCurrentTask();
        final Sentence sentence = task.sentence;
        ctx.order = sentence.getTemporalOrder();
        ctx.truth = sentence.truth;

        if (ctx.truth == null) {
            return;
        }

        ctx.subject = statement.getSubject();
        ctx.predicate = statement.getPredicate();

        // execute preamble to initialize variables for the rule
        rule.preamble.apply(compoundTerm, index, nal, ctx);

        // we have to derive the conclusions if the preconditions of the conclusions are true
        for (final Conclusion iteratorConclusion : rule.conclusions) {
            if (iteratorConclusion.test.apply(ctx)) {
                iteratorConclusion.action.apply(ctx);

                rule.postConclusion.apply(ctx);
                return;
            }
        }
    }

    public static class Rule {
        public final CheckPrecondition precondition;
        public final Preamble preamble;
        public final Conclusion[] conclusions;
        public final PostConclusion postConclusion;

        public Rule(
            final CheckPrecondition precondition,
            final Preamble preamble,
            final Conclusion[] conclusions,
            final PostConclusion postConclusion
        ) {
            this.precondition = precondition;
            this.preamble = preamble;
            this.conclusions = conclusions;
            this.postConclusion = postConclusion;
        }
    }

    public static class Context {
        // never changed directly by preamble
        public DerivationContext nal;
        public TruthValue truth;
        public int order; // temporal order

        public Term component;
        public CompoundTerm compound;

        public Term subject, predicate;
        public int index = -1; // -1 is invalid

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
        public void assignStatementWithOldCopula(final Term subject, final Term predicate, final TruthValue truth) {
            final Task task = nal.getCurrentTask();
            final Term oldContent = task.getTerm();
            if (oldContent instanceof Statement) {
                final Statement content = Statement.make((Statement)oldContent, subject, predicate, order);
                if (content != null) {
                    final BudgetValue budget = BudgetFunctions.compoundForward(truth, content, nal);
                    nal.singlePremiseTask(content, truth, budget);
                }

            }
        }
    }

    public static class Conclusion {
        public ContextTest test;
        public ApplyContext action;

        public Conclusion(final ContextTest test, final ApplyContext action) {
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

    interface ApplyContext {
        void apply(final Context ctx);
    }

    interface CheckPrecondition {
        boolean test(final Context ctx);
    }
}

