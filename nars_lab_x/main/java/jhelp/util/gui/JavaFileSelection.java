package jhelp.util.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Java file list selection in drag and drop operation between Swing GUI and Operating System <br>
 * <br>
 * Last modification : 9 aoet 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class JavaFileSelection
      implements Transferable
{
   /** Accepted flavors */
   public static final DataFlavor[] FLAVORS =
                                            {
                                               DataFlavor.javaFileListFlavor
                                            };

   /** File list carry */
   private final List<File>         listFile;

   /**
    * Constructs JavaFileSelection
    * 
    * @param files
    *           List of files
    */
   public JavaFileSelection(final File... files)
   {
      this.listFile = new ArrayList<File>();

      for(final File file : files)
      {
         this.listFile.add(file);
      }
   }

   /**
    * Constructs JavaFileSelection
    * 
    * @param listFile
    *           List file
    */
   public JavaFileSelection(final List<File> listFile)
   {
      this.listFile = listFile;
   }

   /**
    * Return the data associated to a flavor type
    * 
    * @param flavor
    *           Flavor type
    * @return List of files
    * @throws UnsupportedFlavorException
    *            If flavor is not {@link DataFlavor#javaFileListFlavor}
    * @throws IOException
    *            On IO issue
    * @see Transferable#getTransferData(DataFlavor)
    */
   @Override
   public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException
   {
      if(flavor.isFlavorJavaFileListType() == true)
      {
         return this.listFile;
      }

      throw new UnsupportedFlavorException(flavor);
   }

   /**
    * Supported flavor list
    * 
    * @return Supported flavor list
    * @see Transferable#getTransferDataFlavors()
    */
   @Override
   public DataFlavor[] getTransferDataFlavors()
   {
      return JavaFileSelection.FLAVORS;
   }

   /**
    * Indicates if flavor is supported
    * 
    * @param flavor
    *           Tested flavor
    * @return {@code true} if flavor supported
    * @see Transferable#isDataFlavorSupported(DataFlavor)
    */
   @Override
   public boolean isDataFlavorSupported(final DataFlavor flavor)
   {
      return flavor.isFlavorJavaFileListType();
   }
}