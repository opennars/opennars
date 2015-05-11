package org.projog.core.function.debug;

import static org.projog.core.KnowledgeBaseUtils.getSpyPoints;

import org.projog.core.SpyPoints;
import org.projog.core.function.AbstractSingletonPredicate;

/* TEST
 %LINK prolog-debugging
 */
/**
 * <code>notrace</code> - disables exhaustive tracing.
 * <p>
 * By disabling exhaustive tracing the programmer will no longer be informed of every goal their program attempts to
 * resolve. Any tracing due to the presence of spy points <i>will</i> continue.
 * </p>
 */
public final class NoTrace extends AbstractSingletonPredicate {
   private SpyPoints spyPoints;

   @Override
   protected void init() {
      spyPoints = getSpyPoints(getKB());
   }

   @Override
   public boolean evaluate() {
      spyPoints.setTraceEnabled(false);
      return true;
   }
}