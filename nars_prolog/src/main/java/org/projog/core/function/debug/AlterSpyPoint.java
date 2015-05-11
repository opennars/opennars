package org.projog.core.function.debug;

import static org.projog.core.KnowledgeBaseUtils.getPredicateKeysByName;
import static org.projog.core.KnowledgeBaseUtils.getSpyPoints;

import java.util.List;

import org.projog.core.PredicateKey;
import org.projog.core.ProjogException;
import org.projog.core.SpyPoints;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PTerm;

/* TEST
 %LINK prolog-debugging
 */
/**
 * <code>spy X</code> / <code>nospy X</code> - add or remove a spy point for a predicate.
 * <p>
 * <code>spy X</code> - add a spy point for a predicate. By adding a spy point for the predicate name instantiated to
 * <code>X</code> the programmer will be informed how it is used in the resolution of a goal.
 * </p>
 * <p>
 * <code>nospy X</code> - removes a spy point for a predicate. By removing a spy point for the predicate name
 * instantiated to <code>X</code> the programmer will no longer be informed how it is used in the resolution of a goal.
 * </p>
 */
public final class AlterSpyPoint extends AbstractSingletonPredicate {
   public static AlterSpyPoint spy() {
      return new AlterSpyPoint(true);
   }

   public static AlterSpyPoint noSpy() {
      return new AlterSpyPoint(false);
   }

   private final boolean valueToSetSpyPointTo;
   private SpyPoints spyPoints;

   /**
    * The {@code valueToSetSpyPointTo} parameter specifies whether spy points matched by the {@link #evaluate(PTerm)}
    * method should be enabled or disabled.
    * 
    * @param valueToSetSpyPointTo {@code true} to enable spy points, {@code false} to disable spy points
    */
   private AlterSpyPoint(boolean valueToSetSpyPointTo) {
      this.valueToSetSpyPointTo = valueToSetSpyPointTo;
   }

   @Override
   protected void init() {
      spyPoints = getSpyPoints(getKB());
   }

   @Override
   public boolean evaluate(PTerm t) {
      switch (t.type()) {
         case ATOM:
            List<PredicateKey> keys = getPredicateKeysByName(getKB(), t.getName());
            setSpyPoints(keys);
            break;
         case STRUCTURE:
            PredicateKey key = PredicateKey.createFromNameAndArity(t);
            setSpyPoint(key);
            break;
         default:
            throw new ProjogException("Expected an atom or a structure but got a " + t.type() + " with value: " + t);
      }
      return true;
   }

   private void setSpyPoints(List<PredicateKey> keys) {
      for (PredicateKey key : keys) {
         setSpyPoint(key);
      }
   }

   private void setSpyPoint(PredicateKey key) {
      spyPoints.setSpyPoint(key, valueToSetSpyPointTo);
   }
}