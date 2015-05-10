package org.projog.core.udp.compiler;

import static org.projog.core.KnowledgeBaseUtils.getProjogProperties;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.List;

import org.projog.core.KnowledgeBase;
import org.projog.core.ProjogException;
import org.projog.core.udp.ClauseModel;

/**
 * Constructs and compiles source code for new {@link CompiledPredicate} classes.
 */
public final class CompiledPredicateClassGenerator {
   /**
    * Translates the specified {@code implications} into Java source code before compiling it and returning an instance
    * of the newly created class.
    */
   public static CompiledPredicate generateCompiledPredicate(KnowledgeBase kb, List<ClauseModel> implications) {
      File dynamicContentDir = getDynamicContentDir(kb);
      CompiledPredicateWriter writer = new CompiledPredicateWriter(kb, implications);
      new CompiledPredicateSourceGenerator(writer).generateSource();
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      writer.writeSource(pw);
      pw.close();
      try {
         sw.close();
      } catch (Exception e) {
      }
      writer.save(dynamicContentDir);
      return compileSource(kb, dynamicContentDir, writer.getClassName(), sw.toString());
   }

   /** Compiles the specified {@code sourceContent} Java code and returns a new instance of the new class. */
   private static CompiledPredicate compileSource(KnowledgeBase kb, File dynamicContentDir, String className, String sourceContent) {
      try {
         Class<?> c = JavaSourceCompiler.compileClass(dynamicContentDir, className, sourceContent);
         Constructor<?> constructor = c.getConstructor(KnowledgeBase.class);
         return (CompiledPredicate) constructor.newInstance(kb);
      } catch (Throwable e) {
         throw new ProjogException("Caught " + e.getClass().getName() + " while attempting to compile class: " + className + " with message: " + e.getMessage(), e);
      }
   }

   /** Returns the root directory to store generated source files */
   private static File getDynamicContentDir(KnowledgeBase kb) {
      File f = new File(getProjogProperties(kb).getRuntimeCompilationOutputDirectory());
      if (!f.exists()) {
         throw new RuntimeException("Directory required to store classes generated at runtime does not exist: " + f.getAbsolutePath());
      }
      return f;
   }
}