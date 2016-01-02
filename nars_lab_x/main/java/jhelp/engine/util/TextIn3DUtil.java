/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.util<br>
 * Class : TextUtil<br>
 * Date : 14 sept. 2008<br>
 * By JHelp
 */
package jhelp.engine.util;

import jhelp.engine.Texture;
import jhelp.engine.twoD.Object2D;
import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;

import java.util.StringTokenizer;

/**
 * Utilities for text <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class TextIn3DUtil
{
   /** Next ID for texture text name */
   private static int NEXT_ID = 0;

   /**
    * Create texture with text
    * 
    * @param text
    *           Text to print. You can use '\n' character to print several lines
    * @param font
    *           Font used
    * @param textAlignment
    *           Text alignment
    * @param foreGround
    *           Color for print
    * @param backGround
    *           Background color
    * @return Created texture
    */
   public static Texture createTextTexture(final String text, final Font font, final TextAlignment textAlignment, final Color foreGround, final Color backGround)
   {
      String lines[];
      StringTokenizer stringTokenizer;
      int length;
      int index;

      // Cut the text in lines
      stringTokenizer = new StringTokenizer(text, "\n", false);
      length = stringTokenizer.countTokens();
      lines = new String[length];
      index = 0;
      while(stringTokenizer.hasMoreTokens() == true)
      {
         lines[index++] = stringTokenizer.nextToken();
      }

      // Create the texture
      return TextIn3DUtil.createTextTexture(lines, font, textAlignment, foreGround, backGround);
   }

   /**
    * Create a texture with a text print inside
    * 
    * @param lines
    *           Text lines
    * @param font
    *           Font used to print
    * @param textAlignment
    *           Text alignment
    * @param foreGround
    *           Color used for the text
    * @param backGround
    *           Color used on back ground (can be transparent ;-) )
    * @return Created texture
    */
   @SuppressWarnings("deprecation")
   public static Texture createTextTexture(final String[] lines, final Font font, final TextAlignment textAlignment, final Color foreGround, final Color backGround)
   {
      Texture texture;
      int length;
      int index;
      FontMetrics fontMetrics;
      int fontHeight;
      int height;
      int width;
      int[] linesWidth;
      int heightP2;
      int widthP2;
      int x;
      int y;

      // No lines, so little texture is need
      length = lines.length;
      if(length < 1)
      {
         return new Texture("TEXT_" + (TextIn3DUtil.NEXT_ID++), 1, 1, backGround);
      }

      linesWidth = new int[length];

      // Compute textu height
      fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
      fontHeight = fontMetrics.getHeight();
      height = fontHeight * length;

      // Compute texture width and memorize each line width
      width = 0;
      for(index = 0; index < length; index++)
      {
         linesWidth[index] = fontMetrics.stringWidth(lines[index]);
         width = Math.max(width, linesWidth[index]);
      }

      // Compute near power of dimension values
      widthP2 = Math3D.computePowerOf2couple(width)[1];
      heightP2 = Math3D.computePowerOf2couple(height)[1];

      Debug.println(DebugLevel.VERBOSE, "TextUtil.createTextTexture() BEFORE " + width + "x" + height + " => " + widthP2 + "x" + heightP2);
      if((widthP2 < width) && (widthP2 < 1024))
      {
         widthP2 *= 2;
      }
      if((heightP2 < height) && (heightP2 < 1024))
      {
         heightP2 *= 2;
      }
      Debug.println(DebugLevel.VERBOSE, "TextUtil.createTextTexture() AFTER " + width + "x" + height + " => " + widthP2 + "x" + heightP2);

      // For good card only ...
      // widthP2 = width;
      // heightP2 = height;
      // ... For good card only

      // Create empty texture
      texture = new Texture("TEXT_" + (TextIn3DUtil.NEXT_ID++), widthP2, heightP2, ColorsUtil.TRANSPARENT);
      texture.fillRect(0, 0, width, height, backGround, false);

      // Print each line
      y = fontMetrics.getAscent();
      for(index = 0; index < length; index++)
      {
         x = 0;
         switch(textAlignment)
         {
            case LEFT:
               x = 0;
            break;
            case CENTER:
               x = (width - linesWidth[index]) / 2;
            break;
            case RIGHT:
               x = width - linesWidth[index];
            break;
         }

         texture.fillString(x, y, lines[index], foreGround, font, false);
         y += fontHeight;
      }

      texture.flush();

      return texture;
   }

   /**
    * Create a 2D object with text print
    * 
    * @param x
    *           X position of the object
    * @param y
    *           Y position of the object
    * @param text
    *           Text to print. You can use '\n' character to print several lines
    * @param font
    *           Font used
    * @param textAlignment
    *           Text alignment
    * @param foreGround
    *           Color for print
    * @param backGround
    *           Background color
    * @return Created object
    */
   public static Object2D creteTextObject2D(final int x, final int y, final String text, final Font font, final TextAlignment textAlignment, final Color foreGround, final Color backGround)
   {
      Texture texture;
      Object2D object2D;

      texture = TextIn3DUtil.createTextTexture(text, font, textAlignment, foreGround, backGround);
      object2D = new Object2D(x, y, texture.getWidth(), texture.getHeight());
      object2D.setTexture(texture);

      return object2D;
   }

   /**
    * Create a 2D object with text print
    * 
    * @param x
    *           X position of the object
    * @param y
    *           Y position of the object
    * @param lines
    *           Text lines
    * @param font
    *           Font used
    * @param textAlignment
    *           Text alignment
    * @param foreGround
    *           Color for print
    * @param backGround
    *           Background color
    * @return Created object
    */
   public static Object2D creteTextObject2D(final int x, final int y, final String[] lines, final Font font, final TextAlignment textAlignment, final Color foreGround, final Color backGround)
   {
      Texture texture;
      Object2D object2D;

      texture = TextIn3DUtil.createTextTexture(lines, font, textAlignment, foreGround, backGround);
      object2D = new Object2D(x, y, texture.getWidth(), texture.getHeight());
      object2D.setTexture(texture);

      return object2D;
   }
}