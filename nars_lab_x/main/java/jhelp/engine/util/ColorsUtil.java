/**
 */
package jhelp.engine.util;

/**
 * Utilities for color manipulation <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class ColorsUtil
{
   /** High influent blue transparent */
   public static final Color BLUE_HIGH    = new Color(0xC00000FF, true);
   /** Low influent blue transparent */
   public static final Color BLUE_LOW     = new Color(0x400000FF, true);
   /** Semi influent blue transparent */
   public static final Color BLUE_SEMI    = new Color(0x800000FF, true);
   /** High influent green transparent */
   public static final Color GREEN_HIGH   = new Color(0xC000FF00, true);
   /** Low influent green transparent */
   public static final Color GREEN_LOW    = new Color(0x4000FF00, true);
   /** Semi influent green transparent */
   public static final Color GREEN_SEMI   = new Color(0x8000FF00, true);
   /** Light */
   public static final Color LIGHT        = new Color(0x80C0C0C0, true);
   /** Bright light */
   public static final Color LIGHT_BRIGHT = new Color(0xC0FFFFFF, true);
   /** High influent red transparent */
   public static final Color RED_HIGH     = new Color(0xC0FF0000, true);
   /** Low influent red transparent */
   public static final Color RED_LOW      = new Color(0x40FF0000, true);
   /** Semi influent red transparent */
   public static final Color RED_SEMI     = new Color(0x80FF0000, true);
   /** Shadow */
   public static final Color SHADOW       = new Color(0x80404040, true);
   /** Shadow dark */
   public static final Color SHADOW_DARK  = new Color(0xC0000000, true);
   /** Transparent color */
   public static final Color TRANSPARENT  = new Color(0, 0, 0, 0);

   /**
    * Change color bright
    * 
    * @param color
    *           Base color
    * @param factor
    *           Bright factor (factor>1 => more bright | 0<factor<1 => more dark)
    * @return Computed color
    */
   public static Color changeBright(final Color color, final float factor)
   {
      // Get color parts
      int red = color.getRed();
      int green = color.getGreen();
      int blue = color.getBlue();

      // Convert in YUV
      double y = (red * 0.299) + (green * 0.587) + (blue * 0.114);
      final double u = ((-0.169 * red) - (0.331 * green)) + (0.500 * blue) + 128.0;
      final double v = ((0.500 * red) - (0.419 * green) - (0.081 * blue)) + 128.0;

      // Apply the factor
      y *= factor;

      // Convert to RGB
      red = ColorsUtil.limite0_255((y - (0.0009267 * (u - 128))) + (1.4016868 * (v - 128)));
      green = ColorsUtil.limite0_255(y - (0.3436954 * (u - 128)) - (0.7141690 * (v - 128)));
      blue = ColorsUtil.limite0_255(y + (1.7721604 * (u - 128)) + (0.0009902 * (v - 128)));

      // Return the new color
      return new Color(red, green, blue);
   }

   /**
    * Take the integer part of a number and put it in [0, 255]<br>
    * That is to say if integer<0, return 0. If integer>255, return 2555. Return the integer on other case
    * 
    * @param number
    *           Number to limit
    * @return Limited value
    */
   public static int limite0_255(final double number)
   {
      final int integer = (int) number;
      if(integer < 0)
      {
         return 0;
      }
      if(integer > 255)
      {
         return 255;
      }
      return integer;
   }

   /**
    * Compute a brighter color
    * 
    * @param color
    *           Base color
    * @return Brighter color
    */
   public static Color moreBright(final Color color)
   {
      return ColorsUtil.changeBright(color, 2);
   }

   /**
    * Compute a darker color
    * 
    * @param color
    *           Base color
    * @return Darker color
    */
   public static Color moreDark(final Color color)
   {
      return ColorsUtil.changeBright(color, 0.5f);
   }
}