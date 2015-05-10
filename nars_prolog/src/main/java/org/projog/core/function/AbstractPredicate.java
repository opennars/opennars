package org.projog.core.function;

import org.projog.core.Predicate;
import org.projog.core.term.PTerm;

/** A skeletal implementation of {@link Predicate} */
abstract class AbstractPredicate implements Predicate {
   @Override
   public boolean evaluate(PTerm... args) {
      switch (args.length) {
         case 0:
            return evaluate();
         case 1:
            return evaluate(args[0]);
         case 2:
            return evaluate(args[0], args[1]);
         case 3:
            return evaluate(args[0], args[1], args[2]);
         default:
            throw createWrongNumberOfArgumentsException(args.length);
      }
   }

   protected boolean evaluate() {
      throw createWrongNumberOfArgumentsException(0);
   }

   protected boolean evaluate(PTerm arg) {
      throw createWrongNumberOfArgumentsException(1);
   }

   protected boolean evaluate(PTerm arg1, PTerm arg2) {
      throw createWrongNumberOfArgumentsException(2);
   }

   protected boolean evaluate(PTerm arg1, PTerm arg2, PTerm arg3) {
      throw createWrongNumberOfArgumentsException(3);
   }

   private IllegalArgumentException createWrongNumberOfArgumentsException(int numberOfArguments) {
      throw new IllegalArgumentException("The predicate: " + getClass() + " does next accept the number of arguments: " + numberOfArguments);
   }
}
