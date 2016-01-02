package jhelp.util.gui;

import java.awt.*;

/**
 * Utilities for image manipulation
 * 
 * @author JHelp
 */
public class UtilImage
{
   /**
    * Triangle way
    * 
    * @author JHelp
    */
   public enum WayTriangle
   {
      /** Triangle for down */
      DOWN,
      /** Triangle for left */
      LEFT,
      /** Triangle for right */
      RIGHT,
      /** Triangle for up */
      UP
   }

   /**
    * Draw a triangle for go down
    * 
    * @param x
    *           X position
    * @param y
    *           Y position
    * @param size
    *           Triangle size
    * @param image
    *           Image where draw
    */
   public static void drawIncrustedDownTriangle(final int x, final int y, final int size, final JHelpImage image)
   {
      final boolean drawMode = image.isDrawMode();

      if(drawMode == false)
      {
         image.startDrawMode();
      }

      final int xx = x + (size >> 1);
      image.drawLine(x, y, x + size, y, 0xFF000000);
      image.drawLine(x, y, xx, y + size, 0xFF808080);
      image.drawLine(x + size, y, xx, y + size, 0xFFFFFFFF);

      if(drawMode == false)
      {
         image.endDrawMode();
      }
   }

   /**
    * Draw a triangle for go left
    * 
    * @param x
    *           X position
    * @param y
    *           Y position
    * @param size
    *           Triangle size
    * @param image
    *           Image where draw
    */
   public static void drawIncrustedLeftTriangle(final int x, final int y, final int size, final JHelpImage image)
   {
      final boolean drawMode = image.isDrawMode();

      if(drawMode == false)
      {
         image.startDrawMode();
      }

      final int yy = y + (size >> 1);
      image.drawLine(x, yy, x + size, y, 0xFF000000);
      image.drawLine(x, yy, x + size, y + size, 0xFF808080);
      image.drawLine(x + size, y, x + size, y + size, 0xFFFFFFFF);

      if(drawMode == false)
      {
         image.endDrawMode();
      }
   }

   /**
    * Draw a triangle for go right
    * 
    * @param x
    *           X position
    * @param y
    *           Y position
    * @param size
    *           Triangle size
    * @param image
    *           Image where draw
    */
   public static void drawIncrustedRightTriangle(final int x, final int y, final int size, final JHelpImage image)
   {
      final boolean drawMode = image.isDrawMode();

      if(drawMode == false)
      {
         image.startDrawMode();
      }

      final int yy = y + (size >> 1);
      image.drawLine(x + size, yy, x, y, 0xFF000000);
      image.drawLine(x + size, yy, x, y + size, 0xFFFFFFFF);
      image.drawLine(x, y, x, y + size, 0xFF808080);

      if(drawMode == false)
      {
         image.endDrawMode();
      }
   }

   /**
    * Draw a triangle for go in a given way
    * 
    * @param x
    *           X position
    * @param y
    *           Y position
    * @param size
    *           Triangle size
    * @param image
    *           Image where draw
    * @param wayTriangle
    *           Triangle way
    */
   public static void drawIncrustedTriangle(final int x, final int y, final int size, final JHelpImage image, final WayTriangle wayTriangle)
   {
      if(wayTriangle == null)
      {
         throw new NullPointerException("wayTriangle musn't be null");
      }

      switch(wayTriangle)
      {
         case DOWN:
            UtilImage.drawIncrustedDownTriangle(x, y, size, image);
         break;
         case LEFT:
            UtilImage.drawIncrustedLeftTriangle(x, y, size, image);
         break;
         case RIGHT:
            UtilImage.drawIncrustedRightTriangle(x, y, size, image);
         break;
         case UP:
            UtilImage.drawIncrustedUpTriangle(x, y, size, image);
         break;
      }
   }

   /**
    * Draw a triangle for go up
    * 
    * @param x
    *           X position
    * @param y
    *           Y position
    * @param size
    *           Triangle size
    * @param image
    *           Image where draw
    */
   public static void drawIncrustedUpTriangle(final int x, final int y, final int size, final JHelpImage image)
   {
      final boolean drawMode = image.isDrawMode();

      if(drawMode == false)
      {
         image.startDrawMode();
      }

      final int xx = x + (size >> 1);
      image.drawLine(x, y + size, x + size, y + size, 0xFF000000);
      image.drawLine(x, y + size, xx, y, 0xFF808080);
      image.drawLine(x + size, y + size, xx, y, 0xFFFFFFFF);

      if(drawMode == false)
      {
         image.endDrawMode();
      }
   }

   /**
    * Invert a color
    * 
    * @param color
    *           Color to invert
    * @return Inverted color
    */
   public static Color invertColor(final Color color)
   {
      return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
   }
}