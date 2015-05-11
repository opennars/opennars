package org.projog.core.term;

/**
 * Defines the type of terms supported by Projog.
 * 
 * @see PTerm#type()
 */
public enum PrologOperator {
   NONE(false, false, false, -1),

   /** @see PVar */
   NAMED_VARIABLE(false, false, true, 1),
   /** @see DecimalFraction */
   FRACTION(false, true, false, 2),
   /** @see IntegerNumber */
   INTEGER(false, true, false, 3),
   /** @see EmptyList */
   EMPTY_LIST(false, false, false, 4),
   /** @see PAtom */
   ATOM(false, false, false, 5),
   /** @see PStruct */
   STRUCTURE(true, false, false, 6),
   /** @see PList */
   LIST(true, false, false, 6);

   private final boolean isStructure;
   private final boolean isNumeric;
   private final boolean isVariable;
   private final int precedence;

   private PrologOperator(boolean isStructure, boolean isNumeric, boolean isVariable, int precedence) {
      this.isStructure = isStructure;
      this.isNumeric = isNumeric;
      this.isVariable = isVariable;
      this.precedence = precedence;
   }

   /**
    * @return {@code true} if this type represents "compound structure"
    */
   public boolean isStructure() {
      return isStructure;
   }

   /**
    * @return {@code true} if this type represents instances of {@link Numeric}
    */
   public boolean isNumeric() {
      return isNumeric;
   }

   /**
    * @return {@code true} if this type represents a variable
    */
   public boolean isVariable() {
      return isVariable;
   }

   /**
    * Used to consistently order {@link PTerm}s of different types.
    * 
    * @return precedence of this type
    * @see TermComparator#compare(PTerm, PTerm)
    */
   public int getPrecedence() {
      return precedence;
   }
}