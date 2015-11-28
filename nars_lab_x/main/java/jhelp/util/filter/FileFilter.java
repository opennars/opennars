/**
 * Project : JHelpGUI<br>
 * Package : jhelp.gui.filter<br>
 * Class : FileFilter<br>
 * Date : 23 avr. 2009<br>
 * By JHelp
 */
package jhelp.util.filter;

import jhelp.util.debug.Debug;
import jhelp.util.io.FileImageInformation;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * File filter on extention.<br>
 * You can add a second filter to filter more<br>
 * <br>
 * Last modification : 23 avr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class FileFilter
      extends javax.swing.filechooser.FileFilter
      implements FilenameFilter, java.io.FileFilter
{
   /**
    * Create filter initialize to filter images
    * 
    * @return Created filter
    */
   public static FileFilter createFilterForImage()
   {
      return FileFilter.createFilterForImage(false, false);
   }

   /**
    * Create a filter for image
    * 
    * @param acceptHidden
    *           Indicates if hidden files are accepted
    * @param acceptVirtualLink
    *           Indicates if virtual links are accepted
    * @return Created filter
    */
   public static FileFilter createFilterForImage(final boolean acceptHidden, final boolean acceptVirtualLink)
   {
      final FileFilter fileFilter = new FileFilter(acceptHidden, acceptVirtualLink);

      fileFilter.addExtension("png");
      fileFilter.addExtension("gif");
      fileFilter.addExtension("jpg");
      fileFilter.addExtension("bmp");

      fileFilter.setInformation("Images");

      return fileFilter;
   }

   /**
    * Create a file filter for image based on image information not on file extention.<br>
    * Same as {@link #createFilterForImageByFileImageInformation(boolean, boolean)
    * createFilterForImageByFileImageInformation(false, false)}
    * 
    * @return Created file filter
    */
   public static FileFilter createFilterForImageByFileImageInformation()
   {
      return FileFilter.createFilterForImageByFileImageInformation(false, false);
   }

   /**
    * Create a file filter for image based on image information not on file extention.
    * 
    * @param acceptHidden
    *           Indicates if hidden files are accepted
    * @param acceptVirtualLink
    *           In,dicates if virtual link are accepted
    * @return Created file filter
    */
   public static FileFilter createFilterForImageByFileImageInformation(final boolean acceptHidden, final boolean acceptVirtualLink)
   {
      final FileFilter fileFilter = new FileFilter(acceptHidden, acceptVirtualLink);

      fileFilter.setSecondFileFilter(FileImageInformation.FILTER_BY_FILE_INFORMATION);
      fileFilter.setInformation("Images");

      return fileFilter;
   }

   /** Indicates if directories are accepted */
   private boolean                 acceptDirectory;
   /** Indicates if hidden files are accepted */
   private final boolean           acceptHidden;
   /** Indicates if virtual links are accepted */
   private final boolean           acceptVirtualLink;
   /** Filtered extentions */
   private final ArrayList<String> extentions;
   /** Filter informations */
   private String                  information;
   /** Second filter */
   private java.io.FileFilter      secondFileFilter;

   /**
    * Constructs FileFilter
    */
   public FileFilter()
   {
      this(false, false);
   }

   /**
    * Create a new instance of FileFilter
    * 
    * @param acceptHidden
    *           Indicates if hidden files are accepted
    * @param acceptVirtualLink
    *           Indicates if virtual links files are accepted
    */
   public FileFilter(final boolean acceptHidden, final boolean acceptVirtualLink)
   {
      this.extentions = new ArrayList<String>();
      this.information = "All";

      this.acceptHidden = acceptHidden;
      this.acceptVirtualLink = acceptVirtualLink;
      this.acceptDirectory = true;
   }

   /**
    * Indicates if a file pass this filter
    * 
    * @param file
    *           File test
    * @return {@code true} if the file pass
    * @see javax.swing.filechooser.FileFilter#accept(File)
    * @see java.io.FileFilter#accept(File)
    */
   @Override
   public boolean accept(final File file)
   {
      try
      {
         if((file == null) || ((this.acceptHidden == false) && (file.isHidden() == true)) || ((this.acceptVirtualLink == false) && (file.getCanonicalPath().equals(file.getAbsolutePath()) == false)) || (file.exists() == false)
               || (file.canRead() == false))
         {
            return false;
         }
      }
      catch(final Exception exception)
      {
         Debug.printException(exception, "Issue while filter : ", file.getAbsolutePath());

         return false;
      }

      if((this.secondFileFilter != null) && (this.secondFileFilter.accept(file) == false))
      {
         return false;
      }

      if(file.isDirectory() == true)
      {
         return this.acceptDirectory;
      }

      return this.isFiltered(file.getName());
   }

   /**
    * Indicates if a file pass this filter
    * 
    * @param dir
    *           Directory path
    * @param name
    *           File name
    * @return {@code true} if the file pass this filter
    * @see FilenameFilter#accept(File, String)
    */
   @Override
   public boolean accept(final File dir, final String name)
   {
      return this.accept(new File(dir, name));
   }

   /**
    * Add an extention in the filter
    * 
    * @param extention
    *           Extention added
    */
   public void addExtension(String extention)
   {
      if(extention == null)
      {
         throw new NullPointerException("extention musn't be null");
      }

      extention = extention.trim().toLowerCase();

      if(extention.length() == 0)
      {
         throw new NullPointerException("extention musn't be empty");
      }

      this.extentions.add(extention);
   }

   /**
    * Filter string
    * 
    * @return Filter string
    */
   public String filter()
   {
      final int size = this.extentions.size();
      if(size == 0)
      {
         return "*";
      }

      final StringBuffer stringBuffer = new StringBuffer();

      stringBuffer.append("*.");
      stringBuffer.append(this.extentions.get(0));

      for(int i = 1; i < size; i++)
      {
         stringBuffer.append(";*.");
         stringBuffer.append(this.extentions.get(i));
      }

      return stringBuffer.toString();
   }

   /**
    * Filter description
    * 
    * @return Filter description
    * @see javax.swing.filechooser.FileFilter#getDescription()
    */
   @Override
   public String getDescription()
   {
      final StringBuffer stringBuffer = new StringBuffer();
      if(this.information != null)
      {
         stringBuffer.append(this.information);
         stringBuffer.append(" [");
      }

      stringBuffer.append(this.filter());

      if(this.information != null)
      {
         stringBuffer.append("]");
      }

      return stringBuffer.toString();
   }

   /**
    * Get an extention in the filter
    * 
    * @param index
    *           Extention index
    * @return Extention
    */
   public String getExtention(final int index)
   {
      return this.extentions.get(index);
   }

   /**
    * Return information
    * 
    * @return information
    */
   public String getInformation()
   {
      return this.information;
   }

   /**
    * Return secondFileFilter
    * 
    * @return secondFileFilter
    */
   public java.io.FileFilter getSecondFileFilter()
   {
      return this.secondFileFilter;
   }

   /**
    * Indicates if directories are accepted
    * 
    * @return {@code true} if directories are accepted
    */
   public boolean isAcceptDirectory()
   {
      return this.acceptDirectory;
   }

   /**
    * Indicates if an extention is filter
    * 
    * @param extention
    *           Extention test
    * @return {@code true} if the extention is filter
    */
   public boolean isAnExtentionFiltered(String extention)
   {
      if(extention == null)
      {
         throw new NullPointerException("extention musn't be null");
      }

      extention = extention.trim().toLowerCase();

      if(extention.length() == 0)
      {
         throw new NullPointerException("extention musn't be empty");
      }

      return this.extentions.contains(extention);
   }

   /**
    * Indicates if a file is filter
    * 
    * @param fileName
    *           File name
    * @return {@code true} if the file is filter
    */
   public boolean isFiltered(String fileName)
   {
      if(this.extentions.isEmpty() == true)
      {
         return true;
      }

      final int index = fileName.lastIndexOf('.');
      if(index < 0)
      {
         return false;
      }

      fileName = fileName.substring(index + 1).toLowerCase();

      for(final String extention : this.extentions)
      {
         if(extention.equals(fileName) == true)
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Number of extentions
    * 
    * @return Number of extentions
    */
   public int numberOfExtentions()
   {
      return this.extentions.size();
   }

   /**
    * Remove an extention
    * 
    * @param extention
    *           Extension to remove
    */
   public void removeExtention(String extention)
   {
      if(extention == null)
      {
         throw new NullPointerException("extention musn't be null");
      }

      extention = extention.trim().toLowerCase();

      if(extention.length() == 0)
      {
         throw new NullPointerException("extention musn't be empty");
      }

      this.extentions.remove(extention);
   }

   /**
    * Change the accept directories value
    * 
    * @param acceptDirectory
    *           Accept or not directories
    */
   public void setAcceptDirectory(final boolean acceptDirectory)
   {
      this.acceptDirectory = acceptDirectory;
   }

   /**
    * Modify information
    * 
    * @param information
    *           New information value
    */
   public void setInformation(final String information)
   {
      this.information = information;
   }

   /**
    * Modify secondFileFilter
    * 
    * @param secondFileFilter
    *           New secondFileFilter value
    */
   public void setSecondFileFilter(final java.io.FileFilter secondFileFilter)
   {
      this.secondFileFilter = secondFileFilter;
   }
}