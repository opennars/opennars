package org.projog.core.function.io;

import org.projog.core.FileHandles;
import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PAtom;
import org.projog.core.term.PTerm;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;
import static org.projog.core.term.TermUtils.getAtomName;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>see(X)</code> - opens a file and sets it as the current input stream.
 * <p>
 * If <code>X</code> refers to a handle, rather than a filename, then the current input stream is set to the stream
 * represented by the handle.
 */
public final class See extends AbstractSingletonPredicate {
   private FileHandles fileHandles;

   @Override
   protected void init() {
      fileHandles = getFileHandles(getKB());
   }

   @Override
   public boolean evaluate(PTerm source) {
      String fileName = getAtomName(source);
      try {
         if (!fileHandles.isHandle(fileName)) {
            PAtom handle = fileHandles.openInput(fileName);
            fileHandles.setInput(handle);
         } else {
            fileHandles.setInput(source);
         }
         return true;
      } catch (Exception e) {
         throw new ProjogException("Unable to open input for: " + source, e);
      }
   }
}