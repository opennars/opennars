package org.projog.core.function.io;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;

import org.projog.core.FileHandles;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>current_input(X)</code> - match a term to the current input stream.
 * <p>
 * <code>current_input(X)</code> succeeds if the name of the current input stream matches with <code>X</code>, else
 * fails.
 * </p>
 */
public final class CurrentInput extends AbstractSingletonPredicate {
   private FileHandles fileHandles;

   @Override
   protected void init() {
      fileHandles = getFileHandles(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(PTerm argument) {
      return argument.unify(fileHandles.getCurrentInputHandle());
   }
}