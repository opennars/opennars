package jhelp.engine.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//import jhelp.util.debug.Debug;
//import jhelp.util.io.base64.Base64InputStream;
//import jhelp.util.io.base64.Base64OutputStream;
//import jhelp.util.list.Pair;
//import jhelp.util.text.StringCutter;
//import jhelp.util.text.UtilText;

/**
 * Utilities for Input/Output streams
 * 
 * @author JHelp
 */
public final class UtilIO
{
   /** "Home" directory */
   private static File        homeDirectory;
   /** Directory external of the code */
   private static File        outsideDirectory;
   /** Temporary directory */
   private static File        temporaryDirectory;
   /** Buffer size */
   public static final int    BUFFER_SIZE        = 4 * UtilIO.MEGA_BYTES;
   /** Path that represents the current directory */
   public static final String CURRENT_DIRECTORY  = ".";
   /** Size of a file header */
   public static final int    HEADER_SIZE        = UtilIO.KILO_BYTES;
   /** One kilo-byte in bytes */
   public static final int    KILO_BYTES         = 1024;
   /** One mega-byte in bytes */
   public static final int    MEGA_BYTES         = 1024 * UtilIO.KILO_BYTES;
   /** Path separator used in URL, ZIP, JAR */
   public static final char   PATH_SEPARATOR     = '/';

   /** Path the represents the parent directory */
   public static final String PREVIOUS_DIRECTORY = "..";

   /**
    * Create a double from a byte array.<br>
    * Work good with byte array generated with {@link #doubleToByteArray(double)}
    * 
    * @param array
    *           Array to convert
    * @return Double obtain
    */
   public static double byteArrayToDouble(final byte[] array)
   {
      return Double.longBitsToDouble(UtilIO.byteArrayToLong(array));
   }

   /**
    * Create a long from a byte array.<br>
    * Work good with byte array generated with {@link #longToByteArray(long)}
    * 
    * @param array
    *           Array to convert
    * @return Long obtain
    */
   public static long byteArrayToLong(final byte[] array)
   {
      return ((long) (array[0] & 0xFF) << 56L) | ((long) (array[1] & 0xFF) << 48L) | ((long) (array[2] & 0xFF) << 40L) | ((long) (array[3] & 0xFF) << 32L)
            | ((long) (array[4] & 0xFF) << 24L) | ((long) (array[5] & 0xFF) << 16L) | ((long) (array[6] & 0xFF) << 8L) | (array[7] & 0xFF);
   }

   /**
    * Compute the SHA code of a stream
    * 
    * @param inputStream
    *           Stream to read
    * @return SHA code
    * @throws NoSuchAlgorithmException
    *            If SHA not implemented (Should never append, its java base algorithm)
    * @throws IOException
    *            On reading issue
    */
   public static BigInteger computeBigIntegerSHA(InputStream inputStream) throws NoSuchAlgorithmException, IOException
   {
      final MessageDigest sha = MessageDigest.getInstance("SHA");
      byte[] temp = new byte[4096];

      int read = inputStream.read(temp);
      while(read >= 0)
      {
         sha.update(temp, 0, read);

         read = inputStream.read(temp);
      }

      inputStream.close();
      inputStream = null;

      temp = sha.digest();

      BigInteger bigInteger = BigInteger.ZERO;

      for(final byte b : temp)
      {
         bigInteger = bigInteger.shiftLeft(8).add(BigInteger.valueOf((b & 0xFF)));
      }

      return bigInteger;
   }

   /**
    * Compute SHA for a stream
    * 
    * @param inputStream
    *           Stream to read
    * @return MD5 of the stream
    * @throws NoSuchAlgorithmException
    *            If system not support SHA
    * @throws IOException
    *            On reading stream issue
    */
   public static byte[] computeByteArraySHA(final InputStream inputStream) throws NoSuchAlgorithmException, IOException
   {
      final MessageDigest sha = MessageDigest.getInstance("SHA");
      final byte[] temp = new byte[4096];

      int read = inputStream.read(temp);
      while(read >= 0)
      {
         sha.update(temp, 0, read);

         read = inputStream.read(temp);
      }

      return sha.digest();
   }

//   /**
//    * Compute MD5 of an image
//    *
//    * @param bufferedImage
//    *           Image to compute it's MD5
//    * @return Image MD5
//    * @throws NoSuchAlgorithmException
//    *            If system not support MD5
//    * @throws IOException
//    *            On reading image issue
//    */
//   public static String computeMD5(final BufferedImage bufferedImage) throws NoSuchAlgorithmException, IOException
//   {
//      bufferedImage.flush();
//
//      final int width = bufferedImage.getWidth();
//      final int height = bufferedImage.getHeight();
//
//      int[] pixels = new int[(width * height) + 2];
//
//      pixels[0] = width;
//      pixels[1] = height;
//
//      pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 2, width);
//
//      return UtilIO.computeMD5(pixels);
//   }
//
//   /**
//    * Compute MD5 of a file
//    *
//    * @param file
//    *           File to compute its MD5
//    * @return Computed MD5
//    * @throws NoSuchAlgorithmException
//    *            If system not support MD5
//    * @throws IOException
//    *            On reading file issue
//    */
//   public static String computeMD5(final File file) throws NoSuchAlgorithmException, IOException
//   {
//      if((file.exists() == false) || (file.isFile() == false) || (UtilIO.isVirtualLink(file) == true))
//      {
//         return null;
//      }
//
//      FileInputStream fileInputStream = null;
//
//      try
//      {
//         fileInputStream = new FileInputStream(file);
//
//         return UtilIO.computeMD5(fileInputStream);
//      }
//      catch(final NoSuchAlgorithmException exception)
//      {
//         throw exception;
//      }
//      catch(final IOException exception)
//      {
//         throw exception;
//      }
//      finally
//      {
//         if(fileInputStream != null)
//         {
//            try
//            {
//               fileInputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//   }

   /**
    * Compute MD5 for a stream
    * 
    * @param inputStream
    *           Stream to read
    * @return MD5 of the stream
    * @throws NoSuchAlgorithmException
    *            If system not support MD5
    * @throws IOException
    *            On reading stream issue
    */
   public static String computeMD5(InputStream inputStream) throws NoSuchAlgorithmException, IOException
   {
      final MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte[] temp = new byte[4096];

      int read = inputStream.read(temp);
      while(read >= 0)
      {
         md5.update(temp, 0, read);

         read = inputStream.read(temp);
      }

      inputStream.close();
      inputStream = null;

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
//
//   /**
//    * Compute MD5 for an array of integer
//    *
//    * @param data
//    *           Array to compute its MD5
//    * @return Array MD5
//    * @throws NoSuchAlgorithmException
//    *            If system not support MD5
//    * @throws IOException
//    *            On reading array issue
//    */
//   public static String computeMD5(final int[] data) throws NoSuchAlgorithmException, IOException
//   {
//      return UtilIO.computeMD5(new IntegerArrayInputStream(data));
//   }
//
//   /**
//    * Compute MD5 and SHA for a file, can be us as unique ID
//    *
//    * @param file
//    *           File to read
//    * @return MD5, SHA pair unique ID of the file
//    * @throws NoSuchAlgorithmException
//    *            If system not support MD5 or SHA
//    * @throws IOException
//    *            On reading stream issue
//    */
//   public static String computeMD5_SHA_ID(final File file) throws NoSuchAlgorithmException, IOException
//   {
//      if((file.exists() == false) || (file.isFile() == false) || (UtilIO.isVirtualLink(file) == true))
//      {
//         return null;
//      }
//
//      FileInputStream fileInputStream = null;
//
//      try
//      {
//         fileInputStream = new FileInputStream(file);
//
//         return UtilIO.computeMD5_SHA_ID(fileInputStream);
//      }
//      catch(final NoSuchAlgorithmException exception)
//      {
//         throw exception;
//      }
//      catch(final IOException exception)
//      {
//         throw exception;
//      }
//      finally
//      {
//         if(fileInputStream != null)
//         {
//            try
//            {
//               fileInputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//   }
//
//   /**
//    * Compute MD5 and SHA for a stream, can be us as unique ID
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return MD5, SHA pair unique ID of the stream
//    * @throws NoSuchAlgorithmException
//    *            If system not support MD5 or SHA
//    * @throws IOException
//    *            On reading stream issue
//    */
//   public static String computeMD5_SHA_ID(InputStream inputStream) throws NoSuchAlgorithmException, IOException
//   {
//      final MessageDigest md5 = MessageDigest.getInstance("MD5");
//      final MessageDigest sha = MessageDigest.getInstance("SHA");
//      byte[] temp = new byte[4096];
//
//      int read = inputStream.read(temp);
//      while(read >= 0)
//      {
//         md5.update(temp, 0, read);
//         sha.update(temp, 0, read);
//
//         read = inputStream.read(temp);
//      }
//
//      inputStream.close();
//      inputStream = null;
//
//      temp = md5.digest();
//      final StringBuffer stringBuffer = new StringBuffer();
//      for(final byte b : temp)
//      {
//         read = b & 0xFF;
//         stringBuffer.append(Integer.toHexString((read >> 4) & 0xF));
//         stringBuffer.append(Integer.toHexString(read & 0xF));
//      }
//
//      stringBuffer.append('_');
//
//      temp = sha.digest();
//      for(final byte b : temp)
//      {
//         read = b & 0xFF;
//         stringBuffer.append(Integer.toHexString((read >> 4) & 0xF));
//         stringBuffer.append(Integer.toHexString(read & 0xF));
//      }
//
//      temp = null;
//
//      return stringBuffer.toString();
//   }
//
//   /**
//    * Compute relative path for go from a file to an other
//    *
//    * @param start
//    *           Start file
//    * @param destination
//    *           Destination file
//    * @return Relative path
//    */
//   public static String computeRelativePath(final File start, final File destination)
//   {
//      final String[] pathStart = start.getAbsolutePath().split(File.separator);
//      final int lengthStart = pathStart.length;
//
//      final String[] pathArrive = destination.getAbsolutePath().split(File.separator);
//      final int lengthArrive = pathArrive.length;
//
//      int common = 0;
//      final int max = Math.min(lengthStart, lengthArrive);
//
//      for(; common < max; common++)
//      {
//         if(pathStart[common].equals(pathArrive[common]) == false)
//         {
//            break;
//         }
//      }
//
//      final StringBuilder stringBuilder = new StringBuilder();
//
//      for(int i = common; i < lengthStart; i++)
//      {
//         stringBuilder.append(UtilIO.PREVIOUS_DIRECTORY);
//
//         stringBuilder.append('/');
//      }
//
//      for(int i = common; i < lengthArrive; i++)
//      {
//         stringBuilder.append(pathArrive[i]);
//
//         stringBuilder.append('/');
//      }
//
//      if(stringBuilder.length() > 0)
//      {
//         stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
//      }
//
//      return stringBuilder.toString();
//   }
//
//   /**
//    * Compute SHA for a file
//    *
//    * @param file
//    *           File to read
//    * @return SHA of the file
//    * @throws NoSuchAlgorithmException
//    *            If system not support SHA
//    * @throws IOException
//    *            On reading stream issue
//    */
//   public static String computeSHA(final File file) throws NoSuchAlgorithmException, IOException
//   {
//      if((file.exists() == false) || (file.isFile() == false) || (UtilIO.isVirtualLink(file) == true))
//      {
//         return null;
//      }
//
//      FileInputStream fileInputStream = null;
//
//      try
//      {
//         fileInputStream = new FileInputStream(file);
//
//         return UtilIO.computeSHA(fileInputStream);
//      }
//      catch(final NoSuchAlgorithmException exception)
//      {
//         throw exception;
//      }
//      catch(final IOException exception)
//      {
//         throw exception;
//      }
//      finally
//      {
//         if(fileInputStream != null)
//         {
//            try
//            {
//               fileInputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//   }
//
//   /**
//    * Compute SHA for a stream
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return MD5 of the stream
//    * @throws NoSuchAlgorithmException
//    *            If system not support SHA
//    * @throws IOException
//    *            On reading stream issue
//    */
//   public static String computeSHA(InputStream inputStream) throws NoSuchAlgorithmException, IOException
//   {
//      final MessageDigest sha = MessageDigest.getInstance("SHA");
//      byte[] temp = new byte[4096];
//
//      int read = inputStream.read(temp);
//      while(read >= 0)
//      {
//         sha.update(temp, 0, read);
//
//         read = inputStream.read(temp);
//      }
//
//      inputStream.close();
//      inputStream = null;
//
//      temp = sha.digest();
//      final StringBuffer stringBuffer = new StringBuffer();
//      for(final byte b : temp)
//      {
//         read = b & 0xFF;
//         stringBuffer.append(Integer.toHexString((read >> 4) & 0xF));
//         stringBuffer.append(Integer.toHexString(read & 0xF));
//      }
//      temp = null;
//
//      return stringBuffer.toString();
//   }
//
//   /**
//    * Copy a file or directory. If directory, all content and sub-directory are copied
//    *
//    * @param source
//    *           File or directory source
//    * @param destination
//    *           File or directory destination
//    * @throws IOException
//    *            On coping issue
//    */
//   public static void copy(final File source, final File destination) throws IOException
//   {
//      final Stack<Pair<File, File>> stack = new Stack<Pair<File, File>>();
//      Pair<File, File> pair;
//
//      stack.push(new Pair<File, File>(source, destination));
//
//      while(stack.isEmpty() == false)
//      {
//         pair = stack.pop();
//
//         if(UtilIO.isVirtualLink(pair.element1) == false)
//         {
//            if(pair.element1.isDirectory() == false)
//            {
//               UtilIO.write(pair.element1, pair.element2);
//            }
//            else
//            {
//               UtilIO.createDirectory(pair.element2);
//
//               for(final File file : pair.element1.listFiles())
//               {
//                  stack.push(new Pair<File, File>(file, new File(pair.element2, file.getName())));
//               }
//            }
//         }
//      }
//   }
//
//   /**
//    * Create a directory and its parents if needs
//    *
//    * @param directory
//    *           Directory to create
//    * @return {@code true} if creation succeed. {@code false} if failed
//    */
//   public static boolean createDirectory(final File directory)
//   {
//      if((directory == null) || (UtilIO.isVirtualLink(directory) == true))
//      {
//         return false;
//      }
//
//      if(directory.exists() == true)
//      {
//         return directory.isDirectory();
//      }
//
//      if(UtilIO.createDirectory(directory.getParentFile()) == true)
//      {
//         return directory.mkdir();
//      }
//
//      return false;
//   }
//
//   /**
//    * Create a file and its parent directory if need
//    *
//    * @param file
//    *           File to create
//    * @return {@code true} if creation succeed. {@code false} if failed
//    */
//   public static boolean createFile(final File file)
//   {
//      if((file == null) || (UtilIO.isVirtualLink(file) == true))
//      {
//         return false;
//      }
//
//      if(file.exists() == true)
//      {
//         return file.isDirectory() == false;
//      }
//
//      if(UtilIO.createDirectory(file.getParentFile()) == true)
//      {
//         try
//         {
//            return file.createNewFile();
//         }
//         catch(final IOException exception)
//         {
//            Debug.printException(exception, "Failed to create file : ", file.getAbsolutePath());
//
//            return false;
//         }
//      }
//
//      return false;
//   }
//
//   /**
//    * Create a temporary file.<br>
//    * That is to say, a file inside the temporary directory
//    *
//    * @param string
//    *           File name
//    * @return Created file
//    * @throws IOException
//    *            On creation issue
//    */
//   public static File createTemporaryFile(final String string) throws IOException
//   {
//      final File file = new File(UtilIO.obtainTemporaryDirectory(), string);
//
//      if(UtilIO.createFile(file) == false)
//      {
//         throw new IOException("Can't create temporary file " + file.getAbsolutePath());
//      }
//
//      return file;
//   }
//
//   /**
//    * Delete a file or a directory.<br>
//    * If it is a directory, its delete all children first
//    *
//    * @param file
//    *           File/directory to delete
//    * @return {@code true} if succeed. {@code false} if failed, may be some deletion have happen
//    */
//   public static boolean delete(final File file)
//   {
//      if((file == null) || (file.exists() == false))
//      {
//         return true;
//      }
//
//      if((UtilIO.isVirtualLink(file) == false) && (file.isDirectory() == true))
//      {
//         for(final File child : file.listFiles())
//         {
//            if(UtilIO.delete(child) == false)
//            {
//               return false;
//            }
//         }
//      }
//
//      try
//      {
//         if(file.delete() == true)
//         {
//            return true;
//         }
//      }
//      catch(final Exception exception)
//      {
//         Debug.printException(exception, "Failed to delete : ", file.getAbsolutePath());
//
//         return false;
//      }
//
//      try
//      {
//         file.deleteOnExit();
//      }
//      catch(final Exception exception)
//      {
//         Debug.printException(exception, "Failed to delete on exit : ", file.getAbsolutePath());
//
//         return false;
//      }
//
//      return true;
//   }
//
//   /**
//    * Create a byte array from double.<br>
//    * Can be revert with {@link #byteArrayToDouble(byte[])}
//    *
//    * @param d
//    *           Double to convert
//    * @return Byte array created
//    */
//   public static byte[] doubleToByteArray(final double d)
//   {
//      return UtilIO.longToByteArray(Double.doubleToLongBits(d));
//   }
//
//   /**
//    * Write a base 64 String to stream as decoded binary.<br>
//    * Stream not close by the method
//    *
//    * @param base64
//    *           Base 64 string
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing issue
//    */
//   public static void fromBase64(final String base64, final OutputStream outputStream) throws IOException
//   {
//      final StringInputStream stringInputStream = new StringInputStream(base64);
//      final Base64InputStream base64InputStream = new Base64InputStream(stringInputStream);
//      UtilIO.write(base64InputStream, outputStream);
//   }
//
//   /**
//    * Indicates if a file is a virtual link.<br>
//    * A virtual link in Linux system is a way to have a reference to a file/directory as if it is in place, but the real file is
//    * other place. It is a way to share the same file by several directory
//    *
//    * @param file
//    *           File to test
//    * @return {@code true} if it is a virtual link
//    */
//   public static boolean isVirtualLink(final File file)
//   {
//      if((file == null) || (file.exists() == false))
//      {
//         return false;
//      }
//
//      try
//      {
//         return file.getCanonicalPath().equals(file.getAbsolutePath()) == false;
//      }
//      catch(final IOException exception)
//      {
//         Debug.printException(exception, "Failed to determine virtual link : ", file.getAbsolutePath());
//
//         return false;
//      }
//   }
//
//   /**
//    * Create a byte array from long.<br>
//    * Can be revert with {@link #byteArrayToLong(byte[])}
//    *
//    * @param l
//    *           Long to convert
//    * @return Byte array created
//    */
//   public static byte[] longToByteArray(final long l)
//   {
//      final byte[] array = new byte[8];
//
//      array[0] = (byte) ((l >> 56) & 0xFF);
//      array[1] = (byte) ((l >> 48) & 0xFF);
//      array[2] = (byte) ((l >> 40) & 0xFF);
//      array[3] = (byte) ((l >> 32) & 0xFF);
//      array[4] = (byte) ((l >> 24) & 0xFF);
//      array[5] = (byte) ((l >> 16) & 0xFF);
//      array[6] = (byte) ((l >> 8) & 0xFF);
//      array[7] = (byte) (l & 0xFF);
//
//      return array;
//   }
//
//   /**
//    * Obtain a file outside of the code.<br>
//    * If this class is in a jar called A.jar, and this jar is in /My/Path/A.jar then the file will be relative to /My/Path
//    *
//    * @param path
//    *           Relative path
//    * @return The file
//    */
//   public static File obtainExternalFile(final String path)
//   {
//      return UtilIO.obtainFile(UtilIO.obtainOutsideDirectory(), path);
//   }
//
//   /**
//    * Obtain a file relative to a directory
//    *
//    * @param directory
//    *           Directory reference
//    * @param path
//    *           Path search
//    * @return The file
//    */
//   public static File obtainFile(final File directory, final String path)
//   {
//      return UtilIO.obtainFile(directory, path, UtilIO.PATH_SEPARATOR);
//   }
//
//   /**
//    * Obtain a file relative to a directory
//    *
//    * @param directory
//    *           Directory reference
//    * @param path
//    *           Path search
//    * @param separator
//    *           Separator use inside the path
//    * @return The file
//    */
//   public static File obtainFile(final File directory, final String path, final char separator)
//   {
//      File file = directory;
//      final StringCutter stringCutter = new StringCutter(path, separator);
//
//      String next = stringCutter.next();
//
//      while(next != null)
//      {
//         if(UtilIO.PREVIOUS_DIRECTORY.equals(next) == true)
//         {
//            file = file.getParentFile();
//         }
//         else if((UtilIO.CURRENT_DIRECTORY.equals(next) == false) && (next.length() > 0))
//         {
//            file = new File(file, next);
//         }
//
//         next = stringCutter.next();
//      }
//
//      return file;
//   }
//
//   /**
//    * Obtain "home" directory
//    *
//    * @return "Home" directory
//    */
//   public static File obtainHomeDirectory()
//   {
//      if(UtilIO.homeDirectory != null)
//      {
//         return UtilIO.homeDirectory;
//      }
//
//      UtilIO.homeDirectory = UtilIO.obtainOutsideDirectory();
//
//      try
//      {
//         final String home = System.getProperty("user.home");
//
//         if(home != null)
//         {
//            final File directory = new File(home);
//            if((directory.exists() == true) && (directory.canRead() == true) && (directory.canWrite() == true))
//            {
//               UtilIO.homeDirectory = directory;
//            }
//         }
//      }
//      catch(final Exception exception)
//      {
//         Debug.printException(exception, "Failed to get home directory, use outside directory");
//      }
//
//      return UtilIO.homeDirectory;
//   }
//
//   /**
//    * Obtain directory outside the code
//    *
//    * @return Directory outside the code
//    */
//   public static File obtainOutsideDirectory()
//   {
//      if(UtilIO.outsideDirectory == null)
//      {
//         String className = UtilIO.class.getName();
//
//         int index = className.lastIndexOf('.');
//         if(index >= 0)
//         {
//            className = className.substring(index + 1);
//         }
//
//         className += ".class";
//
//         final URL url = UtilIO.class.getResource(className);
//         final String path = url.getFile();
//
//         index = path.indexOf(".jar!");
//
//         int start = 0;
//         if(path.startsWith("file://") == true)
//         {
//            start = 7;
//         }
//         else if(path.startsWith("file:") == true)
//         {
//            start = 5;
//         }
//
//         if(index > 0)
//         {
//            UtilIO.outsideDirectory = new File(path.substring(start, path.lastIndexOf('/', index - 1)));
//         }
//         else
//         {
//            UtilIO.outsideDirectory = (new File(path)).getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
//         }
//      }
//
//      return UtilIO.outsideDirectory;
//   }
//
//   /**
//    * Temporary directory
//    *
//    * @return Temporary directory
//    */
//   public static File obtainTemporaryDirectory()
//   {
//      if(UtilIO.temporaryDirectory != null)
//      {
//         return UtilIO.temporaryDirectory;
//      }
//
//      String path = System.getProperty("user.home");
//      File directory = null;
//
//      if(path != null)
//      {
//         directory = new File(path);
//         if((directory.exists() == false) || (directory.canRead() == false) || (directory.canWrite() == false))
//         {
//            directory = null;
//         }
//      }
//
//      if(directory == null)
//      {
//         path = System.getProperty("user.dir");
//
//         if(path != null)
//         {
//            directory = new File(path);
//            if((directory.exists() == false) || (directory.canRead() == false) || (directory.canWrite() == false))
//            {
//               directory = null;
//            }
//         }
//      }
//
//      if(directory == null)
//      {
//         directory = UtilIO.obtainOutsideDirectory();
//      }
//
//      directory = new File(directory, "JHelp/temporary");
//      UtilIO.createDirectory(directory);
//
//      return UtilIO.temporaryDirectory = directory;
//   }
//
//   /**
//    * Read a {@link BigInteger} from a stream.<br>
//    * Previously write with {@link #writeBigInteger(BigInteger, OutputStream)}
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return {@link BigInteger} read
//    * @throws IOException
//    *            On reading issue
//    */
//   public static BigInteger readBigInteger(final InputStream inputStream) throws IOException
//   {
//      final int length = UtilIO.readInteger(inputStream);
//      final byte[] temp = new byte[length];
//
//      UtilIO.readStream(inputStream, temp);
//
//      return new BigInteger(temp);
//   }
//
//   /**
//    * Read a {@link Binarizable} inside a stream.<br>
//    * The {@link Binarizable} should be previously written by {@link #writeBinarizable(Binarizable, OutputStream)}
//    *
//    * @param <B>
//    *           {@link Binarizable} type
//    * @param clas
//    *           Class of the {@link Binarizable}
//    * @param inputStream
//    *           Stream to read
//    * @return The {@link Binarizable} read
//    * @throws IOException
//    *            On read the stream or the data not represents the asked {@link Binarizable}
//    */
//   public static <B extends Binarizable> B readBinarizable(final Class<B> clas, final InputStream inputStream) throws IOException
//   {
//      try
//      {
//         final ByteArray byteArray = new ByteArray();
//
//         UtilIO.write(inputStream, byteArray.getOutputStream());
//
//         return byteArray.readBinarizable(clas);
//      }
//      catch(final Exception exception)
//      {
//         throw new IOException("Failed to read the Binarizable " + clas.getName() + " in the given stream !", exception);
//      }
//   }
//
//   /**
//    * Read a {@link Binarizable} inside a stream.<br>
//    * The {@link Binarizable} should be previously written by {@link #writeBinarizableNamed(Binarizable, OutputStream)}
//    *
//    * @param <B>
//    *           {@link Binarizable} type
//    * @param inputStream
//    *           Stream to read
//    * @return The {@link Binarizable} read
//    * @throws IOException
//    *            On read the stream or the data not represents the asked {@link Binarizable}
//    */
//   public static <B extends Binarizable> B readBinarizableNamed(final InputStream inputStream) throws IOException
//   {
//      try
//      {
//         final String name = UtilIO.readString(inputStream);
//
//         if(name == null)
//         {
//            return null;
//         }
//
//         @SuppressWarnings("unchecked")
//         final Class<B> clas = (Class<B>) Class.forName(name);
//
//         return UtilIO.readBinarizable(clas, inputStream);
//      }
//      catch(final Exception exception)
//      {
//         throw new IOException("Failed to read the Binarizable in the given stream !", exception);
//      }
//   }
//
//   /**
//    * Read a byte array from stream
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return Read array
//    * @throws IOException
//    *            On reading issue
//    */
//   public static byte[] readByteArray(final InputStream inputStream) throws IOException
//   {
//      final int length = UtilIO.readInteger(inputStream);
//
//      if(length < 0)
//      {
//         return null;
//      }
//
//      final byte[] array = new byte[length];
//
//      final int read = UtilIO.readStream(inputStream, array, 0, length);
//
//      return Arrays.copyOfRange(array, 0, read);
//   }
//
//   /**
//    * Read double from stream
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return Read double
//    * @throws IOException
//    *            On reading problem
//    */
//   public static double readDouble(final InputStream inputStream) throws IOException
//   {
//      return Double.longBitsToDouble(UtilIO.readLong(inputStream));
//   }
//
//   /**
//    * Read a file header (First bytes of a file)
//    *
//    * @param file
//    *           File to read header
//    * @return Header read
//    * @throws IOException
//    *            On reading issue
//    */
//   public static byte[] readFileHeader(final File file) throws IOException
//   {
//      final int size = (int) Math.min(UtilIO.HEADER_SIZE, file.length());
//      final byte[] header = new byte[size];
//      FileInputStream fileInputStream = null;
//
//      try
//      {
//         fileInputStream = new FileInputStream(file);
//         UtilIO.readStream(fileInputStream, header);
//      }
//      catch(final Exception exception)
//      {
//         throw new IOException("Failed to get header of " + file.getAbsolutePath(), exception);
//      }
//      finally
//      {
//         if(fileInputStream != null)
//         {
//            try
//            {
//               fileInputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//
//      return header;
//   }
//
//   /**
//    * Read float from a stream
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return Float read
//    * @throws IOException
//    *            On read issue
//    */
   public static float readFloat(final InputStream inputStream) throws IOException
   {
      return Float.intBitsToFloat(UtilIO.readInteger(inputStream));
   }
//
//   /**
//    * Read float[] from a stream
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return Float array read
//    * @throws IOException
//    *            On read issue
//    */
//   public static float[] readFloatArray(final InputStream inputStream) throws IOException
//   {
//      final int length = UtilIO.readInteger(inputStream);
//
//      if(length < 0)
//      {
//         return null;
//      }
//
//      final float[] array = new float[length];
//      for(int a = 0; a < length; a++)
//      {
//         array[a] = UtilIO.readFloat(inputStream);
//      }
//
//      return array;
//   }
//
//   /**
//    * Read an integer from stream
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return Integer read
//    * @throws IOException
//    *            On reading issue
//    */
   public static int readInteger(final InputStream inputStream) throws IOException
   {
      return (inputStream.read() << 24) | (inputStream.read() << 16) | (inputStream.read() << 8) | inputStream.read();
   }
//
//   /**
//    * Read long from stream
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return Read long
//    * @throws IOException
//    *            On reading problem
//    */
//   public static long readLong(final InputStream inputStream) throws IOException
//   {
//      long integer = 0;
//
//      integer |= (long) inputStream.read() << 56L;
//      integer |= (long) inputStream.read() << 48L;
//      integer |= (long) inputStream.read() << 40L;
//      integer |= (long) inputStream.read() << 32L;
//      integer |= (long) inputStream.read() << 24L;
//      integer |= (long) inputStream.read() << 16L;
//      integer |= (long) inputStream.read() << 8L;
//      integer |= inputStream.read();
//
//      return integer;
//   }
//
//   /**
//    * Read stream and fill an array.<br>
//    * The write in array start at begin.<br>
//    * It stop to read stream if stream reach its end or the array is full.<br>
//    * Do same as {@link #readStream(InputStream, byte[], int, int) readStream(inputStream, array, 0, array.length)}
//    *
//    * @param inputStream
//    *           Stream to read
//    * @param array
//    *           Array to fill
//    * @return Number of bytes read (It can be less than array length if stream have not enough data to fill totaly the array)
//    * @throws IOException
//    *            On reading issue
//    */
//   public static int readStream(final InputStream inputStream, final byte[] array) throws IOException
//   {
//      return UtilIO.readStream(inputStream, array, 0, array.length);
//   }
//
//   /**
//    * Read stream and fill an array.<br>
//    * The write in array start at the offset specify.<br>
//    * It stop to read stream if stream reach its end or the array is full<br>
//    * Do same as {@link #readStream(InputStream, byte[], int, int) readStream(inputStream, array, offset, array.length -
//    * offset)}
//    *
//    * @param inputStream
//    *           Stream to read
//    * @param array
//    *           Array to fill
//    * @param offset
//    *           To start writing
//    * @return Number of bytes read (It can be less than array length if stream have not enough data to fill totaly the array)
//    * @throws IOException
//    *            On reading issue
//    */
//   public static int readStream(final InputStream inputStream, final byte[] array, final int offset) throws IOException
//   {
//      return UtilIO.readStream(inputStream, array, offset, array.length - offset);
//   }
//
//   /**
//    * Read stream and fill an array.<br>
//    * The write in array start at the offset specify.<br>
//    * It stop to read stream if stream reach its end or the specify length is reach
//    *
//    * @param inputStream
//    *           Stream to read
//    * @param array
//    *           Array to fill
//    * @param offset
//    *           To start writing
//    * @param length
//    *           Number of bytes to read at maximum
//    * @return Number of bytes read (It can be less than specify length if stream have not enough data to respect the length)
//    * @throws IOException
//    *            On reading issue
//    */
//   public static int readStream(final InputStream inputStream, final byte[] array, int offset, final int length) throws IOException
//   {
//      int left = Math.min(array.length - offset, length);
//
//      if(left <= 0)
//      {
//         return 0;
//      }
//
//      int total = 0;
//
//      int read = inputStream.read(array, offset, left);
//
//      if(read < 0)
//      {
//         return -1;
//      }
//
//      offset += read;
//      total += read;
//      left -= read;
//
//      while((read >= 0) && (left > 0))
//      {
//         read = inputStream.read(array, offset, left);
//
//         if(read >= 0)
//         {
//            offset += read;
//            total += read;
//            left -= read;
//         }
//      }
//
//      return total;
//   }
//
//   /**
//    * Read stream from stream
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return Read string
//    * @throws IOException
//    *            On reading issue
//    */
//   public static String readString(final InputStream inputStream) throws IOException
//   {
//      final byte[] utf8 = UtilIO.readByteArray(inputStream);
//
//      if(utf8 == null)
//      {
//         return null;
//      }
//
//      return UtilText.readUTF8(utf8, 0, utf8.length);
//   }
//
//   /**
//    * Do a prompt in console, waiting user type something (finish by enter) in console.<br>
//    * {@code null} is return in case of issue
//    *
//    * @return The user input OR {@code null} in case of issue
//    */
//   public static String readUserInputInConsole()
//   {
//      try
//      {
//         final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
//         return bufferedReader.readLine();
//      }
//      catch(final Exception exception)
//      {
//         Debug.printException(exception, "Issue while reading console user input");
//         return null;
//      }
//   }
//
//   /**
//    * Rename a file
//    *
//    * @param source
//    *           File source
//    * @param destination
//    *           File destination
//    * @throws IOException
//    *            On rename issue
//    */
//   public static void rename(final File source, final File destination) throws IOException
//   {
//      if(source.getAbsolutePath().equals(destination.getAbsolutePath()) == true)
//      {
//         return;
//      }
//
//      UtilIO.copy(source, destination);
//      UtilIO.delete(source);
//   }
//
//   /**
//    * Transform a binary stream to base 64 string.<br>
//    * Stream not close by the method
//    *
//    * @param inputStream
//    *           Stream to read
//    * @return Base 64 string
//    * @throws IOException
//    *            On reading issue
//    */
//   public static String toBase64(final InputStream inputStream) throws IOException
//   {
//      final StringOutputStream stringOutputStream = new StringOutputStream();
//      final Base64OutputStream base64OutputStream = new Base64OutputStream(stringOutputStream);
//      UtilIO.write(inputStream, base64OutputStream);
//      return stringOutputStream.getString();
//   }
//
//   /**
//    * Unzip a file inside a directory
//    *
//    * @param directoryDestination
//    *           Directory where unzip
//    * @param zip
//    *           Zip file
//    * @throws IOException
//    *            On extracting issue
//    */
//   public static void unzip(final File directoryDestination, final File zip) throws IOException
//   {
//      FileInputStream fileInputStream = null;
//
//      try
//      {
//         fileInputStream = new FileInputStream(zip);
//
//         UtilIO.unzip(directoryDestination, fileInputStream);
//      }
//      catch(final IOException exception)
//      {
//         throw exception;
//      }
//      finally
//      {
//         if(fileInputStream != null)
//         {
//            try
//            {
//               fileInputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//   }
//
//   /**
//    * Unzip a stream inside a directory
//    *
//    * @param directoryDestination
//    *           Directory where unzip
//    * @param inputStreamZip
//    *           Stream to unzip
//    * @throws IOException
//    *            On unzipping issue
//    */
//   public static void unzip(final File directoryDestination, final InputStream inputStreamZip) throws IOException
//   {
//      File destination;
//      final ZipInputStream zipInputStream = new ZipInputStream(inputStreamZip);
//
//      ZipEntry zipEntry = zipInputStream.getNextEntry();
//      String name;
//
//      while(zipEntry != null)
//      {
//         name = zipEntry.getName();
//         destination = UtilIO.obtainFile(directoryDestination, name);
//
//         if(name.endsWith("/") == true)
//         {
//            if(UtilIO.createDirectory(destination) == false)
//            {
//               throw new IOException("Can't create the directory " + destination.getAbsolutePath());
//            }
//         }
//         else
//         {
//            if(UtilIO.createFile(destination) == false)
//            {
//               throw new IOException("Can't create the file " + destination.getAbsolutePath());
//            }
//
//            UtilIO.write(zipInputStream, destination);
//         }
//
//         zipInputStream.closeEntry();
//
//         zipEntry = zipInputStream.getNextEntry();
//      }
//   }
//
//   /**
//    * Copy a file inside an other one
//    *
//    * @param fileSource
//    *           Source file
//    * @param fileDestination
//    *           Destination file
//    * @throws IOException
//    *            On copying issue
//    */
//   public static void write(final File fileSource, final File fileDestination) throws IOException
//   {
//      FileInputStream fileInputStream = null;
//      FileOutputStream fileOutputStream = null;
//
//      if(UtilIO.createFile(fileDestination) == false)
//      {
//         throw new IOException("Can't create the file " + fileDestination.getAbsolutePath());
//      }
//
//      try
//      {
//         fileInputStream = new FileInputStream(fileSource);
//         fileOutputStream = new FileOutputStream(fileDestination);
//
//         UtilIO.write(fileInputStream, fileOutputStream);
//      }
//      catch(final IOException exception)
//      {
//         throw exception;
//      }
//      finally
//      {
//         if(fileOutputStream != null)
//         {
//            try
//            {
//               fileOutputStream.flush();
//            }
//            catch(final Exception exception)
//            {
//            }
//
//            try
//            {
//               fileOutputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//
//         if(fileInputStream != null)
//         {
//            try
//            {
//               fileInputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//   }
//
//   /**
//    * Write a file inside a stream
//    *
//    * @param fileSource
//    *           Source file
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On copying issue
//    */
//   public static void write(final File fileSource, final OutputStream outputStream) throws IOException
//   {
//      FileInputStream fileInputStream = null;
//
//      try
//      {
//         fileInputStream = new FileInputStream(fileSource);
//
//         UtilIO.write(fileInputStream, outputStream);
//      }
//      catch(final IOException exception)
//      {
//         throw exception;
//      }
//      finally
//      {
//         if(fileInputStream != null)
//         {
//            try
//            {
//               fileInputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//   }
//
//   /**
//    * Write a stream inside a file
//    *
//    * @param inputStream
//    *           Stream source
//    * @param fileDestination
//    *           File destination
//    * @throws IOException
//    *            On copying issue
//    */
//   public static void write(final InputStream inputStream, final File fileDestination) throws IOException
//   {
//      FileOutputStream fileOutputStream = null;
//
//      if(UtilIO.createFile(fileDestination) == false)
//      {
//         throw new IOException("Can't create the file " + fileDestination.getAbsolutePath());
//      }
//
//      try
//      {
//         fileOutputStream = new FileOutputStream(fileDestination);
//
//         UtilIO.write(inputStream, fileOutputStream);
//      }
//      catch(final IOException exception)
//      {
//         throw exception;
//      }
//      finally
//      {
//         if(fileOutputStream != null)
//         {
//            try
//            {
//               fileOutputStream.flush();
//            }
//            catch(final Exception exception)
//            {
//            }
//
//            try
//            {
//               fileOutputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//   }
//
//   /**
//    * Write a stream inside on other one
//    *
//    * @param inputStream
//    *           Stream source
//    * @param outputStream
//    *           Stream destination
//    * @throws IOException
//    *            On copying issue
//    */
//   public static void write(final InputStream inputStream, final OutputStream outputStream) throws IOException
//   {
//      final byte[] buffer = new byte[UtilIO.BUFFER_SIZE];
//
//      int read = inputStream.read(buffer);
//
//      while(read >= 0)
//      {
//         outputStream.write(buffer, 0, read);
//
//         read = inputStream.read(buffer);
//      }
//   }
//
//   /**
//    * Write a {@link BigInteger} in stream.<br>
//    * To read later, you can use {@link #readBigInteger(InputStream)}
//    *
//    * @param bigInteger
//    *           {@link BigInteger} to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing issue
//    */
//   public static void writeBigInteger(final BigInteger bigInteger, final OutputStream outputStream) throws IOException
//   {
//      final byte[] temp = bigInteger.toByteArray();
//
//      UtilIO.writeInteger(temp.length, outputStream);
//      outputStream.write(temp);
//   }
//
//   /**
//    * Write a {@link Binarizable} inside a stream.<br>
//    * To read it later, use {@link #readBinarizable(Class, InputStream)}
//    *
//    * @param binarizable
//    *           {@link Binarizable} to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing issue
//    */
//   public static void writeBinarizable(final Binarizable binarizable, final OutputStream outputStream) throws IOException
//   {
//      final ByteArray byteArray = new ByteArray();
//
//      byteArray.writeBinarizable(binarizable);
//
//      UtilIO.write(byteArray.getInputStream(), outputStream);
//   }
//
//   /**
//    * Write a {@link Binarizable} inside a stream.<br>
//    * To read it later, use {@link #readBinarizableNamed(InputStream)}
//    *
//    * @param binarizable
//    *           {@link Binarizable} to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing issue
//    */
//   public static void writeBinarizableNamed(final Binarizable binarizable, final OutputStream outputStream) throws IOException
//   {
//      UtilIO.writeString(binarizable.getClass().getName(), outputStream);
//
//      UtilIO.writeBinarizable(binarizable, outputStream);
//   }
//
//   /**
//    * Write a part of byte array on stream
//    *
//    * @param array
//    *           Array to write
//    * @param offset
//    *           Offset where start read the array
//    * @param length
//    *           Number of byte to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing issue
//    */
//   public static void writeByteArray(final byte[] array, final int offset, final int length, final OutputStream outputStream) throws IOException
//   {
//      final int len = Math.min(array.length - offset, length);
//
//      UtilIO.writeInteger(len, outputStream);
//      outputStream.write(array, offset, len);
//   }
//
//   /**
//    * Write an array on stream.<br>
//    * Same as {@link #writeByteArray(byte[], int, int, OutputStream) writeByteArray(array, 0, array.length, outputStream)}
//    *
//    * @param array
//    *           Array to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing issue
//    */
//   public static void writeByteArray(final byte[] array, final OutputStream outputStream) throws IOException
//   {
//      UtilIO.writeByteArray(array, 0, array.length, outputStream);
//   }
//
//   /**
//    * Write double in a stream
//    *
//    * @param d
//    *           Double to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing problem
//    */
//   public static void writeDouble(final double d, final OutputStream outputStream) throws IOException
//   {
//      UtilIO.writeLong(Double.doubleToLongBits(d), outputStream);
//   }
//
//   /**
//    * Write a float in stream
//    *
//    * @param f
//    *           Float to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing issue
//    */
//   public static void writeFloat(final float f, final OutputStream outputStream) throws IOException
//   {
//      UtilIO.writeInteger(Float.floatToIntBits(f), outputStream);
//   }
//
//   /**
//    * Write a float[] in stream
//    *
//    * @param array
//    *           Float array to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing issue
//    */
//   public static void writeFloatArray(final float[] array, final OutputStream outputStream) throws IOException
//   {
//      if(array == null)
//      {
//         UtilIO.writeInteger(-1, outputStream);
//
//         return;
//      }
//
//      final int length = array.length;
//      UtilIO.writeInteger(length, outputStream);
//
//      for(int a = 0; a < length; a++)
//      {
//         UtilIO.writeFloat(array[a], outputStream);
//      }
//   }
//
//   /**
//    * Write an integer to stream
//    *
//    * @param integer
//    *           Integer to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing issue
//    */
//   public static void writeInteger(final int integer, final OutputStream outputStream) throws IOException
//   {
//      outputStream.write((integer >> 24) & 0xFF);
//      outputStream.write((integer >> 16) & 0xFF);
//      outputStream.write((integer >> 8) & 0xFF);
//      outputStream.write(integer & 0xFF);
//   }
//
//   /**
//    * Write long in a stream
//    *
//    * @param integer
//    *           Long to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing problem
//    */
//   public static void writeLong(final long integer, final OutputStream outputStream) throws IOException
//   {
//      outputStream.write((int) ((integer >> 56) & 0xFF));
//      outputStream.write((int) ((integer >> 48) & 0xFF));
//      outputStream.write((int) ((integer >> 40) & 0xFF));
//      outputStream.write((int) ((integer >> 32) & 0xFF));
//      outputStream.write((int) ((integer >> 24) & 0xFF));
//      outputStream.write((int) ((integer >> 16) & 0xFF));
//      outputStream.write((int) ((integer >> 8) & 0xFF));
//      outputStream.write((int) (integer & 0xFF));
//   }
//
//   /**
//    * Write string to stream
//    *
//    * @param string
//    *           String to write
//    * @param outputStream
//    *           Stream where write
//    * @throws IOException
//    *            On writing issue
//    */
//   public static void writeString(final String string, final OutputStream outputStream) throws IOException
//   {
//      final byte[] utf8 = UtilText.toUTF8(string);
//
//      UtilIO.writeByteArray(utf8, 0, utf8.length, outputStream);
//   }
//
//   /**
//    * Zip a file or directory inside a file
//    *
//    * @param source
//    *           File/directory to zip
//    * @param destination
//    *           File destination
//    * @throws IOException
//    *            On zipping issue
//    */
//   public static void zip(final File source, final File destination) throws IOException
//   {
//      UtilIO.zip(source, destination, false);
//   }
//
//   /**
//    * Zip a file or directory inside a file
//    *
//    * @param source
//    *           File/directory to zip
//    * @param destination
//    *           File destination
//    * @param onlyContentIfDirectory
//    *           Indicates to zip only directory content (not the directory itself) if the given file is a directory.
//    * @throws IOException
//    *            On zipping issue
//    */
//   public static void zip(final File source, final File destination, final boolean onlyContentIfDirectory) throws IOException
//   {
//      if(UtilIO.createFile(destination) == false)
//      {
//         throw new IOException("Can't create " + destination.getAbsolutePath());
//      }
//
//      FileOutputStream fileOutputStream = null;
//
//      try
//      {
//         fileOutputStream = new FileOutputStream(destination);
//
//         UtilIO.zip(source, fileOutputStream, onlyContentIfDirectory);
//      }
//      catch(final IOException exception)
//      {
//         throw exception;
//      }
//      finally
//      {
//         if(fileOutputStream != null)
//         {
//            try
//            {
//               fileOutputStream.flush();
//            }
//            catch(final Exception exception)
//            {
//            }
//
//            try
//            {
//               fileOutputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//   }
//
//   /**
//    * Zip a file or directory inside a stream
//    *
//    * @param source
//    *           File/directory to zip
//    * @param outputStreamZip
//    *           Where write the zip
//    * @throws IOException
//    *            On zipping issue
//    */
//   public static void zip(final File source, final OutputStream outputStreamZip) throws IOException
//   {
//      UtilIO.zip(source, outputStreamZip, false);
//   }
//
//   /**
//    * Zip a file or directory inside a stream
//    *
//    * @param source
//    *           File/directory to zip
//    * @param outputStreamZip
//    *           Where write the zip
//    * @param onlyContentIfDirectory
//    *           Indicates to zip only directory content (not the directory itself) if the given file is a directory.
//    * @throws IOException
//    *            On zipping issue
//    */
//   public static void zip(final File source, final OutputStream outputStreamZip, final boolean onlyContentIfDirectory) throws IOException
//   {
//      ZipEntry zipEntry;
//      final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStreamZip);
//      // For the best compression
//      zipOutputStream.setLevel(9);
//
//      Pair<String, File> pair = new Pair<String, File>(source.getName(), source);
//      final Stack<Pair<String, File>> stack = new Stack<Pair<String, File>>();
//
//      stack.push(pair);
//      boolean ignore = (source.isDirectory() == true) && (onlyContentIfDirectory == true);
//
//      while(stack.isEmpty() == false)
//      {
//         pair = stack.pop();
//
//         if(UtilIO.isVirtualLink(pair.element2) == false)
//         {
//            if(pair.element2.isDirectory() == true)
//            {
//               if(ignore == false)
//               {
//                  for(final File child : pair.element2.listFiles())
//                  {
//                     stack.push(new Pair<String, File>(pair.element1 + "/" + child.getName(), child));
//                  }
//               }
//               else
//               {
//                  for(final File child : pair.element2.listFiles())
//                  {
//                     stack.push(new Pair<String, File>(child.getName(), child));
//                  }
//               }
//            }
//            else if(ignore == false)
//            {
//               zipEntry = new ZipEntry(pair.element1);
//               // For the best compression
//               zipEntry.setMethod(ZipEntry.DEFLATED);
//
//               zipOutputStream.putNextEntry(zipEntry);
//
//               UtilIO.write(pair.element2, zipOutputStream);
//
//               zipOutputStream.closeEntry();
//            }
//         }
//
//         ignore = false;
//      }
//
//      zipOutputStream.finish();
//      zipOutputStream.flush();
//   }
//
//   /**
//    * To avoid instance
//    */
//   private UtilIO()
//   {
//   }
}