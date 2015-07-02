/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.anim<br>
 * Class : AnimationPositionObject2D<br>
 * Date : 4 sept. 2008<br>
 * By JHelp
 */
package jhelp.engine.anim;

import jhelp.engine.twoD.Object2D;
import jhelp.engine.util.PositionObject2D;

/**
 * Animation witch move a 2D object <br>
 * <br>
 * Last modification : 21 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class AnimationPositionObject2D
      extends AnimationKeyFrame<Object2D, PositionObject2D>
{

   /**
    * Constructs AnimationPositionObject2D
    * 
    * @param object
    *           Object to move
    */
   public AnimationPositionObject2D(final Object2D object)
   {
      super(object);
   }

   /**
    * Interpolate a value
    * 
    * @param object
    *           Object to move
    * @param before
    *           Position just before the computed position
    * @param after
    *           Position just after the computed position
    * @param percent
    *           Percent of interpolation
    * @see AnimationKeyFrame#interpolateValue(Object, Object, Object, float)
    */
   @Override
   protected void interpolateValue(final Object2D object, final PositionObject2D before, final PositionObject2D after, final float percent)
   {
      float anti;

      anti = 1f - percent;

      object.setX((int) ((before.x * anti) + (after.x * percent)));
      object.setY((int) ((before.y * anti) + (after.y * percent)));
      object.setWidth((int) ((before.width * anti) + (after.width * percent)));
      object.setHeight((int) ((before.height * anti) + (after.height * percent)));
   }

   /**
    * Compute object position
    * 
    * @param object
    *           Object to get it's position
    * @return Object's position
    * @see AnimationKeyFrame#obtainValue(Object)
    */
   @Override
   protected PositionObject2D obtainValue(final Object2D object)
   {
      return new PositionObject2D(object);
   }

   /**
    * Change object position
    * 
    * @param object
    *           Object to change
    * @param value
    *           New value
    * @see AnimationKeyFrame#setValue(Object, Object)
    */
   @Override
   protected void setValue(final Object2D object, final PositionObject2D value)
   {
      object.setX(value.x);
      object.setY(value.y);
      object.setWidth(value.width);
      object.setHeight(value.height);
   }
}