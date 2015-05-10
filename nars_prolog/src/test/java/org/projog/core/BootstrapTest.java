package org.projog.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.ADD_CALCULATABLE_KEY;
import static org.projog.TestUtils.ADD_PREDICATE_KEY;
import static org.projog.TestUtils.BOOTSTRAP_FILE;
import static org.projog.TestUtils.parseTermsFromFile;
import static org.projog.core.KnowledgeBaseUtils.QUESTION_PREDICATE_NAME;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.function.AbstractRetryablePredicate;
import org.projog.core.term.PTerm;

/**
 * Tests contents of {@code etc/projog-bootstrap.pl}.
 * <p>
 * {@code etc/projog-bootstrap.pl} is used to configure the build-in predicates and arithmetic functions.
 */
public class BootstrapTest {
   private final KnowledgeBase kb = TestUtils.createKnowledgeBase();

   @Test
   public void testBuiltInPredicates() throws Exception {
      List<PTerm> terms = getQueriesByKey(ADD_PREDICATE_KEY);
      assertFalse(terms.isEmpty());
      for (PTerm t : terms) {
         assertBuiltInPredicate(t.arg(0));
      }
   }

   @Test
   public void testCalculatables() throws Exception {
      List<PTerm> terms = getQueriesByKey(ADD_CALCULATABLE_KEY);
      assertFalse(terms.isEmpty());
      for (PTerm t : terms) {
         assertCalculatable(t.arg(1));
      }
   }

   private List<PTerm> getQueriesByKey(PredicateKey key) {
      List<PTerm> result = new ArrayList<PTerm>();
      PTerm[] terms = parseTermsFromFile(BOOTSTRAP_FILE);
      for (PTerm next : terms) {
         if (QUESTION_PREDICATE_NAME.equals(next.getName())) {
            PTerm t = next.arg(0);
            if (key.equals(PredicateKey.createForTerm(t))) {
               result.add(t);
            }
         }
      }
      return result;
   }

   @SuppressWarnings("rawtypes")
   private void assertBuiltInPredicate(PTerm nameAndArity) throws Exception {
      PredicateKey key = PredicateKey.createFromNameAndArity(nameAndArity);
      PredicateFactory ef = kb.getPredicateFactory(key);
      assertFinal(ef);
      Class[] methodParameters = getMethodParameters(key);
      if (ef instanceof AbstractRetryablePredicate) {
         assertClassImplementsOptimisedGetPredicateMethod(ef, methodParameters);
      }
      if (ef instanceof Predicate) {
         assertClassImplementsOptimisedEvaluateMethod(ef, methodParameters);
      }
   }

   private void assertCalculatable(PTerm className) throws Exception {
      Class<?> c = Class.forName(className.getName());
      Object o = c.newInstance();
      assertTrue(o instanceof Calculatable);
      assertFinal(o);
   }

   private void assertFinal(Object o) {
      Class<? extends Object> c = o.getClass();
      assertTrue("Not final: " + c, Modifier.isFinal(c.getModifiers()));
   }

   @SuppressWarnings("rawtypes")
   private Class<?>[] getMethodParameters(PredicateKey key) {
      int numberOfArguments = key.getNumArgs();
      Class<?>[] args = new Class[numberOfArguments];
      for (int i = 0; i < numberOfArguments; i++) {
         args[i] = PTerm.class;
      }
      return args;
   }

   @SuppressWarnings("rawtypes")
   private void assertClassImplementsOptimisedGetPredicateMethod(PredicateFactory ef, Class[] methodParameters) {
      try {
         // Test that the getPredicate method has a return type of the actual sub-class rather than Predicate
         Method m = ef.getClass().getDeclaredMethod("getPredicate", methodParameters);
         assertSame(ef.getClass() + "'s getPredicate(" + Arrays.toString(methodParameters) + ") method returns " + m.getReturnType(), ef.getClass(), m.getReturnType());
      } catch (NoSuchMethodException e) {
         fail("Testing getPredicate method of " + ef.getClass() + " with: " + methodParameters.length + " arguments caused: " + e.toString());
      }
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private void assertClassImplementsOptimisedEvaluateMethod(PredicateFactory ef, Class[] methodParameters) {
      Class c = ef.getClass();
      boolean success = false;
      while (success == false && c != null) {
         try {
            Method m = c.getDeclaredMethod("evaluate", methodParameters);
            assertSame(boolean.class, m.getReturnType());
            success = true;
         } catch (NoSuchMethodException e) {
            // if we can't find a matching method in the class then try it's superclass
            c = c.getSuperclass();
         }
      }
      if (success == false) {
         fail(ef.getClass() + " does not implement an evaluate method with " + methodParameters.length + " parameters");
      }
   }
}