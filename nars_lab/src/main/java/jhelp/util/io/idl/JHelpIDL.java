package jhelp.util.io.idl;

import jhelp.util.classLoader.JHelpClassLoader;
import jhelp.util.compiler.Compiler;
import jhelp.util.compiler.Compiler.NameCode;
import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.io.Binarizable;
import jhelp.util.io.ByteArray;
import jhelp.util.io.UtilIO;
import jhelp.util.io.pipe.PipeReader;
import jhelp.util.io.pipe.PipeWriter;
import jhelp.util.list.SortedArray;
import jhelp.util.reflection.Reflector;
import jhelp.util.thread.MessageHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Way to call distant method betwwen two Java applications in same computer.<br>
 * Solution in 100% Java :)<br>
 * One and only one caller and one and only one receiver per registered interface.<br>
 * Firstly register the interfaces you want call distant.<br>
 * The implementation of the interface must be in receiver.<br>
 * Interfaces shared must respect following rules :<br>
 * 1) All refered class, interfaces, enum or exception must be know by sender and receiver.<br>
 * 2) methods return type or parameters MUST be primitives, java.lang.String, enum or {@link Binarizable}<br>
 * 3) can extends other interface only if they respect (2) and (3) rules<br>
 * 4) Must registered in same time all linked interfaces that will be exchange
 * 
 * @author JHelp
 */
public class JHelpIDL
{
   /** Answer extention */
   private static final String                  ANSWER       = "Answer";
   /** Class file ending */
   private static final String                  CLASS        = ".class";
   /** Class loader for loads dynamic created code */
   private static final JHelpClassLoader        CLASS_LOADER = new JHelpClassLoader(JHelpIDL.class.getClassLoader());
   /** Common extention */
   private static final String                  COMMON       = "Common";
   /** Compile code directory */
   private static final File                    COMPILE_DIRECTORY_BIN;
   /** IDL work directory */
   private static final File                    IDL_DIRECTORY;
   /** Already created instances */
   private static final HashMap<String, Object> INSTANCES    = new HashMap<String, Object>();
   /** Request extention */
   private static final String                  REQUEST      = "Request";
   /** Source extention */
   private static final String                  SOURCE       = "Source";
   /** Target extention */
   private static final String                  TARGET       = "Target";
   /** Pipe main directory (Where lies all pipes, see {@link PipeReader} and {@link PipeWriter} for know more about pipes */
   public static final File                     PIPE_DIRECTORY;

   static
   {
      final String dirSubPath = ".JHelp" + File.separator + ".jhelpidl";
      File directory = null;
      final String[] paths =
      {
         System.getProperty("user.home"), System.getProperty("user.dir"), System.getProperty("java.home"), UtilIO.obtainOutsideDirectory().getAbsolutePath()
      };

      for(final String path : paths)
      {
         if((path == null) || (path.trim().length() == 0))
         {
            continue;
         }

         directory = new File(path, dirSubPath);

         if(UtilIO.createDirectory(directory) == true)
         {
            break;
         }
      }

      IDL_DIRECTORY = directory;
      COMPILE_DIRECTORY_BIN = new File(directory, ".compilebin");
      PIPE_DIRECTORY = new File(directory, ".pipe");

      Debug.println(DebugLevel.VERBOSE, "IDL_DIRECTORY=", JHelpIDL.IDL_DIRECTORY.getAbsolutePath());
   }

   /**
    * Generate codes link to an interface
    * 
    * @param interfaceIDL
    *           Interface to get is classes
    * @return Classes description list
    */
   private static List<NameCode> createClassesForInterface(final Class<?> interfaceIDL)
   {
      if(interfaceIDL.isInterface() == false)
      {
         throw new IllegalArgumentException("The class " + interfaceIDL.getName() + " is not an interface");
      }

      final List<NameCode> codes = new ArrayList<NameCode>();

      final String packageName = interfaceIDL.getPackage().getName();
      final String simpleName = interfaceIDL.getSimpleName();
      final String complelteName = interfaceIDL.getName();
      StringBuilder code;
      String name;

      // Collect all need classes to import
      final SortedArray<String> needToImport = new SortedArray<String>(String.class, true);
      final Method[] methods = interfaceIDL.getMethods();
      Class<?> claz;
      for(final Method method : methods)
      {
         claz = method.getReturnType();

         if((claz != null) && (claz.isArray() == true))
         {
            claz = claz.getComponentType();
         }

         if((claz != null) && (claz.isPrimitive() == false) && (claz.getSimpleName().equalsIgnoreCase("void") == false))
         {
            needToImport.add(claz.getName());
         }

         for(final Class<?> classParam : method.getParameterTypes())
         {
            claz = classParam;

            if((claz != null) && (claz.isArray() == true))
            {
               claz = claz.getComponentType();
            }

            if((claz != null) && (claz.isPrimitive() == false) && (claz.getSimpleName().equalsIgnoreCase("void") == false))
            {
               needToImport.add(claz.getName());
            }
         }

         for(final Class<?> classParam : method.getExceptionTypes())
         {
            claz = classParam;

            if((claz != null) && (claz.isArray() == true))
            {
               claz = claz.getComponentType();
            }

            if((claz != null) && (claz.isPrimitive() == false) && (claz.getSimpleName().equalsIgnoreCase("void") == false))
            {
               needToImport.add(claz.getName());
            }
         }
      }

      // Create common interface
      code = new StringBuilder();
      name = complelteName + JHelpIDL.COMMON;

      code.append("package ");
      code.append(packageName);
      code.append(";\n\n");

      code.append("import java.io.File;\n");
      code.append("import jhelp.util.io.idl.JHelpIDL;\n");

      code.append("\npublic interface ");
      code.append(simpleName);
      code.append(JHelpIDL.COMMON);
      code.append("{\n");

      int value = 1;
      for(final Method method : methods)
      {
         code.append("   public static final int REQUEST_");
         code.append(method.getName());
         code.append(" = ");
         code.append(value);
         code.append(";\n");

         claz = method.getReturnType();
         if((claz != null) && (claz.getSimpleName().equalsIgnoreCase("void") == false))
         {
            code.append("   public static final int ANSWER_");
            code.append(method.getName());
            code.append(" = -");
            code.append(value);
            code.append(";\n");
         }

         value++;
      }

      code.append("   public static final File PIPE_DIRECTORY_REQUEST = new File(JHelpIDL.PIPE_DIRECTORY, \"");
      code.append(complelteName);
      code.append(JHelpIDL.REQUEST);
      code.append("\");\n");

      code.append("   public static final File PIPE_DIRECTORY_ANSWER = new File(JHelpIDL.PIPE_DIRECTORY, \"");
      code.append(complelteName);
      code.append(JHelpIDL.ANSWER);
      code.append("\");\n");

      code.append("}");

      Debug.printMark(DebugLevel.VERBOSE, name);
      Debug.println(DebugLevel.VERBOSE, code);
      codes.add(new NameCode(name, code.toString()));

      // Create request message
      code = new StringBuilder();
      name = complelteName + JHelpIDL.REQUEST;

      code.append("package ");
      code.append(packageName);
      code.append(";\n\n");

      code.append("import jhelp.util.io.ByteArray;\n");
      code.append("import jhelp.util.io.Binarizable;\n");
      for(final String needImport : needToImport)
      {
         code.append("import ");
         code.append(needImport);
         code.append(";\n");
      }

      code.append("\npublic class ");
      code.append(simpleName);
      code.append(JHelpIDL.REQUEST);
      code.append(" implements ");
      code.append(simpleName);
      code.append(JHelpIDL.COMMON);
      code.append(",Binarizable\n{\n");

      Class<?>[] parametersType;
      Class<?> type;
      int length;
      for(final Method method : methods)
      {
         code.append("   public static ");
         code.append(simpleName);
         code.append(JHelpIDL.REQUEST);
         code.append(" createRequest");
         code.append(method.getName());
         code.append("(");

         parametersType = method.getParameterTypes();
         length = parametersType.length;
         for(int index = 0; index < length; index++)
         {
            type = parametersType[index];

            code.append(type.getSimpleName());
            code.append(" p");
            code.append(index);

            if(index < (length - 1))
            {
               code.append(", ");
            }
         }

         code.append(")\n   {\n      ");
         code.append(simpleName);
         code.append(JHelpIDL.REQUEST);
         code.append(" request=new ");
         code.append(simpleName);
         code.append(JHelpIDL.REQUEST);
         code.append("();\n");
         code.append("      request.type = REQUEST_");
         code.append(method.getName());
         code.append(";\n");

         for(int index = 0; index < length; index++)
         {
            type = parametersType[index];

            code.append("      request.request.write");
            code.append(JHelpIDL.obtainEndByteArrayMethodName(type));
            code.append("(p");
            code.append(index);
            code.append(");\n");
         }

         code.append("      return request;\n   \n   }\n");
      }

      code.append("\n   public final ByteArray request;\n");
      code.append("   public int type;\n");

      code.append("   public ");
      code.append(simpleName);
      code.append(JHelpIDL.REQUEST);
      code.append("()\n   {\n      request=new ByteArray();\n   }\n");

      code.append("   public void parseBinary(ByteArray byteArray)\n   {\n      type=byteArray.readInteger();\n      request.clear();\n      request.write(byteArray.readByteArray());\n   }\n");
      code.append("   public void serializeBinary(ByteArray byteArray)\n   {\n      byteArray.writeInteger(type);\n      byteArray.writeByteArray(request.toArray());\n   }\n");

      code.append("}");

      Debug.printMark(DebugLevel.VERBOSE, name);
      Debug.println(DebugLevel.VERBOSE, code);
      codes.add(new NameCode(name, code.toString()));

      // Create answer message
      code = new StringBuilder();
      name = complelteName + JHelpIDL.ANSWER;

      code.append("package ");
      code.append(packageName);
      code.append(";\n\n");

      code.append("import jhelp.util.io.ByteArray;\n");
      code.append("import jhelp.util.io.Binarizable;\n");
      for(final String needImport : needToImport)
      {
         code.append("import ");
         code.append(needImport);
         code.append(";\n");
      }

      code.append("\npublic class ");
      code.append(simpleName);
      code.append(JHelpIDL.ANSWER);
      code.append(" implements ");
      code.append(simpleName);
      code.append(JHelpIDL.COMMON);
      code.append(",Binarizable\n{\n");

      Class<?> returnType;
      for(final Method method : methods)
      {
         returnType = method.getReturnType();
         if((returnType == null) || (returnType.getSimpleName().equalsIgnoreCase("void") == true))
         {
            continue;
         }

         code.append("   public static ");
         code.append(simpleName);
         code.append(JHelpIDL.ANSWER);
         code.append(" createAnswer");
         code.append(method.getName());
         code.append("(");
         code.append(returnType.getSimpleName());
         code.append(" p)\n   {\n      ");
         code.append(simpleName);
         code.append(JHelpIDL.ANSWER);
         code.append(" answer=new ");
         code.append(simpleName);
         code.append(JHelpIDL.ANSWER);
         code.append("();\n");
         code.append("      answer.type = ANSWER_");
         code.append(method.getName());
         code.append(";\n");
         code.append("      answer.answer.write");
         code.append(JHelpIDL.obtainEndByteArrayMethodName(returnType));
         code.append("(p);\n      return answer;\n   }\n");
      }

      code.append("\n   public final ByteArray answer;\n");
      code.append("   public int type;\n");

      code.append("   public ");
      code.append(simpleName);
      code.append(JHelpIDL.ANSWER);
      code.append("()\n   {\n      answer=new ByteArray();\n   }\n");

      code.append("   public void parseBinary(ByteArray byteArray)\n   {\n      type=byteArray.readInteger();\n      answer.clear();\n      answer.write(byteArray.readByteArray());\n   }\n");
      code.append("   public void serializeBinary(ByteArray byteArray)\n   {\n      byteArray.writeInteger(type);\n      byteArray.writeByteArray(answer.toArray());\n   }\n");

      code.append("}");

      Debug.printMark(DebugLevel.VERBOSE, name);
      Debug.println(DebugLevel.VERBOSE, code);
      codes.add(new NameCode(name, code.toString()));

      // Create source
      code = new StringBuilder();
      name = complelteName + JHelpIDL.SOURCE;

      code.append("package ");
      code.append(packageName);
      code.append(";");
      code.append('\n');
      code.append('\n');

      code.append("import ");
      code.append(complelteName);
      code.append(";");
      code.append('\n');
      code.append("import jhelp.util.io.ByteArray;\n");
      code.append("import jhelp.util.io.Binarizable;\n");
      code.append("import jhelp.util.io.idl.JHelpIDL;\n");
      code.append("import jhelp.util.io.pipe.PipeReader;\n");
      code.append("import jhelp.util.io.pipe.PipeWriter;\n");

      for(final String needImport : needToImport)
      {
         code.append("import ");
         code.append(needImport);
         code.append(";\n");
      }

      code.append('\n');

      code.append("public class ");
      code.append(simpleName);
      code.append(JHelpIDL.SOURCE);
      code.append(" implements ");
      code.append(simpleName);
      code.append(JHelpIDL.COMMON);
      code.append(", ");
      code.append(simpleName);
      code.append('\n');

      code.append("{");
      code.append('\n');

      code.append("   private final PipeReader pipeReaderAnswer = new PipeReader(PIPE_DIRECTORY_ANSWER);\n");
      code.append("   private final PipeWriter pipeWrterRequest = new PipeWriter(PIPE_DIRECTORY_REQUEST);\n");

      code.append("   public ");
      code.append(simpleName);
      code.append(JHelpIDL.SOURCE);
      code.append("()\n   {\n   }\n");

      Class<?>[] throwTypes;
      for(final Method method : methods)
      {
         returnType = method.getReturnType();
         code.append("   public ");
         code.append(returnType == null
               ? "void"
               : returnType.getSimpleName());
         code.append(" ");
         code.append(method.getName());
         code.append("(");

         parametersType = method.getParameterTypes();
         length = parametersType.length;
         for(int i = 0; i < length; i++)
         {
            type = parametersType[i];

            code.append(type.getSimpleName());
            code.append(" p");
            code.append(i);

            if(i < (length - 1))
            {
               code.append(", ");
            }
         }

         code.append(")");

         throwTypes = method.getExceptionTypes();
         length = throwTypes.length;
         if(length > 0)
         {
            code.append(" throws ");
            code.append(throwTypes[0].getSimpleName());

            for(int i = 1; i < length; i++)
            {
               code.append(", ");
               code.append(throwTypes[i].getSimpleName());
            }
         }

         code.append("\n   {\n      try\n      {\n         ");
         code.append(simpleName);
         code.append(JHelpIDL.REQUEST);
         code.append(" request = ");
         code.append(simpleName);
         code.append(JHelpIDL.REQUEST);
         code.append(".createRequest");
         code.append(method.getName());
         code.append("(");

         length = parametersType.length;
         for(int i = 0; i < length; i++)
         {
            code.append("p");
            code.append(i);

            if(i < (length - 1))
            {
               code.append(", ");
            }
         }

         code.append(");\n         pipeWrterRequest.write(request);\n");

         if((returnType != null) && (returnType.getSimpleName().equalsIgnoreCase("void") == false))
         {
            code.append("         ");
            code.append(simpleName);
            code.append(JHelpIDL.ANSWER);
            code.append(" answer = pipeReaderAnswer.read(");
            code.append(simpleName);
            code.append(JHelpIDL.ANSWER);
            code.append(".class);\n         return answer.answer.read");
            code.append(JHelpIDL.obtainEndByteArrayMethodName(returnType));
            code.append("(");

            if(JHelpIDL.needClassAsReadParameter(returnType) == true)
            {
               code.append(returnType.getSimpleName());
               code.append(".class");
            }

            code.append(");");
         }

         code.append("\n      }\n      catch(Exception exception)\n      {\n         throw new RuntimeException(\"Failed to do request : ");
         code.append(simpleName);
         code.append(".");
         code.append(method.getName());
         code.append("\",exception);\n      }\n   }\n");
      }

      code.append("}");

      Debug.printMark(DebugLevel.VERBOSE, name);
      Debug.println(DebugLevel.VERBOSE, code);
      codes.add(new NameCode(name, code.toString()));

      // Create target
      code = new StringBuilder();
      name = complelteName + JHelpIDL.TARGET;

      code.append("package ");
      code.append(packageName);
      code.append(";");
      code.append('\n');
      code.append('\n');

      code.append("import ");
      code.append(complelteName);
      code.append(";");
      code.append('\n');
      code.append("import jhelp.util.io.ByteArray;\n");
      code.append("import jhelp.util.io.Binarizable;\n");
      code.append("import jhelp.util.io.idl.JHelpIDL;\n");
      code.append("import jhelp.util.io.pipe.PipeReader;\n");
      code.append("import jhelp.util.io.pipe.PipeWriter;\n");
      code.append("import jhelp.util.thread.MessageHandler;\n");

      for(final String needImport : needToImport)
      {
         code.append("import ");
         code.append(needImport);
         code.append(";\n");
      }

      code.append('\n');

      code.append("public class ");
      code.append(simpleName);
      code.append(JHelpIDL.TARGET);
      code.append(" extends MessageHandler<");
      code.append(simpleName);
      code.append(JHelpIDL.REQUEST);
      code.append("> implements ");
      code.append(simpleName);
      code.append(JHelpIDL.COMMON);
      code.append('\n');

      code.append("{");
      code.append('\n');

      code.append("   private final static ");
      code.append(simpleName);
      code.append(JHelpIDL.REQUEST);
      code.append(" NEXT_READ = new ");
      code.append(simpleName);
      code.append(JHelpIDL.REQUEST);
      code.append("();\n");
      code.append("   private final PipeReader pipeReaderRequest = new PipeReader(PIPE_DIRECTORY_REQUEST);\n");
      code.append("   private final PipeWriter pipeWrterAnswer = new PipeWriter(PIPE_DIRECTORY_ANSWER);\n");
      code.append("   private final ");
      code.append(simpleName);
      code.append("   listener;\n");

      code.append("   public ");
      code.append(simpleName);
      code.append(JHelpIDL.TARGET);
      code.append("(");
      code.append(simpleName);
      code.append(" listener)\n   {\n      this.listener=listener;\n      postMessage(NEXT_READ);\n   }\n");

      code.append("   protected  void messageArrived(");
      code.append(simpleName);
      code.append(JHelpIDL.REQUEST);
      code.append(" message)\n   {\n       try\n      {\n         ");
      code.append(simpleName);
      code.append(JHelpIDL.REQUEST);
      code.append(" request=pipeReaderRequest.read(");
      code.append(simpleName);
      code.append(JHelpIDL.REQUEST);
      code.append(".class);\n         switch(request.type)\n         {\n");
      for(final Method method : methods)
      {
         code.append("            case REQUEST_");
         code.append(method.getName());
         code.append(" :\n            {\n");

         parametersType = method.getParameterTypes();
         length = parametersType.length;
         for(int i = 0; i < length; i++)
         {
            type = parametersType[i];

            code.append("               ");
            code.append(type.getSimpleName());
            code.append(" p");
            code.append(i);
            code.append(" = request.request.read");
            code.append(JHelpIDL.obtainEndByteArrayMethodName(type));
            code.append("(");

            if(JHelpIDL.needClassAsReadParameter(type) == true)
            {
               code.append(type.getSimpleName());
               code.append(".class");
            }

            code.append(");\n");
         }

         code.append("               ");
         returnType = method.getReturnType();
         if((returnType != null) && (returnType.getSimpleName().equalsIgnoreCase("void") == false))
         {
            code.append(returnType.getSimpleName());
            code.append(" r=");
         }

         code.append("listener.");
         code.append(method.getName());
         code.append("(");

         if(length > 0)
         {
            code.append("p0");

            for(int i = 1; i < length; i++)
            {
               code.append(", p");
               code.append(i);
            }
         }

         code.append(");\n");

         if((returnType != null) && (returnType.getSimpleName().equalsIgnoreCase("void") == false))
         {
            code.append("               ");
            code.append(simpleName);
            code.append(JHelpIDL.ANSWER);
            code.append(" answer = ");
            code.append(simpleName);
            code.append(JHelpIDL.ANSWER);
            code.append(".createAnswer");
            code.append(method.getName());
            code.append("(r);\n               pipeWrterAnswer.write(answer);\n");
         }

         code.append("            }\n            break;\n");
      }

      code.append("         }\n      }\n      catch(Exception exception)\n      {\n      }\n      postMessage(NEXT_READ);\n   }\n");

      code.append("   protected void willBeTerminated()\n   {\n      pipeReaderRequest.stopRead();\n   }\n}");

      Debug.printMark(DebugLevel.VERBOSE, name);
      Debug.println(DebugLevel.VERBOSE, code);
      codes.add(new NameCode(name, code.toString()));

      // End
      return codes;
   }

   /**
    * Indicates if have to add class name to get the stored value in {@link ByteArray}
    * 
    * @param type
    *           Type to read
    * @return {@code true} if have to add class name to get the stored value in {@link ByteArray}
    */
   private static boolean needClassAsReadParameter(final Class<?> type)
   {
      if(type.isPrimitive() == true)
      {
         return false;
      }

      if(type.isEnum() == true)
      {
         return true;
      }

      if(Reflector.isInherit(type, String.class) == true)
      {
         return false;
      }

      if(Reflector.isInherit(type, Binarizable.class) == true)
      {
         return true;
      }

      if(type.isArray() == false)
      {
         return false;
      }

      return JHelpIDL.needClassAsReadParameter(type.getComponentType());
   }

   /**
    * Obtain read or write method ending to read a value in {@link ByteArray}
    * 
    * @param type
    *           Type to read
    * @return Read or write extention
    */
   private static String obtainEndByteArrayMethodName(final Class<?> type)
   {
      if(type.isPrimitive() == true)
      {
         final String name = type.getName();

         if(Reflector.PRIMITIVE_BOOLEAN.equals(name) == true)
         {
            return "Boolean";
         }

         if(Reflector.PRIMITIVE_BYTE.equals(name) == true)
         {
            return "Byte";
         }

         if(Reflector.PRIMITIVE_CHAR.equals(name) == true)
         {
            return "Char";
         }

         if(Reflector.PRIMITIVE_DOUBLE.equals(name) == true)
         {
            return "Double";
         }

         if(Reflector.PRIMITIVE_FLOAT.equals(name) == true)
         {
            return "Float";
         }

         if(Reflector.PRIMITIVE_INT.equals(name) == true)
         {
            return "Integer";
         }

         if(Reflector.PRIMITIVE_LONG.equals(name) == true)
         {
            return "Long";
         }

         if(Reflector.PRIMITIVE_SHORT.equals(name) == true)
         {
            return "Short";
         }

         return null;
      }

      if(type.isEnum() == true)
      {
         return "Enum";
      }

      if(Reflector.isInherit(type, String.class) == true)
      {
         return "String";
      }

      if(Reflector.isInherit(type, Binarizable.class) == true)
      {
         return "Binarizable";
      }

      if(type.isArray() == false)
      {
         return null;
      }

      final String name = JHelpIDL.obtainEndByteArrayMethodName(type.getComponentType());

      if(name == null)
      {
         return null;
      }

      return name + "Array";
   }

   /**
    * Register several interfaces in same time.<br>
    * Remember to declare all shared linked interfaces in same time
    * 
    * @param interfacesIDL
    *           Interfaces to register
    * @throws IOException
    *            On registering issue
    */
   public static void declareIDLs(final Class<?>... interfacesIDL) throws IOException
   {
      final ArrayList<NameCode> codes = new ArrayList<NameCode>();

      for(final Class<?> interfaceIDL : interfacesIDL)
      {
         codes.addAll(JHelpIDL.createClassesForInterface(interfaceIDL));
      }

      Compiler.compil(JHelpIDL.COMPILE_DIRECTORY_BIN, codes.toArray(new NameCode[codes.size()]));

      for(final NameCode nameCode : codes)
      {
         JHelpIDL.CLASS_LOADER.add(new File(JHelpIDL.COMPILE_DIRECTORY_BIN, nameCode.getName().replace('.', File.separatorChar) + JHelpIDL.CLASS));
      }
   }

   /**
    * Indicates if a receiver is already registered for a given interface
    * 
    * @param <TYPE>
    *           Interface type
    * @param interfaceIDL
    *           Interface class
    * @return {@code true} if a receiver is already registered for a given interface
    */
   public static <TYPE> boolean hasReceiver(final Class<TYPE> interfaceIDL)
   {
      final String className = interfaceIDL.getName() + JHelpIDL.TARGET;
      return JHelpIDL.INSTANCES.get(className) != null;
   }

   /**
    * Obtain the sender instance for call distant methods for an interface
    * 
    * @param <TYPE>
    *           Interface type
    * @param interfaceIDL
    *           Interface class
    * @return Sender instance for call distant methods for an interface
    * @throws ClassNotFoundException
    *            If the interface is not registered
    */
   @SuppressWarnings("unchecked")
   public static <TYPE> TYPE obtainSenderInstance(final Class<TYPE> interfaceIDL) throws ClassNotFoundException
   {
      final String className = interfaceIDL.getName() + JHelpIDL.SOURCE;
      TYPE sender = (TYPE) JHelpIDL.INSTANCES.get(className);

      if(sender == null)
      {
         sender = (TYPE) Reflector.newInstance(className, JHelpIDL.CLASS_LOADER);
         JHelpIDL.INSTANCES.put(className, sender);
      }

      return sender;
   }

   /**
    * Link a receiver to an interface.<br>
    * It a way to say what to answer when receiver message.<br>
    * For one interface, only one receiver is allowed, have to call {@link #stopReceiver(Class)} for be able change to
    * registered object
    * 
    * @param <TYPE>
    *           Interface type
    * @param interfaceIDLImplementation
    *           Given implementation
    * @param interfaceIDL
    *           Interface class
    * @throws ClassNotFoundException
    *            If interface not registered
    * @throws NoSuchMethodException
    *            If registeration had previously issue
    * @throws SecurityException
    *            If the interface targetted is protected by security form refelection
    * @throws InstantiationException
    *            If registeration had previously issue
    * @throws IllegalAccessException
    *            If the interface targetted is protected by security form refelection
    * @throws IllegalArgumentException
    *            If the implementation given is wrong type
    * @throws InvocationTargetException
    *            If the interface targetted is protected by security form refelection or registeration had previously issue
    */
   @SuppressWarnings("unchecked")
   public static <TYPE> void registerReceiver(final TYPE interfaceIDLImplementation, final Class<TYPE> interfaceIDL) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
         IllegalAccessException, IllegalArgumentException, InvocationTargetException
   {
      if(interfaceIDLImplementation == null)
      {
         throw new NullPointerException("interfaceIDLImplementation musn't be null");
      }

      final String className = interfaceIDL.getName() + JHelpIDL.TARGET;
      TYPE receiver = (TYPE) JHelpIDL.INSTANCES.get(className);

      if(receiver != null)
      {
         throw new IllegalStateException("A receiver is already registered !");
      }

      final Class<?> claz = JHelpIDL.CLASS_LOADER.loadClass(className);
      final Constructor<?> constructor = claz.getConstructor(interfaceIDL);
      receiver = (TYPE) constructor.newInstance(interfaceIDLImplementation);

      JHelpIDL.INSTANCES.put(className, receiver);
   }

   /**
    * Unregister previous receiver for an interface, to allow to register an other one and/or stop properly linked threads<br>
    * Do nothing if no receiver is rgister for the interface
    * 
    * @param <TYPE>
    *           Interface type
    * @param interfaceIDL
    *           Interface class
    */
   @SuppressWarnings("unchecked")
   public static <TYPE> void stopReceiver(final Class<TYPE> interfaceIDL)
   {
      final String className = interfaceIDL.getName() + JHelpIDL.TARGET;
      final TYPE receiver = (TYPE) JHelpIDL.INSTANCES.get(className);

      if(receiver == null)
      {
         return;
      }

      ((MessageHandler<?>) receiver).terminate();
      JHelpIDL.INSTANCES.remove(className);
   }
}