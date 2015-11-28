/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : InvalidTextException<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

/**
 * Exception if a text is not valid <br>
 * <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class InvalidTextException
      extends ExceptionXML
{
   /** serialVersionUID */
   private static final long serialVersionUID = 4289961699877912162L;

   /**
    * Constructs InvalidTextException
    */
   public InvalidTextException()
   {
   }

   /**
    * Constructs InvalidTextException
    * 
    * @param message
    *           Message
    */
   public InvalidTextException(final String message)
   {
      super(message);
   }

   /**
    * Constructs InvalidTextException
    * 
    * @param message
    *           Message
    * @param cause
    *           Cause
    */
   public InvalidTextException(final String message, final Throwable cause)
   {
      super(message, cause);
   }

   /**
    * Constructs InvalidTextException
    * 
    * @param cause
    *           Cause
    */
   public InvalidTextException(final Throwable cause)
   {
      super(cause);
   }
}