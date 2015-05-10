package org.projog.core.function.io;

import static org.projog.core.FileHandles.USER_OUTPUT_HANDLE;
import static org.projog.core.KnowledgeBaseUtils.getFileHandles;

import org.projog.core.FileHandles;
import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>told</code> - closes the current output stream.
 * <p>
 * The new input stream becomes <code>user_output</code>.
 */
public final class Told extends AbstractSingletonPredicate {
   private FileHandles fileHandles;

   @Override
   protected void init() {
      fileHandles = getFileHandles(getKnowledgeBase());
   }

   @Override
   public boolean evaluate() {
      PTerm handle = fileHandles.getCurrentOutputHandle();
      close(handle);
      fileHandles.setOutput(USER_OUTPUT_HANDLE);
      return true;
   }

   private void close(PTerm handle) {
      try {
         fileHandles.close(handle);
      } catch (Exception e) {
         throw new ProjogException("Unable to close stream for: " + handle, e);
      }
   }
}