package org.projog.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Associates arbitrary objects with a {@code KnowledgeBase}.
 * <p>
 * Provides a way to implement a one-to-one relationship between a {@code KnowledgeBase} and its services. i.e. A
 * {@code KnowledgeBase} can be associated with one, and only one, {@code SpyPoints} - and a {@code SpyPoints} can be
 * associated with one, and only one, {@code KnowledgeBase}.
 * </p>
 * <p>
 * <img src="doc-files/KnowledgeBaseServiceLocator.png">
 * </p>
 */
public class KnowledgeBaseServiceLocator {
   private static final Map<KnowledgeBase, KnowledgeBaseServiceLocator> CACHE = new WeakHashMap<>();

   /**
    * Returns the {@code KnowledgeBaseServiceLocator} associated with the specified {@code KnowledgeBase}.
    * <p>
    * If no {@code KnowledgeBaseServiceLocator} is already associated with the specified {@code KnowledgeBase} then a
    * new {@code KnowledgeBaseServiceLocator} will be created.
    * </p>
    */
   public static KnowledgeBaseServiceLocator getServiceLocator(KnowledgeBase kb) {
      KnowledgeBaseServiceLocator l = CACHE.get(kb);
      if (l == null) {
         l = createServiceLocator(kb);
      }
      return l;
   }

   private static KnowledgeBaseServiceLocator createServiceLocator(KnowledgeBase kb) {
      synchronized (CACHE) {
         KnowledgeBaseServiceLocator l = CACHE.get(kb);
         if (l == null) {
            l = new KnowledgeBaseServiceLocator(kb);
            CACHE.put(kb, l);
         }
         return l;
      }
   }

   private final KnowledgeBase kb;
   private final Map<Class<?>, Object> services = new HashMap<>();

   /** @see #getServiceLocator */
   private KnowledgeBaseServiceLocator(KnowledgeBase kb) {
      this.kb = kb;
   }

   /**
    * Adds the specified {@code instance} with the specified {@code referenceType} as its key.
    * 
    * @throws IllegalArgumentException If {@code instance} is not an instance of {@code ReferenceType}.
    * @throws IlegalStateException If there is already a service associated with {@code referenceType}.
    */
   public void addInstance(Class<?> referenceType, Object instance) {
      assertInstanceOf(referenceType, instance);
      synchronized (services) {
         Object r = services.get(referenceType);
         if (r == null) {
            services.put(referenceType, instance);
         } else {
            throw new IllegalStateException("Already have a service with key: " + referenceType);
         }
      }
   }

   /**
    * Returns the {@code Object} associated the specified {@code instanceType}.
    * <p>
    * If no {@code Object} is already associated with {@code instanceType} then a new instance of {@code instanceType}
    * will be created and associated with {@code instanceType} for future use.
    * </p>
    * 
    * @throws RuntimeException if an attempt to instantiate a new instance of the {@code instanceType} fails. e.g. If it
    * does not have a public constructor that accepts either no arguments or a single {@code KnowledgeBase} argument.
    */
   public <T> T getInstance(Class<?> instanceType) {
      return getInstance(instanceType, instanceType);
   }

   /**
    * Returns the {@code Object} associated the specified {@code referenceType}.
    * <p>
    * If no {@code Object} is already associated with {@code referenceType} then a new instance of {@code instanceType}
    * will be created and associated with {@code referenceType} for future use.
    * </p>
    * 
    * @param referenceType The class to use as the key to retrieve an existing service.
    * @param instanceType The class to create a new instance of if there is no existing service associated with
    * {@code referenceType}.
    * @throws RuntimeException If an attempt to instantiate a new instance of the {@code instanceType} fails. e.g. If
    * {@code instanceType} does not have a public constructor that accepts either no arguments or a single
    * {@code KnowledgeBase} argument - or if {@code referenceType} is not the same as, nor is a superclass or
    * superinterface of, {@code instanceType}.
    */
   @SuppressWarnings("unchecked")
   public <T> T getInstance(Class<?> referenceType, Class<?> instanceType) {
      Object r = services.get(referenceType);
      if (r == null) {
         r = createInstance(referenceType, instanceType);
      }
      return (T) r;
   }

   private Object createInstance(Class<?> referenceType, Class<?> instanceType) {
      synchronized (services) {
         Object r = services.get(referenceType);
         if (r == null) {
            assertAssignableFrom(referenceType, instanceType);
            r = newInstance(instanceType);
            services.put(referenceType, r);
         }
         return r;
      }
   }

   private void assertAssignableFrom(Class<?> referenceType, Class<?> instanceType) {
      if (!referenceType.isAssignableFrom(instanceType)) {
         throw new IllegalArgumentException(instanceType + " is not of type: " + referenceType);
      }
   }

   private void assertInstanceOf(Class<?> referenceType, Object instance) {
      if (!referenceType.isInstance(instance)) {
         throw new IllegalArgumentException(instance + " is not of type: " + referenceType);
      }
   }

   /**
    * Returns a new instance of the specified class.
    * <p>
    * If the class has a constructor that takes a KnowledgeBase as its single argument then an attempt is made to use
    * that to construct the new instance - else an attempt is made to construct a new instance using the no-arg
    * constructor.
    */
   private Object newInstance(Class<?> c) {
      try {
         Constructor<?> constructor = getKnowledgeBaseArgumentConstructor(c);
         if (constructor != null) {
            return constructor.newInstance(kb);
         } else {
            return c.newInstance();
         }
      } catch (Exception e) {
         throw new RuntimeException("Could not create new instance of service: " + c, e);
      }
   }

   private Constructor<?> getKnowledgeBaseArgumentConstructor(Class<?> c) throws InstantiationException, IllegalAccessException, InvocationTargetException {
      for (Constructor<?> constructor : c.getConstructors()) {
         Class<?>[] parameterTypes = constructor.getParameterTypes();
         if (parameterTypes.length == 1 && parameterTypes[0] == KnowledgeBase.class) {
            return constructor;
         }
      }
      return null;
   }
}
