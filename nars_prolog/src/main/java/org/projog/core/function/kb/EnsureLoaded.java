package org.projog.core.function.kb;

import static org.projog.core.KnowledgeBaseUtils.getProjogEventsObservable;
import static org.projog.core.term.TermUtils.getAtomName;

import java.util.HashSet;
import java.util.Set;

import org.projog.core.ProjogSourceReader;
import org.projog.core.event.ProjogEvent;
import org.projog.core.event.ProjogEventType;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %LINK prolog-io
 */
/**
 * <code>ensure_loaded(X)</code> - reads clauses and goals from a file.
 * <p>
 * <code>ensure_loaded(X)</code> reads clauses and goals from a file. <code>X</code> must be instantiated to the name of
 * a text file containing Prolog clauses and goals which will be added to the knowledge base. Will do nothing when
 * <code>X</code> represents a file that has already been loaded using <code>ensure_loaded(X)</code>.
 * </p>
 */
public final class EnsureLoaded extends AbstractSingletonPredicate {
   private final Object lock = new Object();

   private final Set<String> loadedResources = new HashSet<>();

   @Override
   public boolean evaluate(PTerm arg) {
      String resourceName = getResourceName(arg);
      synchronized (lock) {
         if (loadedResources.contains(resourceName)) {
            ProjogEvent event = new ProjogEvent(ProjogEventType.INFO, "Already loaded: " + resourceName, this);
            getProjogEventsObservable(getKB()).notifyObservers(event);
         } else {
            ProjogSourceReader.parseResource(getKB(), resourceName);
            loadedResources.add(resourceName);
         }
      }
      return true;
   }

   private String getResourceName(PTerm arg) {
      String resourceName = getAtomName(arg);
      if (resourceName.indexOf('.') == -1) {
         return resourceName + ".pl";
      } else {
         return resourceName;
      }
   }
}