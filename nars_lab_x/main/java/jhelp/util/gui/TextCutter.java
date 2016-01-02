package jhelp.util.gui;

import java.awt.image.BufferedImage;

/**
 * Cut text to take a limited number of pixels.<br>
 * The text is cut in middle, and cut characters are replace by ...<br>
 * By example "The house is in fire", can become "The ho...n fire"
 * 
 * @author JHelp
 */
public class TextCutter
{
   /** Measure strings */
   private FontMetrics fontMetrics;
   /** Width limit */
   private int         width;

   /**
    * Create a new instance of TextCutter
    * 
    * @param width
    *           Width limit in pixels
    * @param font
    *           Font to use
    */
   public TextCutter(final int width, final Font font)
   {
      this.setWidth(width);
      this.setFont(font);
   }

   /**
    * Convert a string to a cut one using current settings
    * 
    * @param string
    *           String to cut
    * @return Cut string
    */
   public String convert(final String string)
   {
      int w = this.fontMetrics.stringWidth(string);

      if(w <= this.width)
      {
         return string;
      }

      final int length = string.length();
      int milEnd = length >> 1;
      int milStart = milEnd;
      String converted = string.substring(0, milStart) + "..." + string.substring(milEnd);
      w = this.fontMetrics.stringWidth(converted);

      while(w > this.width)
      {
         if(milStart > 0)
         {
            milStart--;
         }

         if(milEnd < length)
         {
            milEnd++;
         }

         converted = string.substring(0, milStart) + "..." + string.substring(milEnd);
         w = this.fontMetrics.stringWidth(converted);
      }

      return converted;
   }

   /**
    * Maximum width in pixels
    * 
    * @return Maximum width in pixels
    */
   public int getWidth()
   {
      return this.width;
   }

   /**
    * Change current font
    * 
    * @param font
    *           New font
    */
   public void setFont(final Font font)
   {
      final BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D graphics2d = bufferedImage.createGraphics();
      this.fontMetrics = graphics2d.getFontMetrics(font);
      graphics2d.dispose();
      bufferedImage.flush();
   }

   /**
    * Change maximum width
    * 
    * @param width
    *           new maximum width in pixels
    */
   public void setWidth(final int width)
   {
      this.width = Math.max(width, 128);
   }
}