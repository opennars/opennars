package org.projog.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.projog.TestUtils.createKnowledgeBase;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

public class KBServiceLocatorTest {
   /** Tests one-to-one relationship between KnowledgeBase and KnowledgeBaseServiceLocator instances */
   @Test
   public void testCreation() {
      KB kb1 = createKnowledgeBase();
      KnowledgeBaseServiceLocator sl1 = KnowledgeBaseServiceLocator.getServiceLocator(kb1);
      assertNotNull(sl1);
      assertSame(sl1, KnowledgeBaseServiceLocator.getServiceLocator(kb1));

      KB kb2 = createKnowledgeBase();
      KnowledgeBaseServiceLocator sl2 = KnowledgeBaseServiceLocator.getServiceLocator(kb2);
      assertNotNull(sl2);
      assertNotSame(sl1, sl2);
   }

   @Test
   public void testGetInstance_OneArgument() {
      KnowledgeBaseServiceLocator l = createKnowledgeBaseServiceLocator();
      Object o = l.getInstance(Object.class);
      assertSame(o, l.getInstance(Object.class));

      StringBuilder sb = l.getInstance(StringBuilder.class);
      assertSame(sb, l.getInstance(StringBuilder.class));
      assertNotSame(sb, o);
      assertNotSame(sb, l.getInstance(StringBuffer.class));
   }

   @Test
   public void testGetInstance_TwoArguments() {
      KnowledgeBaseServiceLocator l = createKnowledgeBaseServiceLocator();

      StringBuilder o = l.getInstance(Object.class, StringBuilder.class);
      assertSame(o, l.getInstance(Object.class, StringBuilder.class));
      assertSame(o, l.getInstance(Object.class, StringBuffer.class));
      assertSame(o, l.getInstance(Object.class));

      StringBuilder c = l.getInstance(CharSequence.class, StringBuilder.class);
      assertSame(c, l.getInstance(CharSequence.class, StringBuilder.class));
      assertSame(c, l.getInstance(CharSequence.class, StringBuffer.class));
      assertSame(c, l.getInstance(CharSequence.class));

      assertNotSame(o, c);
      assertNotSame(o, l.getInstance(StringBuilder.class));
      assertNotSame(c, l.getInstance(StringBuilder.class));
   }

   @Test
   public void testGetInstance_Interface() {
      try {
         createKnowledgeBaseServiceLocator().getInstance(Serializable.class);
         fail();
      } catch (RuntimeException e) {
         assertEquals("Could not create new instance of service: interface java.io.Serializable", e.getMessage());
      }
   }

   @Test
   public void testGetInstance_NoValidConstructor() {
      try {
         createKnowledgeBaseServiceLocator().getInstance(Integer.class);
         fail();
      } catch (RuntimeException e) {
         assertEquals("Could not create new instance of service: class java.lang.Integer", e.getMessage());
      }
   }

   @Test
   public void testGetInstance_InstanceDoesNotExtendReference() {
      try {
         createKnowledgeBaseServiceLocator().getInstance(StringBuffer.class, StringBuilder.class);
         fail();
      } catch (IllegalArgumentException e) {
         assertEquals("class java.lang.StringBuilder is not of type: class java.lang.StringBuffer", e.getMessage());
      }
   }

   public void testGetInstance_InstanceDoesNotImplementReference() {
      try {
         createKnowledgeBaseServiceLocator().getInstance(Map.class, StringBuilder.class);
         fail();
      } catch (IllegalArgumentException e) {
         assertEquals("class java.lang.StringBuilder is not of type: interface java.util.Map", e.getMessage());
      }
   }

   @Test
   public void testAddInstance() {
      KnowledgeBaseServiceLocator l = createKnowledgeBaseServiceLocator();
      String s = "hello";
      l.addInstance(String.class, s);
      assertSame(s, l.getInstance(String.class));
   }

   @Test
   public void testAddInstance_IllegalStateException() {
      KnowledgeBaseServiceLocator l = createKnowledgeBaseServiceLocator();
      l.addInstance(String.class, "hello");
      try {
         l.addInstance(String.class, "hello");
         fail();
      } catch (IllegalStateException e) {
         assertEquals("Already have a service with key: class java.lang.String", e.getMessage());
      }
   }

   @Test
   public void testAddInstance_IllegalArgumentException() {
      try {
         createKnowledgeBaseServiceLocator().addInstance(StringBuilder.class, "hello");
         fail();
      } catch (IllegalArgumentException e) {
         assertEquals("hello is not of type: class java.lang.StringBuilder", e.getMessage());
      }
   }

   /** Test that the KnowledgeBase gets passed as an argument to the constructor of new services */
   @Test
   public void testClassWithSingleKnowledgeBaseArgumentConstrutor() {
      KB kb = createKnowledgeBase();
      KnowledgeBaseServiceLocator l = KnowledgeBaseServiceLocator.getServiceLocator(kb);
      DummyService s = l.getInstance(DummyService.class);
      assertSame(s, l.getInstance(DummyService.class));
      assertSame(kb, s.kb);
   }

   private KnowledgeBaseServiceLocator createKnowledgeBaseServiceLocator() {
      KB kb = createKnowledgeBase();
      return KnowledgeBaseServiceLocator.getServiceLocator(kb);
   }

   public static class DummyService {
      private final KB kb;

      public DummyService(KB kb) {
         this.kb = kb;
      }
   }
}
