/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.util<br>
 * Class : FileComparator<br>
 * Date : 27 sept. 2008<br>
 * By JHelp
 */
package jhelp.util.io;

import java.io.File;
import java.util.Comparator;

/**
 * Comparator for file <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class FileComparator
      implements Comparator<File>
{
   /** Singleton comparator */
   public static final FileComparator FILE_COMPARATOR = new FileComparator();

   /**
    * Constructs FileComparator
    */
   private FileComparator()
   {
   }

   /**
    * Compare two files
    * 
    * @param file1
    *           File 1
    * @param file2
    *           File 2
    * @return <0 if first before second, ==0 if equals, >0 if first after second
    * @see Comparator#compare(Object, Object)
    */
   @Override
   public int compare(final File file1, final File file2)
   {
      return file1.getName().compareToIgnoreCase(file2.getName());
   }
}