package org.projog.core.function.debug;

import static org.projog.core.KnowledgeBaseUtils.getSpyPoints;

import org.projog.core.SpyPoints;
import org.projog.core.function.AbstractSingletonPredicate;

/* TEST
 %LINK prolog-debugging
 */
/**
 * <code>trace</code> - enables exhaustive tracing.
 * <p>
 * By enabling exhaustive tracing the programmer will be informed of every goal their program attempts to resolve.
 * </p>
 */
public final class Trace extends AbstractSingletonPredicate {
   private SpyPoints spyPoints;

   @Override
   protected void init() {
      spyPoints = getSpyPoints(getKB());
   }

   @Override
   public boolean evaluate() {
      spyPoints.setTraceEnabled(true);
      return true;
   }
}