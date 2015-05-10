package org.projog.core.function.io;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;
import static org.projog.core.KnowledgeBaseUtils.getOperands;

import java.io.InputStreamReader;

import org.projog.core.FileHandles;
import org.projog.core.Operands;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.parser.SentenceParser;
import org.projog.core.term.PTerm;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>read(X)</code> - reads a term from the input stream.
 * <p>
 * <code>read(X)</code> reads the next term from the input stream and matches it with <code>X</code>.
 * </p>
 * <p>
 * Succeeds only once.
 * </p>
 */
public final class Read extends AbstractSingletonPredicate {
   private FileHandles fileHandles;
   private Operands operands;

   @Override
   protected void init() {
      fileHandles = getFileHandles(getKnowledgeBase());
      operands = getOperands(getKnowledgeBase());
   }

   @Override
   public boolean evaluate(PTerm argument) {
      InputStreamReader isr = new InputStreamReader(fileHandles.getCurrentInputStream());
      SentenceParser sp = SentenceParser.getInstance(isr, operands);
      PTerm t = sp.parseTerm();
      return argument.unify(t);
   }
}