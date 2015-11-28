package jhelp.util.gui;

/**
 * Paint of a gradient of 4 colors, each color is put on a corner of the bounding box of the shape to fill, and other pixels are
 * there interpolation
 * 
 * @author JHelp
 */
public class JHelpGradient
      implements JHelpPaint
{
   /** Alpha value of down left corner */
   private final int alphaDownLeft;
   /** Alpha value of down right corner */
   private final int alphaDownRight;
   /** Alpha value of up left corner */
   private final int alphaUpLeft;
   /** Alpha value of up right corner */
   private final int alphaUpRight;
   /** Blue value of down left corner */
   private final int blueDownLeft;
   /** Blue value of down right corner */
   private final int blueDownRight;
   /** Blue value of up left corner */
   private final int blueUpLeft;
   /** Blue value of up right corner */
   private final int blueUpRight;
   /** Green value of down left corner */
   private final int greenDownLeft;
   /** Green value of down right corner */
   private final int greenDownRight;
   /** Green value of up left corner */
   private final int greenUpLeft;
   /** Green value of up right corner */
   private final int greenUpRight;
   /** Actual bounding box height */
   private int       height;
   /** Red value of down left corner */
   private final int redDownLeft;
   /** Red value of down right corner */
   private final int redDownRight;
   /** Red value of up left corner */
   private final int redUpLeft;
   /** Red value of up right corner */
   private final int redUpRight;
   /** Actual bounding box area */
   private int       size;
   /** Actual bounding box width */
   private int       width;

   /**
    * Create a new instance of JHelpGradient
    * 
    * @param upLeft
    *           Up left corner color
    * @param upRight
    *           Up right corner color
    * @param downLeft
    *           Down left corner color
    * @param downRight
    *           Down right corner color
    */
   public JHelpGradient(final int upLeft, final int upRight, final int downLeft, final int downRight)
   {
      this.alphaUpLeft = (upLeft >> 24) & 0xFF;
      this.redUpLeft = (upLeft >> 16) & 0xFF;
      this.greenUpLeft = (upLeft >> 8) & 0xFF;
      this.blueUpLeft = upLeft & 0xFF;

      this.alphaUpRight = (upRight >> 24) & 0xFF;
      this.redUpRight = (upRight >> 16) & 0xFF;
      this.greenUpRight = (upRight >> 8) & 0xFF;
      this.blueUpRight = upRight & 0xFF;

      this.alphaDownLeft = (downLeft >> 24) & 0xFF;
      this.redDownLeft = (downLeft >> 16) & 0xFF;
      this.greenDownLeft = (downLeft >> 8) & 0xFF;
      this.blueDownLeft = downLeft & 0xFF;

      this.alphaDownRight = (downRight >> 24) & 0xFF;
      this.redDownRight = (downRight >> 16) & 0xFF;
      this.greenDownRight = (downRight >> 8) & 0xFF;
      this.blueDownRight = downRight & 0xFF;
   }

   /**
    * Down left corner color
    * 
    * @return Down left corner color
    */
   public int getColorDownLeft()
   {
      return (this.alphaDownLeft << 24) | (this.redDownLeft << 16) | (this.greenDownLeft << 8) | this.blueDownLeft;
   }

   /**
    * Down right corner color
    * 
    * @return Down right corner color
    */
   public int getColorDownRight()
   {
      return (this.alphaDownRight << 24) | (this.redDownRight << 16) | (this.greenDownRight << 8) | this.blueDownRight;
   }

   /**
    * Up left corner color
    * 
    * @return Up left corner color
    */
   public int getColorUpLeft()
   {
      return (this.alphaUpLeft << 24) | (this.redUpLeft << 16) | (this.greenUpLeft << 8) | this.blueUpLeft;
   }

   /**
    * Up right corner color
    * 
    * @return Up right corner color
    */
   public int getColorUpRight()
   {
      return (this.alphaUpRight << 24) | (this.redUpRight << 16) | (this.greenUpRight << 8) | this.blueUpRight;
   }

   /**
    * Initialize the paint before fill a shape <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param width
    *           Bounding box width
    * @param height
    *           Bounding box height
    * @see JHelpPaint#initializePaint(int, int)
    */
   @Override
   public void initializePaint(final int width, final int height)
   {
      this.width = width;
      this.height = height;
      this.size = width * height;
   }

   /**
    * Compute the color for a pixel inside the shape to fill <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param x
    *           Pixel's X
    * @param y
    *           Pixel's Y
    * @return Computed color
    * @see JHelpPaint#obtainColor(int, int)
    */
   @Override
   public int obtainColor(final int x, final int y)
   {
      final int xx = this.width - x;
      final int yy = this.height - y;

      return ((((((this.alphaUpLeft * xx) + (this.alphaUpRight * x)) * yy) + (((this.alphaDownLeft * xx) + (this.alphaDownRight * x)) * y)) / this.size) << 24) | //
            ((((((this.redUpLeft * xx) + (this.redUpRight * x)) * yy) + (((this.redDownLeft * xx) + (this.redDownRight * x)) * y)) / this.size) << 16) | //
            ((((((this.greenUpLeft * xx) + (this.greenUpRight * x)) * yy) + (((this.greenDownLeft * xx) + (this.greenDownRight * x)) * y)) / this.size) << 8) | //
            (((((this.blueUpLeft * xx) + (this.blueUpRight * x)) * yy) + (((this.blueDownLeft * xx) + (this.blueDownRight * x)) * y)) / this.size);
   }
}