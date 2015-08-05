/**
 * Project : game2Dengine<br>
 * Package : jhelp.game2D.engine.util<br>
 * Class : Compiler<br>
 * Date : 13 dec. 2009<br>
 * By JHelp
 */
package jhelp.util.compiler;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.io.UtilIO;
import jhelp.util.text.UtilText;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Compiler of class<br>
 * <br>
 * Last modification : 13 dec. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Compiler
{
   /**
    * Associate a class name to code <br>
    * <br>
    * Last modification : 18 juin 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   public static class NameCode
         implements Comparable<NameCode>
   {
      /** Associate code */
      String code;
      /** Class name */
      String name;

      /**
       * Constructs NameCode
       * 
       * @param name
       *           Class name
       * @param code
       *           Associate code
       */
      public NameCode(final String name, final String code)
      {
         this.name = name;
         this.code = code;
      }

      /**
       * Compare with an other couple
       * 
       * @param o
       *           To compare with
       * @return Compare result
       * @see Comparable#compareTo(Object)
       */
      @Override
      public int compareTo(final NameCode o)
      {
         return this.name.compareTo(o.name);
      }

      /**
       * Indicates if an object is this couple
       * 
       * @param obj
       *           Object to compare
       * @return {@code true} if there equals
       * @see Object#equals(Object)
       */
      @Override
      public boolean equals(final Object obj)
      {
         if(this == obj)
         {
            return true;
         }
         if(obj == null)
         {
            return false;
         }
         if(!(obj instanceof NameCode))
         {
            return false;
         }
         final NameCode other = (NameCode) obj;
         if(this.name == null)
         {
            if(other.name != null)
            {
               return false;
            }
         }
         else if(!this.name.equals(other.name))
         {
            return false;
         }
         return true;
      }

      /**
       * Return code
       * 
       * @return code
       */
      public String getCode()
      {
         return this.code;
      }

      /**
       * Return name
       * 
       * @return name
       */
      public String getName()
      {
         return this.name;
      }

      /**
       * Hash code
       * 
       * @return Hash code
       * @see Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = (prime * result) + ((this.name == null)
               ? 0
               : this.name.hashCode());
         return result;
      }
   }

   /**
    * Compile a list of code inside a specified directory
    * 
    * @param directory
    *           Directory destination of compiled code
    * @param nameCodes
    *           Codes to compile
    * @throws IOException
    *            On writing issue
    */
   public static void compil(final File directory, final NameCode... nameCodes) throws IOException
   {
      Debug.println(DebugLevel.INFORMATION, "Compile in ", directory.getAbsolutePath());

      // Create the compiler
      final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

      // Create the list of class to compile
      final ArrayList<JavaSourceFromString> compilationUnits = new ArrayList<JavaSourceFromString>();
      for(final NameCode code : nameCodes)
      {
         compilationUnits.add(new JavaSourceFromString(code.name, code.code));
      }

      // We create the file manager, and says the directory to use for output
      final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

      UtilIO.createDirectory(directory);

      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(directory));

      // Do the compilation itself
      if(compiler.getTask(null, fileManager, null, null, null, compilationUnits).call() == false)
      {
         throw new RuntimeException("Compilation failed !");
      }

      fileManager.flush();
      fileManager.close();
   }

   /**
    * Compile several class in same time (Use full if a class need an other one to compil them in same time)
    * 
    * @param nameCodes
    *           Couples of name/code to compile
    * @return Directory where generated the .class files
    * @throws IOException
    *            On read/write problem
    */
   public static File compil(final NameCode... nameCodes) throws IOException
   {
      final File directory = new File(UtilIO.obtainTemporaryDirectory(), UtilText.concatenate("Compiler", File.separator));

      Debug.println(DebugLevel.INFORMATION, "Compile in ", directory.getAbsolutePath());

      // Create the compiler
      final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

      // Create the list of class to compile
      final ArrayList<JavaSourceFromString> compilationUnits = new ArrayList<JavaSourceFromString>();
      for(final NameCode code : nameCodes)
      {
         compilationUnits.add(new JavaSourceFromString(code.name, code.code));
      }

      // We create the file manager, and says the directory to use for output
      final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

      UtilIO.createDirectory(directory);

      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(directory));

      // Do the compilation itself
      if(compiler.getTask(null, fileManager, null, null, null, compilationUnits).call() == false)
      {
         throw new RuntimeException("Compilation failed !");
      }

      fileManager.flush();
      fileManager.close();

      return directory;
   }

   /**
    * Compile a class
    * 
    * @param classCompleteName
    *           Class to compile
    * @param code
    *           Code of class
    * @return Directory where lies the compiled files
    * @throws IOException
    *            On read/write issue
    */
   public static File compil(final String classCompleteName, final String code) throws IOException
   {
      final File directory = new File(UtilIO.obtainTemporaryDirectory(), UtilText.concatenate("Compiler", File.separator));

      Debug.println(DebugLevel.INFORMATION, "Compile in ", directory.getAbsolutePath());

      // Create the compiler
      final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

      // Create the list of class to compile
      final ArrayList<JavaSourceFromString> compilationUnits = new ArrayList<JavaSourceFromString>();
      compilationUnits.add(new JavaSourceFromString(classCompleteName, code));

      // We create the file manager, and says the directory to use for output
      final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

      UtilIO.createDirectory(directory);

      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(directory));

      // Do the compilation itself
      if(compiler.getTask(null, fileManager, null, null, null, compilationUnits).call() == false)
      {
         throw new RuntimeException(UtilText.concatenate("Compilation of ", classCompleteName, " failed !"));
      }

      fileManager.flush();
      fileManager.close();

      return directory;
   }
}