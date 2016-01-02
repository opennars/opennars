/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.util.image<br>
 * Class : CompressiveImage<br>
 * Date : 4 janv. 2009<br>
 * By JHelp
 */
package jhelp.engine.graphics;

import jhelp.engine.Texture;
import jhelp.util.debug.Debug;
import jhelp.util.io.UtilIO;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Image compress auto update on decompression, so we see image appear.<br>
 * An image of WxH take in memory 4xWxH bytes, in RGB 3xWxH (BMP without header), here 1.5xWxH without degradation see by eyes.
 * We add LZH algorithm to compress little better <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class CompressiveImage
      implements Runnable
{
   /**
    * Limit and integer in [0, 255]
    * 
    * @param integer
    *           Integer to limit
    * @return Limited integer
    */
   private static int limite0_255(final int integer)
   {
      if(integer <= 0)
      {
         return 0;
      }
      if(integer >= 255)
      {
         return 255;
      }
      return integer;
   }

   /**
    * Encode image
    * 
    * @param name
    *           Image name
    * @param bufferedImage
    *           Image to encode
    * @return Encoded image
    */
   public static CompressiveImage encode(final String name, final BufferedImage bufferedImage)
   {
      // Create compressive image
      final CompressiveImage compressiveImage = new CompressiveImage(name);
      // Get image pixels
      final int width = bufferedImage.getWidth();
      final int height = bufferedImage.getHeight();
      int[] pixels = new int[width * height];
      pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
      // Update compressive image pixels
      compressiveImage.setPixels(width, height, pixels);

      return compressiveImage;
   }

   /**
    * Encode an image in compressive image
    * 
    * @param name
    *           Image name
    * @param image
    *           Image to encode
    * @return Encoded compressive image
    */
   public static CompressiveImage encode(final String name, final Image image)
   {
      // Create the compressive image
      final CompressiveImage compressiveImage = new CompressiveImage(name);
      // Get image pixels
      final int width = image.getWidth(null);
      final int height = image.getHeight(null);
      final int[] pixels = new int[width * height];
      final PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
      try
      {
         pixelGrabber.grabPixels();
      }
      catch(final InterruptedException e)
      {
      }
      // Update compressive image pixels
      compressiveImage.setPixels(width, height, pixels);

      return compressiveImage;
   }

   /**
    * Encode array of pixels
    * 
    * @param name
    *           Image name
    * @param width
    *           Width
    * @param height
    *           Height
    * @param pixels
    *           Pixels
    * @return Encoded image
    */
   public static CompressiveImage encode(final String name, final int width, final int height, final byte[] pixels)
   {
      final CompressiveImage compressiveImage = new CompressiveImage(name);
      compressiveImage.setPixels(width, height, pixels);
      return compressiveImage;
   }

   /**
    * Encode array of pixels
    * 
    * @param name
    *           Image name
    * @param width
    *           Width
    * @param height
    *           Height
    * @param pixels
    *           Pixels
    * @return Encoded image
    */
   public static CompressiveImage encode(final String name, final int width, final int height, final int[] pixels)
   {
      final CompressiveImage compressiveImage = new CompressiveImage(name);
      compressiveImage.setPixels(width, height, pixels);
      return compressiveImage;
   }

   /**
    * Read encoded image form a stream.<br>
    * Image will be update in streaming mode.<br>
    * The method return imediatly, the background process is just started
    * 
    * @param name
    *           Image name
    * @param inputStream
    *           Stream to read
    * @param textureToRefresh
    *           Texture to update on reading
    * @return Encoded image
    * @throws IOException
    *            On reading problem
    */
   public static CompressiveImage read(final String name, final InputStream inputStream, final Texture textureToRefresh) throws IOException
   {
      if(inputStream == null)
      {
         throw new NullPointerException("The inputStream musn't be null !");
      }
      final CompressiveImage compressiveImage = new CompressiveImage(name);
      compressiveImage.textureToRefresh = textureToRefresh;
      final ZipInputStream zipInputStream = new ZipInputStream(inputStream);
      zipInputStream.getNextEntry();
      compressiveImage.inputStream = zipInputStream;
      final Thread thread = new Thread(compressiveImage);
      thread.start();
      return compressiveImage;
   }

   /** Image height */
   private int            height;
   /** Stream to read */
   private ZipInputStream inputStream;
   /** Image name */
   private final String   name;
   /** image pixels */
   private byte[]         pixels;
   /** Texture to refresh while loading */
   private Texture        textureToRefresh;
   /** Image width */
   private int            width;

   /**
    * Constructs empty CompressiveImage
    * 
    * @param name
    *           Image name
    */
   private CompressiveImage(final String name)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }
      this.name = name;
   }

   /**
    * Update image's pixels
    * 
    * @param width
    *           Width
    * @param height
    *           Height
    * @param pixels
    *           Pixels
    */
   private void setPixels(final int width, final int height, final byte[] pixels)
   {
      final int nb = width * height * 4;
      if(nb != pixels.length)
      {
         throw new IllegalArgumentException("The pixels' array length is not width*height*4 !");
      }
      this.width = width;
      this.height = height;
      this.pixels = pixels;
   }

   /**
    * Update image's pixels
    * 
    * @param width
    *           Width
    * @param height
    *           Height
    * @param pixels
    *           Pixels
    */
   private void setPixels(final int width, final int height, final int[] pixels)
   {
      final int nb = width * height;
      if(nb != pixels.length)
      {
         throw new IllegalArgumentException("The pixels' array length is not width*height !");
      }
      this.width = width;
      this.height = height;
      int color;
      this.pixels = new byte[nb * 4];
      int index = 0;
      for(int i = 0; i < nb; i++)
      {
         color = pixels[i];
         this.pixels[index++] = (byte) ((color >> 16) & 0xFF);
         this.pixels[index++] = (byte) ((color >> 8) & 0xFF);
         this.pixels[index++] = (byte) (color & 0xFF);
         this.pixels[index++] = (byte) (255);
      }
   }

   /**
    * Return name
    * 
    * @return name
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Read the image in background.<br>
    * Don't call this method, pass by read method
    * 
    * @see Runnable#run()
    */
   @Override
   public void run()
   {
      try
      {
         // Read image size
         this.width = UtilIO.readInteger(this.inputStream);
         this.height = UtilIO.readInteger(this.inputStream);
         final int nb = this.width * this.height;
         this.pixels = new byte[nb * 4];
         // If there are a texture to refresh, adjust it's size
         if(this.textureToRefresh != null)
         {
            this.textureToRefresh.setPixels(this.width, this.height, this.pixels);
         }

         // Get each square 2x2
         int s0, s1, p00, p01, p10, p11, y00, y01, y10, y11, cb, cr;
         double uvR, uvG, uvB;
         for(int yy = 0; yy < this.height; yy += 2)
         {
            s0 = yy * this.width;
            s1 = s0 + this.width;
            for(int xx = 0; xx < this.width; xx += 2)
            {
               p00 = s0 + xx;
               p10 = p00 + 1;
               p01 = s1 + xx;
               p11 = p01 + 1;

               p00 <<= 2;
               p10 <<= 2;
               p01 <<= 2;
               p11 <<= 2;

               // read current square
               y00 = this.inputStream.read();
               y10 = this.inputStream.read();
               y01 = this.inputStream.read();
               y11 = this.inputStream.read();
               cb = this.inputStream.read();
               cr = this.inputStream.read();

               //
               // To convert YUV to RGB we use :
               // R = 1 * Y - 0.0009267*(U-128) + 1.4016868*(V-128)
               // G = 1 * Y - 0.3436954*(U-128) - 0.7141690*(V-128)
               // B = 1 * Y + 1.7721604*(U-128) + 0.0009902*(V-128)
               //

               // For optimize we compute common pixels parts
               uvR = (-0.0009267 * (cb - 128)) + (1.4016868 * (cr - 128));
               uvG = (-0.3436954 * (cb - 128)) - (0.7141690 * (cr - 128));
               uvB = (1.7721604 * (cb - 128)) + (0.0009902 * (cr - 128));

               // Create each pixel
               this.pixels[p00++] = (byte) (CompressiveImage.limite0_255((int) (y00 + uvR)));
               this.pixels[p00++] = (byte) (CompressiveImage.limite0_255((int) (y00 + uvG)));
               this.pixels[p00++] = (byte) (CompressiveImage.limite0_255((int) (y00 + uvB)));
               this.pixels[p00] = (byte) (255);

               this.pixels[p10++] = (byte) (CompressiveImage.limite0_255((int) (y10 + uvR)));
               this.pixels[p10++] = (byte) (CompressiveImage.limite0_255((int) (y10 + uvG)));
               this.pixels[p10++] = (byte) (CompressiveImage.limite0_255((int) (y10 + uvB)));
               this.pixels[p10] = (byte) (255);

               this.pixels[p01++] = (byte) (CompressiveImage.limite0_255((int) (y01 + uvR)));
               this.pixels[p01++] = (byte) (CompressiveImage.limite0_255((int) (y01 + uvG)));
               this.pixels[p01++] = (byte) (CompressiveImage.limite0_255((int) (y01 + uvB)));
               this.pixels[p01] = (byte) (255);

               this.pixels[p11++] = (byte) (CompressiveImage.limite0_255((int) (y11 + uvR)));
               this.pixels[p11++] = (byte) (CompressiveImage.limite0_255((int) (y11 + uvG)));
               this.pixels[p11++] = (byte) (CompressiveImage.limite0_255((int) (y11 + uvB)));
               this.pixels[p11] = (byte) (255);

               // If there are a texture to refresh, refresh it
               if(this.textureToRefresh != null)
               {
                  this.textureToRefresh.setPixels(this.width, this.height, this.pixels);
               }
            }
         }

         // All is done
         // If there are a texture to refresh, refresh it
         if(this.textureToRefresh != null)
         {
            this.textureToRefresh.setPixels(this.width, this.height, this.pixels);
         }

         // Close the stream
         this.inputStream.closeEntry();
      }
      catch(final IOException e)
      {
         Debug.printException(e);
      }
   }

   /**
    * Transfer image to a texture
    * 
    * @param texture
    *           Texture to transfer (If it is {@code null} a new texture is created)
    * @return Updated texture
    */
   public Texture transferToTexture(Texture texture)
   {
      if(texture == null)
      {
         texture = new Texture(this.name, this.width, this.height, this.pixels);
      }
      else
      {
         texture.setPixels(this.width, this.height, this.pixels);
      }
      this.textureToRefresh = texture;
      return texture;
   }

   /**
    * Write image to a stream
    * 
    * @param outputStream
    *           Stream to wite
    * @throws IOException
    *            On writing problem
    */
   public void write(final OutputStream outputStream) throws IOException
   {
      // Prepare the stream
      final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
      zipOutputStream.setLevel(9);
      zipOutputStream.putNextEntry(new ZipEntry("gg"));
      // Write dimension
      UtilIO.writeInteger(this.width, zipOutputStream);
      UtilIO.writeInteger(this.height, zipOutputStream);

      final int nb = this.width * this.height;
      final int[] y = new int[nb];
      final int[] cb = new int[nb];
      final int[] cr = new int[nb];
      //
      // To convert RGB to YUV, we use this :
      // Y = 0.299*R + 0.587*G + 0.114*B
      // U = -0.169*R - 0.331*G + 0.500*B + 128.0
      // V = 0.500*R - 0.419*G - 0.081*B + 128.0
      //
      int r, g, b;
      int index = 0;
      int pix = 0;
      // Convert RGB pixels value to YUV
      for(index = 0; index < nb; index++)
      {
         r = this.pixels[pix++] & 0xFF;
         g = this.pixels[pix++] & 0xFF;
         b = this.pixels[pix++] & 0xFF;
         pix++;// Ignore alphas

         y[index] = CompressiveImage.limite0_255(((299 * r) + (587 * g) + (114 * b)) / 1000);
         cb[index] = CompressiveImage.limite0_255(((((-169 * r) - (331 * g)) + (500 * b)) / 1000) + 128);
         cr[index] = CompressiveImage.limite0_255((((500 * r) - (419 * g) - (81 * b)) / 1000) + 128);
      }
      // We write value by square 2x2 pixels
      index = 0;
      int s0, s1, l, p00, p01, p10, p11;
      final int w = this.width >> 1;
      for(int yy = 0; yy < this.height; yy += 2)
      {
         l = (yy >> 1) * w;
         s0 = yy * this.width;
         s1 = s0 + this.width;
         for(int xx = 0; xx < this.width; xx += 2)
         {
            index = (l + xx) >> 1;
            p00 = s0 + xx;
            p10 = p00 + 1;
            p01 = s1 + xx;
            p11 = p01 + 1;
            // We write Y parts as it
            zipOutputStream.write(y[p00]);
            zipOutputStream.write(y[p10]);
            zipOutputStream.write(y[p01]);
            zipOutputStream.write(y[p11]);
            // We write the average of U and V
            zipOutputStream.write((cb[p00] + cb[p10] + cb[p01] + cb[p11]) >> 2);
            zipOutputStream.write((cr[p00] + cr[p10] + cr[p01] + cr[p11]) >> 2);
            // So here we write 6 bytes, in RGB we have to write 12, that's why
            // it
            // is smaller
         }
      }
      // Close the stream
      zipOutputStream.closeEntry();
      zipOutputStream.finish();
      zipOutputStream.flush();
   }
}