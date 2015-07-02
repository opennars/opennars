/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.util<br>
 * Class : PositionObject2D<br>
 * Date : 4 sept. 2008<br>
 * By JHelp
 */
package jhelp.engine.util;

import jhelp.engine.twoD.Object2D;

/**
 * 2D object position <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class PositionObject2D
{
   /** Height */
   public int height;
   /** Width */
   public int width;
   /** X */
   public int x;
   /** Y */
   public int y;

   /**
    * Constructs PositionObject2D
    */
   public PositionObject2D()
   {
      this.x = this.y = 0;
      this.width = this.height = 1;
   }

   /**
    * Create a new instance of PositionObject2D
    * 
    * @param x
    *           X
    * @param y
    *           Y
    */
   public PositionObject2D(final int x, final int y)
   {
      this.x = x;
      this.y = y;
      this.width = this.height = 1;
   }

   /**
    * Create a new instance of PositionObject2D
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param width
    *           Width
    * @param height
    *           Height
    */
   public PositionObject2D(final int x, final int y, final int width, final int height)
   {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   /**
    * Constructs PositionObject2D
    * 
    * @param object2D
    *           Base object
    */
   public PositionObject2D(final Object2D object2D)
   {
      this.x = object2D.getX();
      this.y = object2D.getX();
      this.width = object2D.getWidth();
      this.height = object2D.getHeight();
   }

   /**
    * Create a new instance of PositionObject2D
    * 
    * @param positionObject2D
    *           Position to copy
    */
   public PositionObject2D(final PositionObject2D positionObject2D)
   {
      this.x = positionObject2D.x;
      this.y = positionObject2D.y;
      this.width = positionObject2D.width;
      this.height = positionObject2D.height;
   }

   /**
    * Create a copy of the position
    * 
    * @return The copy
    */
   public PositionObject2D copy()
   {
      return new PositionObject2D(this);
   }
}