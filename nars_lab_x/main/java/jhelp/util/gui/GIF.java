/**
 * Project : JHelpGif<br>
 * Package : jhelp.gif<br>
 * Class : GIF<br>
 * Date : 4 avr. 2009<br>
 * By JHelp
 */
package jhelp.util.gui;

import com.sun.imageio.plugins.gif.GIFImageMetadata;
import com.sun.imageio.plugins.gif.GIFImageReader;
import jhelp.util.io.IntegerArrayInputStream;
import jhelp.util.list.ArrayInt;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * GIF image <br>
 * <br>
 * Last modification : 4 avr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
@SuppressWarnings("restriction")
public class GIF
{
   /** Indicates the method NONE */
   private static final int            DISPOSE_METHOD_NONE               = 0;
   /** Indicates the method NOT DISPOSE */
   private static final int            DISPOSE_METHOD_NOT_DISPOSE        = 1;
   /** Indicates the method RESTORE BACKGROUND */
   private static final int            DISPOSE_METHOD_RESTORE_BACKGROUND = 2;
   /** Indicates the method RESTORE PREVIOUS */
   private static final int            DISPOSE_METHOD_RESTORE_PREVIOUS   = 3;

   /** GIF image reader */
   private static final GIFImageReader GIFReader                         = GIF.obtainImageReader();

   /**
    * Obtain GIF image reader
    * 
    * @return GIF image reader
    */
   private static GIFImageReader obtainImageReader()
   {
      final Iterator<ImageReader> iterator = ImageIO.getImageReadersBySuffix("GIF");
      while(iterator.hasNext() == true)
      {
         return (GIFImageReader) iterator.next();
      }
      return null;
   }

   /** Images delay */
   private final ArrayInt delays;
   /** Image height */
   private int            height;
   /** Images contains in the GIF */
   private JHelpImage[]   images;
   /** Last seen index in automatic show */
   private int            previousIndex;
   /** Start animation time */
   private long           startTime;
   /** Total animation time */
   private int            totalTime;
   /** Image width */
   private int            width;

   /**
    * Constructs GIF
    * 
    * @param inputStream
    *           Stream to read the GIF
    * @throws IOException
    *            On reading problem
    */
   public GIF(InputStream inputStream)
         throws IOException
   {
      if(inputStream == null)
      {
         throw new NullPointerException("inputStream musn't be null");
      }

      this.delays = new ArrayInt();
      this.totalTime = 0;

      // Get stream to read the image
      ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
      // Given the stream to the reader
      GIF.GIFReader.setInput(imageInputStream, true, false);

      // Read parameters
      ImageReadParam imageReadParam = GIF.GIFReader.getDefaultReadParam();

      // Initialization
      ArrayList<JHelpImage> images = new ArrayList<JHelpImage>();
      JHelpImage cumulateImage = null;
      JHelpImage readImage;
      JHelpImage newImage;
      int index = 0;
      boolean oneMore = true;
      int x, y, time;
      GIFImageMetadata imageMetadata;
      int disposalMethod = GIF.DISPOSE_METHOD_NONE;

      // While there are one image to extract
      while(oneMore == true)
      {
         try
         {
            // Read actual image
            readImage = JHelpImage.createImage(GIF.GIFReader.read(index, imageReadParam));
            // Read actual informations
            imageMetadata = (GIFImageMetadata) GIF.GIFReader.getImageMetadata(index);

            // Get image position in the total image
            x = imageMetadata.imageLeftPosition;
            y = imageMetadata.imageTopPosition;

            // If this is first image, prepare the cumulative image
            if(cumulateImage == null)
            {
               this.width = readImage.getWidth();
               this.height = readImage.getHeight();
               cumulateImage = new JHelpImage(this.width, this.height);
            }

            readImage.getWidth();
            readImage.getHeight();

            // Switch the method to do with the previous
            switch(disposalMethod)
            {
            // Over write the parcel on the cumulative image (over write also
            // alpha)
               case GIF.DISPOSE_METHOD_RESTORE_BACKGROUND:
               case GIF.DISPOSE_METHOD_RESTORE_PREVIOUS:
                  cumulateImage.startDrawMode();
                  cumulateImage.drawImage(x, y, readImage, false);
                  cumulateImage.endDrawMode();
               break;

               // Just draw the image on the cumulative (alpha processing)
               case GIF.DISPOSE_METHOD_NOT_DISPOSE:
               case GIF.DISPOSE_METHOD_NONE:
               default:
                  cumulateImage.startDrawMode();
                  cumulateImage.drawImage(x, y, readImage, true);
                  cumulateImage.endDrawMode();
               break;
            }

            // Get method to use for next image
            disposalMethod = imageMetadata.disposalMethod;

            // Create the final actual image
            newImage = new JHelpImage(this.width, this.height);
            newImage.startDrawMode();
            newImage.drawImage(0, 0, cumulateImage, false);
            newImage.endDrawMode();

            // Add the image
            images.add(newImage);
            time = Math.max(1, imageMetadata.delayTime);
            this.delays.add(time);
            this.totalTime += time;

            // Go to next image
            index++;
         }
         catch(final Exception exception)
         {
            // On problem, we have no more image
            oneMore = false;
         }
      }

      // Free memory
      imageMetadata = null;
      cumulateImage = null;
      readImage = null;
      newImage = null;

      // Get extracted images
      this.images = new JHelpImage[images.size()];
      this.images = images.toArray(this.images);

      // Free memory
      images.clear();
      images = null;
      imageReadParam = null;
      imageInputStream = null;
      GIF.GIFReader.dispose();
      inputStream.close();
      inputStream = null;

      if(this.images.length == 0)
      {
         throw new IOException("Failed to load GIF, no extracted image");
      }
   }

   /**
    * Compute GIF MD5
    * 
    * @return GIF MD5
    * @throws NoSuchAlgorithmException
    *            If MD5 unknown
    * @throws IOException
    *            On computing problem
    */
   public String computeMD5() throws NoSuchAlgorithmException, IOException
   {
      final MessageDigest md5 = MessageDigest.getInstance("MD5");

      final int numberOfImages = this.images.length;

      byte[] temp = new byte[4096];
      temp[0] = (byte) ((numberOfImages >> 24) & 0xFF);
      temp[1] = (byte) ((numberOfImages >> 16) & 0xFF);
      temp[2] = (byte) ((numberOfImages >> 8) & 0xFF);
      temp[3] = (byte) (numberOfImages & 0xFF);

      md5.update(temp, 0, 4);

      IntegerArrayInputStream inputStream;
      JHelpImage bufferedImage;
      int[] pixels;
      int read, width, height;

      for(int image = 0; image < numberOfImages; image++)
      {
         bufferedImage = this.images[image];

         width = bufferedImage.getWidth();
         height = bufferedImage.getHeight();

         pixels = bufferedImage.getPixels(0, 0, width, height, 2);
         pixels[0] = width;
         pixels[1] = height;

         inputStream = new IntegerArrayInputStream(pixels);
         pixels = null;

         read = inputStream.read(temp);
         while(read >= 0)
         {
            md5.update(temp, 0, read);

            read = inputStream.read(temp);
         }

         inputStream.close();
         inputStream = null;
      }

      temp = md5.digest();
      final StringBuffer stringBuffer = new StringBuffer();
      for(final byte b : temp)
      {
         read = b & 0xFF;
         stringBuffer.append(Integer.toHexString((read >> 4) & 0xF));
         stringBuffer.append(Integer.toHexString(read & 0xF));
      }
      temp = null;

      return stringBuffer.toString();
   }

   /**
    * Destroy the gif to free memory
    */
   public void destroy()
   {
      for(int i = this.images.length - 1; i >= 0; i--)
      {
         this.images[i] = null;
      }

      this.images = null;
   }

   /**
    * Obtin an image delay
    * 
    * @param index
    *           Image index
    * @return Delay in millisecond
    */
   public int getDelay(final int index)
   {
      return this.delays.getInteger(index);
   }

   /**
    * Image height
    * 
    * @return Image height
    */
   public int getHeight()
   {
      return this.height;
   }

   /**
    * Get a image
    * 
    * @param index
    *           Image index
    * @return Desired image
    */
   public JHelpImage getImage(final int index)
   {
      return this.images[index];
   }

   /**
    * Get the image suggest between last time {@link #startAnimation()} was called and time this method is called based on
    * images dellays
    * 
    * @return Image since last time {@link #startAnimation()} was called
    */
   public JHelpImage getImageFromStartAnimation()
   {
      final long time = (System.currentTimeMillis() - this.startTime) >> 3L;
      final int max = this.images.length - 1;
      final int relativeTime = (int) (time % this.totalTime);
      int index = 0;
      int actualTime = 0;
      int delay;

      for(; index < max; index++)
      {
         delay = this.delays.getInteger(index);
         actualTime += delay;

         if(actualTime >= relativeTime)
         {
            break;
         }
      }

      final int nextIndex = ((this.previousIndex + 1) % this.images.length);
      if(index != this.previousIndex)
      {
         index = nextIndex;
      }

      this.previousIndex = index;
      return this.images[index];
   }

   /**
    * Total animation time
    * 
    * @return Total animation time
    */
   public int getTotalTime()
   {
      return this.totalTime;
   }

   /**
    * Image width
    * 
    * @return Image width
    */
   public int getWidth()
   {
      return this.width;
   }

   /**
    * Indicates if this instance can be use.<br>
    * That is to say if {@link #destroy()} never call
    * 
    * @return {@code true} if the instance can be use
    */
   public boolean isUsable()
   {
      return this.images != null;
   }

   /**
    * Number of images
    * 
    * @return Number of images
    */
   public int numberOfImage()
   {
      return this.images.length;
   }

   /**
    * Satart/restart animation from begening, to follow evolution, use {@link #getImageFromStartAnimation()} to have current
    * image of the animation
    */
   public void startAnimation()
   {
      this.startTime = System.currentTimeMillis();
   }
}