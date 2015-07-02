/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : ParseXMLlistener<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

import java.util.Hashtable;

/**
 * Listener of parse XML<br>
 * <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public interface ParseXMLlistener
{
   /**
    * Call when comment found
    * 
    * @param comment
    *           Comment find
    */
   public void commentFind(String comment);

   /**
    * Call when a end of markup meet
    * 
    * @param markupName
    *           Markup name
    * @throws ExceptionXML
    *            On parsing issue. (Usually if markup can't aend now, see {@link UnexpectedEndOfMarkup})
    */
   public void endMarkup(String markupName) throws ExceptionXML;

   /**
    * Call when parsing end
    * 
    * @throws ExceptionXML
    *            On XML parsing issue (Usually when meet an unexpected end of parse. See {@link UnexpectedEndOfParse})
    */
   public void endParse() throws ExceptionXML;

   /**
    * Call when exception happen that force the parsing to end
    * 
    * @param exceptionParseXML
    *           Exception cause
    */
   public void exceptionForceEndParse(ExceptionParseXML exceptionParseXML);

   /**
    * Call when start of markup meet.<br>
    * You can use {@link ParserXML#obtainBoolean(String, Hashtable, String, boolean, boolean)},
    * {@link ParserXML#obtainInteger(String, Hashtable, String, boolean, int)} and
    * {@link ParserXML#obtainParameter(String, Hashtable, String, boolean)} as helpers to obtain parameter value
    * 
    * @param markupName
    *           Markup name
    * @param parameters
    *           Markup parameters
    * @throws ExceptionXML
    *            On XML parsing issue (Usually if a mandatory parameter missing, or a parameter value is invalid. See
    *            {@link MissingRequiredParameterException} and {@link InvalidParameterValueException})
    */
   public void startMakup(String markupName, Hashtable<String, String> parameters) throws ExceptionXML;

   /**
    * Call when parsing start
    */
   public void startParse();

   /**
    * Call when a text meet
    * 
    * @param text
    *           Text found
    * @throws ExceptionXML
    *            On XML parsing issue (Usually when text is not valid. See {@link InvalidTextException})
    */
   public void textFind(String text) throws ExceptionXML;
}