package org.projog.core.term;

import java.util.Map;

/**
 * A {@link PTerm} consisting of a functor (name) and a number of other {@link PTerm} arguments.
 * <p>
 * Also known as a "compound term".
 * <p>
 * <img src="doc-files/Structure.png">
 */
public final class PStruct implements PTerm {
   private final String functor;
   private final PTerm[] args;
   private final boolean immutable;

   /**
    * Factory method for creating {@code Structure} instances.
    * <p>
    * The reason that {@code Structure}s have to be created via a factory method, rather than a constructor, is to
    * enforce:
    * <ul>
    * <li>structures with the functor {@code .} and two arguments are created as instances of {@link PList}</li>
    * <li>no structures can be created without any arguments</li>
    * </ul>
    * 
    * @param functor the name of the new term
    * @param args arguments for the new term
    * @return either a new {@link PStruct}, a new {@link PList} or {@link EmptyList#EMPTY_LIST}
    */
   public static PTerm make(String functor, PTerm[] args) {
      if (args.length == 0) {
         throw new IllegalArgumentException("Cannot create structure with no arguments");
      }

      if (ListFactory.LIST_PREDICATE_NAME.equals(functor)) {
         if (args.length == 2) {
            return ListFactory.createList(args[0], args[1]);
         }
         functor = ListFactory.LIST_PREDICATE_NAME;
      }

      return new PStruct(functor, args, isImmutable(args));
   }

   private static boolean isImmutable(PTerm[] args) {
      for (PTerm t : args) {
         if (t.constant() == false) {
            return false;
         }
      }
      return true;
   }

   /**
    * Private constructor to force use of {@link #make(String, PTerm[])}
    * 
    * @param immutable is this structure immutable (i.e. are all it's arguments known to be immutable)?
    */
   private PStruct(String functor, PTerm[] args, boolean immutable) {
      this.functor = functor;
      this.args = args;
      this.immutable = immutable;
   }

   /**
    * Returns the functor of this structure.
    * 
    * @return the functor of this structure
    */
   @Override
   public String getName() {
      return functor;
   }

   @Override
   public PTerm[] terms() {
      return args;
   }

   @Override
   public int length() {
      return args.length;
   }

   @Override
   public PTerm term(int index) {
      return args[index];
   }

   /**
    * Returns {@link PrologOperator#STRUCTURE}.
    * 
    * @return {@link PrologOperator#STRUCTURE}
    */
   @Override
   public PrologOperator type() {
      return PrologOperator.STRUCTURE;
   }

   @Override
   public boolean constant() {
      return immutable;
   }

   @Override
   public PStruct get() {
      if (immutable) {
         return this;
      } else {
         boolean returnThis = true;
         boolean newImmutable = true;
         PTerm newArgs[] = new PTerm[args.length];
         for (int i = 0; i < args.length; i++) {
            newArgs[i] = args[i].get();
            if (newArgs[i] != args[i]) {
               returnThis = false;
            }
            if (newArgs[i].constant() == false) {
               newImmutable = false;
            }
         }
         if (returnThis) {
            return this;
         } else {
            return new PStruct(functor, newArgs, newImmutable);
         }
      }
   }

   @Override
   public PStruct copy(Map<PVar, PVar> sharedVariables) {
      if (immutable) {
         return this;
      } else {
         boolean returnThis = true;
         boolean newIsImmutable = true;
         PTerm newArgs[] = new PTerm[args.length];
         for (int i = 0; i < args.length; i++) {
            newArgs[i] = args[i].copy(sharedVariables);
            if (newArgs[i] != args[i]) {
               returnThis = false;
            }
            if (newArgs[i].constant() == false) {
               newIsImmutable = false;
            }
         }
         if (returnThis) {
            return this;
         } else {
            return new PStruct(functor, newArgs, newIsImmutable);
         }
      }
   }

   @Override
   public boolean unify(PTerm t) {
      PrologOperator tType = t.type();
      if (tType == PrologOperator.STRUCTURE) {
         PTerm[] tArgs = t.terms();
         if (args.length != tArgs.length) {
            return false;
         }
         if (!functor.equals(t.getName())) {
            return false;
         }
         for (int i = 0; i < args.length; i++) {
            if (!args[i].unify(tArgs[i])) {
               return false;
            }
         }
         return true;
      } else if (tType.isVariable()) {
         return t.unify(this);
      } else {
         return false;
      }
   }

   /**
    * Performs a strict comparison of this term to the specified term.
    * <p>
    * A {@code Structure} is considered strictly equal to another term if:
    * <ul>
    * <li>The other term is of type {@link PrologOperator#STRUCTURE}</li>
    * <li>The two terms have the equal functors</li>
    * <li>The two terms have the same number of arguments</li>
    * <li>All corresponding arguments are strictly equal</li>
    * </ul>
    * 
    * @param t the term to compare this term against
    * @return {@code true} if the given term is strictly equal to this term
    */
   @Override
   public boolean strictEquals(PTerm t) {
      if (t.type() != PrologOperator.STRUCTURE) {
         return false;
      }
      PTerm[] tArgs = t.terms();
      if (args.length != tArgs.length) {
         return false;
      }
      if (!functor.equals(t.getName())) {
         return false;
      }
      for (int i = 0; i < args.length; i++) {
         if (!args[i].strictEquals(tArgs[i])) {
            return false;
         }
      }
      return true;
   }

   @Override
   public void backtrack() {
      if (!immutable) {
         TermUtils.backtrack(args);
      }
   }

   /**
    * Returns a {@code String} representation of this term.
    * <p>
    * The value returned will consist of the structure's functor followed be a comma separated list of it's arguments
    * enclosed in brackets.
    * <p>
    * Example: {@code functor(arg1, arg2, arg3)}
    */
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(functor);
      sb.append("(");
      boolean first = true;
      if (args != null) {
         for (PTerm arg : args) {
            if (first) {
               first = false;
            } else {
               sb.append(", ");
            }
            sb.append(arg);
         }
      }
      sb.append(")");
      return sb.toString();
   }
}