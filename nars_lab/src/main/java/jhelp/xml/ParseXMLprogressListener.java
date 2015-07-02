/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : ParseXMLerror<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

/**
 * Listener to follow the parsing progression<br>
 * <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public interface ParseXMLprogressListener
{
   /**
    * Call when meet an open of markup
    * 
    * @param markupName
    *           Markup name
    * @param line
    *           Current line in XML
    * @param column
    *           Current column in XML
    */
   public void findOpenMarkup(String markupName, int line, int column);

   /**
    * Call when meet a close of markup
    * 
    * @param markupName
    *           Markup name
    * @param line
    *           Current line in XML
    * @param column
    *           Current column in XML
    */
   public void findCloseMarkup(String markupName, int line, int column);

   /**
    * Call when meet text in markup
    * 
    * @param markupName
    *           Markup name
    * @param text
    *           Text find
    * @param line
    *           Current line in XML
    * @param column
    *           Current column in XML
    */
   public void findTextMarkup(String markupName, String text, int line, int column);

   /**
    * Call when find parameter in markup
    * 
    * @param markupName
    *           Markup name
    * @param parameterName
    *           Parameter name
    * @param line
    *           Current line in XML
    * @param column
    *           Current column in XML
    */
   public void findParameter(String markupName, String parameterName, int line, int column);

   /**
    * Call when parsing cause an exception
    * 
    * @param exceptionParseXML
    *           Exception cause
    */
   public void exceptionAppend(ExceptionParseXML exceptionParseXML);
}