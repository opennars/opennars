package org.projog.core.udp.compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Constructs Java source code.
 */
class JavaSourceWriter {
   private static final String SEMI_COLON = ";";

   private final List<String> lines = new ArrayList<>();

   private String packageStructure;

   private String className;

   void writePackage(String packageStructure) {
      this.packageStructure = packageStructure;
      writeStatement("package " + packageStructure);
   }

   void writeImport(String importStructure) {
      writeStatement("import " + importStructure);
   }

   void beginClass(String className, String extendsAndImplements) {
      this.className = className;
      startBlock("public final class " + className + " " + extendsAndImplements);
   }

   void beginMethod(String statement) {
      startBlock(statement);
   }

   void returnThis() {
      writeStatement("return this");
   }

   void returnTrue() {
      writeStatement("return true");
   }

   void returnFalse() {
      writeStatement("return false");
   }

   void ifFalseReturnFalse(String condition) {
      ifTrueReturnFalse("!" + condition);
   }

   void ifTrueReturnFalse(String condition) {
      beginIf(condition);
      returnFalse();
      endBlock();
   }

   void ifTrueReturnTrue(String condition) {
      beginIf(condition);
      returnTrue();
      endBlock();
   }

   void beginIf(String condition) {
      startBlock("if (" + condition + ")");
   }

   void elseIf(String condition) {
      startBlock("} else if (" + condition + ")");
   }

   void elseStatement() {
      addLine("} else {");
   }

   void endBlock() {
      addLine("}");
   }

   void declare(String className, String destination, String source) {
      writeStatement(className + " " + destination + " = " + source);
   }

   void assignTrue(String destination) {
      assign(destination, "true");
   }

   void assignFalse(String destination) {
      assign(destination, "false");
   }

   void assign(String destination, String source) {
      writeStatement(destination + " = " + source);
   }

   void assign(String destination, int source) {
      writeStatement(destination + " = " + source);
   }

   private void startBlock(String statement) {
      addLine(statement + " {");
   }

   void println(String message) {
      writeStatement("System.out.println(" + message + ")");
   }

   void writeStatement(String statement) {
      addLine(statement + SEMI_COLON);
   }

   void comment(Object comment) {
      addLine("// " + comment);
   }

   void addLine(String line) {
      lines.add(line);
   }

   String getClassName() {
      return packageStructure + "." + className;
   }

   File save(File sourceDirectory) {
      File sourceFile = getSourceFile(sourceDirectory);
      writeSourceToFile(sourceFile);
      return sourceFile;
   }

   private File getSourceFile(File sourceDirectory) {
      File parentDir = sourceDirectory;
      for (String packageName : packageStructure.split("\\.")) {
         parentDir = new File(parentDir, packageName);
      }
      if (!parentDir.exists() && !parentDir.mkdirs()) {
         throw new RuntimeException("Was not able to create directory: " + parentDir);
      }
      return new File(parentDir, className + ".java");
   }

   private void writeSourceToFile(File sourceFile) {
      PrintWriter pw = null;
      try {
         pw = new PrintWriter(sourceFile);
         writeSource(pw);
      } catch (IOException e) {
         throw new RuntimeException("Exception writing source to: " + sourceFile, e);
      } finally {
         try {
            pw.close();
         } catch (Exception e) {
         }
      }

   }

   void beginSwitch(String variable) {
      addLine("switch (" + variable + ") {");
   }

   void beginCase(int constant) {
      addLine("case " + constant + ":");
   }

   void writeSource(PrintWriter writer) {
      int tabCtr = 0;
      for (String line : lines) {
         if (line.indexOf('}') != -1) {
            if ((line.indexOf('{') == -1 || (line.indexOf('}') < line.indexOf('{')))) {
               tabCtr--;
            }
         }

         writeIndents(writer, tabCtr);
         writer.println(line);

         if (line.lastIndexOf('{') > line.lastIndexOf('}')) {
            tabCtr++;
         }
      }
   }

   private void writeIndents(PrintWriter writer, int numTabs) {
      for (int i = 0; i < numTabs; i++) {
         writer.print("   ");
      }
   }
}