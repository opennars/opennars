package jhelp.util.resources;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Represents a resources of texts.<br>
 * Texts are describes in XML.<br>
 * Must have an XML per language, and a generic XML (by default). Each XML are : <code lang="xml"><!--
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
public final class ResourceText
{
   /** XML extension */
   private static final String                   XML = ".xml";
   /** Hash map of key, text */
   private final Hashtable<String, String>       keysText;
   /** Actual locale */
   private Locale                                locale;
   /** Resources reference */
   private final Resources                       resources;
   /** List of listener to alert in language change */
   private final ArrayList<ResourceTextListener> resourceTextListeners;
   /** Path of default XML, without .xml */
   private final String                          xmlReferencePathHeader;

   /**
    * Create a new instance of ResourceText
    * 
    * @param resources
    *           Resources reference
    * @param xmlReferencePathHeader
    *           Path of default XML, without .xml
    */
   ResourceText(final Resources resources, final String xmlReferencePathHeader)
   {
      this.resources = resources;
      this.xmlReferencePathHeader = xmlReferencePathHeader;
      this.resourceTextListeners = new ArrayList<ResourceTextListener>();
      this.keysText = new Hashtable<String, String>();
      this.setLocale(Locale.getDefault());
   }

   /**
    * Fill the hash map of key, text with the content of given resource
    * 
    * @param path
    *           Resource to parse
    */
   @SuppressWarnings("unused")
   private void fillKeysText(final String path)
   {
      final URL url = this.resources.obtainResourceURL(path);

      if(url == null)
      {
         return;
      }

      new ParserXMLText(this.keysText, url, path);
   }

   /**
    * Actual locale
    * 
    * @return Actual locale
    */
   public Locale getLocale()
   {
      return this.locale;
   }

   /**
    * Obtain a text
    * 
    * @param key
    *           Text key
    * @return Text itself
    */
   public String getText(final String key)
   {
      final String value = this.keysText.get(key);

      if(value == null)
      {
         return "/!\\ MISSING KEY /!\\ " + key + " /!\\ MISSING KEY /!\\";
      }

      return value;
   }

   /**
    * Path of default XML, without .xml
    * 
    * @return Path of default XML, without .xml
    */
   public String getXmlReferencePathHeader()
   {
      return this.xmlReferencePathHeader;
   }

   /**
    * Register a listener to be alert when language change
    * 
    * @param resourceTextListener
    *           Listener to register
    */
   public void register(final ResourceTextListener resourceTextListener)
   {
      if(resourceTextListener == null)
      {
         throw new NullPointerException("resourceTextListener musn't be null");
      }

      if(this.resourceTextListeners.contains(resourceTextListener) == false)
      {
         this.resourceTextListeners.add(resourceTextListener);
      }
   }

   /**
    * Change the language
    * 
    * @param locale
    *           New language
    */
   public void setLocale(final Locale locale)
   {
      if(locale == null)
      {
         throw new NullPointerException("locale musn't be null");
      }

      if(locale.equals(this.locale) == true)
      {
         return;
      }

      this.locale = locale;

      this.keysText.clear();

      this.fillKeysText(this.xmlReferencePathHeader + ResourceText.XML);
      this.fillKeysText(this.xmlReferencePathHeader + "_" + locale.getLanguage() + ResourceText.XML);
      this.fillKeysText(this.xmlReferencePathHeader + "_" + locale.getLanguage() + "_" + locale.getCountry() + ResourceText.XML);

      for(final ResourceTextListener resourceTextListener : this.resourceTextListeners)
      {
         resourceTextListener.resourceTextLanguageChanged(this);
      }
   }

   /**
    * Unregister a listener to be no more alert when language change
    * 
    * @param resourceTextListener
    *           Listener to unregister
    */
   public void unregister(final ResourceTextListener resourceTextListener)
   {
      this.resourceTextListeners.remove(resourceTextListener);
   }
}