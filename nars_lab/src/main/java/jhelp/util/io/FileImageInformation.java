package jhelp.util.io;

import jhelp.util.debug.Debug;
import jhelp.util.list.EnumerationIterator;
import jhelp.util.text.UtilText;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Describes information about a image file
 * 
 * @author JHelp
 */
public class FileImageInformation
{
   /** JVM known readers */
   private static final ImageReader[] IMAGES_READERS;
   /** Filter of image, based on file information, not on file extention. Directory are allowed */
   public static final FileFilter     FILTER_BY_FILE_INFORMATION              = new FileFilter()
                                                                              {
                                                                                 /**
                                                                                  * Indicates if a file is an image or a
                                                                                  * directory <br>
                                                                                  * <br>
                                                                                  * <b>Parent documentation:</b><br>
                                                                                  * {@inheritDoc}
                                                                                  * 
                                                                                  * @param pathname
                                                                                  *           Tested file
                                                                                  * @return {@code true} if its an image or a
                                                                                  *         directory
                                                                                  * @see FileFilter#accept(File)
                                                                                  */
                                                                                 @Override
                                                                                 public boolean accept(final File pathname)
                                                                                 {
                                                                                    if(pathname.isDirectory() == true)
                                                                                    {
                                                                                       return true;
                                                                                    }

                                                                                    return (new FileImageInformation(pathname)).getFormatName() != null;
                                                                                 }
                                                                              };
   /** Filter of image, based on file information, not on file extention. Directory are forbiden */
   public static final FileFilter     FILTER_BY_FILE_INFORMATION_NO_DIRECTORY = new FileFilter()
                                                                              {
                                                                                 /**
                                                                                  * Indicates if a file is an image <br>
                                                                                  * <br>
                                                                                  * <b>Parent documentation:</b><br>
                                                                                  * {@inheritDoc}
                                                                                  * 
                                                                                  * @param pathname
                                                                                  *           Tested file
                                                                                  * @return {@code true} if its an image
                                                                                  * @see FileFilter#accept(File)
                                                                                  */
                                                                                 @Override
                                                                                 public boolean accept(final File pathname)
                                                                                 {
                                                                                    if(pathname.isDirectory() == true)
                                                                                    {
                                                                                       return false;
                                                                                    }

                                                                                    return (new FileImageInformation(pathname)).getFormatName() != null;
                                                                                 }
                                                                              };

   static
   {
      final ArrayList<ImageReader> imageReaders = new ArrayList<ImageReader>();

      final String[] suffixs =
      {
         "JPG", "PNG", "GIF", "BMP"
      };

      for(final String suffix : suffixs)
      {
         for(final ImageReader imageReader : new EnumerationIterator<ImageReader>(ImageIO.getImageReadersBySuffix(suffix)))
         {
            imageReaders.add(imageReader);
         }
      }

      IMAGES_READERS = imageReaders.toArray(new ImageReader[imageReaders.size()]);
   }

   /** Image file */
   private final File                 file;
   /** File format name */
   private String                     formatName;
   /** Image height */
   private int                        height;
   /** Image width */
   private int                        width;

   /**
    * Create a new instance of FileImageInformation
    * 
    * @param file
    *           Image file
    */
   public FileImageInformation(final File file)
   {
      this.file = file;
      this.width = this.height = -1;

      if((file.exists() == false) || (file.isDirectory() == true) || (file.canRead() == false))
      {
         return;
      }

      try
      {
         FileImageInputStream fileInputStream;

         for(final ImageReader imageReader : FileImageInformation.IMAGES_READERS)
         {
            fileInputStream = null;

            try
            {
               fileInputStream = new FileImageInputStream(file);
               imageReader.setInput(fileInputStream, false, false);
               final int nb = imageReader.getNumImages(true);

               for(int i = 0; i < nb; i++)
               {
                  this.width = Math.max(this.width, imageReader.getWidth(i));
                  this.height = Math.max(this.height, imageReader.getHeight(i));
               }

               this.formatName = imageReader.getFormatName();

               break;
            }
            catch(final Exception exception)
            {
            }
            finally
            {
               if(fileInputStream != null)
               {
                  try
                  {
                     fileInputStream.close();
                  }
                  catch(final Exception exception)
                  {
                  }
               }
               fileInputStream = null;
            }
         }
      }
      catch(final Exception exception)
      {
         Debug.printException(exception);
      }
   }

   /**
    * Image file
    * 
    * @return Image file
    */
   public File getFile()
   {
      return this.file;
   }

   /**
    * File format name or {@code null} if it's not an image
    * 
    * @return File format name or {@code null} if it's not an image
    */
   public String getFormatName()
   {
      return this.formatName;
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
    * Image width
    * 
    * @return Image width
    */
   public int getWidth()
   {
      return this.width;
   }

   /**
    * String representation <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return String representation
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      return UtilText.concatenate(this.file.getAbsolutePath(), " : ", this.width, "x", this.height, " ", this.formatName);
   }
}