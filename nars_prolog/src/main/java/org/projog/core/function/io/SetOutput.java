package org.projog.core.function.io;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;

import org.projog.core.FileHandles;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>set_output(X)</code> - sets the current output.
 * <p>
 * <code>set_output(X)</code> sets the current output to the stream represented by <code>X</code>.
 * </p>
 * <p>
 * <code>X</code> will be a term returned as the third argument of <code>open</code>, or the atom
 * <code>user_input</code>, which specifies that output is to go to the computer display.
 * </p>
 */
public final class SetOutput extends AbstractSingletonPredicate {
   private FileHandles fileHandles;

   @Override
   protected void init() {
      fileHandles = getFileHandles(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(PTerm arg) {
      fileHandles.setOutput(arg);
      return true;
   }
}