package jhelp.engine.anim;

import com.jogamp.opengl.GL;
import jhelp.engine.Animation;
import jhelp.engine.Material;
import jhelp.engine.Texture;
import jhelp.engine.TextureInterpolator;
import jhelp.engine.TextureInterpolator.InterpolationType;
import jhelp.engine.twoD.Object2D;

/**
 * Animate a texture
 * 
 * @author JHelp
 */
public class AnimationTexture
      implements Animation
{
   /**
    * Create an animation texture for color to grey or grey to color
    * 
    * @param numberOfFrame
    *           Number of frame to do the transition
    * @param texture
    *           Texture to modify
    * @param pingPong
    *           Indicates if transformation are ping-pong
    * @param numberOfLoop
    *           Number of loop to repeat the transformation
    * @param interpolationType
    *           Interpolation type
    * @param toGray
    *           Grey way. {@code true} goto grey. {@code false} goto color
    * @return Created Animation
    */
   public static AnimationTexture graySwitch(final int numberOfFrame, final Texture texture, final boolean pingPong, final int numberOfLoop,
         final InterpolationType interpolationType, final boolean toGray)
   {
      final Texture gray = new Texture(texture.getTextureName() + "_gray", texture.getWidth(), texture.getHeight());
      gray.setPixels(texture);
      gray.toGray();

      if(toGray == true)
      {
         return new AnimationTexture(numberOfFrame, texture, gray, pingPong, numberOfLoop, interpolationType);
      }

      return new AnimationTexture(numberOfFrame, gray, texture, pingPong, numberOfLoop, interpolationType);
   }

   /** Number of loop left */
   private int                       loopLeft;
   /** Numbre of frame to do the transition between 2 textures */
   private final float               numberOfFrame;
   /** Number total of loop */
   private final int                 numberOfLoop;
   /** Indicates if its a "ping-pong" animation */
   private final boolean             pingPong;
   /** Start of animation */
   private float                     startAbsoluteFrame;
   /** Interpolator of textures */
   private final TextureInterpolator textureInterpolator;
   /** Indicates if interpolation goes up */
   private boolean                   wayUp;

   /**
    * Create a new instance of AnimationTexture played one time only
    * 
    * @param numberOfFrame
    *           Number of frame to interpolate 2 textures
    * @param textureStart
    *           Texture start
    * @param textureEnd
    *           Texture end
    */
   public AnimationTexture(final int numberOfFrame, final Texture textureStart, final Texture textureEnd)
   {
      this(numberOfFrame, textureStart, textureEnd, false, 1, InterpolationType.UNDEFINED);
   }

   /**
    * Create a new instance of AnimationTexture played "infinite" time
    * 
    * @param numberOfFrame
    *           Number of frame to interpolate 2 textures
    * @param textureStart
    *           Texture start
    * @param textureEnd
    *           Texture end
    * @param pingPong
    *           Indicates if its a "ping-pong" animation
    */
   public AnimationTexture(final int numberOfFrame, final Texture textureStart, final Texture textureEnd, final boolean pingPong)
   {
      this(numberOfFrame, textureStart, textureEnd, pingPong, Integer.MAX_VALUE, InterpolationType.UNDEFINED);
   }

   /**
    * Create a new instance of AnimationTexture a number of time (For "infinite" you can use {@link Integer#MAX_VALUE})
    * 
    * @param numberOfFrame
    *           Number of frame to interpolate 2 textures
    * @param textureStart
    *           Texture start
    * @param textureEnd
    *           Texture end
    * @param pingPong
    *           Indicates if its a "ping-pong" animation
    * @param numberOfLoop
    *           Number of loop (For "infinite" you can use {@link Integer#MAX_VALUE})
    */
   public AnimationTexture(final int numberOfFrame, final Texture textureStart, final Texture textureEnd, final boolean pingPong, final int numberOfLoop)
   {
      this(numberOfFrame, textureStart, textureEnd, pingPong, numberOfFrame, InterpolationType.UNDEFINED);
   }

   /**
    * Create a new instance of AnimationTexture a number of time (For "infinite" you can use {@link Integer#MAX_VALUE})
    * 
    * @param numberOfFrame
    *           Number of frame to interpolate 2 textures
    * @param textureStart
    *           Texture start
    * @param textureEnd
    *           Texture end
    * @param pingPong
    *           Indicates if its a "ping-pong" animation
    * @param numberOfLoop
    *           Number of loop (For "infinite" you can use {@link Integer#MAX_VALUE})
    * @param interpolationType
    *           Interpaolation type
    */
   public AnimationTexture(final int numberOfFrame, final Texture textureStart, final Texture textureEnd, final boolean pingPong, final int numberOfLoop,
         final InterpolationType interpolationType)
   {
      this.pingPong = pingPong;
      this.numberOfLoop = Math.max(1, numberOfLoop);
      this.loopLeft = this.numberOfLoop;
      this.numberOfFrame = Math.max(1, numberOfFrame);
      this.textureInterpolator = new TextureInterpolator(textureStart, textureEnd, textureStart.getTextureName() + "_" + textureEnd.getTextureName()
            + "_interpolation", interpolationType);
   }

   /**
    * Create a new instance of AnimationTexture played "infinite" time
    * 
    * @param numberOfFrame
    *           Number of frame to interpolate 2 textures
    * @param textureStart
    *           Texture start
    * @param textureEnd
    *           Texture end
    * @param pingPong
    *           Indicates if its a "ping-pong" animation
    * @param interpolationType
    *           Interpaolation type
    */
   public AnimationTexture(final int numberOfFrame, final Texture textureStart, final Texture textureEnd, final boolean pingPong,
         final InterpolationType interpolationType)
   {
      this(numberOfFrame, textureStart, textureEnd, pingPong, Integer.MAX_VALUE, interpolationType);
   }

   /**
    * Create a new instance of AnimationTexture played one time only
    * 
    * @param numberOfFrame
    *           Number of frame to interpolate 2 textures
    * @param textureStart
    *           Texture start
    * @param textureEnd
    *           Texture end
    * @param interpolationType
    *           Interpaolation type
    */
   public AnimationTexture(final int numberOfFrame, final Texture textureStart, final Texture textureEnd, final InterpolationType interpolationType)
   {
      this(numberOfFrame, textureStart, textureEnd, false, 1, interpolationType);
   }

   /**
    * Called each time animation refresh <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param gl
    *           Open GL context
    * @param absoluteFrame
    *           Absolute frame
    * @return {@code true} if animation have to continue
    * @see jhelp.engine.Animation#animate(javax.media.opengl.GL, float)
    */
   @Override
   public boolean animate(final GL gl, final float absoluteFrame)
   {
      float frame = absoluteFrame - this.startAbsoluteFrame;
      boolean anOther = frame < this.numberOfFrame;

      if(anOther == false)
      {
         frame = this.numberOfFrame;
      }

      if(this.wayUp == false)
      {
         frame = this.numberOfFrame - frame;
      }

      if(anOther == false)
      {
         this.startAbsoluteFrame = absoluteFrame;

         if(this.pingPong == true)
         {
            if(this.wayUp == true)
            {
               anOther = true;
            }

            this.wayUp = !this.wayUp;
         }

         if(anOther == false)
         {
            this.loopLeft--;
            anOther = this.loopLeft > 0;
         }
      }

      this.textureInterpolator.setFactor(frame / this.numberOfFrame);

      return anOther;
   }

   /**
    * Interpolated texture, can be use by example in {@link Material} or {@link Object2D}
    * 
    * @return Interpolated texture
    */
   public Texture getInterpolatedTexture()
   {
      return this.textureInterpolator.getTextureInterpolated();
   }

   /**
    * Called when animation initialized <br>
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
      this.startAbsoluteFrame = startAbsoluteFrame;
      this.textureInterpolator.setFactor(0);
      this.loopLeft = this.numberOfLoop;
      this.wayUp = true;
   }
}