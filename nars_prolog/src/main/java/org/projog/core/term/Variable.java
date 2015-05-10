package org.projog.core.term;

import java.util.Map;

/**
 * Represents an unspecified {@link PTerm}.
 * <p>
 * A {@code Variable} can be either instantiated (representing another single {@link PTerm}) or uninstantiated (not
 * representing any other {@link PTerm}). {@code Variable}s are not constants. What {@link PTerm}, if any, a
 * {@code Variable} is instantiated with can vary during it's life time. A {@code Variable} becomes instantiated by
 * calls to {@link #unify(PTerm)} and becomes uninstantiated again by calls to {@link #backtrack()}.
 * <p>
 * <img src="doc-files/Variable.png">
 */
public final class Variable implements PTerm {
   /**
    * The value by which the variable can be identified
    */
   private final String id;

   /**
    * The {@link PTerm} this object is currently instantiated with (or {@code null} if it is currently uninstantiated)
    */
   private PTerm value;

   /**
    * @param id value by which this variable can be identified
    */
   public Variable(String id) {
      this.id = id;
   }

   /**
    * Calls {@link PTerm#getName()} on the {@link PTerm} this variable is instantiated with.
    * 
    * @throws NullPointerException if the {@code Variable} is currently uninstantiated
    */
   @Override
   public String getName() {
      if (value == null) {
         throw new NullPointerException();
      }
      return getValue().getName();
   }

   /**
    * @return value provided in constructor by which this variable can be identified
    */
   public String getId() {
      return id;
   }

   /**
    * Calls {@link PTerm#getArgs()} on the {@link PTerm} this variable is instantiated with.
    * 
    * @throws NullPointerException if the {@code Variable} is currently uninstantiated
    */
   @Override
   public PTerm[] getArgs() {
      if (value == null) {
         throw new NullPointerException();
      }
      return getValue().getArgs();
   }

   /**
    * Calls {@link PTerm#args()} on the {@link PTerm} this variable is instantiated with.
    * 
    * @throws NullPointerException if the {@code Variable} is currently uninstantiated
    */
   @Override
   public int args() {
      if (value == null) {
         throw new NullPointerException();
      }
      return getValue().args();
   }

   /**
    * Calls {@link PTerm#arg(int)} on the {@link PTerm} this variable is instantiated with.
    * 
    * @throws NullPointerException if the {@code Variable} is currently uninstantiated
    */
   @Override
   public PTerm arg(int index) {
      if (value == null) {
         throw new NullPointerException();
      }
      return getValue().arg(index);
   }

   @Override
   public boolean unify(PTerm t) {
      if (value == null) {
         if (this != t) {
            value = t;
         }
         return true;
      } else {
         return getValue().unify(t.get());
      }
   }

   @Override
   public boolean strictEquals(PTerm t) {
      boolean b;
      if (this == t) {
         b = true;
      } else if (value != null) {
         b = getValue().strictEquals(t);
      } else if (t.type() == TermType.NAMED_VARIABLE && ((Variable) t).value != null) {
         // this is for when two unassigned variables are unified with each other
         b = t.strictEquals(this);
      } else {
         b = false;
      }
      return b;
   }

   /**
    * Returns {@link TermType#NAMED_VARIABLE} if uninstantiated else {@link TermType} of instantiated {@link PTerm}.
    * 
    * @return {@link TermType#NAMED_VARIABLE} if this variable is uninstantiated else calls {@link PTerm#type()} on
    * the {@link PTerm} this variable is instantiated with.
    */
   @Override
   public TermType type() {
      if (value == null) {
         return TermType.NAMED_VARIABLE;
      } else {
         return getValue().type();
      }
   }

   /**
    * Always returns {@code false} even if instantiated with an immutable {@link PTerm}.
    * 
    * @return {@code false}
    */
   @Override
   public boolean constant() {
      return false;
   }

   @Override
   public PTerm copy(Map<Variable, Variable> sharedVariables) {
      if (value == null) {
         Variable result = sharedVariables.get(this);
         if (result == null) {
            result = new Variable(id);
            sharedVariables.put(this, result);
         }
         return result.get();
      } else {
         return getValue().copy(sharedVariables);
      }
   }

   /**
    * @return itself if this variable is uninstantiated else calls {@link PTerm#type()} on the {@link PTerm} this
    * variable is instantiated with.
    */
   @Override
   public PTerm get() {
      if (value == null) {
         return this;
      } else {
         return getValue().get();
      }
   }

   private PTerm getValue() {
      if (value.getClass() == Variable.class) {
         // if variable assigned to another variable use while loop
         // rather than value.getTerm() to avoid StackOverflowError
         PTerm t = value;
         do {
            Variable v = (Variable) t;
            if (v.value == null) {
               return v;
            }
            if (v.value.getClass() != Variable.class) {
               return v.value;
            }
            t = v.value;
         } while (true);
      } else {
         return value;
      }
   }

   /**
    * Reverts this variable to an uninstantiated state.
    */
   @Override
   public void backtrack() {
      value = null;
   }

   /**
    * @return if this variable is uninstantiated then returns this variable's id else calls {@code toString()} on the
    * {@link PTerm} this variable is instantiated with.
    */
   @Override
   public String toString() {
      if (value == null) {
         return id;
      } else {
         return getValue().toString();
      }
   }
}