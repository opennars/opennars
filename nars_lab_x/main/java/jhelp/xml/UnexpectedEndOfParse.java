/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : UnexpectedEndOfParse<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

/**
 * Exception when parsing end at wrong moment <br>
 * <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class UnexpectedEndOfParse
      extends ExceptionXML
{

   /** serialVersionUID */
   private static final long serialVersionUID = 283666531550640596L;

   /**
    * Constructs UnexpectedEndOfParse
    */
   public UnexpectedEndOfParse()
   {
   }

   /**
    * Constructs UnexpectedEndOfParse
    * 
    * @param message
    *           Message
    */
   public UnexpectedEndOfParse(final String message)
   {
      super(message);
   }

   /**
    * Constructs UnexpectedEndOfParse
    * 
    * @param message
    *           Message
    * @param cause
    *           Cause
    */
   public UnexpectedEndOfParse(final String message, final Throwable cause)
   {
      super(message, cause);
   }

   /**
    * Constructs UnexpectedEndOfParse
    * 
    * @param cause
    *           Cause
    */
   public UnexpectedEndOfParse(final Throwable cause)
   {
      super(cause);
   }
}