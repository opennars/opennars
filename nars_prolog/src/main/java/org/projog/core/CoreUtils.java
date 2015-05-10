package org.projog.core;

import java.lang.reflect.Method;

/** Provides a location for code that is required by multiple classes in {@code org.projog.core}. */
class CoreUtils {
   /**
    * Returns a new object created using reflection.
    * <p>
    * The {@code input} parameter can be in one of two formats:
    * <ol>
    * <li>The class name - e.g. {@code java.lang.String} - this will cause an attempt to create a new instance of the
    * specified class using its no argument constructor.</li>
    * <li>The class name <i>and</i> a method name (separated by a {@code /}) - e.g.
    * {@code java.util.Calendar/getInstance} - this will cause an attempt to create a new instance of the class by
    * invoking the specified method (as a no argument static method) of the specified class.</li>
    * </ol>
    */
   @SuppressWarnings("unchecked")
   static <T> T instantiate(String input) throws ReflectiveOperationException {
      int slashPos = input.indexOf('/');
      if (slashPos != -1) {
         String className = input.substring(0, slashPos);
         String methodName = input.substring(slashPos + 1);
         Method m = Class.forName(className).getMethod(methodName);
         return (T) m.invoke(null);
      } else {
         return (T) Class.forName(input).newInstance();
      }
   }
}
