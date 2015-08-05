package org.projog.core.function.db;

import org.projog.core.PredicateKey;
import org.projog.core.term.IntegerNumber;
import org.projog.core.term.PAtom;
import org.projog.core.term.PStruct;
import org.projog.core.term.PTerm;

import java.util.Arrays;

import static org.projog.core.term.TermUtils.createAnonymousVariable;

/** Represents a record stored in a {@code RecordedDatabase}. */
class Record {
   private final PredicateKey key;
   private final IntegerNumber reference;
   private final PTerm value;

   Record(PredicateKey key, IntegerNumber reference, PTerm value) {
      this.key = key;
      this.reference = reference;
      this.value = value;
   }

   PTerm getKey() {
      String name = key.getName();
      int numArgs = key.getNumArgs();
      if (numArgs == 0) {
         return new PAtom(name);
      } else {
         PTerm[] args = new PTerm[numArgs];
         Arrays.fill(args, createAnonymousVariable());
         return PStruct.make(name, args);
      }
   }

   IntegerNumber getReference() {
      return reference;
   }

   PTerm getValue() {
      return value;
   }
}
