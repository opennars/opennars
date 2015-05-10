package org.projog.core.function.io;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;
import static org.projog.core.term.TermUtils.getAtomName;

import org.projog.core.FileHandles;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>put_char(X)</code> - writes a character.
 * <p>
 * <code>put_char(X)</code> - writes character <code>X</code> to the output stream.
 * </p>
 * <p>
 * Writes to the current output stream. Succeeds only once and the operation is not undone on backtracking.
 * </p>
 */
public final class PutChar extends AbstractSingletonPredicate {
   private FileHandles fileHandles;

   @Override
   protected void init() {
      fileHandles = getFileHandles(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(PTerm argument) {
      String textToOutput = getAtomName(argument);
      fileHandles.getCurrentOutputStream().print(textToOutput);
      return true;
   }
}