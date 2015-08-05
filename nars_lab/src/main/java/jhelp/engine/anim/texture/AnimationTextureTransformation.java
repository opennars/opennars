package jhelp.engine.anim.texture;

import com.jogamp.opengl.GL;
import jhelp.engine.Animation;
import jhelp.engine.Texture;
import jhelp.util.list.SortedArray;

/**
 * Animation that change/transform a texture
 * 
 * @author JHelp
 */
public class AnimationTextureTransformation
      implements Animation
{
   /**
    * A frame of transformation
    * 
    * @author JHelp
    */
   class AnimationTextureFrame
         implements Comparable<AnimationTextureFrame>
   {
      /** Frame number/place */
      final int             frame;
      /** Transformation apply to a texture */
      TextureTransformation textureTransformation;

      /**
       * Create a new instance of AnimationTextureFrame
       * 
       * @param frame
       *           Frame position
       * @param textureTransformation
       *           Transformation apply to a texture
       */
      AnimationTextureFrame(final int frame, final TextureTransformation textureTransformation)
      {
         this.frame = frame;
         this.textureTransformation = textureTransformation;
      }

      /**
       * Compare this frame with an other one<br>
       * Negative result means this frame is before given one<br>
       * Null results, they are in same place<br>
       * Positive means this frame is after given one <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param animationTextureFrame
       *           Frame to compare with
       * @return Comparison result
       * @see Comparable#compareTo(Object)
       */
      @Override
      public int compareTo(final AnimationTextureFrame animationTextureFrame)
      {
         return this.frame - animationTextureFrame.frame;
      }
   }

   /** Animation frames */
   private final SortedArray<AnimationTextureFrame> frames;
   /** Current animation index */
   private int                                      index;
   /** Frame where animation started */
   private float                                    startAbsoluteFrame;
   /** Texture to modify */
   private final Texture                            texture;

   /**
    * Create a new instance of AnimationTextureTransformation
    * 
    * @param texture
    *           Texture to modify
    */
   public AnimationTextureTransformation(final Texture texture)
   {
      if(texture == null)
      {
         throw new NullPointerException("texture musn't be null");
      }

      this.frames = new SortedArray<AnimationTextureFrame>(AnimationTextureFrame.class, true);
      this.texture = texture;
   }

   /**
    * Add a frame to the animation.<br>
    * If the frame already exist, the transformation is replaced
    * 
    * @param frame
    *           Frame index
    * @param textureTransformation
    *           Texture transformation to add/replace
    */
   public void addFrame(final int frame, final TextureTransformation textureTransformation)
   {
      if(frame < 0)
      {
         throw new IllegalArgumentException("frame must be >=0");
      }

      if(textureTransformation == null)
      {
         throw new NullPointerException("textureTransformation musn't be null");
      }

      final AnimationTextureFrame animationTextureFrame = new AnimationTextureFrame(frame, textureTransformation);
      final int index = this.frames.indexOf(animationTextureFrame);

      if(index < 0)
      {
         this.frames.add(animationTextureFrame);
      }
      else
      {
         this.frames.getElement(index).textureTransformation = textureTransformation;
      }
   }

   /**
    * Play the animation <br>
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
   public boolean animate(final GL gl, final float absoluteFrame)
   {
      if(this.index >= this.frames.getSize())
      {
         return false;
      }

      final float frame = absoluteFrame - this.startAbsoluteFrame;
      AnimationTextureFrame animationTextureFrame = this.frames.getElement(this.index);

      while(frame >= animationTextureFrame.frame)
      {
         animationTextureFrame.textureTransformation.apply(this.texture);
         this.index++;

         if(this.index >= this.frames.getSize())
         {
            return false;
         }

         animationTextureFrame = this.frames.getElement(this.index);
      }

      return this.index < this.frames.getSize();
   }

   /**
    * Called when animation start <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param startAbsoluteFrame
    *           Started frame
    * @see jhelp.engine.Animation#setStartAbsoluteFrame(float)
    */
   @Override
   public void setStartAbsoluteFrame(final float startAbsoluteFrame)
   {
      this.startAbsoluteFrame = startAbsoluteFrame;
      this.index = 0;
   }
}