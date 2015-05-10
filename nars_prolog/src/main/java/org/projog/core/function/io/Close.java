package org.projog.core.function.io;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;

import org.projog.core.FileHandles;
import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>close(X)</code> - closes a stream.
 * <p>
 * <code>close(X)</code> closes the stream represented by <code>X</code>. The stream is closed and can no longer be
 * used.
 * </p>
 */
public final class Close extends AbstractSingletonPredicate {
   private FileHandles fileHandles;

   @Override
   protected void init() {
      fileHandles = getFileHandles(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(PTerm argument) {
      try {
         fileHandles.close(argument);
         return true;
      } catch (Exception e) {
         throw new ProjogException("Unable to close stream for: " + argument, e);
      }
   }
}