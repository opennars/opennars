package org.projog.core.udp.compiler;

import org.projog.core.ProjogException;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;

/** Compiles Java source code into bytecode. */
final class JavaSourceCompiler {
   /**
    * Compiles the specified java source code and returns the resulting Class object.
    * <p>
    * The class file generated as the output of the compilation process is stored in the specified directory. <b>Note
    * that an exception will be thrown if the output directory specified by the {@code dynamicContentDir} parameter is
    * not included in the application's classpath.<b>
    * 
    * @param dynamicContentDir directory to store generated .class file
    * @param className class name to compile
    * @param sourceCode java source code to compile
    * @return the newly compiled class
    * @throws ProjogException
    */
   static Class<?> compileClass(File dynamicContentDir, String className, String sourceCode) {
      String classpath = System.getProperty("java.class.path");
      assertClasspathContainsDirectory(classpath, dynamicContentDir);
      try {
         compile(dynamicContentDir, className, sourceCode);
         return load(className);
      } catch (Exception e) {
         throw new ProjogException("Cannot compile: " + className + " using classpath: " + classpath, e);
      }
   }

   /**
    * Throws a ProjogException if the specified directory does not exist in the specified classpath.
    * <p>
    * The directory where generated class files will be stored needs to be present in the application's classpath in
    * order for the class to be found when we try to load it.
    */
   private static final void assertClasspathContainsDirectory(String classpath, File dynamicContentDir) {
      if (classpath.indexOf(dynamicContentDir.getName()) == -1) {
         throw new ProjogException("The directory: " + dynamicContentDir.getAbsolutePath() + " is not in the java classpath: " + classpath);
      }
   }

   private static final void compile(File dynamicContentDir, String className, String sourceCode) {
      SimpleJavaFileObject fileObject = new DynamicJavaSourceCodeObject(className, sourceCode);
      JavaFileObject javaFileObjects[] = new JavaFileObject[] {fileObject};

      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

      StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);

      Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(javaFileObjects);

      String[] compileOptions = new String[] {"-d", dynamicContentDir.getPath()};
      Iterable<String> compilationOptionss = Arrays.asList(compileOptions);

      DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

      CompilationTask compilerTask = compiler.getTask(null, stdFileManager, diagnostics, compilationOptionss, null, compilationUnits);

      // Invoke call on compilerTask to invoke compilation.
      boolean status = compilerTask.call();

      if (!status) {
         // If compilation error occurs then throw exception with as much information as possible
         StringBuilder reasons = new StringBuilder();
         for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
            reasons.append(" Error on line " + diagnostic.getLineNumber() + " in " + diagnostic);
         }
         throw new ProjogException("Failed compiling using java.class.path value of: " + System.getProperty("java.class.path") + reasons);
      }
      try {
         stdFileManager.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private static Class<?> load(String className) throws ClassNotFoundException, MalformedURLException {
      return ClassLoader.getSystemClassLoader().loadClass(className);
   }

   private static class DynamicJavaSourceCodeObject extends SimpleJavaFileObject {
      // Based on article at:
      // http://accordess.com/wpblog/2011/03/06/an-overview-of-java-compilation-api-jsr-199/
      // Generating Java classes dynamically through Java compiler API
      private final String sourceCode;

      /**
       * Converts the name to an URI, as that is the format expected by JavaFileObject
       * 
       * @param name fully qualified name given to the class file
       * @param code the source code string
       */
      protected DynamicJavaSourceCodeObject(String name, String code) {
         super(URI.create("string:///" + name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
         this.sourceCode = code;
      }

      @Override
      public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
         return sourceCode;
      }
   }
}