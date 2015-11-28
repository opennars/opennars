package jhelp.util.io;

import java.io.File;
import java.util.Comparator;

/**
 * Comparator for file
 * 
 * @author JHelp
 */
public class ComparatorFile
      implements Comparator<File>
{
   /** Comparator unique instance */
   public static final ComparatorFile COMPARATOR_FILE = new ComparatorFile();

   /**
    * Create a new instance of ComparatorFile
    */
   private ComparatorFile()
   {
   }

   /**
    * Compare 2 files.<br>
    * Return -1 if first before, 0 if equals, 1 if first after <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param file1
    *           First file
    * @param file2
    *           Second file
    * @return Comparison result
    * @see Comparator#compare(Object, Object)
    */
   @Override
   public int compare(final File file1, final File file2)
   {
      final boolean virtualLink1 = UtilIO.isVirtualLink(file1);
      final boolean directory1 = virtualLink1 == true
            ? false
            : file1.isDirectory();

      final boolean virtualLink2 = UtilIO.isVirtualLink(file2);
      final boolean directory2 = virtualLink2 == true
            ? false
            : file2.isDirectory();

      if(directory1 != directory2)
      {
         if(directory1 == true)
         {
            return -1;
         }

         return 1;
      }

      if(virtualLink1 != virtualLink2)
      {
         if(virtualLink1 == true)
         {
            return -1;
         }

         return 1;
      }

      final String name1 = file1.getName();
      final String name2 = file2.getName();

      final int comp = name1.compareToIgnoreCase(name2);
      if(comp != 0)
      {
         return comp;
      }

      return name1.compareTo(name2);
   }
}