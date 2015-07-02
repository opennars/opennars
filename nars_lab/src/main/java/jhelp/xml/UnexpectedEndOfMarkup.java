/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : UnexpectedEndOfMarkup<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

/**
 * Signal that a end of markup append, at wrong moment<br>
 * <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class UnexpectedEndOfMarkup
      extends ExceptionXML
{
   /** serialVersionUID */
   private static final long serialVersionUID = -5176669198228983758L;

   /**
    * Constructs UnexpectedEndOfMarkup
    */
   public UnexpectedEndOfMarkup()
   {
   }

   /**
    * Constructs UnexpectedEndOfMarkup
    * 
    * @param message
    *           Message
    */
   public UnexpectedEndOfMarkup(final String message)
   {
      super(message);
   }

   /**
    * Constructs UnexpectedEndOfMarkup
    * 
    * @param message
    *           Message
    * @param cause
    *           Cause
    */
   public UnexpectedEndOfMarkup(final String message, final Throwable cause)
   {
      super(message, cause);
   }

   /**
    * Constructs UnexpectedEndOfMarkup
    * 
    * @param cause
    *           Cause
    */
   public UnexpectedEndOfMarkup(final Throwable cause)
   {
      super(cause);
   }
}