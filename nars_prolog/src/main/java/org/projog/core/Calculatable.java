package org.projog.core;

import org.projog.core.term.Numeric;
import org.projog.core.term.PTerm;

/**
 * Represents an arithmetic expression.
 * <p>
 * <img src="doc-files/Calculatable.png">
 * 
 * @see Calculatables
 */
public interface Calculatable {
   /**
    * Returns the result of the calculation using the specified arguments.
    * 
    * @param args the arguments to use in the calculation
    * @return the result of the calculation using the specified arguments
    */
   Numeric calculate(PTerm... args);

   /**
    * Provides a reference to a {@code KnowledgeBase}.
    * <p>
    * Meaning this object will always have access to a {@code KnowledgeBase} by the time it's {@code calculate} method
    * is invoked.
    */
   void setKnowledgeBase(KnowledgeBase kb);
}