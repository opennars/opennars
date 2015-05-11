package org.projog.core.function.io;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;

import org.projog.core.FileHandles;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>current_output(X)</code> - match a term to the current output stream.
 * <p>
 * <code>current_output(X)</code> succeeds if the name of the current output stream matches with <code>X</code>, else
 * fails.
 * </p>
 */
public final class CurrentOutput extends AbstractSingletonPredicate {
   private FileHandles fileHandles;

   @Override
   protected void init() {
      fileHandles = getFileHandles(getKB());
   }

   @Override
   public boolean evaluate(PTerm argument) {
      return argument.unify(fileHandles.getCurrentOutputHandle());
   }
}