package org.projog.core;

import static org.projog.core.CoreUtils.instantiate;

import java.util.HashMap;
import java.util.Map;

import org.projog.core.term.Numeric;
import org.projog.core.term.PTerm;
import org.projog.core.term.TermType;
import org.projog.core.term.TermUtils;

/**
 * Maintains a collection of {@link Calculatable} instances.
 * <p>
 * This class provides a mechanism for "plugging in" or "injecting" implementations of {@link Calculatable} at runtime.
 * This mechanism provides an easy way to configure and extend the arithmetic operations supported by Projog.
 * <p>
 * Each {@link org.projog.core.KnowledgeBase} has a single unique {@code CalculatableFactory} instance.
 */
public final class Calculatables {
   private final KnowledgeBase kb;
   private final Object lock = new Object();
   private final Map<PredicateKey, String> calculatableClassNames = new HashMap<>();
   private final Map<PredicateKey, Calculatable> calculatableInstances = new HashMap<>();

   public Calculatables(KnowledgeBase kb) {
      this.kb = kb;
   }

   /**
    * Associates a {@link Calculatable} with this {@code KnowledgeBase}.
    * 
    * @param calculatable The instance of {@code Calculatable} to be associated with {@code key}.
    */
   public void addCalculatable(PredicateKey key, Calculatable calculatable) {
      synchronized (lock) {
         if (calculatableClassNames.containsKey(key)) {
            throw new ProjogException("Already defined calculatable: " + key);
         } else {
            calculatableClassNames.put(key, calculatable.getClass().getName());
            calculatableInstances.put(key, calculatable);
         }
      }
   }

   /**
    * Associates a {@link Calculatable} with this {@code KnowledgeBase}.
    * 
    * @param calculatableClassName The class name of the {@code Calculatable} to be associated with {@code key}.
    */
   public void addCalculatable(PredicateKey key, String calculatableClassName) {
      synchronized (lock) {
         if (calculatableClassNames.containsKey(key)) {
            throw new ProjogException("Already defined calculatable: " + key);
         } else {
            calculatableClassNames.put(key, calculatableClassName);
         }
      }
   }

   /**
    * Returns the result of evaluating the specified arithmetic expression.
    * 
    * @param t a {@code Term} that can be evaluated as an arithmetic expression (e.g. a {@code Structure} of the form
    * {@code +(1,2)} or a {@code Numeric})
    * @return the result of evaluating the specified arithmetic expression
    * @throws ProjogException if the specified term does not represent an arithmetic expression
    */
   public Numeric getNumeric(PTerm t) {
      TermType type = t.type();
      switch (type) {
         case FRACTION:
         case INTEGER:
            return TermUtils.castToNumeric(t);
         case STRUCTURE:
            return calculate(t, t.getArgs());
         case ATOM:
            return calculate(t, TermUtils.EMPTY_ARRAY);
         default:
            throw new ProjogException("Cannot get Numeric for term: " + t + " of type: " + type);
      }
   }

   private Numeric calculate(PTerm term, PTerm[] args) {
      return getCalculatable(term).calculate(args);
   }

   private Calculatable getCalculatable(PTerm term) {
      PredicateKey key = PredicateKey.createForTerm(term);
      Calculatable e = calculatableInstances.get(key);
      if (e != null) {
         return e;
      } else if (calculatableClassNames.containsKey(key)) {
         return instantiateCalculatable(key);
      } else {
         throw new ProjogException("Cannot find calculatable: " + key);
      }
   }

   private Calculatable instantiateCalculatable(PredicateKey key) {
      synchronized (lock) {
         Calculatable calculatable = calculatableInstances.get(key);
         if (calculatable == null) {
            calculatable = instantiateCalculatable(calculatableClassNames.get(key));
            calculatable.setKnowledgeBase(kb);
            calculatableInstances.put(key, calculatable);
         }
         return calculatable;
      }
   }

   private Calculatable instantiateCalculatable(String className) {
      try {
         return instantiate(className);
      } catch (Exception e) {
         throw new RuntimeException("Could not create new Calculatable using: " + className, e);
      }
   }
}
