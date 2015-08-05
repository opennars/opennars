package org.projog.core.function.io;

import org.projog.core.FileHandles;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>set_input(X)</code> - sets the current input.
 * <p>
 * <code>set_input(X)</code> sets the current input to the stream represented by <code>X</code>.
 * </p>
 * <p>
 * <code>X</code> will be a term returned as the third argument of <code>open</code>, or the atom
 * <code>user_input</code>, which specifies that input to come from the keyboard.
 * </p>
 */
public final class SetInput extends AbstractSingletonPredicate {
   private FileHandles fileHandles;

   @Override
   protected void init() {
      fileHandles = getFileHandles(getKB());
   }

   @Override
   public boolean evaluate(PTerm arg) {
      fileHandles.setInput(arg);
      return true;
   }
}