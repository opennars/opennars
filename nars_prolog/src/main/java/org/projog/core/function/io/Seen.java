package org.projog.core.function.io;

import org.projog.core.FileHandles;
import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

import static org.projog.core.FileHandles.USER_INPUT_HANDLE;
import static org.projog.core.KnowledgeBaseUtils.getFileHandles;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>seen</code> - closes the current input stream.
 * <p>
 * The new input stream becomes <code>user_input</code>.
 */
public final class Seen extends AbstractSingletonPredicate {
   private FileHandles fileHandles;

   @Override
   protected void init() {
      fileHandles = getFileHandles(getKB());
   }

   @Override
   public boolean evaluate() {
      PTerm handle = fileHandles.getCurrentInputHandle();
      close(handle);
      fileHandles.setInput(USER_INPUT_HANDLE);
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