package org.projog.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.junit.Test;

public class CoreUtilsTest {
   @Test
   public void testInstantiateUsingNoArgConstructor() throws Exception {
      String s = CoreUtils.instantiate("java.lang.String");
      assertEquals("", s);
   }

   @Test
   public void testInstantiateUsingStaticMethod() throws Exception {
      Calendar c = CoreUtils.instantiate("java.util.Calendar/getInstance");
      assertNotNull(c);
   }

   @Test(expected = ClassNotFoundException.class)
   public void testInstantiateClassNotFound() throws Exception {
      CoreUtils.instantiate("org.projog.DoesntExist");
   }

   @Test(expected = NoSuchMethodException.class)
   public void testInstantiateNoSuchMethod() throws Exception {
      CoreUtils.instantiate("java.lang.String/getInstance");
   }

   @Test(expected = IllegalAccessException.class)
   public void testInstantiateIllegalAccess() throws Exception {
      CoreUtils.instantiate("java.util.Calendar");
   }
}
