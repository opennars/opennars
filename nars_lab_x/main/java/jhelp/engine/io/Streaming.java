/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.io<br>
 * Class : Streaming<br>
 * Date : 4 sept. 2008<br>
 * By JHelp
 */
package jhelp.engine.io;

import jhelp.engine.Texture;
import jhelp.util.debug.Debug;
import jhelp.util.list.Queue;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * For read textures on streaming <br>
 * <br>
 * Last modification : 22 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Streaming
      implements Runnable
{
   /**
    * Store stream informations <br>
    * <br>
    * Last modification : 22 janv. 2009<br>
    * Version 0.0.1<br>
    * 
    * @author JHelp
    */
   class Stream
   {
      /** Stream to read */
      public InputStream inputStream;
      /** Texture to update */
      public Texture     texture;
   }

   /** Streaming access */
   public static final Streaming STREAMING = new Streaming();

   /** Streams left to read */
   private final Queue<Stream>   streams;
   /** Thread for do the streaming */
   private Thread                thread;

   /**
    * Constructs Streaming
    */
   private Streaming()
   {
      this.streams = new Queue<Stream>();
   }

   /**
    * Launch the loading (If need)
    */
   private void launch()
   {
      if(this.thread == null)
      {
         this.thread = new Thread(this);
         this.thread.start();
      }
   }

   /**
    * Read the data on a stream
    * 
    * @param inputStream
    *           Stream to read
    * @return Read data
    * @throws IOException
    *            On reading problem
    */
   private byte[] readData(final InputStream inputStream) throws IOException
   {
      // Initialize
      ByteArrayOutputStream byteArrayOutputStream;
      byte[] temp;
      int read;
      temp = new byte[4096];
      byteArrayOutputStream = new ByteArrayOutputStream();

      // Read data until there no more to read
      read = inputStream.read(temp);
      while(read >= 0)
      {
         byteArrayOutputStream.write(temp, 0, read);

         try
         {
            Thread.sleep(1);
         }
         catch(final Exception exception)
         {
         }

         read = inputStream.read(temp);
      }

      // Close streams
      byteArrayOutputStream.flush();
      byteArrayOutputStream.close();
      inputStream.close();
      try
      {
         Thread.sleep(10);
      }
      catch(final Exception exception)
      {
      }

      temp = null;

      return byteArrayOutputStream.toByteArray();
   }

   /**
    * Transfer data to texture
    * 
    * @param data
    *           Data to transfer
    * @param texture
    *           Texture to update
    * @throws IOException
    *            On reading problem
    */
   private void transferDataTo(final byte[] data, final Texture texture) throws IOException
   {
      // Initialization
      BufferedImage bufferedImage;
      int width;
      int height;
      int[] pixels;

      // Transform data into image
      bufferedImage = ImageIO.read(new ByteArrayInputStream(data));
      try
      {
         Thread.sleep(10);
      }
      catch(final Exception exception)
      {
      }

      // Get image dimension
      width = bufferedImage.getWidth();
      height = bufferedImage.getHeight();

      // Extract image's pixels
      pixels = new int[width * height];
      pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
      bufferedImage.flush();
      bufferedImage = null;
      try
      {
         Thread.sleep(10);
      }
      catch(final Exception exception)
      {
      }

      // Update the texture
      texture.setPixels(width, height, pixels);
      pixels = null;
   }

   /**
    * Launch a texture loading in streaming.<br>
    * The texture return will be update when loading is done.<br>
    * You can use it now.
    * 
    * @param name
    *           Name give to the texture
    * @param inputStream
    *           Stream where the texture is
    * @return The texture auto update when loading is done
    * @throws IOException
    *            On reading problem
    */
   public Texture loadTexture(final String name, final InputStream inputStream) throws IOException
   {
      return this.loadTexture(name, inputStream, Color.GRAY);
   }

   /**
    * Launch a texture loading in streaming.<br>
    * The texture return will be update when loading is done.<br>
    * You can use it now.
    * 
    * @param name
    *           Name give to the texture
    * @param inputStream
    *           Stream where the texture is
    * @param defaultColor
    *           The color use on the texture until it is loading
    * @return The texture auto update when loading is done
    * @throws IOException
    *            On reading problem
    */
   public Texture loadTexture(final String name, final InputStream inputStream, final Color defaultColor) throws IOException
   {
      // Create Stream informations and the texture
      final Stream stream = new Stream();
      stream.inputStream = inputStream;
      stream.texture = new Texture(name, 1, 1, defaultColor);

      // Enqueue the stream on the list off texture to load
      this.streams.inQueue(stream);

      // Launch (if need) the loading
      this.launch();

      // The texture
      return stream.texture;
   }

   /**
    * Do the streaming
    */
   @Override
   public void run()
   {
      Stream stream;
      byte[] data;

      // For each stream
      while((this.thread != null) && (this.streams.isEmpty() == false))
      {
         // Get the stream
         stream = this.streams.outQueue();

         try
         {
            // Get data from stream
            data = this.readData(stream.inputStream);

            // Transfer data to texture
            if(stream.texture != null)
            {
               this.transferDataTo(data, stream.texture);
            }
         }
         catch(final Exception exception)
         {
            Debug.printException(exception);
         }

         stream = null;
         data = null;

         try
         {
            Thread.sleep(10);
         }
         catch(final Exception exception)
         {
         }
      }

      this.thread = null;
   }
}