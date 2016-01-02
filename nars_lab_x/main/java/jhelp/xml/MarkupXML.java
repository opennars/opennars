/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : MarkupXML<br>
 * Date : 22 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

import jhelp.util.list.EnumerationIterator;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Markup XML<br>
 * <br>
 * Last modification : 22 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class MarkupXML
{
   /**
    * Print characters before write a line
    * 
    * @param printStream
    *           Where write
    * @param decal
    *           Header length to write
    */
   private static void printDecal(final PrintStream printStream, final int decal)
   {
      for(int i = 0; i < decal; i++)
      {
         printStream.print('\t');
      }
   }

   /**
    * Load XML
    * 
    * @param inputStream
    *           Stream to read
    * @return Loaded XML
    * @throws IOException
    *            On reading problem
    * @throws ExceptionParseXML
    *            On parsing problem
    */
   public static MarkupXML load(final InputStream inputStream) throws IOException, ExceptionParseXML
   {
      final DefaultParseXMLlistener defaultParseXMLlistener = new DefaultParseXMLlistener();

      final ParserXML parserXML = new ParserXML();
      parserXML.parse(defaultParseXMLlistener, inputStream);

      return defaultParseXMLlistener.getMarkupXML();
   }

   /** Markup children */
   private final ArrayList<MarkupXML>      children;
   /** Index in parent */
   private int                             index;
   /** Markup name */
   private final String                    name;
   /** Markup parameters */
   private final Hashtable<String, String> parameters;
   /** Markup parent */
   private MarkupXML                       parent;

   /** Text associate */
   private String                          text;

   /**
    * Constructs MarkupXML
    * 
    * @param name
    *           Markup name
    */
   public MarkupXML(final String name)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }
      this.name = name;
      this.parameters = new Hashtable<String, String>();
      this.children = new ArrayList<MarkupXML>();
      this.text = "";
   }

   /**
    * The "next" XML after an index
    * 
    * @param index
    *           Index start
    * @return "Next" Markup or {@code null} if no next
    */
   private MarkupXML getNext(final int index)
   {
      if(this.children.size() > index)
      {
         return this.children.get(index);
      }
      if(this.parent == null)
      {
         return null;
      }
      return this.parent.getNext(this.index + 1);
   }

   /**
    * Write XML to stream
    * 
    * @param printStream
    *           Stream where write
    * @param decal
    *           Header line level
    */
   private void println(final PrintStream printStream, final int decal)
   {
      MarkupXML.printDecal(printStream, decal);
      printStream.print('<');
      printStream.print(this.name);
      final Enumeration<String> keys = this.parameters.keys();
      String key;
      while(keys.hasMoreElements() == true)
      {
         key = keys.nextElement();
         printStream.print(' ');
         printStream.print(key);
         printStream.print("=\"");
         printStream.print(this.parameters.get(key));
         printStream.print('"');
      }
      key = this.text == null
            ? ""
            : this.text.trim();
      if((key.length() == 0) && (this.children.isEmpty() == true))
      {
         if(decal >= 0)
         {
            printStream.println("/>");
         }
         else
         {
            printStream.print("/>");
         }
         return;
      }
      if(decal >= 0)
      {
         printStream.println('>');
      }
      else
      {
         printStream.print('>');
      }
      if(key.length() > 0)
      {
         MarkupXML.printDecal(printStream, decal + 1);
         if(decal >= 0)
         {
            printStream.println(key);
         }
         else
         {
            printStream.print(key);
         }
      }
      for(final MarkupXML child : this.children)
      {
         child.println(printStream, decal + 1);
      }
      MarkupXML.printDecal(printStream, decal);
      printStream.print("</");
      printStream.print(this.name);
      if(decal >= 0)
      {
         printStream.println('>');
      }
      else
      {
         printStream.print('>');
      }
   }

   /**
    * Add a child
    * 
    * @param child
    *           Child to add
    */
   public void addChild(final MarkupXML child)
   {
      if(child == null)
      {
         throw new NullPointerException("child musn't be null");
      }
      if(child.parent == this)
      {
         return;
      }
      if(child.parent != null)
      {
         child.parent.children.remove(child);
      }
      child.parent = this;
      child.index = this.children.size();
      this.children.add(child);
   }

   /**
    * Add/change parameter
    * 
    * @param key
    *           Parameter key
    * @param value
    *           Parameter value
    */
   public void addParameter(final String key, final boolean value)
   {
      this.addParameter(key, String.valueOf(value));
   }

   /**
    * Add/change parameter
    * 
    * @param key
    *           Parameter key
    * @param value
    *           Parameter value
    */
   public void addParameter(final String key, final byte value)
   {
      this.addParameter(key, String.valueOf(value));
   }

   /**
    * Add/change parameter
    * 
    * @param key
    *           Parameter key
    * @param value
    *           Parameter value
    */
   public void addParameter(final String key, final char value)
   {
      this.addParameter(key, String.valueOf(value));
   }

   /**
    * Add/change parameter
    * 
    * @param key
    *           Parameter key
    * @param value
    *           Parameter value
    */
   public void addParameter(final String key, final double value)
   {
      this.addParameter(key, String.valueOf(value));
   }

   /**
    * Add/change parameter
    * 
    * @param key
    *           Parameter key
    * @param value
    *           Parameter value
    */
   public void addParameter(final String key, final float value)
   {
      this.addParameter(key, String.valueOf(value));
   }

   /**
    * Add/change parameter
    * 
    * @param key
    *           Parameter key
    * @param value
    *           Parameter value
    */
   public void addParameter(final String key, final int value)
   {
      this.addParameter(key, String.valueOf(value));
   }

   /**
    * Add/change parameter
    * 
    * @param key
    *           Parameter key
    * @param value
    *           Parameter value
    */
   public void addParameter(final String key, final long value)
   {
      this.addParameter(key, String.valueOf(value));
   }

   /**
    * Add/change parameter
    * 
    * @param key
    *           Parameter key
    * @param value
    *           Parameter value
    */
   public void addParameter(final String key, final short value)
   {
      this.addParameter(key, String.valueOf(value));
   }

   /**
    * Add/change parameter
    * 
    * @param key
    *           Parameter key
    * @param value
    *           Parameter value
    */
   public void addParameter(final String key, final String value)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }
      if(value == null)
      {
         throw new NullPointerException("value musn't be null");
      }
      this.parameters.put(key, value);
   }

   /**
    * Number of children
    * 
    * @return Number of children
    */
   public int childrenCount()
   {
      return this.children.size();
   }

   /**
    * Markup child
    * 
    * @param index
    *           Child index
    * @return Markup child
    */
   public MarkupXML getChild(final int index)
   {
      return this.children.get(index);
   }

   /**
    * Return name
    * 
    * @return name
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * The "next" XML markup
    * 
    * @return "Next" XML markup
    */
   public MarkupXML getNext()
   {
      return this.getNext(0);
   }

   /**
    * Return parent
    * 
    * @return parent
    */
   public MarkupXML getParent()
   {
      return this.parent;
   }

   /**
    * Return text
    * 
    * @return text
    */
   public String getText()
   {
      return this.text;
   }

   /**
    * Indicate if a key is a parameter
    * 
    * @param key
    *           Key tested
    * @return {@code true} if the key is a parameter
    */
   public boolean isParameter(final String key)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }
      return this.parameters.containsKey(key);
   }

   /**
    * List of children
    * 
    * @return List of children
    */
   public EnumerationIterator<MarkupXML> obtainChildren()
   {
      return new EnumerationIterator<MarkupXML>(this.children.iterator());
   }

   /**
    * List of children with the given name
    * 
    * @param name
    *           Name search
    * @return List of children with the given name
    */
   public EnumerationIterator<MarkupXML> obtainChildren(final String name)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }
      final ArrayList<MarkupXML> arrayList = new ArrayList<MarkupXML>();
      for(final MarkupXML child : this.children)
      {
         if(child.name.equals(name) == true)
         {
            arrayList.add(child);
         }
      }
      return new EnumerationIterator<MarkupXML>(arrayList.iterator());
   }

   /**
    * Obtain a parameter
    * 
    * @param key
    *           Parameter key
    * @return Value or {@code null} if parameter not found
    */
   public String obtainParameter(final String key)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }
      return this.parameters.get(key);
   }

   /**
    * Obtain parameter
    * 
    * @param key
    *           Parameter key
    * @param defaultValue
    *           Default value if parameter not exists
    * @return Parameter value
    */
   public boolean obtainParameter(final String key, final boolean defaultValue)
   {
      final String value = this.obtainParameter(key);
      if(value == null)
      {
         return defaultValue;
      }
      try
      {
         return Boolean.parseBoolean(value);
      }
      catch(final Exception exception)
      {
         return defaultValue;
      }
   }

   /**
    * Obtain parameter
    * 
    * @param key
    *           Parameter key
    * @param defaultValue
    *           Default value if parameter not exists
    * @return Parameter value
    */
   public byte obtainParameter(final String key, final byte defaultValue)
   {
      final String value = this.obtainParameter(key);
      if(value == null)
      {
         return defaultValue;
      }
      try
      {
         return Byte.parseByte(value);
      }
      catch(final Exception exception)
      {
         return defaultValue;
      }
   }

   /**
    * Obtain parameter
    * 
    * @param key
    *           Parameter key
    * @param defaultValue
    *           Default value if parameter not exists
    * @return Parameter value
    */
   public char obtainParameter(final String key, final char defaultValue)
   {
      final String value = this.obtainParameter(key);
      if(value == null)
      {
         return defaultValue;
      }
      try
      {
         return value.charAt(0);
      }
      catch(final Exception exception)
      {
         return defaultValue;
      }
   }

   /**
    * Obtain parameter
    * 
    * @param key
    *           Parameter key
    * @param defaultValue
    *           Default value if parameter not exists
    * @return Parameter value
    */
   public double obtainParameter(final String key, final double defaultValue)
   {
      final String value = this.obtainParameter(key);
      if(value == null)
      {
         return defaultValue;
      }
      try
      {
         return Double.parseDouble(value);
      }
      catch(final Exception exception)
      {
         return defaultValue;
      }
   }

   /**
    * Obtain parameter
    * 
    * @param key
    *           Parameter key
    * @param defaultValue
    *           Default value if parameter not exists
    * @return Parameter value
    */
   public float obtainParameter(final String key, final float defaultValue)
   {
      final String value = this.obtainParameter(key);
      if(value == null)
      {
         return defaultValue;
      }
      try
      {
         return Float.parseFloat(value);
      }
      catch(final Exception exception)
      {
         return defaultValue;
      }
   }

   /**
    * Obtain parameter
    * 
    * @param key
    *           Parameter key
    * @param defaultValue
    *           Default value if parameter not exists
    * @return Parameter value
    */
   public int obtainParameter(final String key, final int defaultValue)
   {
      final String value = this.obtainParameter(key);
      if(value == null)
      {
         return defaultValue;
      }
      try
      {
         return Integer.parseInt(value);
      }
      catch(final Exception exception)
      {
         return defaultValue;
      }
   }

   /**
    * Obtain parameter
    * 
    * @param key
    *           Parameter key
    * @param defaultValue
    *           Default value if parameter not exists
    * @return Parameter value
    */
   public long obtainParameter(final String key, final long defaultValue)
   {
      final String value = this.obtainParameter(key);
      if(value == null)
      {
         return defaultValue;
      }
      try
      {
         return Long.parseLong(value);
      }
      catch(final Exception exception)
      {
         return defaultValue;
      }
   }

   /**
    * Obtain parameter
    * 
    * @param key
    *           Parameter key
    * @param defaultValue
    *           Default value if parameter not exists
    * @return Parameter value
    */
   public short obtainParameter(final String key, final short defaultValue)
   {
      final String value = this.obtainParameter(key);
      if(value == null)
      {
         return defaultValue;
      }
      try
      {
         return Short.parseShort(value);
      }
      catch(final Exception exception)
      {
         return defaultValue;
      }
   }

   /**
    * Obtain parameter
    * 
    * @param key
    *           Parameter key
    * @param defaultValue
    *           Default value if parameter not exists
    * @return Parameter value
    */
   public String obtainParameter(final String key, final String defaultValue)
   {
      final String value = this.obtainParameter(key);
      if(value == null)
      {
         return defaultValue;
      }
      return value;
   }

   /**
    * List of parameters
    * 
    * @return List of parameters
    */
   public EnumerationIterator<String> obtainParametersName()
   {
      return new EnumerationIterator<String>(this.parameters.keys());
   }

   /**
    * Write XML to file in human readable mode
    * 
    * @param file
    *           File where write
    * @throws FileNotFoundException
    *            If file not exists
    */
   public void println(final File file) throws FileNotFoundException
   {
      this.println(new PrintStream(new FileOutputStream(file), true), 0);
   }

   /**
    * Write XML to stream in human readable mode
    * 
    * @param outputStream
    *           Stream where write
    */
   public void println(final OutputStream outputStream)
   {
      this.println(new PrintStream(outputStream, true), 0);
   }

   /**
    * Write XML to stream in human readable mode
    * 
    * @param printStream
    *           Stream where write
    */
   public void println(final PrintStream printStream)
   {
      this.println(printStream, 0);
   }

   /**
    * Remove a child
    * 
    * @param child
    *           Child to remove
    */
   public void removeChild(final MarkupXML child)
   {
      if(child == null)
      {
         throw new NullPointerException("child musn't be null");
      }
      if(child.parent != this)
      {
         return;
      }
      this.children.remove(child);
      child.parent = null;
   }

   /**
    * Remove parameter
    * 
    * @param key
    *           Parameter key
    */
   public void removeParameter(final String key)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }
      this.parameters.remove(key);
   }

   /**
    * Modify text
    * 
    * @param text
    *           New text value
    */
   public void setText(final String text)
   {
      this.text = text;
   }

   /**
    * Write XML to stream in compact mode
    * 
    * @param outputStream
    *           Stream where write
    * @throws IOException
    *            On writing problems
    */
   public void write(final OutputStream outputStream) throws IOException
   {
      this.println(new PrintStream(outputStream, false), Integer.MIN_VALUE);
      outputStream.flush();
   }
}