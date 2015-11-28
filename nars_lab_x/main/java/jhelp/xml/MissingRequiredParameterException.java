/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : MissingRequiredParameterException<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

/**
 * Indicates that a require parameter miss in Markup XML <br>
 * <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class MissingRequiredParameterException
      extends ExceptionXML
{
   /** serialVersionUID */
   private static final long serialVersionUID = -3394387170589763395L;

   /**
    * Constructs MissingRequiredParameterException
    * 
    * @param parameterName
    *           Name of missing parameter
    * @param markupName
    *           Markup name
    */
   public MissingRequiredParameterException(final String parameterName, final String markupName)
   {
      this(parameterName, markupName, null);
   }

   /**
    * Constructs MissingRequiredParameterException
    * 
    * @param parameterName
    *           Name of missing parameter
    * @param markupName
    *           Markup name
    * @param cause
    *           Cause of the exception
    */
   public MissingRequiredParameterException(final String parameterName, final String markupName, final Throwable cause)
   {
      super("The markup '" + markupName + "' requiered the parameter " + parameterName, cause);
   }
}