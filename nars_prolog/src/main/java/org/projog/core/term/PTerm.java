package org.projog.core.term;


import java.util.Map;

/**
 * The building blocks used to construct Prolog programs and queries.
 * <p>
 * <img src="doc-files/Term.png">
 */
public interface PTerm  {
   /**
    * Returns a string representation of this term.
    * <p>
    * Exact value returned will vary by {@link PrologOperator}.
    * 
    * @return a string representation of this term
    */
   String getName();

   /**
    * Returns an array of this terms's arguments.
    * <p>
    * <b>Note: for performance reasons the array returned is the same array used internally be the term instance so be
    * careful not to alter the array returned as changes will be reflected in the original term.</b>
    * 
    * @return array of this terms's arguments
    * @see #term(int)
    */
   PTerm[] terms();

   /**
    * Returns the number of arguments in this term.
    *
    * @return number of arguments in this term
    */
   int length();

   /**
    * Returns the term at the specified position in this term's arguments.
    * 
    * @param index index of the argument to return
    * @return the term at the specified position in this term's arguments
    * @throws RuntimeException if the index is out of range ({@code index < 0 || index >= getNumberOfArguments()})
    */
   PTerm term(int index);

   /**
    * Returns the {@link PrologOperator} represented by this term.
    * 
    * @return the {@link PrologOperator} this term represents
    */
   PrologOperator type();

   /**
    * Returns a copy of this term.
    * <p>
    * The returned copy will share any immutable terms contained in this term. The returned copy will contain new
    * instances for any {@link PVar}s contained in this term. The {@code sharedVariables} parameter keeps track of
    * which {@link PVar}s have already been copied.
    * 
    * @param sharedVariables keeps track of which {@link PVar}s have already been copied (key = original version,
    * value = version used in copy)
    * @return a copy of this term
    */
   PTerm copy(Map<PVar, PVar> sharedVariables);

   /**
    * Returns the current instantiated state of this term.
    * <p>
    * Returns a representation of this term with all instantiated {@link PVar}s replaced with the terms they are
    * instantiated with.
    * 
    * @return a representation of this term with all instantiated {@link PVar}s replaced with the terms they are
    * instantiated with.
    */
   PTerm get();

   /**
    * Attempts to unify this term to the specified term.
    * <p>
    * The rules for deciding if two terms are unifiable are as follows:
    * <ul>
    * <li>An uninstantiated {@link PVar} will unify with any term. As a result the {@link PVar} will become
    * instantiated to the other term. The instantiaton will be undone when {@link #backtrack()} is next called on the
    * {@link PVar}</li>
    * <li>Non-variable terms will unify with other terms that are of the same {@link PrologOperator} and have the same value.
    * The exact meaning of "having the same value" will vary between term types but will include that the two terms
    * being unified have the same number of arguments and that all of their corresponding arguments unify.</li>
    * </ul>
    * <b>Note: can leave things in "half-state" on failure as neither List or Predicate backtrack earlier args.</b>
    * 
    * @param t the term to unify this term against
    * @return {@code true} if the attempt to unify this term to the given term was successful
    * @see #backtrack()
    */
   boolean unify(PTerm t);

   /**
    * Performs a strict comparison of this term to the specified term.
    * <p>
    * "Strict" equality means that an uninstantiated {@link PVar} will only be considered equal to itself or another
    * {@code Variable} that is already instantiated to it.
    * 
    * @param t the term to compare this term against
    * @return {@code true} if the given term represents a {@code Term} strictly equivalent to this term
    */
   boolean strictEquals(PTerm t);

   /**
    * Reverts this term back to it's original state prior to any unifications.
    * <p>
    * Makes all {@link PVar}s that this term consists of uninstantiated.
    * 
    * @see #unify(PTerm)
    */
   void backtrack();

   /**
    * Returns {@code true} is this term is immutable.
    * <p>
    * A term is considered immutable if it's value will never change as a result of executing it's {@link #unify(PTerm)}
    * or {@link #backtrack()} methods. A term will not be considered immutable if it is a {@link PVar} or any of
    * it's arguments are not immutable.
    * 
    * @return {@code true} is this term is immutable
    */
   boolean constant();
}