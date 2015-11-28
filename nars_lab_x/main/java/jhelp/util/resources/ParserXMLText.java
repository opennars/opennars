package jhelp.util.resources;

import jhelp.util.debug.Debug;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;

/**
 * Parser of XML that describes a text resources for a language.<br>
 * The XML look like :<code lang="xml"><!--
 * <Texts>
 *    <Text key="textKey">
 *       The text isself
 *    </Text>
 * </Texts>
 * --></code> The markup <b><font color="#008800">"Text"</font></b> defines an association to a <font
 * color="#008800">"key"</font> with some text. The parameter <font color="#008800">"key"</font> specify the text key. The text
 * between opening and closing markup <b><font color="#008800">"Text"</font></b> is the corresponding text in the specific
 * language.
 * 
 * @author JHelp
 */
class ParserXMLText
      extends DefaultHandler
{
   /** Markup "Text" where describes the association key<->text */
   private static final String             MARKUP_TEXT   = "Text";
   /** Parameter "key" for specify the key */
   private static final String             PARAMETER_KEY = "key";
   /** Current key to associate with */
   private String                          key;
   /** Hash map to fill with associations */
   private final Hashtable<String, String> texts;
   /** Current text value */
   private StringBuilder                   value;

   /**
    * Create a new instance of ParserXMLText
    * 
    * @param texts
    *           Hash map to fill with associations
    * @param url
    *           Stream where find the XML to parse
    * @param reference
    *           Reference to the XML file (XML file identifier)
    */
   public ParserXMLText(final Hashtable<String, String> texts, final URL url, final String reference)
   {
      this.texts = texts;

      final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

      InputStream inputStream = null;

      try
      {
         inputStream = url.openStream();

         final SAXParser parser = parserFactory.newSAXParser();
         parser.parse(inputStream, this);
      }
      catch(final Exception exception)
      {
         Debug.printException(exception, "Issue while parsing XML of texts : ", reference);
      }
      finally
      {
         if(inputStream != null)
         {
            try
            {
               inputStream.close();
            }
            catch(final Exception exception)
            {
            }
         }
      }
   }

   /**
    * Called each time a part of a text is read <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param ch
    *           Array of read characters
    * @param start
    *           Starting offset where find the start of reading text
    * @param length
    *           Number of character read
    * @throws SAXException
    *            Not throw here
    * @see DefaultHandler#characters(char[], int, int)
    */
   @Override
   public void characters(final char[] ch, final int start, final int length) throws SAXException
   {
      if(this.value != null)
      {
         this.value.append(ch, start, length);
      }
   }

   /**
    * Called each time a end of markup arrive <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param uri
    *           URI source
    * @param localName
    *           Local name
    * @param qName
    *           Q-name
    * @throws SAXException
    *            Not throw here
    * @see DefaultHandler#endElement(String, String, String)
    */
   @Override
   public void endElement(final String uri, final String localName, final String qName) throws SAXException
   {
      final String name = ((localName == null) || (localName.length() == 0))
            ? qName
            : localName;

      if(ParserXMLText.MARKUP_TEXT.equals(name) == true)
      {
         this.texts.put(this.key, this.value.toString().trim().replace("\\n", "\n").replace("\\t", "     "));

         this.value = null;
      }
   }

   /**
    * Called each time a markup start <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param uri
    *           URI source
    * @param localName
    *           Local name
    * @param qName
    *           Q-name
    * @param attributes
    *           Parameters of the markup
    * @throws SAXException
    *            If the markup is "Text" and the parameter "key" is not present
    * @see DefaultHandler#startElement(String, String, String,
    *      Attributes)
    */
   @Override
   public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
   {
      final String name = ((localName == null) || (localName.length() == 0))
            ? qName
            : localName;

      if(ParserXMLText.MARKUP_TEXT.equals(name) == true)
      {
         this.key = attributes.getValue(ParserXMLText.PARAMETER_KEY);

         if(this.key == null)
         {
            throw new SAXException("Missing a key attribute inside a Text markup");
         }

         this.value = new StringBuilder();
      }
   }
}