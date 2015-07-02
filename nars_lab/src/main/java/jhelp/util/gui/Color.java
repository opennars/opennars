package jhelp.util.gui;

/**
 * Represents a color
 * 
 * @author JHelp
 */
public class Color
      implements Comparable<Color>
{
   /** Precision for decide if two colors are similar */
   static int precision = 0;

   /**
    * Indicates if 2 colors are similar
    * 
    * @param red
    *           Fist color red
    * @param green
    *           First color green
    * @param blue
    *           First color blue
    * @param color
    *           Second color
    * @param precision
    *           Precision to use
    * @return {@code true} if colors are similar
    */
   public static boolean isNear(final int red, final int green, final int blue, final int color, final int precision)
   {
      return (Math.abs(red - ((color >> 16) & 0xFF)) <= precision) && (Math.abs(green - ((color >> 8) & 0xFF)) <= precision) && (Math.abs(blue - (color & 0xFF)) <= precision);
   }

   /**
    * Indicates if 2 colors are similar
    * 
    * @param red1
    *           First color red
    * @param green1
    *           First color green
    * @param blue1
    *           First color blue
    * @param red2
    *           Second color red
    * @param green2
    *           Second color green
    * @param blue2
    *           Second color blue
    * @param precision
    *           Precision to use
    * @return {@code true} if colors are similar
    */
   public static boolean isNear(final int red1, final int green1, final int blue1, final int red2, final int green2, final int blue2, final int precision)
   {
      return (Math.abs(red1 - red2) <= precision) && (Math.abs(green1 - green2) <= precision) && (Math.abs(blue1 - blue2) <= precision);
   }

   /** Color alpha */
   int alpha;
   /** Color blue */
   int blue;
   /** Color itself */
   int color;
   /** Color green */
   int green;
   /** Additional information */
   int info;
   /** Color red */
   int red;

   /**
    * Create a new instance of Color
    * 
    * @param color
    *           The color
    */
   public Color(final int color)
   {
      this.color = color;
      this.alpha = (color >> 24) & 0xFF;
      this.red = (color >> 16) & 0xFF;
      this.green = (color >> 8) & 0xFF;
      this.blue = color & 0xFF;
   }

   /**
    * Create a new instance of Color
    * 
    * @param alpha
    *           Alpha
    * @param red
    *           Red
    * @param green
    *           Green
    * @param blue
    *           Blue
    */
   public Color(final int alpha, final int red, final int green, final int blue)
   {
      this.alpha = alpha & 0xFF;
      this.red = red & 0xFF;
      this.green = green & 0xFF;
      this.blue = blue & 0xFF;

      this.color = (alpha << 24) | (red << 16) | (green << 8) | blue;
   }

   /**
    * Compare with an other color <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param color
    *           Color to compare with
    * @return Comparison result
    * @see Comparable#compareTo(Object)
    */
   @Override
   public int compareTo(final Color color)
   {
      int diff = this.red - color.red;
      if((diff < -Color.precision) || (diff > Color.precision))
      {
         return diff;
      }

      diff = this.green - color.green;
      if((diff < -Color.precision) || (diff > Color.precision))
      {
         return diff;
      }

      diff = this.blue - color.blue;
      if((diff < -Color.precision) || (diff > Color.precision))
      {
         return diff;
      }

      return 0;
   }

   /**
    * Indicate in an Object is equals to this color <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param object
    *           Object to test
    * @return {@code true} in equality
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(final Object object)
   {
      if(this == object)
      {
         return true;
      }

      if((object == null) || (Color.class.getName().equals(object.getClass()) == false))
      {
         return false;
      }

      return this.isNear((Color) object, Color.precision);
   }

   /**
    * Indicates if a color is similar to this color
    * 
    * @param color
    *           Color to compare
    * @param precision
    *           Precision to use
    * @return {@code true} if the color is similar
    */
   public boolean isNear(final Color color, final int precision)
   {
      return (Math.abs(this.red - color.red) <= precision) && (Math.abs(this.green - color.green) <= precision) && (Math.abs(this.blue - color.blue) <= precision);
   }
}