package jhelp.util.gui;

/**
 * Represents a part of text
 * 
 * @author JHelp
 */
public class JHelpTextLine
{
   /** Part height */
   private final int       height;
   /** Mask to use */
   private final JHelpMask mask;
   /** Text carry */
   private final String    text;
   /** Part width */
   private final int       width;
   /** Y location */
   private final int       y;
   /** X location */
   int                     x;

   /**
    * Create a new instance of JHelpTextLine
    * 
    * @param text
    *           Text to carry
    * @param x
    *           X position
    * @param y
    *           Y position
    * @param width
    *           Part width
    * @param height
    *           Part height
    * @param mask
    *           Mask to use
    */
   public JHelpTextLine(final String text, final int x, final int y, final int width, final int height, final JHelpMask mask)
   {
      this.text = text;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.mask = mask;
   }

   /**
    * Part height
    * 
    * @return Part height
    */
   public int getHeight()
   {
      return this.height;
   }

   /**
    * Mask to use
    * 
    * @return Mask to use
    */
   public JHelpMask getMask()
   {
      return this.mask;
   }

   /**
    * Text carry
    * 
    * @return Text carry
    */
   public String getText()
   {
      return this.text;
   }

   /**
    * Part width
    * 
    * @return Part width
    */
   public int getWidth()
   {
      return this.width;
   }

   /**
    * X location
    * 
    * @return X location
    */
   public int getX()
   {
      return this.x;
   }

   /**
    * Y location
    * 
    * @return Y location
    */
   public int getY()
   {
      return this.y;
   }
}