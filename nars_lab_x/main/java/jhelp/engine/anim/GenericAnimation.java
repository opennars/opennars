package jhelp.engine.anim;

import com.jogamp.opengl.GL;
import jhelp.engine.Animation;

/**
 * A generic animation for able to play animation without link with JOGL need from external program
 * 
 * @author JHelp
 */
public class GenericAnimation
      implements Animation
{
   /** External animation */
   private final ExternalAnimation externalAnimation;
   /** Start absolute frame */
   private float                   startAbsoluteFrame;

   /**
    * Create a new instance of GenericAnimation
    * 
    * @param externalAnimation
    *           External animation
    */
   public GenericAnimation(final ExternalAnimation externalAnimation)
   {
      if(externalAnimation == null)
      {
         throw new NullPointerException("externalAnimation musn't be null");
      }

      this.externalAnimation = externalAnimation;
   }

   /**
    * Called when animation playing <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param gl
    *           Open GL context
    * @param absoluteFrame
    *           Absolute frame
    * @return {@code true} if animation should continue
    * @see jhelp.engine.Animation#animate(javax.media.opengl.GL, float)
    */
   @Override
   public final boolean animate(final GL gl, final float absoluteFrame)
   {
      return this.externalAnimation.playAnimation(absoluteFrame - this.startAbsoluteFrame);
   }

   /**
    * Called when animation started <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param startAbsoluteFrame
    *           Start absolute frame
    * @see jhelp.engine.Animation#setStartAbsoluteFrame(float)
    */
   @Override
   public final void setStartAbsoluteFrame(final float startAbsoluteFrame)
   {
      this.startAbsoluteFrame = startAbsoluteFrame;
      this.externalAnimation.initializeAnimation();
   }
}