package org.projog.example;

import static org.projog.core.term.TermUtils.castToNumeric;

import org.projog.core.Calculatable;
import org.projog.core.KB;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.Numeric;
import org.projog.core.term.PTerm;

public class CalculatableExample implements Calculatable {
   @Override
   public void setKnowledgeBase(KB kb) {
   }

   @Override
   public Numeric calculate(PTerm... args) {
      Numeric input = castToNumeric(args[0]);
      long rounded = Math.round(input.getDouble());
      return new IntegerNumber(rounded);
   }
}