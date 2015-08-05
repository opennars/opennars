package org.projog.core.udp.compiler;

import org.projog.core.PredicateKey;
import org.projog.core.term.PTerm;
import org.projog.core.term.PVar;
import org.projog.core.term.PrologOperator;

/**
 * Contains static methods which aid the construction of {@link CompiledPredicate} source code.
 */
final class CompiledPredicateSourceGeneratorUtils {
   /**
    * Private constructor as all methods are static.
    */
   private CompiledPredicateSourceGeneratorUtils() {
      // do nothing
   }

   static String getClassNameMinusPackage(Object o) {
      String className = o.getClass().getName();
      return className.substring(className.lastIndexOf('.') + 1);
   }

   static String encodeName(PTerm t) {
      return encodeName(t.getName());
   }

   static String encodeName(String s) {
      return "\"" + s.replace("\\", "\\\\") + "\"";
   }

   static String getKeyGeneration(PredicateKey key) {
      return "new PredicateKey(" + encodeName(key.getName()) + ", " + key.getNumArgs() + ")";
   }

   static boolean isNoMoreThanTwoElementList(PTerm t) {
      if (t.type() != PrologOperator.LIST) {
         return false;
      }
      PTerm tail = t.term(1);
      return tail.type() != PrologOperator.LIST;
   }

   static String getUnifyStatement(String variable1, String variable2) {
      return variable1 + ".unify(" + variable2 + ")";
   }

   static String getNewVariableSyntax(PTerm variable) {
      return "new Variable(\"" + ((PVar) variable).getId() + "\")";
   }

   static String getNewListSyntax(String head, String tail) {
      return "ListFactory.createList(" + head + ", " + tail + ")";
   }
}