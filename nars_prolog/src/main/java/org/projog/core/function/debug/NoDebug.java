package org.projog.core.function.debug;

import static org.projog.core.KnowledgeBaseUtils.getSpyPoints;

import java.util.Map;

import org.projog.core.PredicateKey;
import org.projog.core.SpyPoints;
import org.projog.core.function.AbstractSingletonPredicate;

/* TEST
 %LINK prolog-debugging
 */
/**
 * <code>nodebug</code> - removes all current spy points.
 */
public final class NoDebug extends AbstractSingletonPredicate {
   private SpyPoints spyPoints;

   @Override
   protected void init() {
      spyPoints = getSpyPoints(getKnowledgeBase());
   }

   @Override
   public boolean evaluate() {
      Map<PredicateKey, SpyPoints.SpyPoint> map = spyPoints.getSpyPoints();
      for (Map.Entry<PredicateKey, SpyPoints.SpyPoint> e : map.entrySet()) {
         spyPoints.setSpyPoint(e.getKey(), false);
      }
      return true;
   }
}