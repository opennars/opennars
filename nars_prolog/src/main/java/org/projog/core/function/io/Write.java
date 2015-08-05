package org.projog.core.function.io;

import org.projog.core.FileHandles;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermFormatter;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;
import static org.projog.core.KnowledgeBaseUtils.getTermFormatter;

/* TEST
 %QUERY write( 1+1 )
 %OUTPUT 1 + 1
 %ANSWER/
 %QUERY write( '+'(1,1) )
 %OUTPUT 1 + 1
 %ANSWER/
 */
/**
 * <code>write(X)</code> - writes a term to the output stream.
 * <p>
 * Writes the term <code>X</code> to the current output stream. <code>write</code> takes account of current operator
 * declarations - thus an infix operator will be printed out between it's arguments. <code>write</code> represents lists
 * as a comma separated sequence of elements enclosed in square brackets.
 * </p>
 * <p>
 * Succeeds only once.
 * </p>
 * 
 * @see #toString(PTerm)
 */
public final class Write extends AbstractSingletonPredicate {
   private TermFormatter termFormatter;
   private FileHandles fileHandles;

   @Override
   protected void init() {
      termFormatter = getTermFormatter(getKB());
      fileHandles = getFileHandles(getKB());
   }

   @Override
   public boolean evaluate(PTerm arg) {
      print(toString(arg));
      return true;
   }

   private String toString(PTerm t) {
      return termFormatter.toString(t);
   }

   private void print(String s) {
      fileHandles.getCurrentOutputStream().print(s);
   }
}