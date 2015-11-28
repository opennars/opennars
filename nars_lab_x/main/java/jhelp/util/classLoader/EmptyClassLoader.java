/**
 * Project : JHelpUtil<br>
 * Package : jhelp.util.classLoader<br>
 * Class : EmptyClassLoader<br>
 * Date : 5 juin 2010<br>
 * By JHelp
 */
package jhelp.util.classLoader;

import jhelp.util.compiler.Compiler;
import jhelp.util.compiler.Compiler.NameCode;
import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.io.UtilIO;
import jhelp.util.text.UtilText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.WeakHashMap;
import java.util.jar.JarFile;

/**
 * Class loader that make loaded class empty.<br>
 * It creates dynamically empty code and compile it for be able load it <br>
 * <br>
 * Last modification : 5 juin 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class EmptyClassLoader
      extends ClassLoader
{
   /** Next temporary file index */
   private static int tempFileIndex = 0;

   /**
    * Create temporary file. Use for simulate reference to a valid file
    * 
    * @return XML value of file name
    */
   private static String createFile()
   {
      final File file = new File(UtilIO.obtainTemporaryDirectory(), UtilText.concatenate("EmptyClassLoader", File.separator, "file", EmptyClassLoader.tempFileIndex++));
      UtilIO.createFile(file);
      return UtilText.concatenate('"', file.getAbsolutePath().replace("\\", "\\\\"), '"');
   }

   /** Already loaded classes */
   private final WeakHashMap<String, SoftReference<Class<?>>> classes;
   /** Directory where class are compiled */
   private File                                               directory;
   /** Base jar class loader */
   private final JHelpJarClassLoader                          jarClassLoader;
   /** On creation class for avoid infinite loop */
   private final HashSet<String>                              onCreation;

   /**
    * Constructs EmptyClassLoader
    * 
    * @param jarsToMock
    *           Jars to see as empty
    */
   public EmptyClassLoader(final JarFile... jarsToMock)
   {
      this.jarClassLoader = new JHelpJarClassLoader();
      for(final JarFile jarFile : jarsToMock)
      {
         this.jarClassLoader.add(jarFile);
      }

      this.onCreation = new HashSet<String>();
      this.directory = null;
      this.classes = new WeakHashMap<String, SoftReference<Class<?>>>();
   }

   /**
    * Add import in list
    * 
    * @param base
    *           Base class name
    * @param set
    *           List of import
    * @param name
    *           Class name
    */
   private void addImport(final String base, final HashSet<String> set, final String name)
   {
      if(name.startsWith(base + "$") == false)
      {
         set.add(name.replace('$', '.'));
      }
   }

   /**
    * Append content of class to current code
    * 
    * @param internal
    *           Indicates if class is internal
    * @param decal
    *           Start of each line
    * @param name
    *           Class name
    * @param stringBuilder
    *           Where append
    * @param classInJar
    *           Class to append
    * @param simpleName
    *           Short name of class
    * @throws ClassNotFoundException
    *            If class not found
    */
   private void appendContent(final boolean internal, final String decal, final String name, final StringBuilder stringBuilder, final Class<?> classInJar, final String simpleName) throws ClassNotFoundException
   {
      // write header
      stringBuilder.append(decal);
      int or = 0;
      if(classInJar.isEnum() == true)
      {
         or = Modifier.ABSTRACT;
      }
      stringBuilder.append(Modifier.toString(classInJar.getModifiers() & (~(Modifier.FINAL | or))));
      final boolean stat = Modifier.isStatic(classInJar.getModifiers());
      if(Modifier.isInterface(classInJar.getModifiers()) == true)
      {
         stringBuilder.append(' ');
      }
      else if(classInJar.isEnum() == true)
      {
         stringBuilder.append(" enum ");
      }
      else
      {
         stringBuilder.append(" class ");
      }
      stringBuilder.append(simpleName);

      Class<?> temp = classInJar.getSuperclass();
      if((temp != null) && (classInJar.isEnum() == false))
      {
         stringBuilder.append(" extends ");
         stringBuilder.append(temp.getName().replace('$', '.'));
      }

      final Class<?>[] in = classInJar.getInterfaces();
      boolean first;
      if((in != null) && (in.length > 0))
      {
         first = true;
         if(classInJar.isInterface() == true)
         {
            stringBuilder.append(" extends ");
         }
         else
         {
            stringBuilder.append(" implements ");
         }
         for(final Class<?> interf : in)
         {
            if(first == false)
            {
               stringBuilder.append(", ");
            }
            first = false;
            stringBuilder.append(interf.getName().replace('$', '.'));
         }
      }

      stringBuilder.append(decal);
      stringBuilder.append("{");

      if(classInJar.isEnum() == true)
      {
         stringBuilder.append(decal);
         stringBuilder.append("\t");

         first = true;
         for(final Field field : classInJar.getDeclaredFields())
         {
            if(field.isEnumConstant() == true)
            {
               if(first == false)
               {
                  stringBuilder.append(", ");
               }
               first = false;

               stringBuilder.append(field.getName());
            }
         }
         stringBuilder.append(";");
      }
      else
      {
         boolean inter;
         int count;
         String nameTemp;
         boolean asEmpty = false;

         // Fields
         for(final Field field : classInJar.getDeclaredFields())
         {
            if(Modifier.isPublic(field.getModifiers()) == true)
            {
               stringBuilder.append(decal);
               stringBuilder.append("\t");
               stringBuilder.append(Modifier.toString(field.getModifiers()));
               stringBuilder.append(" ");
               temp = field.getType();
               stringBuilder.append(this.getRootType(temp).getName().replace('$', '.'));
               stringBuilder.append(this.getArrayLevel(temp));
               stringBuilder.append(" ");
               stringBuilder.append(field.getName());

               stringBuilder.append("=");

               if(temp.isPrimitive() == true)
               {
                  nameTemp = temp.getName();

                  if(nameTemp.equals("boolean") == true)
                  {
                     stringBuilder.append("true");
                  }
                  else if(nameTemp.equals("char") == true)
                  {
                     stringBuilder.append("' '");
                  }
                  else if(nameTemp.equals("byte") == true)
                  {
                     stringBuilder.append("(byte)0");
                  }
                  else if(nameTemp.equals("short") == true)
                  {
                     stringBuilder.append("(short)0");
                  }
                  else if(nameTemp.equals("int") == true)
                  {
                     stringBuilder.append("0");
                  }
                  else if(nameTemp.equals("long") == true)
                  {
                     stringBuilder.append("0l");
                  }
                  else if(nameTemp.equals("float") == true)
                  {
                     stringBuilder.append("0f");
                  }
                  else if(nameTemp.equals("double") == true)
                  {
                     stringBuilder.append("0.0");
                  }
               }
               else if(temp.equals(String.class) == true)
               {
                  stringBuilder.append("\"\"");
               }
               else
               {
                  stringBuilder.append("null");
               }

               stringBuilder.append(";");
            }
         }

         // Constructors
         for(final Constructor<?> constructor : classInJar.getDeclaredConstructors())
         {
            if(Modifier.isPublic(constructor.getModifiers()) == true)
            {
               stringBuilder.append(decal);
               stringBuilder.append("\t");
               stringBuilder.append(Modifier.toString(constructor.getModifiers()));
               stringBuilder.append(' ');
               stringBuilder.append(simpleName);
               stringBuilder.append('(');

               inter = internal;
               count = 0;
               for(final Class<?> parameter : constructor.getParameterTypes())
               {
                  if((count == 0) && (inter == true) && (stat == false))
                  {
                     inter = false;
                  }
                  else
                  {
                     if(count > 0)
                     {
                        stringBuilder.append(", ");
                     }

                     stringBuilder.append(this.getRootType(parameter).getName().replace('$', '.'));
                     stringBuilder.append(this.getArrayLevel(parameter));
                     stringBuilder.append(" a");
                     stringBuilder.append(count);

                     count++;
                  }
               }
               if(count == 0)
               {
                  asEmpty = true;
               }

               stringBuilder.append(")");
               stringBuilder.append(decal);

               // Call super constructor, if need
               final Class<?> parent = classInJar.getSuperclass();
               if(parent != null)
               {
                  Constructor<?> constructorParent = null;
                  Class<?>[] parameterTypes = null;
                  Class<?>[] parameterTypesLook;
                  int nbLook;
                  int nbArguments = Integer.MAX_VALUE;

                  for(final Constructor<?> constructorParentLook : parent.getDeclaredConstructors())
                  {
                     if((constructorParentLook.getModifiers() & Modifier.PRIVATE) == 0)
                     {
                        parameterTypesLook = constructorParentLook.getParameterTypes();

                        if(parameterTypesLook == null)
                        {
                           nbLook = 0;
                        }
                        else
                        {
                           nbLook = parameterTypesLook.length;
                        }

                        if(nbLook < nbArguments)
                        {
                           constructorParent = constructorParentLook;
                           parameterTypes = parameterTypesLook;
                           nbArguments = nbLook;
                        }
                     }
                  }

                  final Class<?>[] exceptions = constructorParent.getExceptionTypes();
                  if((exceptions != null) && (exceptions.length > 0))
                  {
                     stringBuilder.append(" throws ");
                     stringBuilder.append(exceptions[0].getName());

                     for(int i = 1; i < exceptions.length; i++)
                     {
                        stringBuilder.append(", ");
                        stringBuilder.append(exceptions[i].getName());
                     }
                  }

                  stringBuilder.append("\t{");
                  stringBuilder.append(decal);
                  stringBuilder.append("\t\tsuper(");
                  for(int arg = 0; arg < nbArguments; arg++)
                  {
                     temp = parameterTypes[arg];

                     if(temp.isPrimitive() == true)
                     {
                        nameTemp = temp.getName();

                        if(nameTemp.equals("boolean") == true)
                        {
                           stringBuilder.append("true");
                        }
                        else if(nameTemp.equals("char") == true)
                        {
                           stringBuilder.append("' '");
                        }
                        else if(nameTemp.equals("byte") == true)
                        {
                           stringBuilder.append("(byte)0");
                        }
                        else if(nameTemp.equals("short") == true)
                        {
                           stringBuilder.append("(short)0");
                        }
                        else if(nameTemp.equals("int") == true)
                        {
                           stringBuilder.append("0");
                        }
                        else if(nameTemp.equals("long") == true)
                        {
                           stringBuilder.append("0l");
                        }
                        else if(nameTemp.equals("float") == true)
                        {
                           stringBuilder.append("0f");
                        }
                        else if(nameTemp.equals("double") == true)
                        {
                           stringBuilder.append("0.0");
                        }
                     }
                     else if(temp.equals(String.class) == true)
                     {
                        stringBuilder.append("\"\"");
                     }
                     else if(temp.equals(File.class) == true)
                     {
                        stringBuilder.append("new java.io.File(");
                        stringBuilder.append(EmptyClassLoader.createFile());
                        stringBuilder.append(")");
                     }
                     else
                     {
                        stringBuilder.append('(');
                        stringBuilder.append(temp.getName());
                        stringBuilder.append(")null");
                     }

                     if(arg < (nbArguments - 1))
                     {
                        stringBuilder.append(", ");
                     }
                  }
                  stringBuilder.append(");");
               }
               //

               stringBuilder.append(decal);
               stringBuilder.append("\t}");
            }
         }

         if((asEmpty == false) && (Modifier.isInterface(classInJar.getModifiers()) == false))
         {
            stringBuilder.append(decal);
            stringBuilder.append("\tpublic ");
            stringBuilder.append(simpleName);
            stringBuilder.append("()");
            stringBuilder.append(decal);

            // Call super constructor, if need
            final Class<?> parent = classInJar.getSuperclass();
            if(parent != null)
            {
               Constructor<?> constructorParent = null;
               Class<?>[] parameterTypes = null;
               Class<?>[] parameterTypesLook;
               int nbLook;
               int nbArguments = Integer.MAX_VALUE;

               for(final Constructor<?> constructorParentLook : parent.getDeclaredConstructors())
               {
                  if((constructorParentLook.getModifiers() & Modifier.PRIVATE) == 0)
                  {
                     parameterTypesLook = constructorParentLook.getParameterTypes();

                     if(parameterTypesLook == null)
                     {
                        nbLook = 0;
                     }
                     else
                     {
                        nbLook = parameterTypesLook.length;
                     }

                     if(nbLook < nbArguments)
                     {
                        constructorParent = constructorParentLook;
                        parameterTypes = parameterTypesLook;
                        nbArguments = nbLook;
                     }
                  }
               }

               final Class<?>[] exceptions = constructorParent.getExceptionTypes();
               if((exceptions != null) && (exceptions.length > 0))
               {
                  stringBuilder.append(" throws ");
                  stringBuilder.append(exceptions[0].getName());

                  for(int i = 1; i < exceptions.length; i++)
                  {
                     stringBuilder.append(", ");
                     stringBuilder.append(exceptions[i].getName());
                  }
               }

               stringBuilder.append("\t{");
               stringBuilder.append(decal);
               stringBuilder.append("\t\tsuper(");
               for(int arg = 0; arg < nbArguments; arg++)
               {
                  temp = parameterTypes[arg];

                  if(temp.isPrimitive() == true)
                  {
                     nameTemp = temp.getName();

                     if(nameTemp.equals("boolean") == true)
                     {
                        stringBuilder.append("true");
                     }
                     else if(nameTemp.equals("char") == true)
                     {
                        stringBuilder.append("' '");
                     }
                     else if(nameTemp.equals("byte") == true)
                     {
                        stringBuilder.append("(byte)0");
                     }
                     else if(nameTemp.equals("short") == true)
                     {
                        stringBuilder.append("(short)0");
                     }
                     else if(nameTemp.equals("int") == true)
                     {
                        stringBuilder.append("0");
                     }
                     else if(nameTemp.equals("long") == true)
                     {
                        stringBuilder.append("0l");
                     }
                     else if(nameTemp.equals("float") == true)
                     {
                        stringBuilder.append("0f");
                     }
                     else if(nameTemp.equals("double") == true)
                     {
                        stringBuilder.append("0.0");
                     }
                  }
                  else if(temp.equals(String.class) == true)
                  {
                     stringBuilder.append("\"\"");
                  }
                  else if(temp.equals(File.class) == true)
                  {
                     stringBuilder.append("new java.io.File(");
                     stringBuilder.append(EmptyClassLoader.createFile());
                     stringBuilder.append(")");
                  }

                  else
                  {
                     stringBuilder.append('(');
                     stringBuilder.append(temp.getName());
                     stringBuilder.append(")null");
                  }

                  if(arg < (nbArguments - 1))
                  {
                     stringBuilder.append(", ");
                  }
               }
               stringBuilder.append(");");
               stringBuilder.append(decal);
               stringBuilder.append("\t}");
            }
            //
         }

         Class<?> ret;

         int modifier;
         final HashSet<String> already = new HashSet<String>();
         StringBuilder end;

         // Methods
         for(final Method method : classInJar.getDeclaredMethods())
         {
            if(Modifier.isPublic(method.getModifiers()) == true)
            {
               end = new StringBuilder();
               end.append(method.getName());
               end.append('(');

               count = 0;
               for(final Class<?> parameter : method.getParameterTypes())
               {
                  if(count > 0)
                  {
                     end.append(", ");
                  }

                  end.append(this.getRootType(parameter).getName().replace('$', '.'));
                  end.append(this.getArrayLevel(parameter));
                  end.append(" a");
                  end.append(count);

                  count++;
               }

               if(already.contains(end.toString()) == true)
               {
                  continue;
               }

               already.add(end.toString());

               modifier = method.getModifiers() & (~(Modifier.NATIVE | Modifier.TRANSIENT | Modifier.VOLATILE));

               stringBuilder.append(decal);
               stringBuilder.append("\t");
               stringBuilder.append(Modifier.toString(modifier & (~(Modifier.FINAL))));
               stringBuilder.append(' ');
               ret = method.getReturnType();
               temp = this.getRootType(ret);
               if(temp == null)
               {
                  stringBuilder.append("void");
               }
               else
               {
                  stringBuilder.append(temp.getName().replace('$', '.'));
                  stringBuilder.append(this.getArrayLevel(ret));
               }

               stringBuilder.append(' ');

               stringBuilder.append(end.toString());

               if(Modifier.isAbstract(modifier) == true)
               {
                  stringBuilder.append(");");
               }
               else
               {
                  stringBuilder.append("){");
                  if((ret != null) && (ret.equals(Void.TYPE) == false) && (ret.equals(Void.class) == false))
                  {
                     if(ret.isPrimitive() == true)
                     {
                        nameTemp = ret.getName();
                        if(nameTemp.equals("boolean") == true)
                        {
                           stringBuilder.append("return true;");
                        }
                        else if(nameTemp.equals("char") == true)
                        {
                           stringBuilder.append("return ' ';");
                        }
                        else if(nameTemp.equals("byte") == true)
                        {
                           stringBuilder.append("return (byte)0;");
                        }
                        else if(nameTemp.equals("short") == true)
                        {
                           stringBuilder.append("return (short)0;");
                        }
                        else if(nameTemp.equals("int") == true)
                        {
                           stringBuilder.append("return 0;");
                        }
                        else if(nameTemp.equals("long") == true)
                        {
                           stringBuilder.append("return 0l;");
                        }
                        else if(nameTemp.equals("float") == true)
                        {
                           stringBuilder.append("return 0f;");
                        }
                        else if(nameTemp.equals("double") == true)
                        {
                           stringBuilder.append("return 0.0;");
                        }
                     }
                     else
                     {
                        stringBuilder.append("return null;");
                     }
                  }
                  stringBuilder.append("}");
               }
            }
         }
      }

      this.appendInternal(decal + "\t", stringBuilder, name);
   }

   /**
    * Append internal class inside a class
    * 
    * @param decal
    *           Add start of each line
    * @param stringBuilder
    *           Where append
    * @param name
    *           Class name
    * @throws ClassNotFoundException
    *            If class not found
    */
   private void appendInternal(final String decal, final StringBuilder stringBuilder, final String name) throws ClassNotFoundException
   {
      String simpleName;

      for(final String nameInternal : this.jarClassLoader.listOfDirectInternal(name))
      {
         simpleName = nameInternal;
         final int index = nameInternal.lastIndexOf('$');
         if(index >= 0)
         {
            simpleName = nameInternal.substring(index + 1);
         }

         this.appendContent(true, decal, nameInternal, stringBuilder, this.jarClassLoader.loadClass(nameInternal), simpleName);

         stringBuilder.append(decal);
         stringBuilder.append("}");
      }
   }

   /**
    * Compile a list of class
    * 
    * @param name
    *           Class to compile
    * @param needCompile
    *           Need to compile list
    * @throws ClassNotFoundException
    *            If class not found
    */
   private void compileClass(String name, final HashSet<NameCode> needCompile) throws ClassNotFoundException
   {
      if(name.indexOf('$') >= 0)
      {
         return;
      }

      this.onCreation.add(name);

      try
      {
         String simpleName = name;
         String pack = "";
         int index = name.lastIndexOf('.');
         if(index >= 0)
         {
            simpleName = name.substring(index + 1);
            pack = name.substring(0, index);
         }

         Class<?> classInJar = null;
         while(classInJar == null)
         {
            try
            {
               classInJar = this.jarClassLoader.loadClass(name);
            }
            catch(final Exception exception)
            {
               final int i = name.lastIndexOf('.');
               if(i < 0)
               {
                  throw exception;
               }

               name = name.substring(0, index);

               simpleName = name;
               pack = "";
               index = name.lastIndexOf('.');
               if(index >= 0)
               {
                  simpleName = name.substring(index + 1);
                  pack = name.substring(0, index);
               }
            }
         }

         final StringBuilder stringBuilder = new StringBuilder("package ");
         stringBuilder.append(pack);
         stringBuilder.append(";\n");

         // Import collection
         final HashSet<String> imports = new HashSet<String>();
         this.fillImports(name, name, imports, classInJar);

         // Write imports
         for(final String imp : imports)
         {
            if((this.onCreation.contains(imp) == false) && (this.classes.containsKey(imp) == false))
            {
               if((imp.startsWith("java.") == false) && (imp.startsWith("javax.") == false) && (imp.startsWith("com.sun.") == false) && (imp.startsWith("sun.") == false) && (imp.startsWith("sunw.") == false))
               {
                  this.compileClass(imp, needCompile);
               }
            }
         }

         this.appendContent(false, "\n", name, stringBuilder, classInJar, simpleName);
         stringBuilder.append("\n}");

         Debug.println(DebugLevel.VERBOSE);
         Debug.println(DebugLevel.VERBOSE, "**** Code generated for : ", name, " *****");
         Debug.println(DebugLevel.VERBOSE, stringBuilder.toString());
         Debug.println(DebugLevel.VERBOSE, "******************************************");
         Debug.println(DebugLevel.VERBOSE);

         needCompile.add(new NameCode(name, stringBuilder.toString()));
      }
      catch(final Exception exception)
      {
         throw new ClassNotFoundException("Can't find/generate class : " + name, exception);
      }
   }

   /**
    * Fill imports
    * 
    * @param base
    *           Base name
    * @param name
    *           Class name
    * @param imports
    *           List import to fill
    * @param classInJar
    *           Class
    * @throws ClassNotFoundException
    *            if class not found
    */
   private void fillImports(final String base, final String name, final HashSet<String> imports, final Class<?> classInJar) throws ClassNotFoundException
   {
      if(classInJar.getSuperclass() != null)
      {
         this.addImport(base, imports, classInJar.getSuperclass().getName());
      }

      Class<?> temp;

      for(final Class<?> interf : classInJar.getInterfaces())
      {
         this.addImport(base, imports, interf.getName());
      }

      for(final Constructor<?> constructor : classInJar.getDeclaredConstructors())
      {
         if(Modifier.isPublic(constructor.getModifiers()) == true)
         {
            for(final Class<?> parameter : constructor.getParameterTypes())
            {
               temp = this.getRootType(parameter);
               if((temp != null) && (temp.isPrimitive() == false))
               {
                  this.addImport(base, imports, temp.getName());
               }
            }
         }
      }
      for(final Field field : classInJar.getDeclaredFields())
      {
         if(Modifier.isPublic(field.getModifiers()) == true)
         {
            temp = this.getRootType(field.getType());
            if((temp != null) && (temp.isPrimitive() == false))
            {
               this.addImport(base, imports, temp.getName());
            }
         }
      }
      for(final Method method : classInJar.getDeclaredMethods())
      {
         if(Modifier.isPublic(method.getModifiers()) == true)
         {
            temp = this.getRootType(method.getReturnType());
            if((temp != null) && (temp.isPrimitive() == false))
            {
               this.addImport(base, imports, temp.getName());
            }

            for(final Class<?> parameter : method.getParameterTypes())
            {
               temp = this.getRootType(parameter);
               if((temp != null) && (temp.isPrimitive() == false))
               {
                  this.addImport(base, imports, temp.getName());
               }
            }
         }
      }

      for(final String nm : this.jarClassLoader.listOfDirectInternal(name))
      {
         this.fillImports(base, nm, imports, this.jarClassLoader.loadClass(nm));
      }
   }

   /**
    * Get class if already known
    * 
    * @param name
    *           Class name
    * @return The class or {@code null}
    */
   private Class<?> getAlreadyKnown(final String name)
   {
      final SoftReference<Class<?>> softReference = this.classes.get(name);
      if(softReference != null)
      {
         return softReference.get();
      }

      return null;
   }

   /**
    * Array level of a class
    * 
    * @param claz
    *           Class
    * @return Array level
    */
   private String getArrayLevel(final Class<?> claz)
   {
      if(claz == null)
      {
         return "";
      }

      int level = 0;

      Class<?> clazz = claz;
      while(clazz.isArray() == true)
      {
         clazz = clazz.getComponentType();
         level++;
      }

      return UtilText.repeat("[]", level);
   }

   /**
    * Root type of a class
    * 
    * @param claz
    *           Class
    * @return Root type or {@code null} for void
    */
   private Class<?> getRootType(final Class<?> claz)
   {
      if(claz == null)
      {
         return null;
      }

      Class<?> clazz = claz;
      while(clazz.isArray() == true)
      {
         clazz = clazz.getComponentType();
      }

      if((clazz.equals(Void.TYPE) == true) || (clazz.equals(Void.class) == true))
      {
         return null;
      }

      return clazz;
   }

   /**
    * Load class from a already compiled file
    * 
    * @param name
    *           File name
    * @param resolve
    *           Indicates if need to resolve
    * @return Loaded class or {@code null}
    */
   private Class<?> loadFromCompiledFile(final String name, final boolean resolve)
   {
      if(this.directory == null)
      {
         return null;
      }

      final File file = new File(this.directory, name.replace('.', File.separatorChar) + ".class");
      if(file.exists() == false)
      {
         return null;
      }

      try
      {
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

         InputStream inputStream = new FileInputStream(file);
         byte[] temp = new byte[4096];

         int read = inputStream.read(temp);
         while(read >= 0)
         {
            byteArrayOutputStream.write(temp, 0, read);

            read = inputStream.read(temp);
         }

         byteArrayOutputStream.flush();
         byteArrayOutputStream.close();
         inputStream.close();
         inputStream = null;

         temp = byteArrayOutputStream.toByteArray();
         byteArrayOutputStream = null;

         final Class<?> clazz = this.defineClass(name, temp, 0, temp.length);
         temp = null;

         if(resolve == true)
         {
            this.resolveClass(clazz);
         }

         return clazz;
      }
      catch(final Exception exception)
      {
         Debug.printException(exception);
      }

      return null;
   }

   /**
    * Load a class
    * 
    * @param name
    *           Class name
    * @param resolve
    *           Indicates if class need to be resolved
    * @return Loaded class
    * @throws ClassNotFoundException
    *            If class not found
    * @see ClassLoader#loadClass(String, boolean)
    */
   @Override
   protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException
   {
      Class<?> claz = this.getAlreadyKnown(name);
      if(claz != null)
      {
         return claz;
      }

      if((name.startsWith("java.") == true) || (name.startsWith("javax.") == true) || (name.startsWith("com.sun.") == true) || (name.startsWith("sun.") == true) || (name.startsWith("sunw.") == true))
      {
         try
         {
            claz = super.loadClass(name, resolve);
            if(claz != null)
            {
               this.classes.put(name, new SoftReference<Class<?>>(claz));

               return claz;
            }
         }
         catch(final Exception exception)
         {
         }
      }

      claz = this.loadFromCompiledFile(name, resolve);
      if(claz != null)
      {
         this.classes.put(name, new SoftReference<Class<?>>(claz));

         return claz;
      }

      final HashSet<NameCode> needCompile = new HashSet<NameCode>();
      this.compileClass(name, needCompile);

      try
      {
         this.directory = Compiler.compil(needCompile.toArray(new NameCode[needCompile.size()]));
      }
      catch(final Exception exception)
      {
         throw new ClassNotFoundException("Can't find/generate class : " + name, exception);
      }

      claz = this.loadFromCompiledFile(name, resolve);
      if(claz != null)
      {
         this.classes.put(name, new SoftReference<Class<?>>(claz));
         return claz;
      }

      throw new ClassNotFoundException("Not found : " + name);
   }
}