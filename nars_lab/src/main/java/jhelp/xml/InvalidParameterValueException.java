/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : InvalidParameterValueException<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

import jhelp.util.text.UtilText;

/**
 * Exception if a parameter is not valid <br>
 * <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class InvalidParameterValueException
      extends ExceptionXML
{
   /** serialVersionUID */
   private static final long serialVersionUID = 1780708232370499648L;

   /**
    * Constructs InvalidParameterValueException
    * 
    * @param parameterName
    *           Parameter name
    * @param markupName
    *           Markup where append
    */
   public InvalidParameterValueException(final String parameterName, final String markupName)
   {
      this(parameterName, markupName, null, null);
   }

   /**
    * Constructs InvalidParameterValueException
    * 
    * @param parameterName
    *           Parameter name
    * @param markupName
    *           Markup where append
    * @param message
    *           Message
    */
   public InvalidParameterValueException(final String parameterName, final String markupName, final String message)
   {
      this(parameterName, markupName, message, null);
   }

   /**
    * Constructs InvalidParameterValueException
    * 
    * @param parameterName
    *           Parameter name
    * @param markupName
    *           Markup where append
    * @param message
    *           Message
    * @param cause
    *           Cause
    */
   public InvalidParameterValueException(final String parameterName, final String markupName, final String message, final Throwable cause)
   {
      super(UtilText.concatenate("The parameter ", parameterName, " in the markup ", markupName, " have invalid value", (message == null
            ? ""
            : " : " + message)), cause);
   }

   /**
    * Constructs InvalidParameterValueException
    * 
    * @param parameterName
    *           Parameter name
    * @param markupName
    *           Markup where append
    * @param cause
    *           Cause
    */
   public InvalidParameterValueException(final String parameterName, final String markupName, final Throwable cause)
   {
      this(parameterName, markupName, null, cause);
   }
}