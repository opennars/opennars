package org.projog.core;

/**
 * Implementation of {@link ProjogProperties} with values determined from system properties.
 * <p>
 * <ul>
 * <li><code>projog.compile</code> - <code>true</code> if the Projog inference engine should run in "compiled mode" or
 * <code>false</code> if the inference engine should run in "interpreted mode". Running in "compiled mode" causes user
 * defined predicates to be compiled to Java bytecode at runtime and can give large performance improvements. Defaults
 * to <code>true</code>.</li>
 * <li><code>projog.spypoints</code> - <code>true</code> if the Projog inference engine should support the creation of
 * spypoints to aid debugging, or <code>false</code> if requests to set spypoints should be ignored. Ignoring spypoints
 * can give small performance improvements. Defaults to <code>true</code>.</li>
 * <li><code>projog.generatedClasses</code> - the directory where the class files of user defined predicates compiled at
 * runtime should be stored. This property is only used if running in "compiled mode". The directory also needs to be
 * included in the Java classpath of the application being executed. Defaults to <code>projogGeneratedClasses</code>.</li>
 * </ul>
 * </p>
 * Example of setting system properties when launching Java:
 * 
 * <pre>
 * java -Dprojog.spypoints=false -Dprojog.compile=true -Dprojog.generatedClasses=projogGeneratedClasses -cp projogGeneratedClasses;lib/projog-core.jar org.projog.example.ProjogExample
 * </pre>
 * 
 * </p>
 */
public class ProjogSystemProperties implements ProjogProperties {
   private static final String DEFAULT_BOOTSTRAP_SCRIPT = "projog-bootstrap.pl";

   private final String dynamicContentDir = System.getProperty("projog.generatedClasses", "projogGeneratedClasses");
   private final boolean isSpyPointsEnabled = !"false".equalsIgnoreCase(System.getProperty("projog.spypoints"));
   private final boolean isRuntimeCompilationEnabled = !"false".equalsIgnoreCase(System.getProperty("projog.compile"));

   /**
    * Returns {@code true} unless there is a system property named "projog.spypoints" with the value "false".
    * 
    * @return {@code true} unless there is a system property named "projog.spypoints" with the value "false".
    */
   @Override
   public boolean isSpyPointsEnabled() {
      return isSpyPointsEnabled;
   }

   /**
    * Returns {@code true} unless there is a system property named "projog.compile" with the value "false".
    * 
    * @return {@code true} unless there is a system property named "projog.compile" with the value "false".
    */
   @Override
   public boolean isRuntimeCompilationEnabled() {
      return isRuntimeCompilationEnabled;
   }

   /**
    * Returns the name of the directory where classes generated at runtime will be stored.
    * 
    * @return Value of system property named "projog.generatedClasses" or, if not set, returns default value of
    * {@code projogGeneratedClasses}
    */
   @Override
   public String getRuntimeCompilationOutputDirectory() {
      return dynamicContentDir;
   }

   /**
    * Returns "projog-bootstrap.pl".
    * <p>
    * {@code projog-bootstrap.pl} is contained in {@code projog-core.jar}.
    * 
    * @return {@code projog-bootstrap.pl}
    */
   @Override
   public String getBootstrapScript() {
      return DEFAULT_BOOTSTRAP_SCRIPT;
   }
}