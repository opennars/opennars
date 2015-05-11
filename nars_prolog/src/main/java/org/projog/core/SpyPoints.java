package org.projog.core;

import static org.projog.core.KnowledgeBaseUtils.getProjogEventsObservable;
import static org.projog.core.KnowledgeBaseUtils.getTermFormatter;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.projog.core.event.ProjogEvent;
import org.projog.core.event.ProjogEventType;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermFormatter;

/**
 * Collection of spy points.
 * <p>
 * Spy points are useful in the debugging of Prolog programs. When a spy point is set on a predicate a
 * {@link ProjogEventType} is generated every time the predicate is executed, fails or succeeds.
 * </p>
 * <p>
 * Each {@link KB} has a single unique {@code SpyPoints} instance.
 * </p>
 * 
 * @see KnowledgeBaseUtils#getSpyPoints(KB)
 */
public final class SpyPoints {
   private final Object lock = new Object();
   private final Map<PredicateKey, SpyPoint> spyPoints = new TreeMap<>();
   private final KB kb;
   private boolean traceEnabled;

   public SpyPoints(KB kb) {
      this.kb = kb;
   }

   public void setTraceEnabled(boolean traceEnabled) {
      synchronized (lock) {
         this.traceEnabled = traceEnabled;
         for (SpyPoints.SpyPoint sp : spyPoints.values()) {
            if (traceEnabled) {
               sp.enabled = true;
            } else {
               sp.enabled = sp.set;
            }
         }
      }
   }

   public void setSpyPoint(PredicateKey key, boolean set) {
      synchronized (lock) {
         SpyPoint sp = getSpyPoint(key);
         sp.set = set;
         sp.enabled = traceEnabled || sp.set;
      }
   }

   public SpyPoint getSpyPoint(PredicateKey key) {
      SpyPoint spyPoint = spyPoints.get(key);
      if (spyPoint == null) {
         spyPoint = createNewSpyPoint(key);
      }
      return spyPoint;
   }

   private SpyPoint createNewSpyPoint(PredicateKey key) {
      synchronized (lock) {
         SpyPoint spyPoint = spyPoints.get(key);
         if (spyPoint == null) {
            spyPoint = new SpyPoint(key);
            spyPoint.enabled = traceEnabled;
            spyPoints.put(key, spyPoint);
         }
         return spyPoint;
      }
   }

   public Map<PredicateKey, SpyPoint> getSpyPoints() {
      return Collections.unmodifiableMap(spyPoints);
   }

   public class SpyPoint {
      private final PredicateKey key;
      private boolean enabled;
      private boolean set;

      private SpyPoint(PredicateKey key) {
         this.key = key;
      }

      public boolean isSet() {
         return set;
      }

      public boolean isEnabled() {
         return enabled;
      }

      /** Generates an event of type {@link ProjogEventType#CALL} */
      public void logCall(Object source, PTerm[] args) {
         log(ProjogEventType.CALL, source, args);
      }

      /** Generates an event of type {@link ProjogEventType#REDO} */
      public void logRedo(Object source, PTerm[] args) {
         log(ProjogEventType.REDO, source, args);
      }

      /** Generates an event of type {@link ProjogEventType#EXIT} */
      public void logExit(Object source, PTerm[] args) {
         log(ProjogEventType.EXIT, source, args);
      }

      /** Generates an event of type {@link ProjogEventType#FAIL} */
      public void logFail(Object source, PTerm[] args) {
         log(ProjogEventType.FAIL, source, args);
      }

      private void log(ProjogEventType type, Object source, PTerm[] args) {
         if (isEnabled() == false) {
            return;
         }

         TermFormatter tf = getTermFormatter(kb);
         StringBuilder sb = new StringBuilder();
         sb.append(key.getName());
         if (args != null) {
            sb.append("( ");
            for (int i = 0; i < args.length; i++) {
               if (i != 0) {
                  sb.append(", ");
               }
               sb.append(tf.toString(args[i]));
            }
            sb.append(" )");
         }
         ProjogEvent event = new ProjogEvent(type, sb.toString(), source);
         getProjogEventsObservable(kb).notifyObservers(event);
      }
   }
}