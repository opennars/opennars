package org.projog.core.term;

/**
 * Unifies the arguments in the head (consequent) of a clause with a query.
 */
public final class Unifier {
   /**
    * Private constructor as {@link #preMatch(PTerm[], PTerm[])} is static.
    */
   private Unifier() {
      // do nothing
   }

   /**
    * Unifies the arguments in the head (consequent) of a clause with a query.
    * <p>
    * When Prolog attempts to answer a query it searches it's knowledge base for all rules with the same functor and
    * arity. For each rule founds it attempts to unify the arguments in the query with the arguments in the head
    * (consequent) of the rule. Only if the query and rule's head can be unified can it attempt to evaluate the body
    * (antecedant) of the rule to determine if the rule is true.
    * 
    * @param inputArgs the arguments contained in the query
    * @param consequentArgs the arguments contained in the head (consequent) of the clause
    * @return {@code true} if the attempt to unify the arguments was successful
    * @see PTerm#unify(PTerm)
    */
   public static boolean preMatch(PTerm[] inputArgs, PTerm[] consequentArgs) {
      for (int i = 0; i < inputArgs.length; i++) {
         if (!inputArgs[i].unify(consequentArgs[i])) {
            return false;
         }
      }
      for (int i = 0; i < inputArgs.length; i++) {
         consequentArgs[i] = consequentArgs[i].get();
      }
      return true;
   }
}