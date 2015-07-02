/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : DefaultParseXMLprogressListener<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

import jhelp.util.debug.Debug;

/**
 * XML parser progress listener by default <br>
 * <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class DefaultParseXMLprogressListener
      implements ParseXMLprogressListener
{

   /**
    * Constructs DefaultParseXMLprogressListener
    */
   public DefaultParseXMLprogressListener()
   {
   }

   /**
    * Call when exception append
    * 
    * @param exceptionParseXML
    *           Exception
    * @see ParseXMLprogressListener#exceptionAppend(ExceptionParseXML)
    */
   @Override
   public void exceptionAppend(final ExceptionParseXML exceptionParseXML)
   {
      Debug.printException(exceptionParseXML);
   }

   /**
    * Call when a markup close meet
    * 
    * @param markupName
    *           Markup name
    * @param line
    *           Line of meeting
    * @param column
    *           Column of meeting
    * @see ParseXMLprogressListener#findCloseMarkup(String, int, int)
    */
   @Override
   public void findCloseMarkup(final String markupName, final int line, final int column)
   {
   }

   /**
    * Call when a markup open meet
    * 
    * @param markupName
    *           Markup name
    * @param line
    *           Line of meeting
    * @param column
    *           Column of meeting
    * @see ParseXMLprogressListener#findOpenMarkup(String, int, int)
    */
   @Override
   public void findOpenMarkup(final String markupName, final int line, final int column)
   {
   }

   /**
    * Call when parameter meet
    * 
    * @param markupName
    *           Markup name
    * @param parameterName
    *           Parameter name
    * @param line
    *           Line
    * @param column
    *           Column
    * @see ParseXMLprogressListener#findParameter(String, String, int, int)
    */
   @Override
   public void findParameter(final String markupName, final String parameterName, final int line, final int column)
   {
   }

   /**
    * Call when meet a text
    * 
    * @param markupName
    *           Markup name
    * @param text
    *           Text
    * @param line
    *           Line
    * @param column
    *           Column
    * @see ParseXMLprogressListener#findTextMarkup(String, String, int, int)
    */
   @Override
   public void findTextMarkup(final String markupName, final String text, final int line, final int column)
   {
   }
}