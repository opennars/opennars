package jhelp.util.gui;

import jhelp.util.cache.Cache;
import jhelp.util.cache.CacheElement;
import jhelp.util.debug.Debug;
import jhelp.util.list.ArrayInt;
import jhelp.util.list.SortedArray;
import jhelp.util.list.Triplet;
import jhelp.util.resources.Resources;
import jhelp.util.text.StringExtractor;
import jhelp.util.text.UtilText;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Text with the possibility to insert some image in it.<br>
 * For example it can replace every <b>:)</b> by a picture like <img src="sourire.png" width=32 height=32/>
 * 
 * @author JHelp
 */
public final class JHelpRichText
{
   /**
    * Cache element of image. It describes how create an image
    * 
    * @author JHelp
    */
   private static class CacheImageElement
         extends CacheElement<JHelpImage>
   {
      /** Image resource name */
      private final String    resource;
      /** Resources set where found the image */
      private final Resources resources;
      /** Image desired size */
      private final int       size;

      /**
       * Create a new instance of CacheImageElement
       * 
       * @param resources
       *           Resources set where found the image
       * @param resource
       *           Image resource name
       * @param size
       *           Image desired size
       */
      public CacheImageElement(final Resources resources, final String resource, final int size)
      {
         this.resources = resources;
         this.resource = resource;
         this.size = size;
      }

      /**
       * Create the image <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @return Created image
       * @see jhelp.util.cache.CacheElement#createElement()
       */
      @Override
      protected JHelpImage createElement()
      {
         try
         {
            return JHelpImage.loadImageThumb(this.resources.obtainResourceStream(this.resource), this.size, this.size);
         }
         catch(final IOException exception)
         {
            Debug.printException(exception, "Failed to load resource ", this.resource);

            return null;
         }
      }
   }

   /**
    * Represents an association between a symbol (For example <b>:)</b> ) and an image
    * 
    * @author JHelp
    */
   private static class Symbol
         implements Comparable<Symbol>
   {
      /** Image resource name */
      private final String resource;
      /** Symbol associated */
      private final String symbol;

      /**
       * Create a new instance of Symbol
       * 
       * @param symbol
       *           The symbol
       * @param resource
       *           Image resource name
       */
      public Symbol(final String symbol, final String resource)
      {
         this.symbol = symbol;
         this.resource = resource;
      }

      /**
       * Compare with an other symbol. <br>
       * It returns :
       * <ul>
       * <li><b>&lt; 0</b> : if this symbol is before the given one</li>
       * <li><b>0</b> : if the compared symbol is the same</li>
       * <li><b>&gt; 0</b> : if this symbol is after the given one</li>
       * </ul>
       * <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param symbol
       *           Symbol to compare
       * @return Comparison result
       * @see Comparable#compareTo(Object)
       */
      @Override
      public int compareTo(final Symbol symbol)
      {
         final int diff = symbol.symbol.length() - this.symbol.length();

         if(diff != 0)
         {
            return diff;
         }

         return this.symbol.compareTo(symbol.symbol);
      }

      /**
       * Image resource name associated
       * 
       * @return Image resource name associated
       */
      public String getResource()
      {
         return this.resource;
      }

      /**
       * Associated symbol
       * 
       * @return Associated symbol
       */
      public String getSymbol()
      {
         return this.symbol;
      }

      /**
       * String representation <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @return String representation
       * @see Object#toString()
       */
      @Override
      public String toString()
      {
         return UtilText.concatenate(Symbol.class.getSimpleName(), " : ", this.symbol, " -> ", this.resource);
      }
   }

   /** Images cache */
   private final Cache<JHelpImage>   CACHE_IMAGES = new Cache<JHelpImage>();
   /** Resources set */
   private final Resources           resources;
   /** Registered associations */
   private final SortedArray<Symbol> symbols;

   /**
    * Create a new instance of JHelpRichText
    * 
    * @param resources
    *           Resources set where find images
    */
   public JHelpRichText(final Resources resources)
   {
      if(resources == null)
      {
         throw new NullPointerException("resources musn't be null");
      }

      this.resources = resources;
      this.symbols = new SortedArray<Symbol>(Symbol.class, true);
   }

   /**
    * Compute image that draw the text with symbols replaced by corresponding images
    * 
    * @param text
    *           Text to draw
    * @param font
    *           Font to use for text
    * @param colorText
    *           Color for text (Used if paintText and texturePaint are {@code null})
    * @param paintText
    *           Paint for text (Used if not {@code null} and texturePaint is {@code null})
    * @param textureText
    *           Texture for text (Used if not {@code null})
    * @return Created image
    */
   private JHelpImage createImage(final String text, final JHelpFont font, final int colorText, final JHelpPaint paintText, final JHelpImage textureText)
   {
      final int height = font.getHeight();
      int imageWidth = 0;
      int imageHeight = 0;
      final ArrayList<Triplet<Boolean, Point, String>> elements = new ArrayList<Triplet<Boolean, Point, String>>();
      int x = 0;
      int y = 0;
      int width;
      int index;
      String sym;
      int length;
      int size;
      boolean found;
      int start;
      int end;
      String part;
      int w;
      int sy;
      JHelpMask mask;
      final StringExtractor lines = new StringExtractor(text, "\n\f\r", "", "");
      final ArrayInt arrayInt = new ArrayInt();
      String line = lines.next();

      while(line != null)
      {
         arrayInt.clear();
         width = 0;
         x = 0;

         for(final Symbol symbol : this.symbols)
         {
            sym = symbol.getSymbol();
            length = sym.length();
            index = line.indexOf(sym);

            while(index >= 0)
            {
               size = arrayInt.getSize();
               found = false;
               for(int i = 0; i < size; i += 2)
               {
                  if((index >= arrayInt.getInteger(i)) && (index < arrayInt.getInteger(i + 1)))
                  {
                     found = true;
                     break;
                  }
               }

               if(found == false)
               {
                  arrayInt.add(index);
                  arrayInt.add(index + length);
               }

               index = line.indexOf(sym, index + length);
            }
         }

         arrayInt.sort();

         size = arrayInt.getSize();
         index = 0;
         length = line.length();

         for(int i = 0; i < size; i += 2)
         {
            start = arrayInt.getInteger(i);
            end = arrayInt.getInteger(i + 1);

            if(index < start)
            {
               part = line.substring(index, start);
               elements.add(new Triplet<Boolean, Point, String>(true, new Point(x, y), part));

               w = font.stringWidth(part);
               x += w;
               width += w;
            }

            part = line.substring(start, end);
            sy = this.symbols.indexOf(new Symbol(part, ""));
            elements.add(new Triplet<Boolean, Point, String>(false, new Point(x, y), this.symbols.getElement(sy).getResource()));
            x += height;
            width += height;
            index = end;
         }

         if(index < length)
         {
            part = line.substring(index, length);
            elements.add(new Triplet<Boolean, Point, String>(true, new Point(x, y), part));

            w = font.stringWidth(part);
            width += w;
         }

         y += height;
         imageHeight += height;
         imageWidth = Math.max(width, imageWidth);
         line = lines.next();
      }

      final JHelpImage result = new JHelpImage(Math.max(1, imageWidth), Math.max(1, imageHeight));

      result.startDrawMode();

      for(final Triplet<Boolean, Point, String> element : elements)
      {
         if(element.element1 == true)
         {
            mask = font.createMask(element.element3);

            if(textureText != null)
            {
               result.paintMask(element.element2.x, element.element2.y, mask, textureText, 0, 0, 0, true);
            }
            else if(paintText != null)
            {
               result.paintMask(element.element2.x, element.element2.y, mask, paintText, 0, true);
            }
            else
            {
               result.paintMask(element.element2.x, element.element2.y, mask, colorText, 0, true);
            }
         }
         else
         {
            result.drawImage(element.element2.x, element.element2.y, this.obtainImage(element.element3, height));
         }
      }

      result.endDrawMode();

      return result;
   }

   /**
    * Obtain an image from cache
    * 
    * @param resource
    *           Image resource name
    * @param size
    *           Image size
    * @return The image
    */
   private JHelpImage obtainImage(final String resource, final int size)
   {
      final String key = size + ':' + resource;

      JHelpImage image = this.CACHE_IMAGES.get(key);

      if(image == null)
      {
         final CacheImageElement cacheImageElement = new CacheImageElement(this.resources, resource, size);
         this.CACHE_IMAGES.add(key, cacheImageElement);
         image = this.CACHE_IMAGES.get(key);
      }

      return image;
   }

   /**
    * Add/modify an association for a symbol to an image
    * 
    * @param symbol
    *           Symbol to associate
    * @param resource
    *           Image resource name
    */
   public void associate(final String symbol, final String resource)
   {
      if(symbol == null)
      {
         throw new NullPointerException("symbol musn't be null");
      }

      if(resource == null)
      {
         throw new NullPointerException("resource musn't be null");
      }

      final Symbol symbolReal = new Symbol(symbol, resource);

      final int index = this.symbols.indexOf(symbolReal);

      if(index >= 0)
      {
         this.symbols.remove(index);
      }

      this.symbols.add(symbolReal);
   }

   /**
    * Create an image for draw the text with symbols replaced by corresponding image
    * 
    * @param text
    *           Text to convert
    * @param font
    *           Font to use for text
    * @param colorText
    *           Text color
    * @return Created image
    */
   public JHelpImage createImage(final String text, final JHelpFont font, final int colorText)
   {
      if(text == null)
      {
         throw new NullPointerException("text musn't be null");
      }

      if(font == null)
      {
         throw new NullPointerException("font musn't be null");
      }

      return this.createImage(text, font, colorText, null, null);
   }

   /**
    * Create an image for draw the text with symbols replaced by corresponding image
    * 
    * @param text
    *           Text to convert
    * @param font
    *           Font to use for text
    * @param textureText
    *           Text texture
    * @return Created image
    */
   public JHelpImage createImage(final String text, final JHelpFont font, final JHelpImage textureText)
   {
      if(text == null)
      {
         throw new NullPointerException("text musn't be null");
      }

      if(font == null)
      {
         throw new NullPointerException("font musn't be null");
      }

      if(textureText == null)
      {
         throw new NullPointerException("textureText musn't be null");
      }

      return this.createImage(text, font, 0, null, textureText);
   }

   /**
    * Create an image for draw the text with symbols replaced by corresponding image
    * 
    * @param text
    *           Text to convert
    * @param font
    *           Font to use for text
    * @param paintText
    *           Text paint
    * @return Created image
    */
   public JHelpImage createImage(final String text, final JHelpFont font, final JHelpPaint paintText)
   {
      if(text == null)
      {
         throw new NullPointerException("text musn't be null");
      }

      if(font == null)
      {
         throw new NullPointerException("font musn't be null");
      }

      if(paintText == null)
      {
         throw new NullPointerException("paintText musn't be null");
      }

      return this.createImage(text, font, 0, paintText, null);
   }

   /**
    * Obtain the image resource name for a symbol
    * 
    * @param symbol
    *           Symbol search
    * @return Image resource name or {@code null} if symbol not registered
    */
   public String obtainAssociation(final String symbol)
   {
      final Symbol symbolReal = new Symbol(symbol, "");

      final int index = this.symbols.indexOf(symbolReal);

      if(index < 0)
      {
         return null;
      }

      return this.symbols.getElement(index).getResource();
   }
}