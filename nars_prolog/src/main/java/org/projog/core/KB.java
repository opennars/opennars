package org.projog.core;

import org.projog.core.event.ProjogEvent;
import org.projog.core.event.ProjogEventType;
import org.projog.core.function.kb.AddPredicateFactory;
import org.projog.core.term.PTerm;
import org.projog.core.udp.DynamicUserDefinedPredicateFactory;
import org.projog.core.udp.UserDefinedPredicateFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.projog.core.CoreUtils.instantiate;
import static org.projog.core.KnowledgeBaseUtils.getProjogEventsObservable;

/**
 * Acts as a repository of rules and facts.
 * <p>
 * The central object that connects the various components of an instance of the "core" inference engine.
 * <p>
 * <img src="doc-files/KnowledgeBase.png">
 */
public final class KB {
   /**
    * Represents the {@code pj_add_predicate/2} predicate hard-coded in every {@code KnowledgeBase}.
    * <p>
    * The {@code pj_add_predicate/2} predicate allows other implementations of {@link PredicateFactory} to be
    * "plugged-in" to a {@code KnowledgeBase} at runtime using Prolog syntax.
    * 
    * @see AddPredicateFactory#evaluate(PTerm[])
    */
   private static final PredicateKey ADD_PREDICATE_KEY = new PredicateKey("pj_add_predicate", 2);

   /**
    * Used to coordinate access to {@link javaPredicateClassNames}, {@link #javaPredicateInstances} and
    * {@link #userDefinedPredicates}
    */
   private final Object predicatesLock = new Object();
   /**
    * The class names of "built-in" Java predicates (i.e. not defined using Prolog syntax) associated with this
    * {@code KnowledgeBase}.
    */
   private final Map<PredicateKey, String> javaPredicateClassNames = new HashMap<>();
   /**
    * The instances of "built-in" Java predicates (i.e. not defined using Prolog syntax) associated with this
    * {@code KnowledgeBase}.
    */
   private final Map<PredicateKey, PredicateFactory> javaPredicateInstances = new HashMap<>();
   /**
    * The user-defined predicates (i.e. defined using Prolog syntax) associated with this {@code KnowledgeBase}.
    * <p>
    * Uses TreeMap to enforce predictable ordering for when iterated (e.g. by <code>listing(X)</code>).
    */
   private final Map<PredicateKey, UserDefinedPredicateFactory> userDefinedPredicates = new TreeMap<>();

   /**
    * @see KnowledgeBaseUtils#createKnowledgeBase()
    * @see KnowledgeBaseUtils#createKnowledgeBase(ProjogProperties)
    */
   KB() {
      addFactory(ADD_PREDICATE_KEY, AddPredicateFactory.class.getName());
   }

   /**
    * Returns details of all the user define predicates of this object.
    */
   public Map<PredicateKey, UserDefinedPredicateFactory> getDefined() {
      return Collections.unmodifiableMap(userDefinedPredicates);
   }

   /**
    * Returns the {@code UserDefinedPredicateFactory} for the specified {@code PredicateKey}.
    * <p>
    * If this object does not already have a {@code UserDefinedPredicateFactory} for the specified {@code PredicateKey}
    * then it will create it.
    * 
    * @throws ProjogException if the specified {@code PredicateKey} represents an existing "plugin" predicate
    */
   //createOrReturnUserDefinedPredicate
   public UserDefinedPredicateFactory getDefined(PredicateKey key) {
      return getDefined(key, true);
   }

   public UserDefinedPredicateFactory getDefined(PredicateKey key, boolean createIfNew) {
      UserDefinedPredicateFactory userDefinedPredicate;
      synchronized (predicatesLock) {
         userDefinedPredicate = userDefinedPredicates.get(key);

         if (userDefinedPredicate == null) {
            if (!createIfNew) return null;

            // assume dynamic
            userDefinedPredicate = new DynamicUserDefinedPredicateFactory(this, key);
            addDefined(userDefinedPredicate);
         }
      }
      return userDefinedPredicate;
   }

   /**
    * Adds a user defined predicate to this object.
    * <p>
    * Any existing {@code UserDefinedPredicateFactory} with the same {@code PredicateKey} will be replaced.
    * 
    * @throws ProjogException if the {@code PredicateKey} of the specified {@code UserDefinedPredicateFactory}
    * represents an existing "plugin" predicate
    */
   public void addDefined(UserDefinedPredicateFactory userDefinedPredicate) {
      PredicateKey key = userDefinedPredicate.getPredicateKey();
      synchronized (predicatesLock) {
         if (hasJavaPredicate(key)) {
            throw new ProjogException("Cannot replace already defined plugin predicate: " + key);
         }

         userDefinedPredicates.put(key, userDefinedPredicate);
      }
   }

   /**
    * Returns the {@code PredicateFactory} associated with the specified {@code Term}.
    * <p>
    * If this object has no {@code PredicateFactory} associated with the {@code PredicateKey} of the specified
    * {@code Term} then {@link UnknownPredicate#UNKNOWN_PREDICATE} is returned.
    */
   public PredicateFactory getPredicateFactory(PTerm term) {
      PredicateKey key = PredicateKey.createForTerm(term);
      return getPredicateFactory(key);
   }

   /**
    * Returns the {@code PredicateFactory} associated with the specified {@code PredicateKey}.
    * <p>
    * If this object has no {@code PredicateFactory} associated with the specified {@code PredicateKey} then
    * {@link UnknownPredicate#UNKNOWN_PREDICATE} is returned.
    */
   public PredicateFactory getPredicateFactory(PredicateKey key) {
      PredicateFactory predicateFactory = getExistingPredicateFactory(key);
      if (predicateFactory != null) {
         return predicateFactory;
      } else if (javaPredicateClassNames.containsKey(key)) {
         return newFactory(key);
      } else {
         return unknownPredicate(key);
      }
   }

   private PredicateFactory getExistingPredicateFactory(PredicateKey key) {
      PredicateFactory predicateFactory = javaPredicateInstances.get(key);
      if (predicateFactory != null) {
         return predicateFactory;
      } else {
         return userDefinedPredicates.get(key);
      }
   }

   private PredicateFactory newFactory(PredicateKey key) {
      synchronized (predicatesLock) {
         PredicateFactory predicateFactory = getExistingPredicateFactory(key);
         if (predicateFactory != null) {
            return predicateFactory;
         } else {
            predicateFactory = newFactory(javaPredicateClassNames.get(key));
            javaPredicateInstances.put(key, predicateFactory);
            return predicateFactory;
         }
      }
   }

   private PredicateFactory newFactory(String className) {
      try {
         PredicateFactory predicateFactory = instantiate(className);
         predicateFactory.setKB(this);
         return predicateFactory;
      } catch (Exception e) {
         throw new RuntimeException("Could not create new PredicateFactory using: " + className, e);
      }
   }

   private PredicateFactory unknownPredicate(PredicateKey key) {
      ProjogEvent event = new ProjogEvent(ProjogEventType.WARN, "Not defined: " + key, this);
      getProjogEventsObservable(this).notifyObservers(event);
      return UnknownPredicate.UNKNOWN_PREDICATE;
   }

   /**
    * Associates a {@link PredicateFactory} with this {@code KnowledgeBase}.
    * <p>
    * This method provides a mechanism for "plugging in" or "injecting" implementations of {@link PredicateFactory} at
    * runtime. This mechanism provides an easy way to configure and extend the functionality of Projog - including
    * adding functionality not possible to define in pure Prolog syntax.
    * </p>
    * 
    * @param key The name and arity to associate the {@link PredicateFactory} with.
    * @param predicateFactoryClassName The name of a class that implements {@link PredicateFactory}.
    */
   public void addFactory(PredicateKey key, String predicateFactoryClassName) {
      synchronized (predicatesLock) {
         if (hasPredicate(key)) {
            throw new ProjogException("Already defined: " + key);
         } else {
            javaPredicateClassNames.put(key, predicateFactoryClassName);
         }
      }
   }

   private boolean hasPredicate(PredicateKey key) {
      return hasJavaPredicate(key) || userDefinedPredicates.containsKey(key);
   }

   private boolean hasJavaPredicate(PredicateKey key) {
      return javaPredicateClassNames.containsKey(key);
   }
}