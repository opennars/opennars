package org.projog.core;

/**
 * Collection of configuration properties.
 * <p>
 * Each {@link KB} has a single {@code ProjogProperties} instance.
 * 
 * @see KnowledgeBaseUtils#getProjogProperties(KB)
 */
public interface ProjogProperties {
   /**
    * Returns {@code true} if the use of spy points is enabled.
    * <p>
    * If spy points are enabled then it will be possible to get information about the sequence of goals being evaluated
    * by Projog as they are evaluated. This facility aids the debugging of Prolog code but can also have a slight impact
    * on performance.
    * 
    * @return {@code true} if the use of spy points is enabled
    * @see SpyPoints
    */
   boolean isSpyPointsEnabled();

   /**
    * Returns {@code true} if user defined predicates should be compiled at runtime.
    * <p>
    * Projog is able to convert user defined predicates specified using Prolog syntax into native Java code. Converting
    * Prolog syntax into Java code offers optimised performance. The generation of the Java code and it's compilation
    * happens at runtime as new clauses are consulted. If runtime compilation is disabled then Projog operates in
    * "interpreted" mode - this will impact performance but avoid the need of compiling Java code at runtime.
    * 
    * @return {@code true} if user defined predicates should be compiled at runtime
    * @see #getRuntimeCompilationOutputDirectory()
    */
   boolean isRuntimeCompilationEnabled();

   /**
    * Returns the directory to store class files of code generated at runtime.
    * <p>
    * This value is only used if runtime compilation is enabled. The directory must be in the application's classpath.
    * 
    * @return the directory to store class files of code generated at runtime
    * @see #isRuntimeCompilationEnabled()
    */
   String getRuntimeCompilationOutputDirectory();

   /**
    * Returns the name of the resource loaded by {@link KnowledgeBaseUtils#bootstrap()}.
    * 
    * @return the name of the resource loaded by {@link KnowledgeBaseUtils#bootstrap()}
    * @see KnowledgeBaseUtils#bootstrap()
    */
   String getBootstrapScript();
}