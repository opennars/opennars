package org.projog.core.udp;

import static org.projog.core.KnowledgeBaseUtils.IMPLICATION_PREDICATE_NAME;

import org.projog.core.term.Atom;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermUtils;

/**
 * Represents a clause.
 * <p>
 * A clause consists of a head and a body. Where a clause is not explicitly specified it defaults to having a body of
 * {@code true}.
 * <p>
 * Called {@code ClauseModel} to differentiate it from {@link org.projog.core.udp.interpreter.ClauseAction}.
 */
public final class ClauseModel {
   private static final PTerm TRUE = new Atom("true");

   private final PTerm original;
   private final PTerm consequent;
   private final PTerm antecedant;

   public static ClauseModel createClauseModel(PTerm original) {
      final PTerm consequent;
      final PTerm antecedant;

      if (DefiniteClauseGrammerConvertor.isDCG(original)) {
         original = DefiniteClauseGrammerConvertor.convert(original);
      }

      if (original.getName().equals(IMPLICATION_PREDICATE_NAME)) {
         PTerm[] implicationArgs = original.getArgs();
         consequent = implicationArgs[0];
         if (implicationArgs.length == 2) {
            antecedant = implicationArgs[1];
         } else if (implicationArgs.length == 1) {
            antecedant = TRUE;
         } else {
            throw new RuntimeException();
         }
      } else {
         consequent = original;
         antecedant = TRUE;
      }

      return new ClauseModel(original, consequent, antecedant);
   }

   private ClauseModel(PTerm original, PTerm consequent, PTerm antecedant) {
      this.original = original;
      this.consequent = consequent;
      this.antecedant = antecedant;
   }

   public PTerm getAntecedant() {
      return antecedant;
   }

   public PTerm getConsequent() {
      return consequent;
   }

   public PTerm getOriginal() {
      return original;
   }

   public ClauseModel copy() {
      PTerm[] newTerms = TermUtils.copy(original, consequent, antecedant);
      return new ClauseModel(newTerms[0], newTerms[1], newTerms[2]);
   }

   @Override
   public String toString() {
      return "[" + super.toString() + " " + consequent + " " + antecedant + "]";
   }
}