/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : DefaultParseXMLlistener<br>
 * Date : 22 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

import jhelp.util.debug.Debug;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Default parser XML listener.<br>
 * It complete while reading in a {@link MarkupXML}<br>
 * <br>
 * Last modification : 22 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class DefaultParseXMLlistener
      implements ParseXMLlistener
{
   /** Read markup XML */
   private MarkupXML markupXML;
   /** Temporary markup */
   private MarkupXML temporary;
   /** Temporary parent */
   private MarkupXML temporaryParent;

   /**
    * Constructs DefaultParseXMLlistener
    */
   public DefaultParseXMLlistener()
   {
   }

   /**
    * Call when comment find
    * 
    * @param comment
    *           Comment
    * @see ParseXMLlistener#commentFind(String)
    */
   @Override
   public void commentFind(final String comment)
   {
   }

   /**
    * Call when read a end of markup
    * 
    * @param markupName
    *           Markup name
    * @throws UnexpectedEndOfMarkup
    *            If end of is markup append at wrong moment
    * @see ParseXMLlistener#endMarkup(String)
    */
   @Override
   public void endMarkup(final String markupName) throws UnexpectedEndOfMarkup
   {
      if(this.temporaryParent != null)
      {
         this.temporary = this.temporaryParent;
         this.temporaryParent = this.temporary.getParent();
      }
   }

   /**
    * Call when parse if finish
    * 
    * @throws UnexpectedEndOfParse
    *            If parse can't finish now
    * @see ParseXMLlistener#endParse()
    */
   @Override
   public void endParse() throws UnexpectedEndOfParse
   {
      this.temporary = null;
      this.temporaryParent = null;
   }

   /**
    * Call when an exception force the end of parse
    * 
    * @param exceptionParseXML
    *           Exception append
    * @see ParseXMLlistener#exceptionForceEndParse(ExceptionParseXML)
    */
   @Override
   public void exceptionForceEndParse(final ExceptionParseXML exceptionParseXML)
   {
      Debug.printException(exceptionParseXML);

      this.temporary = null;
      this.temporaryParent = null;
   }

   /**
    * Read markup
    * 
    * @return Read markup
    */
   public MarkupXML getMarkupXML()
   {
      return this.markupXML;
   }

   /**
    * Call when read a start of markup
    * 
    * @param markupName
    *           Markup name
    * @param parameters
    *           Parameters found in markup
    * @throws MissingRequiredParameterException
    *            If a required parameter missing
    * @throws InvalidParameterValueException
    *            If a parameter have an invalid value
    * @see ParseXMLlistener#startMakup(String, Hashtable)
    */
   @Override
   public void startMakup(final String markupName, final Hashtable<String, String> parameters) throws MissingRequiredParameterException, InvalidParameterValueException
   {
      if(this.markupXML == null)
      {
         this.temporary = this.markupXML = new MarkupXML(markupName);
      }
      else
      {
         this.temporaryParent = this.temporary;
         this.temporary = new MarkupXML(markupName);
      }
      if(this.temporaryParent != null)
      {
         this.temporaryParent.addChild(this.temporary);
      }
      //
      final Enumeration<String> keys = parameters.keys();
      String key;
      while(keys.hasMoreElements() == true)
      {
         key = keys.nextElement();
         this.temporary.addParameter(key, parameters.get(key));
      }
   }

   /**
    * Call on start parsing
    * 
    * @see ParseXMLlistener#startParse()
    */
   @Override
   public void startParse()
   {
      this.markupXML = null;
      this.temporary = null;
      this.temporaryParent = null;
   }

   /**
    * Call when text find
    * 
    * @param text
    *           Text find
    * @throws InvalidTextException
    *            If text is not valid
    * @see ParseXMLlistener#textFind(String)
    */
   @Override
   public void textFind(final String text) throws InvalidTextException
   {
      this.temporary.setText(text);
   }
}