package org.projog.core.function.io;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;
import static org.projog.core.term.TermUtils.getAtomName;

import org.projog.core.FileHandles;
import org.projog.core.ProjogException;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PAtom;
import org.projog.core.term.PTerm;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>tell(X)</code> - opens a file and sets it as the current output stream.
 * <p>
 * If <code>X</code> refers to a handle, rather than a filename, then the current output stream is set to the stream
 * represented by the handle.
 */
public final class Tell extends AbstractSingletonPredicate {
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
            PAtom handle = fileHandles.openOutput(fileName);
            fileHandles.setOutput(handle);
         } else {
            fileHandles.setOutput(source);
         }
         return true;
      } catch (Exception e) {
         throw new ProjogException("Unable to open output for: " + source, e);
      }
   }
}
