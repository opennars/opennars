package org.projog.core.function.debug;

import org.projog.core.FileHandles;
import org.projog.core.PredicateKey;
import org.projog.core.SpyPoints;
import org.projog.core.function.AbstractSingletonPredicate;

import java.util.Map;

import static org.projog.core.KnowledgeBaseUtils.getFileHandles;
import static org.projog.core.KnowledgeBaseUtils.getSpyPoints;

/* TEST
 %LINK prolog-debugging
 */
/**
 * <code>debugging</code> - lists current spy points.
 * <p>
 * The list of spy points currently set is printed as a side effect of <code>debugging</code> being satisfied.
 * </p>
 */
public final class Debugging extends AbstractSingletonPredicate {
   private SpyPoints spyPoints;
   private FileHandles fileHandles;

   @Override
   protected void init() {
      spyPoints = getSpyPoints(getKB());
      fileHandles = getFileHandles(getKB());
   }

   @Override
   public boolean evaluate() {
      Map<PredicateKey, SpyPoints.SpyPoint> map = spyPoints.getSpyPoints();
      for (Map.Entry<PredicateKey, SpyPoints.SpyPoint> e : map.entrySet()) {
         if (e.getValue().isEnabled()) {
            fileHandles.getCurrentOutputStream().println(e.getKey());
         }
      }
      return true;
   }
}