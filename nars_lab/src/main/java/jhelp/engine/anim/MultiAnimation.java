package jhelp.engine.anim;

import com.jogamp.opengl.GL;
import jhelp.engine.Animation;

import java.util.ArrayList;

/**
 * List of animation played sequentially.<br>
 * In other worlds, when first animation of the list is finish, second start, then third, ....<br>
 * List of animation can be loop several times
 * 
 * @author JHelp
 */
public class MultiAnimation
      implements Animation
{
   /** Animation list */
   private final ArrayList<Animation> animations;
   /** Index of actual animation */
   private int                        index;
   /** Number of loop to do at total */
   private final int                  numberOfLoop;
   /** Number of loop left */
   public int                         loopLeft;

   /**
    * Create a new instance of MultiAnimation that played one time
    */
   public MultiAnimation()
   {
      this(1);
   }

   /**
    * Create a new instance of MultiAnimation that played one or "infinite" time
    * 
    * @param loop
    *           {@code true} for "infinite", {@code false} for one time
    */
   public MultiAnimation(final boolean loop)
   {
      this(loop == true
            ? Integer.MAX_VALUE
            : 1);
   }

   /**
    * Create a new instance of MultiAnimation played a determinate number of time
    * 
    * @param numberOfLoop
    *           Number of time to play animation list
    */
   public MultiAnimation(final int numberOfLoop)
   {
      this.numberOfLoop = Math.max(1, numberOfLoop);
      this.loopLeft = this.numberOfLoop;
      this.animations = new ArrayList<Animation>();
      this.index = 0;
   }

   /**
    * Add an animation to the list
    * 
    * @param animation
    *           Added animation
    */
   public void addAnimation(final Animation animation)
   {
      this.animations.add(animation);
   }

   /**
    * Called when animation list is playing <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param gl
    *           Open GL context
    * @param absoluteFrame
    *           Absolute frame
    * @return {@code false} if animation list finished and no more loop left
    * @see jhelp.engine.Animation#animate(javax.media.opengl.GL, float)
    */
   @Override
   public boolean animate(final GL gl, final float absoluteFrame)
   {
      final int size = this.animations.size();
      if(this.index >= size)
      {
         this.loopLeft--;
         if((this.loopLeft <= 0) || (size <= 0))
         {
            return false;
         }

         this.index = 0;
         this.animations.get(0).setStartAbsoluteFrame(absoluteFrame);
      }

      boolean cont = this.animations.get(this.index).animate(gl, absoluteFrame);

      while(cont == false)
      {
         this.index++;

         if(this.index >= size)
         {
            this.loopLeft--;
            if(this.loopLeft <= 0)
            {
               return false;
            }

            this.index = 0;
         }

         this.animations.get(this.index).setStartAbsoluteFrame(absoluteFrame);
         cont = this.animations.get(this.index).animate(gl, absoluteFrame);
      }

      return true;
   }

   /**
    * Called when animation initialize <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param startAbsoluteFrame
    *           Start absolute frame
    * @see jhelp.engine.Animation#setStartAbsoluteFrame(float)
    */
   @Override
   public void setStartAbsoluteFrame(final float startAbsoluteFrame)
   {
      this.index = 0;
      this.loopLeft = this.numberOfLoop;

      if(this.animations.isEmpty() == false)
      {
         this.animations.get(0).setStartAbsoluteFrame(startAbsoluteFrame);
      }
   }
}