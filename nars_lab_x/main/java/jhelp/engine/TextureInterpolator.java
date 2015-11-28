package jhelp.engine;

import jhelp.util.list.Scramble;
import jhelp.util.math.UtilMath;
import jhelp.util.math.random.JHelpRandom;

/**
 * Interpolator of 2 textures in a result one
 * 
 * @author JHelp
 */
public class TextureInterpolator
{
   /**
    * Interpolation type
    * 
    * @author JHelp
    */
   public enum InterpolationType
   {
      /** Start by image borders */
      BORDER,
      /** Start by image corners */
      CORNER,
      /** Melt the two texture */
      MELTED,
      /** Randomly replace pixels */
      RANDOM,
      /** Replace lineary */
      REPLACEMENT,
      /** Choose the mode randomly each time a transition is started or end */
      UNDEFINED
   }

   /** Actual interpolation type */
   private InterpolationType       actualInterpolationType;
   /** Actual factor */
   private double                  factor;
   /** Texture height */
   private final int               height;
   /** Indexes of pixels in random order */
   private final int[]             indexes;
   /** Type of interpolation defined */
   private final InterpolationType interpolationType;
   /** Size of textures in bytes */
   private final int               length;
   /** Size of texture in pixels */
   private final int               lengthSmall;
   /** Target texture */
   private final Texture           textureEnd;
   /** Interpolated texture */
   private final Texture           textureInterpolated;
   /** Source texture */
   private final Texture           textureStart;
   /** Texture width */
   private final int               width;

   /**
    * Create a new instance of TextureInterpolator
    * 
    * @param textureStart
    *           Texture start
    * @param textureEnd
    *           Textuyre end
    * @param name
    *           Interpolation name
    */
   public TextureInterpolator(final Texture textureStart, final Texture textureEnd, final String name)
   {
      this(textureStart, textureEnd, name, 0, InterpolationType.UNDEFINED);
   }

   /**
    * Create a new instance of TextureInterpolator
    * 
    * @param textureStart
    *           Texture start
    * @param textureEnd
    *           Textuyre end
    * @param name
    *           Interpolation name
    * @param factor
    *           Starting factor in [0, 1]
    * @param interpolationType
    *           Interpolation type
    */
   public TextureInterpolator(final Texture textureStart, final Texture textureEnd, final String name, final double factor, final InterpolationType interpolationType)
   {
      this.interpolationType = interpolationType == null
            ? InterpolationType.UNDEFINED
            : interpolationType;
      this.actualInterpolationType = this.interpolationType;
      this.width = textureStart.getWidth();
      this.height = textureStart.getHeight();

      if((textureEnd.getWidth() != this.width) || (textureEnd.getHeight() != this.height))
      {
         throw new IllegalArgumentException("The textures must have same dimensions");
      }

      this.lengthSmall = this.width * this.height;
      this.length = this.width * this.height * 4;
      this.textureStart = textureStart;
      this.textureEnd = textureEnd;
      this.textureInterpolated = new Texture(name, this.width, this.height);

      this.indexes = new int[this.lengthSmall];
      for(int i = 0; i < this.lengthSmall; i++)
      {
         this.indexes[i] = i;
      }

      Scramble.scramble(this.indexes);

      this.setFactor(factor);
   }

   /**
    * Create a new instance of TextureInterpolator
    * 
    * @param textureStart
    *           Texture start
    * @param textureEnd
    *           Textuyre end
    * @param name
    *           Interpolation name
    * @param interpolationType
    *           Interpolation type
    */
   public TextureInterpolator(final Texture textureStart, final Texture textureEnd, final String name, final InterpolationType interpolationType)
   {
      this(textureStart, textureEnd, name, 0, interpolationType);
   }

   /**
    * Interpolated texture
    * 
    * @return Interpolated texture
    */
   public Texture getTextureInterpolated()
   {
      return this.textureInterpolated;
   }

   /**
    * Change interpolation factor
    * 
    * @param factor
    *           New factor in [0, 1]
    * @return Interpolated texture
    */
   public Texture setFactor(final double factor)
   {
      if((factor < 0) || (factor > 1))
      {
         throw new IllegalArgumentException("Factor must be in [0, 1], not " + factor);
      }

      this.factor = factor;

      if((UtilMath.isNul(factor) == true) || (UtilMath.equals(factor, 1) == true))
      {
         Scramble.scramble(this.indexes);
         this.actualInterpolationType = this.interpolationType;
      }

      return this.updateTextureInterpolated();
   }

   /**
    * Force refresh the interpolated texture
    * 
    * @return Interpolated texture
    */
   public Texture updateTextureInterpolated()
   {
      final byte[] pixelsStart = this.textureStart.pixels;
      final byte[] pixelsEnd = this.textureEnd.pixels;
      final byte[] pixelsInterpolated = this.textureInterpolated.pixels;

      while(this.actualInterpolationType == InterpolationType.UNDEFINED)
      {
         this.actualInterpolationType = JHelpRandom.random(InterpolationType.class);
      }

      final double rotcaf = 1.0 - this.factor;
      int nb, bn, index, minX, maxX, minY, maxY, w, h, pix;
      switch(this.actualInterpolationType)
      {
         case MELTED:
            for(int i = this.length - 1; i >= 0; i--)
            {
               pixelsInterpolated[i] = (byte) (((pixelsStart[i] & 0xFF) * rotcaf) + ((pixelsEnd[i] & 0xFF) * this.factor));
            }
         break;
         case RANDOM:
            System.arraycopy(pixelsStart, 0, pixelsInterpolated, 0, this.length);
            nb = (int) (this.factor * this.lengthSmall);
            for(int i = 0; i < nb; i++)
            {
               index = this.indexes[i] * 4;
               pixelsInterpolated[index] = pixelsEnd[index];
               index++;
               pixelsInterpolated[index] = pixelsEnd[index];
               index++;
               pixelsInterpolated[index] = pixelsEnd[index];
               index++;
               pixelsInterpolated[index] = pixelsEnd[index];
            }
         break;
         case REPLACEMENT:
            nb = (int) (rotcaf * this.length);
            bn = this.length - nb;

            if(nb > 0)
            {
               System.arraycopy(pixelsStart, 0, pixelsInterpolated, 0, nb);
            }

            if(bn > 0)
            {
               System.arraycopy(pixelsEnd, nb, pixelsInterpolated, nb, bn);
            }
         break;
         case CORNER:
            System.arraycopy(pixelsStart, 0, pixelsInterpolated, 0, this.length);
            w = (int) (this.factor * this.width * 0.5);
            h = (int) (this.factor * this.height * 0.5);

            if((w > 0) && (h > 0))
            {
               minX = w;
               maxX = this.width - w;
               minY = h;
               maxY = this.height - h;

               pix = 0;
               for(int y = 0; y < this.height; y++)
               {
                  if((y <= minY) || (y >= maxY))
                  {
                     for(int x = 0; x < this.width; x++)
                     {
                        if((x <= minX) || (x >= maxX))
                        {
                           index = pix * 4;
                           pixelsInterpolated[index] = pixelsEnd[index];
                           index++;
                           pixelsInterpolated[index] = pixelsEnd[index];
                           index++;
                           pixelsInterpolated[index] = pixelsEnd[index];
                           index++;
                           pixelsInterpolated[index] = pixelsEnd[index];
                        }

                        pix++;
                     }
                  }
                  else
                  {
                     pix += this.width;
                  }
               }
            }
         break;
         case BORDER:
            System.arraycopy(pixelsStart, 0, pixelsInterpolated, 0, this.length);
            w = (int) (this.factor * this.width * 0.5);
            h = (int) (this.factor * this.height * 0.5);

            if((w > 0) && (h > 0))
            {
               minX = w;
               maxX = this.width - w;
               minY = h;
               maxY = this.height - h;

               pix = 0;
               for(int y = 0; y < this.height; y++)
               {
                  for(int x = 0; x < this.width; x++)
                  {
                     if((y <= minY) || (y >= maxY) || (x <= minX) || (x >= maxX))
                     {
                        index = pix * 4;
                        pixelsInterpolated[index] = pixelsEnd[index];
                        index++;
                        pixelsInterpolated[index] = pixelsEnd[index];
                        index++;
                        pixelsInterpolated[index] = pixelsEnd[index];
                        index++;
                        pixelsInterpolated[index] = pixelsEnd[index];
                     }

                     pix++;
                  }
               }
            }
         break;
         case UNDEFINED:
         // Already treat above
         break;
         default:
         // Should never arrive
         break;
      }

      this.textureInterpolated.flush();

      return this.textureInterpolated;
   }
}