/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.layout<br>
 * Class : CellLayoutConstraints<br>
 * Date : 27 juin 2010<br>
 * By JHelp
 */
package jhelp.engine.gui.layout;

/**
 * Cells layout constraints<br>
 * <br>
 * Last modification : 27 juin 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class CellLayoutConstraints
      implements Constraints
{
   /** Number of cell in height */
   int height;
   /** Number of cell in width */
   int width;
   /** X cell */
   int x;
   /** Y cell */
   int y;

   /**
    * Constructs CellLayoutConstraints
    * 
    * @param x
    *           X cell
    * @param y
    *           Y cell
    * @param width
    *           Number of cell in width
    * @param height
    *           Number of cell in height
    */
   public CellLayoutConstraints(final int x, final int y, final int width, final int height)
   {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   /**
    * Indicates if constraints is equals
    * 
    * @param constraints
    *           Constraints tested
    * @return {@code true} if constraints is equals
    * @see Constraints#equals(Constraints)
    */
   @Override
   public boolean equals(final Constraints constraints)
   {
      if(constraints == this)
      {
         return true;
      }

      if((constraints instanceof CellLayoutConstraints) == false)
      {
         return false;
      }

      final CellLayoutConstraints cellLayoutConstraints = (CellLayoutConstraints) constraints;

      return (this.x == cellLayoutConstraints.x) && (this.y == cellLayoutConstraints.y) && (this.width == cellLayoutConstraints.width) && (this.height == cellLayoutConstraints.height);
   }

   /**
    * Hash code
    * 
    * @return Hash code
    * @see Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return this.x + (this.y << 8) + (this.width << 16) + (this.height << 24);
   }
}