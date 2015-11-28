/**
 * Project : InjectionExemples<br>
 * Package : jhelp.injection.dynamic<br>
 * Class : JavaSourceFromString<br>
 * Date : 24 nov. 2009<br>
 * By JHelp
 */
package jhelp.util.compiler;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * Form Sun Javadoc of {@link javax.tools#JavaCompiler} : <br>
 * A file object used to represent source coming from a string. <br>
 * <br>
 * Last modification : 24 nov. 2009<br>
 * Version 0.0.0<br>
 */
public class JavaSourceFromString
      extends SimpleJavaFileObject
{
   /**
    * The source code of this "file".
    */
   final String code;

   /**
    * Constructs a new JavaSourceFromString.
    * 
    * @param name
    *           the name of the compilation unit represented by this file object
    * @param code
    *           the source code for the compilation unit represented by this file object
    */
   public JavaSourceFromString(String name, String code)
   {
      super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
      this.code = code;
   }

   /**
    * Source code
    * 
    * @param ignoreEncodingErrors
    *           Ignore here
    * @return Source code
    * @see SimpleJavaFileObject#getCharContent(boolean)
    */
   @Override
   public CharSequence getCharContent(boolean ignoreEncodingErrors)
   {
      return this.code;
   }
}