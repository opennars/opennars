package org.projog.core.udp;

import static org.projog.core.KnowledgeBaseUtils.CONJUNCTION_PREDICATE_NAME;
import static org.projog.core.KnowledgeBaseUtils.IMPLICATION_PREDICATE_NAME;
import static org.projog.core.KnowledgeBaseUtils.toArrayOfConjunctions;

import java.util.ArrayList;

import org.projog.core.ProjogException;
import org.projog.core.term.PList;
import org.projog.core.term.ListFactory;
import org.projog.core.term.Structure;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermType;
import org.projog.core.term.Variable;

/**
 * Provides support for Definite Clause Grammars (DCG).
 * <p>
 * DCGs provide a convenient way to express grammar rules.
 */
final class DefiniteClauseGrammerConvertor {
   static boolean isDCG(PTerm dcgTerm) { // should this be moved to KnowledgeBaseUtils?
      return dcgTerm.type() == TermType.STRUCTURE && dcgTerm.args() == 2 && dcgTerm.getName().equals("-->");
   }

   /**
    * @param dcgTerm predicate with name "-->" and two arguments
    */
   static PTerm convert(PTerm dcgTerm) {
      if (isDCG(dcgTerm) == false) {
         throw new ProjogException("Expected two argument predicate named \"-->\" but got: " + dcgTerm);
      }

      PTerm consequent = getConsequent(dcgTerm);
      PTerm antecedant = getAntecedant(dcgTerm);
      // slightly inefficient as will have already converted to an array in validate method
      PTerm antecedants[] = toArrayOfConjunctions(antecedant);

      if (hasSingleListWithSingleAtomElement(antecedants)) {
         return convertSingleListTermAntecedant(consequent, antecedants[0]);
      } else {
         return convertConjunctionOfAtomsAntecedant(consequent, antecedants);
      }
   }

   private static PTerm convertSingleListTermAntecedant(PTerm consequent, PTerm antecedant) {
      String consequentName = consequent.getName();
      Variable variable = new Variable("A");
      PList list = ListFactory.createList(antecedant.arg(0), variable);
      PTerm[] args = {list, variable};
      return Structure.createStructure(consequentName, args);
   }

   // TODO this method is too long - refactor
   private static PTerm convertConjunctionOfAtomsAntecedant(PTerm consequent, PTerm[] conjunctionOfAtoms) {
      ArrayList<PTerm> newSequence = new ArrayList<>();

      Variable lastArg = new Variable("A0");

      int varctr = 1;
      PTerm previous = lastArg;
      PTerm previousList = null;
      for (int i = conjunctionOfAtoms.length - 1; i > -1; i--) {
         PTerm term = conjunctionOfAtoms[i];
         if (term.getName().equals("{")) {
            PTerm newAntecedantArg = term.arg(0).arg(0);
            newSequence.add(0, newAntecedantArg);
         } else if (term.type() == TermType.LIST) {
            if (previousList != null) {
               term = appendToEndOfList(term, previousList);
            }
            previousList = term;
         } else {
            if (previousList != null) {
               Variable next = new Variable("A" + (varctr++));
               PTerm newAntecedantArg = Structure.createStructure("=", new PTerm[] {next, appendToEndOfList(previousList, previous)});
               newSequence.add(0, newAntecedantArg);
               previousList = null;
               previous = next;
            }

            Variable next = new Variable("A" + (varctr++));
            PTerm newAntecedantArg = createNewPredicate(term, next, previous);
            previous = next;
            newSequence.add(0, newAntecedantArg);
         }
      }

      PTerm newAntecedant;
      if (newSequence.isEmpty()) {
         newAntecedant = null;
      } else {
         newAntecedant = newSequence.get(0);
         for (int i = 1; i < newSequence.size(); i++) {
            newAntecedant = Structure.createStructure(CONJUNCTION_PREDICATE_NAME, new PTerm[] {newAntecedant, newSequence.get(i)});
         }
      }

      if (previousList != null) {
         previous = appendToEndOfList(previousList, previous);
      }
      PTerm newConsequent = createNewPredicate(consequent, previous, lastArg);

      if (newAntecedant == null) {
         return newConsequent;
      } else {
         return Structure.createStructure(IMPLICATION_PREDICATE_NAME, new PTerm[] {newConsequent, newAntecedant});
      }
   }

   private static PTerm appendToEndOfList(PTerm list, PTerm newTail) {
      ArrayList<PTerm> terms = new ArrayList<>();
      while (list.type() == TermType.LIST) {
         terms.add(list.arg(0));
         list = list.arg(1);
      }
      return ListFactory.createList(terms.toArray(new PTerm[terms.size()]), newTail);
   }

   private static PTerm createNewPredicate(PTerm original, PTerm previous, PTerm next) {
      PTerm[] args = new PTerm[original.args() + 2];
      for (int a = 0; a < original.args(); a++) {
         args[a] = original.arg(a);
      }
      args[original.args()] = previous;
      args[original.args() + 1] = next;
      return Structure.createStructure(original.getName(), args);
   }

   private static PTerm getConsequent(PTerm dcgTerm) {
      return dcgTerm.arg(0);
   }

   private static PTerm getAntecedant(PTerm dcgTerm) {
      return dcgTerm.arg(1);
   }

   private static boolean hasSingleListWithSingleAtomElement(PTerm[] terms) {
      return terms.length == 1 && terms[0].type() == TermType.LIST && terms[0].arg(0).type() == TermType.ATOM && terms[0].arg(1).type() == TermType.EMPTY_LIST;
   }
}