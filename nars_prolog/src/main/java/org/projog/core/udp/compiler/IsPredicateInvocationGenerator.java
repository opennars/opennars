package org.projog.core.udp.compiler;

import org.projog.core.term.Numeric;
import org.projog.core.term.PTerm;
import org.projog.core.term.PrologOperator;
import org.projog.core.term.TermUtils;

import java.util.HashMap;
import java.util.Map;

import static org.projog.core.KnowledgeBaseUtils.getCalculatables;
import static org.projog.core.udp.compiler.CompiledPredicateSourceGeneratorUtils.getUnifyStatement;

/**
 * Outputs java code that matches functionality of {@link org.projog.core.function.math.Is}
 */
final class IsPredicateInvocationGenerator implements PredicateInvocationGenerator {
   /**
    * Contains a collection of standard mathematical operators.
    * <p>
    * Maps from Prolog version (key) to Java version (value).
    */
   private static final Map<String, String> ops = new HashMap<>();
   static {
      ops.put("+", "+");
      ops.put("-", "-");
      ops.put("*", "*");
      ops.put("rem", "%");
   }

   @Override
   public void generate(CompiledPredicateWriter g) {
      PTerm function = g.currentClause().getCurrentFunction();

      // only add arg1 variables to currentClause.variablesToBackTrack as arg2's will not be updated
      PTerm arg1 = function.term(0);
      g.currentClause().addVariablesToBackTrack(TermUtils.getAllVariablesInTerm(arg1));

      PTerm arg2 = function.term(1);

      String numeric = getCalculatableExpression(g, arg2);

      if (arg1.type() == PrologOperator.NAMED_VARIABLE && g.declareVariableIfNotAlready(arg1, false)) {
         String variableId = g.getVariableId(arg1);
         g.classVariables().addAssignedVariable(variableId);
         g.assign(variableId, numeric);
      } else {
         String variableId = g.outputCreateTermStatement(arg1, true);
         String eval = "!" + getUnifyStatement(variableId, numeric); // reuse unify statement
         g.outputIfTrueThenBreak(eval);
      }
   }

   // TODO method too long - refactor
   private String getCalculatableExpression(CompiledPredicateWriter g, PTerm t) {
      if (t.constant()) {
         return g.outputCreateTermStatement(getNumeric(g, t), true);
      } else if (t.type() == PrologOperator.STRUCTURE && t.length() == 2 && ops.containsKey(t.getName())) {
         String op = ops.get(t.getName());
         PTerm arg1 = t.term(0);
         PTerm arg2 = t.term(1);
         boolean isResultDouble = arg1.type() == PrologOperator.FRACTION || arg2.type() == PrologOperator.FRACTION;
         String string1;
         if (arg1.type().isVariable() == false && arg1.type().isNumeric() == false) {
            string1 = "num" + g.currentClause().getNextNumericIndex();
            g.assign("Numeric " + string1, getNumeric(getCalculatableExpression(g, arg1), g));
         } else {
            string1 = g.outputCreateTermStatement(t.term(0), true);
         }
         String string2;
         if (arg2.type().isVariable() == false && arg2.type().isNumeric() == false) {
            string2 = "num" + g.currentClause().getNextNumericIndex();
            g.assign("Numeric " + string2, getNumeric(getCalculatableExpression(g, arg2), g));
         } else {
            string2 = g.outputCreateTermStatement(t.term(1), true);
         }
         StringBuilder s = new StringBuilder();
         String arg1TempNumericPlaceholder = null;
         String arg2TempNumericPlaceholder = null;
         if (isResultDouble == false) {
            s.append("((");
            if (arg1.type().isNumeric() == false) {
               arg1TempNumericPlaceholder = g.classVariables().getNewTempNumericName();
               g.writeStatement("final Numeric " + arg1TempNumericPlaceholder);
               s.append("(" + arg1TempNumericPlaceholder + "=" + getNumeric(string1, g) + ").getType()==TermType.INTEGER");
            }
            if (arg1.type().isNumeric() == false && arg2.type().isNumeric() == false) {
               s.append(" & ");
            }
            if (arg2.type().isNumeric() == false) {
               arg2TempNumericPlaceholder = g.classVariables().getNewTempNumericName();
               g.writeStatement("final Numeric " + arg2TempNumericPlaceholder);
               s.append("(" + arg2TempNumericPlaceholder + "=" + getNumeric(string2, g) + ").getType()==TermType.INTEGER");
            }
            s.append(")?");
            s.append("new IntegerNumber(");
            if (arg1.type().isNumeric()) {
               s.append(((Numeric) arg1).getLong());
            } else {
               s.append(arg1TempNumericPlaceholder + ".getLong()");
            }
            s.append(op);
            if (arg2.type().isNumeric()) {
               s.append(((Numeric) arg2).getLong());
            } else {
               s.append(arg2TempNumericPlaceholder + ".getLong()");
            }
            s.append(")");
            s.append(":");
         } else {
            if (arg1.type().isNumeric() == false) {
               arg1TempNumericPlaceholder = getNumeric(string1, g);
            }
            if (arg2.type().isNumeric() == false) {
               arg2TempNumericPlaceholder = getNumeric(string2, g);
            }
         }
         s.append("new DecimalFraction(");
         if (arg1.type().isNumeric()) {
            s.append(((Numeric) arg1).getDouble());
         } else {
            s.append(arg1TempNumericPlaceholder + ".getDouble()");
         }
         s.append(op);
         if (arg2.type().isNumeric()) {
            s.append(((Numeric) arg2).getDouble());
         } else {
            s.append(arg2TempNumericPlaceholder + ".getDouble()");
         }
         if (isResultDouble == false) {
            s.append(")");
         }
         s.append(")");
         return s.toString();
      } else {
         return getNumeric(g.outputCreateTermStatement(t, true), g);
      }
   }

   private Numeric getNumeric(CompiledPredicateWriter g, PTerm t) {
      return getCalculatables(g.knowledgeBase()).getNumeric(t);
   }

   private static String getNumeric(final String variableId, final CompiledPredicateWriter g) {
      g.setNeedsCalculatablesStaticVariable(true);
      return "c.getNumeric(" + variableId + ")";
   }
}