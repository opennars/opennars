/**
 * Project : JHelpGUI<br>
 * Package : jhelp.util.gui<br>
 * Class : ClipBoardManager<br>
 * Date : 16 mai 2009<br>
 * By JHelp
 */
package jhelp.util.gui;

import jhelp.util.debug.Debug;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage clip board<br>
 * It tries to link with system clip board, if this link failed, it use an internal clip board<br>
 * <br>
 * Last modification : 16 mai 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class ClipBoardManager
{
   /**
    * Default security manager that accept all.<br>
    * It use when no security manager found <br>
    * <br>
    * Last modification : 16 juin 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   private class AllPermissionSecuryManager
         extends SecurityManager
   {
      /**
       * Constructs AllPermissionSecuryManager
       */
      public AllPermissionSecuryManager()
      {
      }

      /**
       * Check if permission is allowed
       * 
       * @param perm
       *           Tested permission
       * @see SecurityManager#checkPermission(Permission)
       */
      @Override
      public void checkPermission(final Permission perm)
      {
         // Debug.println("Yes you can do : ", perm);
      }
   }

   /** Clip board manager singleton */
   public static final ClipBoardManager CLIP_BOARD = new ClipBoardManager();
   /** Clip bord link with */
   private Clipboard                    clipboard;

   /**
    * Constructs ClipBoardManager
    */
   private ClipBoardManager()
   {
      SecurityManager securityManager = System.getSecurityManager();
      if(securityManager == null)
      {
         securityManager = new AllPermissionSecuryManager();
         System.setSecurityManager(securityManager);
      }

      try
      {
         securityManager.checkSystemClipboardAccess();
         this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      }
      catch(final Exception exception)
      {
         Debug.printException(exception);
      }

      if(this.clipboard == null)
      {
         this.clipboard = new Clipboard(ClipBoardManager.class.getName());
      }
   }

   /**
    * Decode a content store in byte buffer in clip board to string
    * 
    * @param dataFlavor
    *           Nature of data
    * @return Decode string or {@code null} if decode failed
    */
   private String decodeByteBuffer(final DataFlavor dataFlavor)
   {
      try
      {
         final ByteBuffer byteBuffer = (ByteBuffer) this.clipboard.getData(dataFlavor);
         byteBuffer.rewind();
         return new String(byteBuffer.array());
      }
      catch(final Exception exception)
      {
         Debug.printException(exception);
      }
      return null;
   }

   /**
    * Decode a content store in char buffer in clip board to string
    * 
    * @param dataFlavor
    *           Nature of data
    * @return Decode string or {@code null} if decode failed
    */
   private String decodeCharBuffer(final DataFlavor dataFlavor)
   {
      try
      {
         final CharBuffer byteBuffer = (CharBuffer) this.clipboard.getData(dataFlavor);
         byteBuffer.rewind();
         return new String(byteBuffer.array());
      }
      catch(final Exception exception)
      {
         Debug.printException(exception);
      }
      return null;
   }

   /**
    * Decode a content store in input stream in clip board to string
    * 
    * @param dataFlavor
    *           Nature of data
    * @return Decode string or {@code null} if decode failed
    */
   private String decodeInputStream(final DataFlavor dataFlavor)
   {
      try
      {
         final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         final InputStream inputStream = (InputStream) this.clipboard.getData(dataFlavor);
         byte[] temp = new byte[4096];
         int read = inputStream.read(temp);
         while(read >= 0)
         {
            byteArrayOutputStream.write(temp, 0, read);
            read = inputStream.read(temp);
         }
         byteArrayOutputStream.flush();
         inputStream.close();
         byteArrayOutputStream.close();
         temp = null;

         return new String(byteArrayOutputStream.toByteArray());
      }
      catch(final Exception exception)
      {
         Debug.printException(exception);
      }
      return null;
   }

   /**
    * Decode a content store in reader in clip board to string
    * 
    * @param dataFlavor
    *           Nature of data
    * @return Decode string or {@code null} if decode failed
    */
   private String decodeReader(final DataFlavor dataFlavor)
   {
      try
      {
         final StringBuffer stringBuffer = new StringBuffer();
         final BufferedReader reader = new BufferedReader((Reader) this.clipboard.getData(dataFlavor));

         String line = reader.readLine();
         while(line != null)
         {
            stringBuffer.append(line);

            line = reader.readLine();
            if(line != null)
            {
               stringBuffer.append('\n');
            }
         }
         reader.close();

         return stringBuffer.toString();
      }
      catch(final Exception exception)
      {
         Debug.printException(exception);
      }
      return null;
   }

   /**
    * Indicates if clip board contains a file list
    * 
    * @return {@code true} if clip board contains a file list
    */
   public boolean isFileListStore()
   {
      try
      {
         return this.clipboard.getData(DataFlavor.javaFileListFlavor) != null;
      }
      catch(final Exception exception)
      {
         return false;
      }
   }

   /**
    * Indicates if clip board contains a string
    * 
    * @return {@code true} if clip board contains a string
    */
   public boolean isStringStore()
   {
      return this.obtainString() != null;
   }

   /**
    * Obtain list of file from clip board
    * 
    * @return List of file from clip board
    */
   public List<File> obtainListOfFile()
   {
      final ArrayList<File> listFile = new ArrayList<File>();

      try
      {
         final List<?> list = (List<?>) this.clipboard.getData(DataFlavor.javaFileListFlavor);

         if(list == null)
         {
            return null;
         }

         for(final Object file : list)
         {
            if(file != null)
            {
               listFile.add((File) file);
            }
         }
      }
      catch(final Exception e)
      {
         Debug.printException(e);

         return null;
      }

      return listFile;
   }

   /**
    * Obtain the stored string in clip board
    * 
    * @return Stored string or {@code null} if no string inside
    */
   public String obtainString()
   {
      String data;
      for(final DataFlavor dataFlavor : this.clipboard.getAvailableDataFlavors())
      {
         if(dataFlavor.isFlavorTextType() == true)
         {
            if(dataFlavor.isRepresentationClassByteBuffer() == true)
            {
               if((data = this.decodeByteBuffer(dataFlavor)) != null)
               {
                  return data;
               }
            }
            else if(dataFlavor.isRepresentationClassCharBuffer() == true)
            {
               if((data = this.decodeCharBuffer(dataFlavor)) != null)
               {
                  return data;
               }
            }
            else if(dataFlavor.isRepresentationClassInputStream() == true)
            {
               if((data = this.decodeInputStream(dataFlavor)) != null)
               {
                  return data;
               }
            }
            else if(dataFlavor.isRepresentationClassReader() == true)
            {
               if((data = this.decodeReader(dataFlavor)) != null)
               {
                  return data;
               }
            }
            else if(String.class.isAssignableFrom(dataFlavor.getRepresentationClass()) == true)
            {
               try
               {
                  return (String) this.clipboard.getData(dataFlavor);
               }
               catch(final Exception exception)
               {
                  Debug.printException(exception);
               }
            }
         }
      }

      return null;
   }

   /**
    * Store file list in clip board
    * 
    * @param files
    *           File list to store
    * @return {@code true} if store append
    */
   public boolean storeFileList(final File... files)
   {
      if(this.clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
      {
         this.clipboard.setContents(new JavaFileSelection(files), null);

         return true;
      }

      return false;
   }

   /**
    * Store a file list
    * 
    * @param fileList
    *           File list
    * @return {@code true} if store append
    */
   public boolean storeFileList(final List<File> fileList)
   {
      if(this.clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
      {
         this.clipboard.setContents(new JavaFileSelection(fileList), null);

         return true;
      }

      return false;
   }

   /**
    * Store string in clip board
    * 
    * @param text
    *           String to stroe
    */
   public void storeString(final String text)
   {
      this.clipboard.setContents(new StringSelection(text), null);
   }
}