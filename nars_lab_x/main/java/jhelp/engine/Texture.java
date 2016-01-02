/**
 */
package jhelp.engine;

import com.jogamp.opengl.GL;
import jhelp.engine.graphics.CompressiveImage;
import jhelp.engine.util.BufferUtils;
import jhelp.engine.util.ColorsUtil;
import jhelp.engine.util.Math3D;
import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.gui.JHelpImage;
import jhelp.util.list.EnumerationIterator;

import javax.imageio.ImageIO;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Texture, you can draw on the texture<br>
 * You can use mixing mode or over write mode<br>
 * Mixing mode mix the new draw with the old draw with taking care the new draw alpha.<br>
 * Over write mode replace pixels like new one all part are copied (red, green, blue and alpha) are copied as it <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Texture
{
   /** Textures table */
   private static Hashtable<String, Texture> hashtableTextures;
   /** Synchronization lock */
   private static final Object               LOCK                     = new Object();
   /** Next texture ID */
   private static int                        nextTextureID            = 0;

   /** Texture for pick UV */
   private static Texture                    textureForPickUV;

   /** Font render context */
   public final static FontRenderContext     CONTEXT                  = new FontRenderContext(Texture.TRANSFORM, false, false);
   /** Dummy texture */
   public static final Texture               DUMMY                    = new Texture("JHelpDummyTexture", 1, 1);

   /** Reference for buffered image */
   public static final String                REFERENCE_BUFFERED_IMAGE = "ReferenceBufferedImage";
   /** Reference for buffered icon */
   public static final String                REFERENCE_ICON           = "ReferenceIcon";
   /** Reference for image */
   public static final String                REFERENCE_IMAGE          = "ReferenceImage";
   /** Reference for GIF image */
   public static final String                REFERENCE_IMAGE_GIF      = "ReferenceImageGIF";
   /** Reference to a JHelpImage */
   public static final String                REFERENCE_JHELP_IMAGE    = "ReferenceJHelpImage";
   /** Reference for array pixels */
   public static final String                REFERENCE_PIXELS         = "ReferencePixels";
   /** Reference for resources */
   public static final String                REFERENCE_RESOURCES      = "ReferecneResources";
   /** Reference for video */
   public static final String                REFERENCE_VIDEO          = "ReferenceVideo";

   /** Identity transformation */
   public static final AffineTransform       TRANSFORM                = new AffineTransform();

   /**
    * Add two alpha values
    * 
    * @param alpha1
    *           First alpha
    * @param alpha2
    *           Second alpha
    * @return Sum
    */
   private static byte add(final byte alpha1, final byte alpha2)
   {
      final int al1 = alpha1 & 0xFF;
      final int al2 = alpha2 & 0xFF;
      int res = al1 + al2;
      if(res > 255)
      {
         res = 255;
      }
      return (byte) (res & 0xFF);
   }

   /**
    * Compute Bernouilli number at t time
    * 
    * @param n
    *           N
    * @param m
    *           M
    * @param t
    *           Time
    * @return Bernouilli number
    */
   private static double B(final int n, final int m, final double t)
   {
      return Texture.C(n, m) * Math.pow(t, m) * Math.pow(1d - t, n - m);
   }

   /**
    * Compute the number of combination of N element in M
    * 
    * @param n
    *           N
    * @param m
    *           M
    * @return Combination number
    */
   private static long C(final int n, final int m)
   {
      return Texture.factorial(n) / (Texture.factorial(m) * Texture.factorial(n - m));
   }

   /**
    * Compute <code>first + second - 128</code> limit in [0, 255]
    * 
    * @param b1
    *           First
    * @param b2
    *           Second
    * @return Result
    */
   private static byte cont(final byte b1, final byte b2)
   {
      final int i1 = b1 & 0xFF;
      final int i2 = b2 & 0xFF;
      final int i = (i1 + i2) - 128;
      if(i <= 0)
      {
         return (byte) 0;
      }
      if(i >= 255)
      {
         return (byte) 255;
      }
      return (byte) i;
   }

   /**
    * Divide to part color
    * 
    * @param b1
    *           First part
    * @param b2
    *           Second part
    * @return Result
    */
   private static byte div(final byte b1, final byte b2)
   {
      final float f1 = (b1 & 0xFF) / 255f;
      final float f2 = 1f - ((b2 & 0xFF) / 255f);
      return (byte) (f1 * f2 * 255);
   }

   /**
    * Compute the factorial of an integer
    * 
    * @param integer
    *           Integer
    * @return Factorial
    */
   private static long factorial(int integer)
   {
      if(integer < 2)
      {
         return 1;
      }
      long factorial = integer;
      integer--;
      while(integer > 1)
      {
         factorial *= integer;
         integer--;
      }
      return factorial;
   }

   /**
    * Multiply 2 color parts
    * 
    * @param b1
    *           First
    * @param b2
    *           Second
    * @return Result
    */
   private static byte mult(final byte b1, final byte b2)
   {
      final float f1 = (b1 & 0xFF) / 255f;
      final float f2 = (b2 & 0xFF) / 255f;
      return (byte) (f1 * f2 * 255);
   }

   /**
    * Compute cubic interpolation at a given time
    * 
    * @param cp
    *           Current value
    * @param p1
    *           First control value
    * @param p2
    *           Second control value
    * @param p3
    *           End value
    * @param t
    *           Interpolation time
    * @return Interpolated value
    */
   private static double PCubique(final double cp, final double p1, final double p2, final double p3, final double t)
   {
      return (Texture.B(3, 0, t) * cp) + (Texture.B(3, 1, t) * p1) + (Texture.B(3, 2, t) * p2) + (Texture.B(3, 3, t) * p3);
   }

   /**
    * Compute quadric interpolation at a given time
    * 
    * @param cp
    *           Current value
    * @param p1
    *           Control value
    * @param p2
    *           End value
    * @param t
    *           Interpolation time
    * @return Interpolated value
    */
   private static double PQuadrique(final double cp, final double p1, final double p2, final double t)
   {
      return (Texture.B(2, 0, t) * cp) + (Texture.B(2, 1, t) * p1) + (Texture.B(2, 2, t) * p2);
   }

   /**
    * Register a texture
    * 
    * @param texture
    *           Texture to register
    */
   private static void registerTexture(final Texture texture)
   {
      if(Texture.hashtableTextures == null)
      {
         Texture.hashtableTextures = new Hashtable<String, Texture>();
      }

      Texture.hashtableTextures.put(texture.textureName, texture);
   }

   /**
    * Subtract 2 color part
    * 
    * @param b1
    *           First
    * @param b2
    *           Second
    * @return Result
    */
   private static byte sub(final byte b1, final byte b2)
   {
      final int i1 = b1 & 0xFF;
      final int i2 = b2 & 0xFF;
      final int i = i1 - i2;
      if(i <= 0)
      {
         return (byte) 0;
      }
      return (byte) i;
   }

   /**
    * Unregister a texture
    * 
    * @param texture
    *           Texture to unregister
    */
   private static void unregisterTexture(final Texture texture)
   {
      if(Texture.hashtableTextures == null)
      {
         return;
      }

      Texture.hashtableTextures.remove(texture.textureName);
   }

   /**
    * Load texture from file
    * 
    * @param file
    *           Image file
    * @return The texture loaded
    */
   public static Texture load(final File file)
   {
      InputStream inputStream = null;

      try
      {
         inputStream = new FileInputStream(file);
         return new Texture(file.getAbsolutePath(), Texture.REFERENCE_IMAGE, inputStream);
      }
      catch(final Exception exception)
      {
         return Texture.DUMMY;
      }
      finally
      {
         if(inputStream != null)
         {
            try
            {
               inputStream.close();
            }
            catch(final Exception exception)
            {
            }
         }
      }
   }

   /**
    * Obtain a texture by its name
    * 
    * @param name
    *           Texture name
    * @return The texture or {@code null} if no texture with the given name
    */
   public static Texture obtainTexture(final String name)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }
      if(Texture.hashtableTextures == null)
      {
         return null;
      }
      return Texture.hashtableTextures.get(name);
   }

   /**
    * Texture for pick UV
    * 
    * @return Texture for pick UV
    */
   public static Texture obtainTextureForPickUV()
   {
      if(Texture.textureForPickUV != null)
      {
         return Texture.textureForPickUV;
      }

      Texture.textureForPickUV = new Texture("JHELP_TEXTURE_FOR_PICK_UV", 256, 256);

      int index = 0;

      for(int y = 0; y < 256; y++)
      {
         for(int x = 0; x < 256; x++)
         {
            Texture.textureForPickUV.pixels[index++] = (byte) 0;
            Texture.textureForPickUV.pixels[index++] = (byte) y;
            Texture.textureForPickUV.pixels[index++] = (byte) x;
            Texture.textureForPickUV.pixels[index++] = (byte) 255;
         }
      }

      return Texture.textureForPickUV;
   }

   /**
    * Compute interpolated values cubic for a given precision
    * 
    * @param cp
    *           Current value
    * @param p1
    *           First control value
    * @param p2
    *           Second control value
    * @param p3
    *           End value
    * @param precision
    *           Precision used
    * @return Interpolated values
    */
   public static double[] PCubiques(final double cp, final double p1, final double p2, final double p3, final int precision)
   {
      final double[] cub = new double[precision];
      final double step = 1.0 / (precision - 1.0);
      double actual = 0;
      for(int i = 0; i < precision; i++)
      {
         if(i == (precision - 1))
         {
            actual = 1.0;
         }
         cub[i] = Texture.PCubique(cp, p1, p2, p3, actual);
         actual += step;
      }
      return cub;
   }

   /**
    * Compute interpolated values quadric for a given precision
    * 
    * @param cp
    *           Current value
    * @param p1
    *           Control value
    * @param p2
    *           End value
    * @param precision
    *           Precision used
    * @return Interpolated values
    */
   public static double[] PQuadriques(final double cp, final double p1, final double p2, final int precision)
   {
      final double[] quad = new double[precision];
      final double step = 1.0 / (precision - 1.0);
      double actual = 0;
      for(int i = 0; i < precision; i++)
      {
         if(i == (precision - 1))
         {
            actual = 1.0;
         }
         quad[i] = Texture.PQuadrique(cp, p1, p2, actual);
         actual += step;
      }
      return quad;
   }

   /**
    * Forcec refresh all textures
    */
   public static void refreshAllTextures()
   {
      if(Texture.hashtableTextures == null)
      {
         return;
      }

      for(final Texture texture : new EnumerationIterator<Texture>(Texture.hashtableTextures.elements()))
      {
         texture.flush();
      }
   }

   /**
    * Rename a texture
    * 
    * @param texture
    *           Texture to rename
    * @param newName
    *           New name
    */
   public static void renameTexture(final Texture texture, String newName)
   {
      if(texture == null)
      {
         throw new NullPointerException("texture musn't be null");
      }
      if(newName == null)
      {
         throw new NullPointerException("newName musn't be null");
      }
      newName = newName.trim();
      if(newName.length() < 1)
      {
         throw new IllegalArgumentException("Name can't be empty");
      }
      if(texture.textureName.equals(newName) == true)
      {
         return;
      }
      Texture.hashtableTextures.remove(texture.textureName);
      texture.textureName = newName;
      Texture.hashtableTextures.put(newName, texture);
   }

   /** Developer additional information */
   private Object    additionalInformation;
   /** Indicates if auto flush is enable */
   private boolean   autoFlush;

   /** Indicates if the texture need to be refresh */
   private boolean   needToRefresh;

   /** Texture ID */
   private final int textureID;

   /** Texture name */
   private String    textureName;

   /** Texture reference */
   private String    textureReference;

   /** Texture's video memory ID */
   private int       videoMemoryId;

   /** Height of the texture */
   int               height;

   /** Texture pixels */
   byte[]            pixels;
   /** Texture's width */
   int               width;

   /**
    * Constructs Texture
    * 
    * @param name
    *           Texture name
    * @param reference
    *           Texture reference
    */
   Texture(String name, final String reference)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }
      name = name.trim();
      if(name.length() < 1)
      {
         throw new IllegalArgumentException("Name can't be empty");
      }
      this.textureName = name;
      this.textureReference = reference;
      this.textureID = Texture.nextTextureID++;
      this.autoFlush = true;
      Texture.registerTexture(this);
   }

   /**
    * Constructs Texture
    * 
    * @param name
    *           Texture name
    * @param bufferedImage
    *           Base image
    */
   public Texture(final String name, final BufferedImage bufferedImage)
   {
      this(name, Texture.REFERENCE_BUFFERED_IMAGE);
      this.videoMemoryId = -1;
      this.needToRefresh = true;
      final int width = bufferedImage.getWidth();
      final int height = bufferedImage.getHeight();
      int[] pixels = new int[width * height];
      pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
      this.setPixels(width, height, pixels);
      this.autoFlush = true;
   }

   /**
    * Constructs Texture
    * 
    * @param name
    *           Texture name
    * @param icon
    *           Base icon
    */
   public Texture(final String name, final Icon icon)
   {
      this(name, Texture.REFERENCE_ICON);
      this.videoMemoryId = -1;
      this.needToRefresh = true;
      final int width = icon.getIconWidth();
      final int height = icon.getIconHeight();
      final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      icon.paintIcon(null, bufferedImage.createGraphics(), 0, 0);
      bufferedImage.flush();
      int[] pixels = new int[width * height];
      pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);
      this.setPixels(width, height, pixels);
      this.autoFlush = true;
   }

   /**
    * Constructs Texture
    * 
    * @param name
    *           Texture name
    * @param image
    *           Base image
    */
   public Texture(final String name, final Image image)
   {
      this(name, Texture.REFERENCE_IMAGE);
      this.videoMemoryId = -1;
      this.needToRefresh = true;
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
      this.setPixels(width, height, pixels);
      this.autoFlush = true;
   }

   /**
    * Constructs empty Texture
    * 
    * @param name
    *           Texture name
    * @param width
    *           Width
    * @param height
    *           Height
    */
   public Texture(final String name, final int width, final int height)
   {
      this(name, width, height, new byte[width * height * 4]);
      this.autoFlush = true;
   }

   /**
    * Constructs Texture
    * 
    * @param name
    *           Texture name
    * @param width
    *           Width
    * @param height
    *           Height
    * @param pixels
    *           Pixels
    */
   public Texture(final String name, final int width, final int height, final byte[] pixels)
   {
      this(name, Texture.REFERENCE_PIXELS);
      this.videoMemoryId = -1;
      this.needToRefresh = true;
      this.setPixels(width, height, pixels);
      this.autoFlush = true;
   }

   /**
    * Constructs fill color Texture
    * 
    * @param name
    *           Texture name
    * @param width
    *           Width
    * @param height
    *           Height
    * @param color
    *           Color for fill texture
    */
   public Texture(final String name, final int width, final int height, final Color color)
   {
      this(name, width, height, new byte[width * height * 4]);
      this.fillRect(0, 0, width, height, color, false);
      this.needToRefresh = true;
      this.autoFlush = true;
   }

   /**
    * Constructs fill color Texture
    * 
    * @param name
    *           Texture name
    * @param width
    *           Width
    * @param height
    *           Height
    * @param color
    *           Color for fill texture
    */
   public Texture(final String name, final int width, final int height, final int color)
   {
      this(name, width, height, new Color(color, true));
      this.autoFlush = true;
   }

   /**
    * Constructs Texture
    * 
    * @param name
    *           Texture name
    * @param width
    *           Width
    * @param height
    *           Height
    * @param pixels
    *           Pixels
    */
   public Texture(final String name, final int width, final int height, final int[] pixels)
   {
      this(name, Texture.REFERENCE_PIXELS);
      this.videoMemoryId = -1;
      this.needToRefresh = true;
      this.setPixels(width, height, pixels);
      this.autoFlush = true;
   }

   /**
    * Create a new instance of Texture based on JHelpImage
    * 
    * @param name
    *           Texture name
    * @param image
    *           Image to base on
    */
   public Texture(final String name, final JHelpImage image)
   {
      this(name, Texture.REFERENCE_JHELP_IMAGE);
      this.videoMemoryId = -1;
      this.needToRefresh = true;
      final int width = image.getWidth();
      final int height = image.getHeight();
      final int[] pixels = image.getPixels(0, 0, width, height);
      this.setPixels(width, height, pixels);
      this.autoFlush = true;
   }

   /**
    * Constructs Texture
    * 
    * @param name
    *           Texture name
    * @param reference
    *           Texture reference
    * @param inputStream
    *           Stream to read the texture
    * @throws IOException
    *            On reading problem
    */
   public Texture(final String name, final String reference, final InputStream inputStream)
         throws IOException
   {
      this(name, ImageIO.read(inputStream));
      this.textureReference = reference;
      this.autoFlush = true;
   }

   /**
    * Add two alpha values
    * 
    * @param alpha1
    *           First alpha
    * @param al2
    *           Second pre-computed alpha
    * @return Sum
    */
   private byte add(final byte alpha1, final int al2)
   {
      final int al1 = alpha1 & 0xFF;
      int res = al1 + al2;
      if(res > 255)
      {
         res = 255;
      }
      return (byte) (res & 0xFF);
   }

   /**
    * Make a color part brighter
    * 
    * @param part
    *           Part color
    * @param rate
    *           Bright rate
    * @return Result
    */
   private byte bright(final byte part, final float rate)
   {
      final int i = (int) ((part & 0xFF) + (rate * 255f));

      if(i >= 255)
      {
         return (byte) 255;
      }

      return (byte) i;
   }

   /**
    * Make a part color darker
    * 
    * @param part
    *           Color part
    * @param rate
    *           Dark rate
    * @return Result
    */
   private byte dark(final byte part, final float rate)
   {
      final int i = (int) ((part & 0xFF) - (rate * 255f));

      if(i <= 0)
      {
         return (byte) 0;
      }

      return (byte) i;
   }

   /**
    * Draw a path iterator
    * 
    * @param path
    *           Path to draw
    * @param color
    *           Color used
    * @param mix
    *           Indicated if we on or off mixing mode
    * @param precision
    *           Path precision
    */
   private void draw(final PathIterator path, final Color color, final boolean mix, final int precision)
   {
      double x, y, dx, dy;
      x = y = dx = dy = 0;
      final double[] coords = new double[6];
      double[] xx = null;
      double[] yy = null;
      while(!path.isDone())
      {
         final int code = path.currentSegment(coords);
         switch(code)
         {
            case PathIterator.SEG_CLOSE:
               this.drawLine((int) x, (int) y, (int) dx, (int) dy, color, mix);
               x = dx;
               y = dy;
            break;
            case PathIterator.SEG_MOVETO:
               x = dx = coords[0];
               y = dy = coords[1];
            break;
            case PathIterator.SEG_LINETO:
               this.drawLine((int) x, (int) y, (int) coords[0], (int) coords[1], color, mix);
               x = coords[0];
               y = coords[1];
            break;
            case PathIterator.SEG_QUADTO:
               xx = Texture.PQuadriques(x, coords[0], coords[2], precision);
               yy = Texture.PQuadriques(y, coords[1], coords[3], precision);
               this.draws(xx, yy, color, mix);
               x = xx[precision - 1];
               y = yy[precision - 1];
            break;
            case PathIterator.SEG_CUBICTO:
               xx = Texture.PCubiques(x, coords[0], coords[2], coords[4], precision);
               yy = Texture.PCubiques(y, coords[1], coords[3], coords[5], precision);
               this.draws(xx, yy, color, mix);
               x = xx[precision - 1];
               y = yy[precision - 1];
            break;
         }
         path.next();
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Draw an image in pixels array
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param pixels
    *           Image's pixels
    * @param width
    *           Image's width
    * @param height
    *           Image's height
    * @param mayTransparent
    *           Indicates if image can have alpha value
    */
   private void drawImage(int x, int y, final byte[] pixels, int width, int height, final boolean mayTransparent)
   {
      int imageWidth;
      int pixThis;
      int pixImage;
      byte alpha;
      int yThis;
      int yImage;
      int xx;
      int yy;

      imageWidth = width;

      if(x < 0)
      {
         width += x;
         x = 0;
      }
      if(x >= this.width)
      {
         return;
      }
      if((x + width) > this.width)
      {
         width = this.width - x;
      }
      if(width < 1)
      {
         return;
      }
      if(y < 0)
      {
         height += y;
         y = 0;
      }
      if(y >= this.height)
      {
         return;
      }
      if((y + height) > this.height)
      {
         height = this.height - y;
      }
      if(height < 1)
      {
         return;
      }

      if(mayTransparent == true)
      {
         yThis = x + (y * this.width);
         yImage = 0;
         for(yy = 0; yy < height; yy++)
         {
            pixThis = yThis;
            pixImage = yImage;
            for(xx = 0; xx < width; xx++)
            {
               alpha = pixels[(pixImage * 4) + 3];

               this.pixels[pixThis * 4] = this.mix(this.pixels[pixThis * 4], pixels[pixImage * 4], alpha);
               this.pixels[(pixThis * 4) + 1] = this.mix(this.pixels[(pixThis * 4) + 1], pixels[(pixImage * 4) + 1], alpha);
               this.pixels[(pixThis * 4) + 2] = this.mix(this.pixels[(pixThis * 4) + 2], pixels[(pixImage * 4) + 2], alpha);
               this.pixels[(pixThis * 4) + 3] = Texture.add(this.pixels[(pixThis * 4) + 3], alpha);

               pixThis++;
               pixImage++;
            }
            yThis += this.width;
            yImage += imageWidth;
         }
      }
      else
      {
         yThis = x + (y * this.width);
         yImage = 0;
         for(yy = 0; yy < height; yy++)
         {
            pixThis = yThis;
            pixImage = yImage;
            for(xx = 0; xx < width; xx++)
            {
               this.pixels[pixThis * 4] = pixels[pixImage * 4];
               this.pixels[(pixThis * 4) + 1] = pixels[(pixImage * 4) + 1];
               this.pixels[(pixThis * 4) + 2] = pixels[(pixImage * 4) + 2];
               this.pixels[(pixThis * 4) + 3] = (byte) 255;

               pixThis++;
               pixImage++;
            }

            yThis += this.width;
            yImage += imageWidth;
         }
      }

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Draw an image in pixels array
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param pixels
    *           Image's pixels
    * @param width
    *           Image's width
    * @param height
    *           Image's height
    * @param mayTransparent
    *           Indicates if image can have alpha value
    */
   private void drawImage(final int x, final int y, final int[] pixels, final int width, final int height, final boolean mayTransparent)
   {
      byte[] bytePixels;
      int nb;
      int index;
      int pix;
      int color;

      nb = width * height;
      bytePixels = new byte[nb * 4];

      pix = 0;
      for(index = 0; index < nb; index++)
      {
         color = pixels[index];
         bytePixels[pix++] = (byte) ((color >> 16) & 0xFF);
         bytePixels[pix++] = (byte) ((color >> 8) & 0xFF);
         bytePixels[pix++] = (byte) (color & 0xFF);
         bytePixels[pix++] = (byte) ((color >> 24) & 0xFF);
      }

      this.drawImage(x, y, bytePixels, width, height, mayTransparent);

      bytePixels = null;
   }

   /**
    * Draw line in mixing mode
    * 
    * @param x1
    *           X1
    * @param y1
    *           Y1
    * @param x2
    *           X2
    * @param y2
    *           Y2
    * @param r
    *           Red
    * @param g
    *           Green
    * @param b
    *           Blue
    * @param a
    *           Alpha
    */
   private void drawLineWithMix(int x1, int y1, final int x2, final int y2, final byte r, final byte g, final byte b, final byte a)
   {
      final int red = r & 0xFF;
      final int green = g & 0xFF;
      final int blue = b & 0xFF;
      final int alp = a & 0xFF;
      final int pla = 255 - alp;
      int p;
      int dx = x2 - x1;
      int sx = 1;
      if(dx < 0)
      {
         dx = -dx;
         sx = -1;
      }
      int dy = y2 - y1;
      int sy = 1;
      if(dy < 0)
      {
         dy = -dy;
         sy = -1;
      }
      if(dx >= dy)
      {
         int reste = 0;
         if((x1 >= 0) && (x1 < this.width) && (y1 >= 0) && (y1 < this.height))
         {
            p = 4 * (x1 + (y1 * this.width));
            this.pixels[p] = this.mix(this.pixels[p], red, alp, pla);
            this.pixels[p + 1] = this.mix(this.pixels[p + 1], green, alp, pla);
            this.pixels[p + 2] = this.mix(this.pixels[p + 2], blue, alp, pla);
            this.pixels[p + 3] = this.add(this.pixels[p + 3], alp);
         }
         while(x1 != x2)
         {
            x1 += sx;
            reste += dy;
            if(reste >= dx)
            {
               y1 += sy;
               reste -= dx;
            }
            if((x1 >= 0) && (x1 < this.width) && (y1 >= 0) && (y1 < this.height))
            {
               p = 4 * (x1 + (y1 * this.width));
               this.pixels[p] = this.mix(this.pixels[p], red, alp, pla);
               this.pixels[p + 1] = this.mix(this.pixels[p + 1], green, alp, pla);
               this.pixels[p + 2] = this.mix(this.pixels[p + 2], blue, alp, pla);
               this.pixels[p + 3] = this.add(this.pixels[p + 3], alp);
            }
         }
      }
      else
      {
         int reste = 0;
         if((x1 >= 0) && (x1 < this.width) && (y1 >= 0) && (y1 < this.height))
         {
            p = 4 * (x1 + (y1 * this.width));
            this.pixels[p] = this.mix(this.pixels[p], red, alp, pla);
            this.pixels[p + 1] = this.mix(this.pixels[p + 1], green, alp, pla);
            this.pixels[p + 2] = this.mix(this.pixels[p + 2], blue, alp, pla);
            this.pixels[p + 3] = this.add(this.pixels[p + 3], alp);
         }
         while(y1 != y2)
         {
            y1 += sy;
            reste += dx;
            if(reste >= dy)
            {
               x1 += sx;
               reste -= dy;
            }
            if((x1 >= 0) && (x1 < this.width) && (y1 >= 0) && (y1 < this.height))
            {
               p = 4 * (x1 + (y1 * this.width));
               this.pixels[p] = this.mix(this.pixels[p], red, alp, pla);
               this.pixels[p + 1] = this.mix(this.pixels[p + 1], green, alp, pla);
               this.pixels[p + 2] = this.mix(this.pixels[p + 2], blue, alp, pla);
               this.pixels[p + 3] = this.add(this.pixels[p + 3], alp);
            }
         }
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Draw line on over write mode
    * 
    * @param x1
    *           X1
    * @param y1
    *           Y1
    * @param x2
    *           X2
    * @param y2
    *           Y2
    * @param r
    *           Red
    * @param g
    *           Green
    * @param b
    *           Blue
    * @param a
    *           Alpha
    */
   private void drawLineWithoutMix(int x1, int y1, final int x2, final int y2, final byte r, final byte g, final byte b, final byte a)
   {
      int p;
      int dx = x2 - x1;
      int sx = 1;
      if(dx < 0)
      {
         dx = -dx;
         sx = -1;
      }
      int dy = y2 - y1;
      int sy = 1;
      if(dy < 0)
      {
         dy = -dy;
         sy = -1;
      }
      if(dx >= dy)
      {
         int reste = 0;
         if((x1 >= 0) && (x1 < this.width) && (y1 >= 0) && (y1 < this.height))
         {
            p = 4 * (x1 + (y1 * this.width));
            this.pixels[p] = r;
            this.pixels[p + 1] = g;
            this.pixels[p + 2] = b;
            this.pixels[p + 3] = a;
         }
         while(x1 != x2)
         {
            x1 += sx;
            reste += dy;
            if(reste >= dx)
            {
               y1 += sy;
               reste -= dx;
            }
            if((x1 >= 0) && (x1 < this.width) && (y1 >= 0) && (y1 < this.height))
            {
               p = 4 * (x1 + (y1 * this.width));
               this.pixels[p] = r;
               this.pixels[p + 1] = g;
               this.pixels[p + 2] = b;
               this.pixels[p + 3] = a;
            }
         }
      }
      else
      {
         int reste = 0;
         if((x1 >= 0) && (x1 < this.width) && (y1 >= 0) && (y1 < this.height))
         {
            p = 4 * (x1 + (y1 * this.width));
            this.pixels[p] = r;
            this.pixels[p + 1] = g;
            this.pixels[p + 2] = b;
            this.pixels[p + 3] = a;
         }
         while(y1 != y2)
         {
            y1 += sy;
            reste += dx;
            if(reste >= dy)
            {
               x1 += sx;
               reste -= dy;
            }
            if((x1 >= 0) && (x1 < this.width) && (y1 >= 0) && (y1 < this.height))
            {
               p = 4 * (x1 + (y1 * this.width));
               this.pixels[p] = r;
               this.pixels[p + 1] = g;
               this.pixels[p + 2] = b;
               this.pixels[p + 3] = a;
            }
         }
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Draw a set off lines
    * 
    * @param x
    *           X array
    * @param y
    *           Y array
    * @param color
    *           Color used
    * @param mix
    *           Indicates if we use mixing mode
    */
   private void draws(final double[] x, final double[] y, final Color color, final boolean mix)
   {
      final int l = x.length;
      for(int i = 1; i < l; i++)
      {
         this.drawLine((int) x[i - 1], (int) y[i - 1], (int) x[i], (int) y[i], color, mix);
      }
   }

   /**
    * Fill rectangle in mixing mode
    * 
    * @param r
    *           Red
    * @param g
    *           Green
    * @param b
    *           Blue
    * @param a
    *           Alpha
    * @param x
    *           X
    * @param y
    *           Y
    * @param width
    *           Width
    * @param height
    *           Height
    */
   private void fillRectWithMix(final byte r, final byte g, final byte b, final byte a, final int x, final int y, final int width, final int height)
   {
      int line = (x + (y * this.width)) * 4;
      int index;
      final int red = r & 0xFF;
      final int green = g & 0xFF;
      final int blue = b & 0xFF;
      final int alp = a & 0xFF;
      final int pla = 255 - alp;
      final int w = this.width << 2;

      for(int yy = 0, xx = 0; yy < height; yy++)
      {
         index = line;
         for(xx = 0; xx < width; xx++)
         {
            this.pixels[index] = this.mix(this.pixels[index], red, alp, pla);
            this.pixels[index + 1] = this.mix(this.pixels[index + 1], green, alp, pla);
            this.pixels[index + 2] = this.mix(this.pixels[index + 2], blue, alp, pla);
            this.pixels[index + 3] = this.add(this.pixels[index + 3], alp);
            index += 4;
         }
         line += w;
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Fill rectangle with gradient and on mixing alpha
    * 
    * @param redTopLeft
    *           Red top left
    * @param greenTopLeft
    *           Green top left
    * @param blueTopLeft
    *           Blue top left
    * @param alphaTopLeft
    *           Alpha top left
    * @param redTopRight
    *           Red top right
    * @param greenTopRight
    *           Green top right
    * @param blueTopRight
    *           Blue top right
    * @param alphaTopRight
    *           Alpha Top right
    * @param redBottomLeft
    *           Red bottom left
    * @param greenBottomLeft
    *           Green bottom left
    * @param blueBottomLeft
    *           Blue bottom left
    * @param alphaBottomLeft
    *           Alpha bottom left
    * @param redBottomRight
    *           Red bottom right
    * @param greenBottomRight
    *           Green bottom right
    * @param blueBottomRight
    *           Blue bottom right
    * @param alphaBottomRight
    *           Alpha bottom right
    * @param x
    *           X top left corner
    * @param y
    *           Y top left corner
    * @param width
    *           Width
    * @param height
    *           Height
    */
   private void fillRectWithMix(final int redTopLeft, final int greenTopLeft, final int blueTopLeft, final int alphaTopLeft,//
         final int redTopRight, final int greenTopRight, final int blueTopRight, final int alphaTopRight,//
         final int redBottomLeft, final int greenBottomLeft, final int blueBottomLeft, final int alphaBottomLeft,//
         final int redBottomRight, final int greenBottomRight, final int blueBottomRight, final int alphaBottomRight, //
         final int x, final int y, final int width, final int height)
   {
      int line = (x + (y * this.width)) << 2;
      int index;
      final int w = this.width << 2;

      final int div = (height - 1) * (width - 1);
      int alp, pla;

      for(int yy = 0, ay = height - 1; yy < height; yy++, ay--)
      {
         index = line;
         for(int xx = 0, ax = width - 1; xx < width; xx++, ax--)
         {
            alp = (((((alphaTopLeft * ax) + (alphaTopRight * xx)) * ay) + (((alphaBottomLeft * ax) + (alphaBottomRight * xx)) * yy)) / div);
            pla = 255 - alp;

            this.pixels[index] = this.mix(this.pixels[index],
                  (((((redTopLeft * ax) + (redTopRight * xx)) * ay) + (((redBottomLeft * ax) + (redBottomRight * xx)) * yy)) / div), alp, pla);
            this.pixels[index + 1] = this.mix(this.pixels[index + 1],
                  (((((greenTopLeft * ax) + (greenTopRight * xx)) * ay) + (((greenBottomLeft * ax) + (greenBottomRight * xx)) * yy)) / div), alp, pla);
            this.pixels[index + 2] = this.mix(this.pixels[index + 2],
                  (((((blueTopLeft * ax) + (blueTopRight * xx)) * ay) + (((blueBottomLeft * ax) + (blueBottomRight * xx)) * yy)) / div), alp, pla);
            this.pixels[index + 3] = this.add(this.pixels[index + 3], alp);

            index += 4;
         }
         line += w;
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Draw rectangle in over write mode
    * 
    * @param r
    *           Red
    * @param g
    *           Green
    * @param b
    *           Blue
    * @param a
    *           Alpha
    * @param x
    *           X
    * @param y
    *           Y
    * @param width
    *           Width
    * @param height
    *           Height
    */
   private void fillRectWithoutMix(final byte r, final byte g, final byte b, final byte a, final int x, final int y, final int width, final int height)
   {
      int line = (x + (y * this.width)) << 2;
      int index;
      final int w = this.width << 2;

      for(int yy = 0; yy < height; yy++)
      {
         index = line;
         for(int xx = 0; xx < width; xx++)
         {
            this.pixels[index++] = r;
            this.pixels[index++] = g;
            this.pixels[index++] = b;
            this.pixels[index++] = a;
         }
         line += w;
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Fill rectangle with gradient and not mixing alpha
    * 
    * @param redTopLeft
    *           Red top left
    * @param greenTopLeft
    *           Green top left
    * @param blueTopLeft
    *           Blue top left
    * @param alphaTopLeft
    *           Alpha top left
    * @param redTopRight
    *           Red top right
    * @param greenTopRight
    *           Green top right
    * @param blueTopRight
    *           Blue top right
    * @param alphaTopRight
    *           Alpha Top right
    * @param redBottomLeft
    *           Red bottom left
    * @param greenBottomLeft
    *           Green bottom left
    * @param blueBottomLeft
    *           Blue bottom left
    * @param alphaBottomLeft
    *           Alpha bottom left
    * @param redBottomRight
    *           Red bottom right
    * @param greenBottomRight
    *           Green bottom right
    * @param blueBottomRight
    *           Blue bottom right
    * @param alphaBottomRight
    *           Alpha bottom right
    * @param x
    *           X top left corner
    * @param y
    *           Y top left corner
    * @param width
    *           Width
    * @param height
    *           Height
    */
   private void fillRectWithoutMix(final int redTopLeft, final int greenTopLeft, final int blueTopLeft, final int alphaTopLeft,//
         final int redTopRight, final int greenTopRight, final int blueTopRight, final int alphaTopRight,//
         final int redBottomLeft, final int greenBottomLeft, final int blueBottomLeft, final int alphaBottomLeft,//
         final int redBottomRight, final int greenBottomRight, final int blueBottomRight, final int alphaBottomRight, //
         final int x, final int y, final int width, final int height)
   {
      int line = (x + (y * this.width)) << 2;
      int index;
      final int w = this.width << 2;

      final int div = (height - 1) * (width - 1);

      for(int yy = 0, ay = height - 1; yy < height; yy++, ay--)
      {
         index = line;
         for(int xx = 0, ax = width - 1; xx < width; xx++, ax--)
         {
            this.pixels[index++] = (byte) (((((redTopLeft * ax) + (redTopRight * xx)) * ay) + (((redBottomLeft * ax) + (redBottomRight * xx)) * yy)) / div);
            this.pixels[index++] = (byte) (((((greenTopLeft * ax) + (greenTopRight * xx)) * ay) + (((greenBottomLeft * ax) + (greenBottomRight * xx)) * yy)) / div);
            this.pixels[index++] = (byte) (((((blueTopLeft * ax) + (blueTopRight * xx)) * ay) + (((blueBottomLeft * ax) + (blueBottomRight * xx)) * yy)) / div);
            this.pixels[index++] = (byte) (((((alphaTopLeft * ax) + (alphaTopRight * xx)) * ay) + (((alphaBottomLeft * ax) + (alphaBottomRight * xx)) * yy)) / div);
         }
         line += w;
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Fill a shape in mixing mode
    * 
    * @param shape
    *           Shape to fill
    * @param r
    *           Red
    * @param g
    *           Green
    * @param b
    *           Blue
    * @param a
    *           Alpha
    * @param x
    *           X
    * @param y
    *           Y
    * @param width
    *           Width
    * @param height
    *           Height
    */
   private void fillWithMix(final Shape shape, final byte r, final byte g, final byte b, final byte a, final int x, final int y, final int width,
         final int height)
   {
      int line = (x + (y * this.width)) << 2;
      int index;
      final int red = r & 0xFF;
      final int green = g & 0xFF;
      final int blue = b & 0xFF;
      final int alp = a & 0xFF;
      final int pla = 255 - alp;
      final int w = this.width << 2;

      for(int yy = 0, xx = 0; yy < height; yy++)
      {
         index = line;
         for(xx = 0; xx < width; xx++)
         {
            if(shape.contains(xx + x, yy + y) == true)
            {
               this.pixels[index] = this.mix(this.pixels[index], red, alp, pla);
               this.pixels[index + 1] = this.mix(this.pixels[index + 1], green, alp, pla);
               this.pixels[index + 2] = this.mix(this.pixels[index + 2], blue, alp, pla);
               this.pixels[index + 3] = this.add(this.pixels[index + 3], alp);
            }
            index += 4;
         }
         line += w;
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Fill a shape in over write mode
    * 
    * @param shape
    *           Shape to fill
    * @param r
    *           Red
    * @param g
    *           Green
    * @param b
    *           Blue
    * @param a
    *           Alpha
    * @param x
    *           X
    * @param y
    *           Y
    * @param width
    *           Width
    * @param height
    *           Height
    */
   private void fillWithoutMix(final Shape shape, final byte r, final byte g, final byte b, final byte a, final int x, final int y, final int width,
         final int height)
   {
      int line = (x + (y * this.width)) * 4;
      int index;
      for(int yy = 0; yy < height; yy++)
      {
         index = line;
         for(int xx = 0; xx < width; xx++)
         {
            if(shape.contains(xx + x, yy + y) == true)
            {
               this.pixels[index++] = r;
               this.pixels[index++] = g;
               this.pixels[index++] = b;
               this.pixels[index++] = a;
            }
            else
            {
               index += 4;
            }
         }
         line += this.width * 4;
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Limit a number in [0, 255]
    * 
    * @param value
    *           Value to limit
    * @return Result
    */
   private int limite0_255(final double value)
   {
      final int integer = (int) value;

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
    * Mix two color part
    * 
    * @param p1
    *           First part
    * @param p2
    *           Second part
    * @param alpha
    *           Alpha for mix
    * @return Mixed value
    */
   private byte mix(final byte p1, final byte p2, final byte alpha)
   {
      final int par1 = p1 & 0xFF;
      final int par2 = p2 & 0xFF;
      final int alp = alpha & 0xFF;
      final int pla = 255 - alp;
      final int res = ((par1 * pla) + (par2 * alp)) / 255;
      return (byte) (res & 0xFF);
   }

   /**
    * Mix two part color
    * 
    * @param p1
    *           First part
    * @param par2
    *           Second pre-computed part
    * @param alp
    *           Alpha
    * @param pla
    *           1-Alpha
    * @return Mixed value
    */
   private byte mix(final byte p1, final int par2, final int alp, final int pla)
   {
      final int par1 = p1 & 0xFF;
      final int res = ((par1 * pla) + (par2 * alp)) / 255;
      return (byte) (res & 0xFF);
   }

   /**
    * Remove texture from video memory
    * 
    * @param gl
    *           OpenGL context
    */
   void removeFromMemory(final GL gl)
   {
      if(this.videoMemoryId >= 0)
      {
         BufferUtils.TEMPORARY_INT_BUFFER.rewind();
         BufferUtils.TEMPORARY_INT_BUFFER.put(this.videoMemoryId);
         BufferUtils.TEMPORARY_INT_BUFFER.rewind();
         gl.glDeleteTextures(1, BufferUtils.TEMPORARY_INT_BUFFER);

         this.videoMemoryId = -1;
      }

      this.destroy();
   }

   /**
    * Get pixels inside the byte buffer to create the new texture content
    * 
    * @param width
    *           New texture width
    * @param height
    *           New texture height
    */
   void setPixelsFromByteBuffer(final int width, final int height)
   {
      BufferUtils.TEMPORARY_BYTE_BUFFER.rewind();
      this.width = width;
      this.height = height;
      final int nb = (width * height) << 2;
      this.pixels = new byte[nb];

      for(int i = 0; i < nb; i++)
      {
         this.pixels[i] = (byte) (BufferUtils.TEMPORARY_BYTE_BUFFER.get() << 1);
      }

      BufferUtils.TEMPORARY_BYTE_BUFFER.rewind();
      this.needToRefresh = true;
   }

   /**
    * Get pixels inside the float buffer to create the new texture content
    * 
    * @param width
    *           New texture width
    * @param height
    *           New texture height
    */
   void setPixelsFromFloatBuffer(final int width, final int height)
   {
      BufferUtils.TEMPORARY_FLOAT_BUFFER.rewind();
      this.width = width;
      this.height = height;
      final int nb = (width * height) << 2;
      this.pixels = new byte[nb];

      for(int i = 0; i < nb; i++)
      {
         this.pixels[i] = (byte) (BufferUtils.TEMPORARY_FLOAT_BUFFER.get() * 255f);
      }
      BufferUtils.TEMPORARY_FLOAT_BUFFER.rewind();
      this.needToRefresh = true;
   }

   /**
    * Add a texture (pixel by pixel).<br>
    * Added texture must have same dimensions
    * 
    * @param texture
    *           Texture to add
    */
   public void addTexture(final Texture texture)
   {
      if((texture.width != this.width) || (texture.height != this.height))
      {
         throw new IllegalArgumentException("The given texture must have smae size than this texture");
      }

      final int nb = this.width * this.height;
      int index = 0;

      for(int i = 0; i < nb; i++)
      {
         this.pixels[index] = Texture.add(this.pixels[index], texture.pixels[index]);
         index++;
         this.pixels[index] = Texture.add(this.pixels[index], texture.pixels[index]);
         index++;
         this.pixels[index] = Texture.add(this.pixels[index], texture.pixels[index]);
         index += 2;
      }

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Draw the texture on OpenGL
    * 
    * @param gl
    *           OpenGL context
    */
   public void bind(final GL gl)
   {
      // If no video memory ID, create it
      if(this.videoMemoryId < 0)
      {
         BufferUtils.TEMPORARY_INT_BUFFER.rewind();
         BufferUtils.TEMPORARY_INT_BUFFER.put(1);
         BufferUtils.TEMPORARY_INT_BUFFER.rewind();
         gl.glGenTextures(1, BufferUtils.TEMPORARY_INT_BUFFER);
         BufferUtils.TEMPORARY_INT_BUFFER.rewind();
         this.videoMemoryId = BufferUtils.TEMPORARY_INT_BUFFER.get();
      }
      // If the texture need to be refresh
      if(this.needToRefresh == true)
      {
         // Push pixels in video memory
         gl.glBindTexture(GL.GL_TEXTURE_2D, this.videoMemoryId);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
         gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, this.width, this.height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, BufferUtils.transferByte(this.pixels));
      }
      // Draw the texture
      gl.glBindTexture(GL.GL_TEXTURE_2D, this.videoMemoryId);

      this.needToRefresh = false;
   }

   /**
    * Make the texture brighter
    * 
    * @param rate
    *           Bright rate
    */
   public void brighter(final float rate)
   {
      final int nb = this.width * this.height;
      int index = 0;

      for(int i = 0; i < nb; i++)
      {
         this.pixels[index] = this.bright(this.pixels[index], rate);
         index++;
         this.pixels[index] = this.bright(this.pixels[index], rate);
         index++;
         this.pixels[index] = this.bright(this.pixels[index], rate);
         index += 2;
      }

      this.needToRefresh = true;
   }

   /**
    * Change texture bright
    * 
    * @param factor
    *           Factor for bright 0>factor>1 : darker, >1 brighter
    */
   public void changeBright(final float factor)
   {
      int r;
      int g;
      int b;
      final int nb = this.width * this.height * 4;
      double y;
      double u;
      double v;
      for(int i = 0; i < nb; i += 4)
      {
         r = this.pixels[i] & 0xFF;
         g = this.pixels[i + 1] & 0xFF;
         b = this.pixels[i + 2] & 0xFF;
         //
         y = (r * 0.299) + (g * 0.587) + (b * 0.114);
         u = ((-0.169 * r) - (0.331 * g)) + (0.500 * b) + 128.0;
         v = ((0.500 * r) - (0.419 * g) - (0.081 * b)) + 128.0;
         //
         y *= factor;
         //
         r = ColorsUtil.limite0_255((y - (0.0009267 * (u - 128))) + (1.4016868 * (v - 128)));
         g = ColorsUtil.limite0_255(y - (0.3436954 * (u - 128)) - (0.7141690 * (v - 128)));
         b = ColorsUtil.limite0_255(y + (1.7721604 * (u - 128)) + (0.0009902 * (v - 128)));
         //
         this.pixels[i] = (byte) r;
         this.pixels[i + 1] = (byte) g;
         this.pixels[i + 2] = (byte) b;
      }

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Clear the all texture with given color
    * 
    * @param color
    *           Color to fill the texture
    */
   public void clear(final Color color)
   {
      final byte r = (byte) color.getRed();
      final byte g = (byte) color.getGreen();
      final byte b = (byte) color.getBlue();
      final byte a = (byte) color.getAlpha();
      final int nb = this.width * this.height;
      int pix = 0;

      for(int i = 0; i < nb; i++)
      {
         this.pixels[pix++] = r;
         this.pixels[pix++] = g;
         this.pixels[pix++] = b;
         this.pixels[pix++] = a;
      }
   }

   /**
    * Change texture contrast.<br>
    * If factor is :
    * <ul>
    * <li><code>0 &lt;= factor &lt;1</code> : contrast go down</li>
    * <li><code>factor == 1</code> : contrast not change</li>
    * <li><code>factor &gt; 1</code> : contrast go up</li>
    * </ul>
    * 
    * @param factor
    *           Factor contrast
    */
   public void contrast(final float factor)
   {
      int index = 0;

      int rouge = this.pixels[index++] & 0xFF;
      int vert = this.pixels[index++] & 0xFF;
      int bleu = this.pixels[index++] & 0xFF;

      index++;

      double ymin = (0.299 * rouge) + (0.587 * vert) + (0.114 * bleu);
      double ymax = ymin;
      final int nb = this.width * this.height;

      for(int i = 1; i < nb; i++)
      {
         rouge = this.pixels[index++] & 0xFF;
         vert = this.pixels[index++] & 0xFF;
         bleu = this.pixels[index++] & 0xFF;
         index++;

         final double y = (0.299 * rouge) + (0.587 * vert) + (0.114 * bleu);
         if(y < ymin)
         {
            ymin = y;
         }
         if(y > ymax)
         {
            ymax = y;
         }
      }

      index = 0;
      final double ymil = (ymax + ymin) / 2d;

      for(int i = 0; i < nb; i++)
      {
         rouge = this.pixels[index] & 0xFF;
         vert = this.pixels[index + 1] & 0xFF;
         bleu = this.pixels[index + 2] & 0xFF;

         double y = (0.299 * rouge) + (0.587 * vert) + (0.114 * bleu);
         y = ymil + (factor * (y - ymil));
         final double u = ((-0.169 * rouge) - (0.331 * vert)) + (0.500 * bleu);
         final double v = (0.500 * rouge) - (0.419 * vert) - (0.081 * bleu);

         this.pixels[index++] = (byte) this.limite0_255((y - (0.0009267 * u)) + (1.4016868 * v));
         this.pixels[index++] = (byte) this.limite0_255(y - (0.3436954 * u) - (0.7141690 * v));
         this.pixels[index++] = (byte) this.limite0_255(y + (1.7721604 * u) + (0.0009902 * v));
         index++;
      }

      this.needToRefresh = true;
   }

   /**
    * Compute <code>first + second - 128</code> limit in [0, 255] pixel by pixel of tow texture.<br>
    * Textures must have same dimension
    * 
    * @param texture
    *           Second texture
    */
   public void contTexture(final Texture texture)
   {
      if((texture.width != this.width) || (texture.height != this.height))
      {
         throw new IllegalArgumentException("The given texture must have smae size than this texture");
      }

      final int nb = this.width * this.height;
      int index = 0;

      for(int i = 0; i < nb; i++)
      {
         this.pixels[index] = Texture.cont(this.pixels[index], texture.pixels[index]);
         index++;
         this.pixels[index] = Texture.cont(this.pixels[index], texture.pixels[index]);
         index++;
         this.pixels[index] = Texture.cont(this.pixels[index], texture.pixels[index]);
         index += 2;
      }

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Make texture darker
    * 
    * @param rate
    *           Dark rate
    */
   public void darker(final float rate)
   {
      final int nb = this.width * this.height;
      int index = 0;

      for(int i = 0; i < nb; i++)
      {
         this.pixels[index] = this.dark(this.pixels[index], rate);
         index++;
         this.pixels[index] = this.dark(this.pixels[index], rate);
         index++;
         this.pixels[index] = this.dark(this.pixels[index], rate);
         index += 2;
      }

      this.needToRefresh = true;
   }

   /**
    * Destroy the texture.<br>
    * Prefer use {@link JHelpSceneRenderer#removeFromMemory(Texture)}, this method will be call by the system
    */
   public void destroy()
   {
      if(this.videoMemoryId >= 0)
      {
         Debug.println(DebugLevel.WARNING, "Can't delete texture already goes in video memory. Name=", this.textureName);

         return;
      }

      Texture.unregisterTexture(this);

      this.pixels = null;
      this.textureName = null;
      this.textureReference = null;
   }

   /**
    * Divide by texture.<br>
    * Textures must have same dimensions
    * 
    * @param texture
    *           Texture to divide with
    */
   public void divTexture(final Texture texture)
   {
      if((texture.width != this.width) || (texture.height != this.height))
      {
         throw new IllegalArgumentException("The given texture must have smae size than this texture");
      }

      final int nb = this.width * this.height;
      int index = 0;

      for(int i = 0; i < nb; i++)
      {
         this.pixels[index] = Texture.div(this.pixels[index], texture.pixels[index]);
         index++;
         this.pixels[index] = Texture.div(this.pixels[index], texture.pixels[index]);
         index++;
         this.pixels[index] = Texture.div(this.pixels[index], texture.pixels[index]);
         index += 2;
      }

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Draw a glyph vector
    * 
    * @param glyph
    *           Glyph vactor to draw
    * @param x
    *           X
    * @param y
    *           Y
    * @param color
    *           Color used
    * @param mix
    *           Mixing is on or off
    * @param precision
    *           Precision
    */
   public void draw(final GlyphVector glyph, final int x, final int y, final Color color, final boolean mix, final int precision)
   {
      this.draw(glyph.getOutline(x, y), color, mix, precision);
   }

   /**
    * Draw a shape
    * 
    * @param shape
    *           Shape to draw
    * @param color
    *           Color
    * @param mix
    *           Enable/disable mixing mode
    * @param precision
    *           Precision
    */
   public void draw(final Shape shape, final Color color, final boolean mix, int precision)
   {
      if(precision < 2)
      {
         precision = 2;
      }
      this.draw(shape.getPathIterator(Texture.TRANSFORM), color, mix, precision);
   }

   /**
    * Draw an image
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param image
    *           Image to draw
    */
   public void drawImage(final int x, final int y, final BufferedImage image)
   {
      int width;
      int height;
      int[] pixels;
      boolean mayTransparent;

      image.flush();

      width = image.getWidth();
      height = image.getHeight();
      pixels = new int[width * height];

      pixels = image.getRGB(0, 0, width, height, pixels, 0, width);
      mayTransparent = image.getColorModel().hasAlpha();

      this.drawImage(x, y, pixels, width, height, mayTransparent);

      pixels = null;
   }

   /**
    * Draw an image
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param image
    *           Image to draw
    */
   public void drawImage(final int x, final int y, final Image image)
   {
      PixelGrabber pixelGrabber;
      ColorModel colorModel;
      int width;
      int height;
      int[] pixels;
      boolean mayTransparent;

      image.flush();

      width = image.getWidth(null);
      height = image.getHeight(null);

      pixels = new int[width * height];
      pixelGrabber = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
      try
      {
         if(pixelGrabber.grabPixels() == true)
         {
            mayTransparent = true;
            colorModel = pixelGrabber.getColorModel();
            if(colorModel != null)
            {
               mayTransparent = colorModel.hasAlpha();
            }

            this.drawImage(x, y, pixels, width, height, mayTransparent);
         }
      }
      catch(final InterruptedException e)
      {
         Debug.printException(e, "Issue while extracting pixels");
      }

      pixels = null;
      colorModel = null;
      pixelGrabber = null;
   }

   /**
    * Draw a JHelpImage in the texture
    * 
    * @param x
    *           X of top-left corner
    * @param y
    *           Y of top left corner
    * @param image
    *           Image to draw
    */
   public void drawImage(final int x, final int y, final JHelpImage image)
   {
      final int width = image.getWidth();
      final int height = image.getHeight();
      final int[] pixels = image.getPixels(0, 0, width, height);

      this.drawImage(x, y, pixels, width, height, true);
   }

   /**
    * Draw a line
    * 
    * @param x1
    *           X1
    * @param y1
    *           Y1
    * @param x2
    *           X2
    * @param y2
    *           Y2
    * @param color
    *           Color
    * @param mix
    *           Enable/disable mixing mode
    */
   public void drawLine(final int x1, final int y1, final int x2, final int y2, final Color color, final boolean mix)
   {
      final byte r = (byte) color.getRed();
      final byte g = (byte) color.getGreen();
      final byte b = (byte) color.getBlue();
      final byte a = (byte) color.getAlpha();

      if((a == Math3D.BYTE_255) || (mix == false))
      {
         this.drawLineWithoutMix(x1, y1, x2, y2, r, g, b, a);
         return;
      }
      if(a == Math3D.BYTE_0)
      {
         return;
      }
      this.drawLineWithMix(x1, y1, x2, y2, r, g, b, a);
   }

   /**
    * Draw one pixel on the texture
    * 
    * @param x
    *           Pixel X
    * @param y
    *           Pixel Y
    * @param color
    *           Pixel color
    * @param mix
    *           Indicates if mix mode or overwrite mode
    */
   public void drawPixel(final int x, final int y, final Color color, final boolean mix)
   {
      if((x < 0) || (y < 0) || (x >= this.width) || (y >= this.height))
      {
         return;
      }

      final byte r = (byte) color.getRed();
      final byte g = (byte) color.getGreen();
      final byte b = (byte) color.getBlue();
      final byte a = (byte) color.getAlpha();
      int pix = (x + (y * this.width)) << 2;

      if((a == Math3D.BYTE_255) || (mix == false))
      {
         this.pixels[pix++] = r;
         this.pixels[pix++] = g;
         this.pixels[pix++] = b;
         this.pixels[pix++] = a;

         if(this.autoFlush == true)
         {
            this.flush();
         }

         return;
      }

      if(a == Math3D.BYTE_0)
      {
         return;
      }

      final int red = r & 0xFF;
      final int green = g & 0xFF;
      final int blue = b & 0xFF;
      final int alp = a & 0xFF;
      final int pla = 255 - alp;

      this.pixels[pix] = this.mix(this.pixels[pix], red, alp, pla);
      this.pixels[pix + 1] = this.mix(this.pixels[pix + 1], green, alp, pla);
      this.pixels[pix + 2] = this.mix(this.pixels[pix + 2], blue, alp, pla);
      this.pixels[pix + 3] = this.add(this.pixels[pix + 3], alp);

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Draw a string
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param text
    *           String to draw
    * @param color
    *           Color
    * @param font
    *           Font
    * @param mix
    *           Mixing mode on or off
    * @param precision
    *           Precision
    */
   public void drawString(final int x, final int y, final String text, final Color color, final Font font, final boolean mix, final int precision)
   {
      this.draw(font.createGlyphVector(Texture.CONTEXT, text),//
            x, (int) (y + font.getLineMetrics(text, Texture.CONTEXT).getAscent()), color, mix, precision);
   }

   /**
    * Draw a part of a texture
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param texture
    *           Texture to draw
    * @param xTexture
    *           X on texture
    * @param yTexture
    *           Y on texture
    * @param width
    *           Width
    * @param height
    *           height
    */
   public void drawTexture(final int x, final int y, final Texture texture, int xTexture, int yTexture, int width, int height)
   {
      byte[] pixels;
      int nb;
      int yy;
      int y2;
      int line;

      if(xTexture < 0)
      {
         width += xTexture;
         xTexture = 0;
      }
      if(xTexture >= texture.width)
      {
         return;
      }
      if((xTexture + width) > texture.width)
      {
         width = texture.width - xTexture;
      }
      if(width < 1)
      {
         return;
      }
      if(yTexture < 0)
      {
         height += yTexture;
         yTexture = 0;
      }
      if(yTexture >= texture.height)
      {
         return;
      }
      if((yTexture + height) > texture.height)
      {
         height = texture.height - yTexture;
      }
      if(height < 1)
      {
         return;
      }

      nb = width * height * 4;
      pixels = new byte[nb];
      yy = (xTexture + (yTexture * texture.width)) << 2;
      y2 = 0;
      width <<= 2;
      for(line = 0; line < height; line++)
      {
         System.arraycopy(texture.pixels, yy, pixels, y2, width);
         yy += texture.width << 2;
         y2 += width;
      }

      this.drawImage(x, y, pixels, width >> 2, height, true);

      pixels = null;
   }

   /**
    * Fill a glyph vector
    * 
    * @param glyph
    *           Glyph vector to fill
    * @param x
    *           X
    * @param y
    *           Y
    * @param color
    *           Color
    * @param mix
    *           Enable/disable mixing mode
    */
   public void fill(final GlyphVector glyph, final int x, final int y, final Color color, final boolean mix)
   {
      this.fill(glyph.getOutline(x, y), color, mix);
   }

   /**
    * Fill a shape
    * 
    * @param shape
    *           Shape to fill
    * @param color
    *           Color
    * @param mix
    *           Enable/disable mixing mode
    */
   public void fill(final Shape shape, final Color color, final boolean mix)
   {
      final byte r = (byte) color.getRed();
      final byte g = (byte) color.getGreen();
      final byte b = (byte) color.getBlue();
      final byte a = (byte) color.getAlpha();
      final Rectangle rectangle = shape.getBounds();
      int x = rectangle.x;
      int y = rectangle.y;
      int width = rectangle.width;
      int height = rectangle.height;
      if(x < 0)
      {
         width += x;
         x = 0;
      }
      if(x >= this.width)
      {
         return;
      }
      if((x + width) > this.width)
      {
         width = this.width - x;
      }
      if(width < 1)
      {
         return;
      }
      if(y < 0)
      {
         height += y;
         y = 0;
      }
      if(y >= this.height)
      {
         return;
      }
      if((y + height) > this.height)
      {
         height = this.height - y;
      }
      if(height < 1)
      {
         return;
      }
      if((a == Math3D.BYTE_255) || (mix == false))
      {
         this.fillWithoutMix(shape, r, g, b, a, x, y, width, height);
         return;
      }
      if(a == Math3D.BYTE_0)
      {
         return;
      }
      this.fillWithMix(shape, r, g, b, a, x, y, width, height);
   }

   /**
    * Fill an oval
    * 
    * @param x
    *           X up left corner
    * @param y
    *           Y up left corner
    * @param width
    *           Width
    * @param height
    *           Height
    * @param color
    *           Color
    * @param mix
    *           Enable/disable mixing mode
    */
   public void fillOval(final int x, final int y, final int width, final int height, final Color color, final boolean mix)
   {
      this.fill(new Ellipse2D.Double(x, y, width, height), color, mix);
   }

   /**
    * Fill rectangle
    * 
    * @param x
    *           X up left corner
    * @param y
    *           Y up left corner
    * @param width
    *           Width
    * @param height
    *           Height
    * @param color
    *           Color
    * @param mix
    *           Enable/disable mixing mode
    */
   public void fillRect(int x, int y, int width, int height, final Color color, final boolean mix)
   {
      final byte a = (byte) color.getAlpha();
      if((a == Math3D.BYTE_0) && (mix == true))
      {
         return;
      }

      if(x < 0)
      {
         width += x;
         x = 0;
      }
      if(x >= this.width)
      {
         return;
      }

      if((x + width) > this.width)
      {
         width = this.width - x;
      }
      if(width < 1)
      {
         return;
      }

      if(y < 0)
      {
         height += y;
         y = 0;
      }
      if(y >= this.height)
      {
         return;
      }

      if((y + height) > this.height)
      {
         height = this.height - y;
      }
      if(height < 1)
      {
         return;
      }

      final byte r = (byte) color.getRed();
      final byte g = (byte) color.getGreen();
      final byte b = (byte) color.getBlue();

      if((a == Math3D.BYTE_255) || (mix == false))
      {
         this.fillRectWithoutMix(r, g, b, a, x, y, width, height);
         return;
      }

      this.fillRectWithMix(r, g, b, a, x, y, width, height);
   }

   /**
    * Fill a gradient rectangle
    * 
    * @param x
    *           X top left corner
    * @param y
    *           Y top left corner
    * @param width
    *           Width
    * @param height
    *           Height
    * @param colorTopLeft
    *           Top left color
    * @param colorTopRight
    *           Top right color
    * @param colorBottomLeft
    *           Bottom left color
    * @param colorBottomRight
    *           Bottom right color
    * @param mix
    *           Indicates if we mix alpha or not
    */
   public void fillRect(int x, int y, int width, int height, final Color colorTopLeft, final Color colorTopRight, final Color colorBottomLeft,
         final Color colorBottomRight, final boolean mix)
   {
      final int atl = colorTopLeft.getAlpha();
      final int atr = colorTopRight.getAlpha();
      final int abl = colorBottomLeft.getAlpha();
      final int abr = colorBottomRight.getAlpha();

      if((atl == 0) && (atr == 0) && (abl == 0) && (abr == 0) && (mix == true))
      {
         return;
      }

      if(x < 0)
      {
         width += x;
         x = 0;
      }
      if(x >= this.width)
      {
         return;
      }

      if((x + width) > this.width)
      {
         width = this.width - x;
      }
      if(width < 1)
      {
         return;
      }

      if(y < 0)
      {
         height += y;
         y = 0;
      }
      if(y >= this.height)
      {
         return;
      }

      if((y + height) > this.height)
      {
         height = this.height - y;
      }
      if(height < 1)
      {
         return;
      }

      final int rtl = colorTopLeft.getRed();
      final int gtl = colorTopLeft.getGreen();
      final int btl = colorTopLeft.getBlue();

      final int rtr = colorTopRight.getRed();
      final int gtr = colorTopRight.getGreen();
      final int btr = colorTopRight.getBlue();

      final int rbl = colorBottomLeft.getRed();
      final int gbl = colorBottomLeft.getGreen();
      final int bbl = colorBottomLeft.getBlue();

      final int rbr = colorBottomRight.getRed();
      final int gbr = colorBottomRight.getGreen();
      final int bbr = colorBottomRight.getBlue();

      if(((atl == 255) && (atr == 255) && (abl == 255) && (abr == 255)) || (mix == false))
      {
         this.fillRectWithoutMix(rtl, gtl, btl, atl, rtr, gtr, btr, atr, rbl, gbl, bbl, abl, rbr, gbr, bbr, abr, x, y, width, height);
         return;
      }

      this.fillRectWithMix(rtl, gtl, btl, atl, rtr, gtr, btr, atr, rbl, gbl, bbl, abl, rbr, gbr, bbr, abr, x, y, width, height);
   }

   /**
    * Fill a String
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param text
    *           Text to print
    * @param color
    *           Color
    * @param font
    *           Font
    * @param mix
    *           Enable/disable mixing mode
    */
   public void fillString(final int x, final int y, final String text, final Color color, final Font font, final boolean mix)
   {
      this.fill(font.createGlyphVector(Texture.CONTEXT, text), x, //
            (int) (y + font.getLineMetrics(text, Texture.CONTEXT).getAscent()), color, mix);
   }

   /**
    * Flip texture horizontally
    */
   public void flipHorizontal()
   {
      final int time = this.height >> 1;
      final int size = this.width << 2;
      final byte[] line = new byte[size];
      int up = 0;
      int down = this.pixels.length - size;

      for(int i = 0; i < time; i++)
      {
         System.arraycopy(this.pixels, up, line, 0, size);
         System.arraycopy(this.pixels, down, this.pixels, up, size);
         System.arraycopy(line, 0, this.pixels, down, size);
         up += size;
         down -= size;
      }

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Refresh last change
    */
   public void flush()
   {
      this.needToRefresh = true;
   }

   /**
    * Developer additional information.<br>
    * Its an opaque value, that can be use by the API user.<br>
    * The texture just carry it
    * 
    * @return Developer additional information
    */
   public Object getAdditionalInformation()
   {
      return this.additionalInformation;
   }

   /**
    * Texture's height
    * 
    * @return Texture's height
    */
   public int getHeight()
   {
      return this.height;
   }

   /**
    * Texture ID
    * 
    * @return Texture ID
    */
   public int getTextureID()
   {
      return this.textureID;
   }

   /**
    * Return textureName
    * 
    * @return textureName
    */
   public String getTextureName()
   {
      return this.textureName;
   }

   /**
    * Return textureReference
    * 
    * @return textureReference
    */
   public String getTextureReference()
   {
      return this.textureReference;
   }

   /**
    * Texture's width
    * 
    * @return Texture's width
    */
   public int getWidth()
   {
      return this.width;
   }

   /**
    * Invert the texture color
    */
   public void invert()
   {
      final int nb = this.width * this.height;
      int index = 0;

      for(int i = 0; i < nb; i++)
      {
         this.pixels[index] = (byte) (255 - (this.pixels[index] & 0xFF));
         index++;
         this.pixels[index] = (byte) (255 - (this.pixels[index] & 0xFF));
         index++;
         this.pixels[index] = (byte) (255 - (this.pixels[index] & 0xFF));
         index += 2;
      }

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Return autoFlush
    * 
    * @return autoFlush
    */
   public boolean isAutoFlush()
   {
      return this.autoFlush;
   }

   /**
    * 
    Indicaes if a texture is exactly the same as this one
    * 
    * @param texture
    *           Texture to ccompre with
    * @return {@code true} if excatly the same
    */
   public boolean isSameTexture(final Texture texture)
   {
      if(this.width != texture.width)
      {
         return false;
      }

      if(this.height != texture.height)
      {
         return false;
      }

      for(int pix = this.pixels.length - 1; pix >= 0; pix--)
      {
         if(this.pixels[pix] != texture.pixels[pix])
         {
            return false;
         }
      }

      return true;
   }

   /**
    * Make texture brighter
    */
   public void moreBright()
   {
      this.changeBright(2);
   }

   /**
    * Make texture darker
    */
   public void moreDark()
   {
      this.changeBright(0.5f);
   }

   /**
    * Multiply by a texture.<br>
    * Textures must have same dimensions
    * 
    * @param texture
    *           Texture to multiply with
    */
   public void multTexture(final Texture texture)
   {
      if((texture.width != this.width) || (texture.height != this.height))
      {
         throw new IllegalArgumentException("The given texture must have smae size than this texture");
      }

      final int nb = this.width * this.height;
      int index = 0;

      for(int i = 0; i < nb; i++)
      {
         this.pixels[index] = Texture.mult(this.pixels[index], texture.pixels[index]);
         index++;
         this.pixels[index] = Texture.mult(this.pixels[index], texture.pixels[index]);
         index++;
         this.pixels[index] = Texture.mult(this.pixels[index], texture.pixels[index]);
         index += 2;
      }

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Obtain a parcel of the texture
    * 
    * @param x
    *           X of up-left corner rectangle in texture
    * @param y
    *           Y of up-left corner rectangle in texture
    * @param width
    *           Rectangle width
    * @param height
    *           Rectangle height
    * @param suffixName
    *           Suffix to add to the texture name, for identify parcel later
    * @return Texture parcel
    */
   public Texture obtainParcel(int x, int y, int width, int height, String suffixName)
   {
      suffixName = suffixName.trim();
      if(suffixName.length() == 0)
      {
         suffixName = Double.toHexString(Math.random() * Double.MAX_VALUE);
      }

      if(x < 0)
      {
         width += x;
         x = 0;
      }

      if((x + width) > this.width)
      {
         width = this.width - x;
      }

      if(width < 1)
      {
         return new Texture(this.textureName + suffixName, 1, 1);
      }

      if(y < 0)
      {
         height += y;
         y = 0;
      }

      if((y + height) > this.height)
      {
         height = this.height - y;
      }

      if(height < 1)
      {
         return new Texture(this.textureName + suffixName, 1, 1);
      }

      final Texture texture = new Texture(this.textureName + suffixName, width, height);

      int lineThis = (x + (y * this.width)) << 2;
      int lineTexture = 0;

      width <<= 2;
      final int w = this.width << 2;

      for(int l = 0; l < height; l++)
      {
         System.arraycopy(this.pixels, lineThis, texture.pixels, lineTexture, width);
         lineThis += w;
         lineTexture += width;
      }

      return texture;
   }

   /**
    * Try to reduce texture in memory, <br>
    * The texture will take less memory, be will be less defined also
    */
   public void reduce()
   {
      synchronized(Texture.LOCK)
      {
         int w = this.width >> 1;
         int h = this.height >> 1;

         if(w == 0)
         {
            if(h == 0)
            {
               return;
            }

            w = 1;
         }

         if(h == 0)
         {
            h = 1;
         }

         final JHelpImage image = this.toJHelpImage(w, h);
         this.setImage(image);
      }
   }

   /**
    * Modify developer additional information.<br>
    * Its an opaque value, that can be use by the API user.<br>
    * The texture just carry it
    * 
    * @param additionalInformation
    *           Information to carry
    */
   public void setAdditionalInformation(final Object additionalInformation)
   {
      this.additionalInformation = additionalInformation;
   }

   /**
    * Apply an alpha map.<br>
    * Alpha map must have same dimensions
    * 
    * @param texture
    *           Alpha map
    */
   public void setAlphaMap(final Texture texture)
   {
      if((this.width != texture.width) || (this.height != texture.height))
      {
         throw new IllegalArgumentException("Texture and alpha map must have same dimensions");
      }
      final int nb = this.width * this.height * 4;
      double y;
      int c;
      int r;
      int g;
      int b;
      for(int i = 0; i < nb; i += 4)
      {
         r = texture.pixels[i] & 0xFF;
         g = texture.pixels[i + 1] & 0xFF;
         b = texture.pixels[i + 2] & 0xFF;
         y = (r * 0.299) + (g * 0.587) + (b * 0.114);
         c = (int) y;
         if(c < 0)
         {
            c = 0;
         }
         if(c > 255)
         {
            c = 255;
         }
         this.pixels[i + 3] = (byte) c;
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Modify autoFlush
    * 
    * @param autoFlush
    *           New autoFlush value
    */
   public void setAutoFlush(final boolean autoFlush)
   {
      this.autoFlush = autoFlush;
   }

   /**
    * Change the texture pixels with image ones
    * 
    * @param image
    *           Image to put in the texture
    */
   public void setImage(final JHelpImage image)
   {
      final int width = image.getWidth();
      final int height = image.getHeight();
      final int[] pixels = image.getPixels(0, 0, width, height);

      this.setPixels(width, height, pixels);
   }

   /**
    * Change texture pixels
    * 
    * @param width
    *           New width
    * @param height
    *           New height
    * @param pixels
    *           New pixels
    */
   public void setPixels(final int width, final int height, final byte[] pixels)
   {
      final int nb = width * height * 4;
      if(nb != pixels.length)
      {
         throw new IllegalArgumentException("The pixels' array length is not width*height*4 !");
      }
      this.width = width;
      this.height = height;
      this.pixels = pixels;
      this.needToRefresh = true;
   }

   /**
    * Change texture pixels
    * 
    * @param width
    *           New width
    * @param height
    *           New height
    * @param pixels
    *           New pixels
    */
   public void setPixels(final int width, final int height, final int[] pixels)
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
         this.pixels[index++] = (byte) ((color >> 24) & 0xFF);
      }
      this.needToRefresh = true;
   }

   /**
    * Override by an other texture.<br>
    * If dimensions are different, the texture dimmension will change
    * 
    * @param texture
    *           Texture that override
    */
   public void setPixels(final Texture texture)
   {
      if((this.width != texture.width) || (this.height != texture.height))
      {
         this.width = texture.width;
         this.height = texture.height;

         this.pixels = new byte[this.width * this.height * 4];
      }

      System.arraycopy(texture.pixels, 0, this.pixels, 0, this.pixels.length);

      this.needToRefresh = true;
   }

   /**
    * Change texture reference
    * 
    * @param textureReference
    *           New reference
    */
   public void setTextureReference(final String textureReference)
   {
      if(textureReference == null)
      {
         throw new NullPointerException("textureReference musn't be null");
      }
      this.textureReference = textureReference;
   }

   /**
    * Move texture pixels
    * 
    * @param x
    *           X translation
    * @param y
    *           Y translation
    */
   public void shift(final int x, final int y)
   {
      final int nb = this.width * this.height;
      final int nb4 = nb << 2;
      int indice = (x + (y * this.width)) * 4;

      while(indice < nb4)
      {
         indice += nb4;
      }

      indice = indice % nb4;
      int index = 0;
      byte[] temp = new byte[nb4];
      System.arraycopy(this.pixels, 0, temp, 0, nb4);

      for(int i = 0; i < nb; i++)
      {
         this.pixels[index++] = temp[indice++];
         this.pixels[index++] = temp[indice++];
         this.pixels[index++] = temp[indice++];
         this.pixels[index++] = temp[indice++];

         indice = indice % nb4;
      }

      temp = null;

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Subtract a texture.<br>
    * Textures must have same dimensions
    * 
    * @param texture
    *           Texture to subtract
    */
   public void subTexture(final Texture texture)
   {
      if((texture.width != this.width) || (texture.height != this.height))
      {
         throw new IllegalArgumentException("The given texture must have smae size than this texture");
      }

      final int nb = this.width * this.height;
      int index = 0;

      for(int i = 0; i < nb; i++)
      {
         this.pixels[index] = Texture.sub(this.pixels[index], texture.pixels[index]);
         index++;
         this.pixels[index] = Texture.sub(this.pixels[index], texture.pixels[index]);
         index++;
         this.pixels[index] = Texture.sub(this.pixels[index], texture.pixels[index]);
         index += 2;
      }

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Transform texture in BufferedImage
    * 
    * @return BufferedImage
    */
   public BufferedImage toBufferedImage()
   {
      final int nb = this.width * this.height;
      int[] pixels = new int[nb];

      int index = 0, r, g, b, a;
      for(int i = 0; i < nb; i++)
      {
         r = this.pixels[index++] & 0xFF;
         g = this.pixels[index++] & 0xFF;
         b = this.pixels[index++] & 0xFF;
         a = this.pixels[index++] & 0xFF;

         pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
      }

      final BufferedImage bufferedImage = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
      bufferedImage.setRGB(0, 0, this.width, this.height, pixels, 0, this.width);
      bufferedImage.flush();

      pixels = null;

      return bufferedImage;
   }

   /**
    * Transform texture to compressive image
    * 
    * @return Compressive image
    */
   public CompressiveImage toCompressiveImage()
   {
      return CompressiveImage.encode(this.textureName, this.width, this.height, this.pixels);
   }

   /**
    * Transform texture to its gray version
    */
   public void toGray()
   {
      int r;
      int g;
      int b;
      final int nb = this.width * this.height * 4;
      double y;
      byte c;
      for(int i = 0; i < nb; i += 4)
      {
         r = this.pixels[i] & 0xFF;
         g = this.pixels[i + 1] & 0xFF;
         b = this.pixels[i + 2] & 0xFF;
         //
         y = (r * 0.299) + (g * 0.587) + (b * 0.114);
         //
         c = (byte) ColorsUtil.limite0_255(y);
         //
         this.pixels[i] = c;
         this.pixels[i + 1] = c;
         this.pixels[i + 2] = c;
      }

      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Change texture to it's gery invert version
    */
   public void toGrayInvert()
   {
      int r;
      int g;
      int b;
      final int nb = this.width * this.height * 4;
      double y;
      byte c;
      for(int i = 0; i < nb; i += 4)
      {
         r = this.pixels[i] & 0xFF;
         g = this.pixels[i + 1] & 0xFF;
         b = this.pixels[i + 2] & 0xFF;
         //
         y = (r * 0.299) + (g * 0.587) + (b * 0.114);
         //
         c = (byte) (255 - ColorsUtil.limite0_255(y));
         //
         this.pixels[i] = c;
         this.pixels[i + 1] = c;
         this.pixels[i + 2] = c;
      }
      if(this.autoFlush == true)
      {
         this.flush();
      }
   }

   /**
    * Convert to an image of the same size
    * 
    * @return Converted image
    */
   public JHelpImage toJHelpImage()
   {
      final int nb = this.width * this.height;
      final int[] pixels = new int[nb];

      int index = 0, r, g, b, a;
      for(int i = 0; i < nb; i++)
      {
         r = this.pixels[index++] & 0xFF;
         g = this.pixels[index++] & 0xFF;
         b = this.pixels[index++] & 0xFF;
         a = this.pixels[index++] & 0xFF;

         pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
      }

      return new JHelpImage(this.width, this.height, pixels);
   }

   /**
    * Transform the Texture in JHelpImage (It will be scaled if need)
    * 
    * @param width
    *           Image width
    * @param height
    *           Image height
    * @return Created image
    */
   public JHelpImage toJHelpImage(final int width, final int height)
   {
      final int nb = this.width * this.height;
      final int[] pixels = new int[nb];

      int index = 0, r, g, b, a;
      for(int i = 0; i < nb; i++)
      {
         r = this.pixels[index++] & 0xFF;
         g = this.pixels[index++] & 0xFF;
         b = this.pixels[index++] & 0xFF;
         a = this.pixels[index++] & 0xFF;

         pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
      }

      return new JHelpImage(this.width, this.height, pixels, width, height);
   }

   /**
    * Return needToRefresh
    * 
    * @return needToRefresh
    */
   public boolean willBeRefresh()
   {
      return this.needToRefresh;
   }
}