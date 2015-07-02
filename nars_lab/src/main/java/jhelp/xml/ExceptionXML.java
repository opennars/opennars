/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : ExceptionXML<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

/**
 * Exception can be happen in XML <br>
 * <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class ExceptionXML
      extends Exception
{
   /** serialVersionUID */
   private static final long serialVersionUID = 6923267169098778590L;

   /**
    * Constructs ExceptionXML
    */
   public ExceptionXML()
   {
   }

   /**
    * Constructs ExceptionXML
    * 
    * @param message
    *           Message
    */
   public ExceptionXML(final String message)
   {
      super(message);
   }

   /**
    * Constructs ExceptionXML
    * 
    * @param message
    *           Message
    * @param cause
    *           Cause
    */
   public ExceptionXML(final String message, final Throwable cause)
   {
      super(message, cause);
   }

   /**
    * Constructs ExceptionXML
    * 
    * @param cause
    *           Cause
    */
   public ExceptionXML(final Throwable cause)
   {
      super(cause);
   }
}