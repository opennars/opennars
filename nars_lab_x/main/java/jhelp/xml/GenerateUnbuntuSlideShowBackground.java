/**
 * You can use the following code as you want.<br>
 * We are not responsible any bad effect that the code can produce.
 */
package jhelp.xml;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.filter.FileFilter;
import jhelp.util.io.UtilIO;
import jhelp.util.list.Scramble;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Stack;

/**
 * Generate Background slide show description for Ubuntu.<br>
 * Usage : GenerateUnbuntuSlideShowBackground &lt;directory&gt; &lt;file&gt; &lt;staticTime&gt; &lt;transitionTime&gt;.<br>
 * Where
 * <table>
 * <tr>
 * <th>&lt;directory&gt;</th>
 * <td>Directory where images lies</td>
 * </tr>
 * <tr>
 * <th>&lt;file&gt;</th>
 * <td>File to generate</td>
 * </tr>
 * <tr>
 * <th>&lt;staticTime&gt;</th>
 * <td>Time duration of each image in seconds</td>
 * </tr>
 * <tr>
 * <th>&lt;transitionTime&gt;</th>
 * <td>Time of transition between each image in seconds</td>
 * </tr>
 * </table>
 * <br>
 * 
 * @author jhelp
 */
public class GenerateUnbuntuSlideShowBackground
{

   /**
    * Print the usage : <br>
    * Usage : GenerateUnbuntuSlideShowBackground &lt;directory&gt; &lt;file&gt; &lt;staticTime&gt; &lt;transitionTime&gt;<br>
    * Where
    * <table>
    * <tr>
    * <th>&lt;directory&gt;</th>
    * <td>Directory where images lies</td>
    * </tr>
    * <tr>
    * <th>&lt;file&gt;</th>
    * <td>File to generate</td>
    * </tr>
    * <tr>
    * <th>&lt;staticTime&gt;</th>
    * <td>Time duration of each image in seconds</td>
    * </tr>
    * <tr>
    * <th>&lt;transitionTime&gt;</th>
    * <td>Time of transition between each image in seconds</td>
    * </tr>
    * </table>
    */
   private static void printUsage()
   {
      Debug.println(DebugLevel.INFORMATION, "Usage :");
      Debug.println(DebugLevel.INFORMATION, "GenerateUnbuntuSlideShowBackground <directory> <file> <staticTime> <transitionTime>");
      Debug.println(DebugLevel.INFORMATION);
      Debug.println(DebugLevel.INFORMATION, "\t <directory> \t\t:\t Directory where images lies");
      Debug.println(DebugLevel.INFORMATION, "\t <file> \t\t:\t File to generate");
      Debug.println(DebugLevel.INFORMATION, "\t <staticTime> \t\t:\t Time duration of each image in seconds");
      Debug.println(DebugLevel.INFORMATION, "\t <transitionTime> \t:\t Time of transition between each image in seconds");
   }

   /**
    * Launch the generation.<br>
    * Usage : GenerateUnbuntuSlideShowBackground &lt;directory&gt; &lt;file&gt; &lt;staticTime&gt; &lt;transitionTime&gt;.<br>
    * Where
    * <table>
    * <tr>
    * <th>&lt;directory&gt;</th>
    * <td>Directory where images lies</td>
    * </tr>
    * <tr>
    * <th>&lt;file&gt;</th>
    * <td>File to generate</td>
    * </tr>
    * <tr>
    * <th>&lt;staticTime&gt;</th>
    * <td>Time duration of each image in seconds</td>
    * </tr>
    * <tr>
    * <th>&lt;transitionTime&gt;</th>
    * <td>Time of transition between each image in seconds</td>
    * </tr>
    * </table>
    * 
    * @param args
    *           Arguments give by shell command
    */
   public static void main(final String[] args)
   {
      if(args == null || args.length != 4)
      {
         GenerateUnbuntuSlideShowBackground.printUsage();

         System.exit(0);
      }

      BufferedWriter bufferedWriter = null;
      final FileFilter fileFilter = FileFilter.createFilterForImage();

      try
      {
         File directory = new File(args[0]);

         if(directory.exists() == false || directory.isDirectory() == false || directory.canRead() == false)
         {
            throw new IllegalArgumentException("The directory '" + directory.getAbsolutePath() + "' dosen't exits or not a directory or can't be access in read");
         }

         final File file = new File(args[1]);

         if(UtilIO.createFile(file) == false)
         {
            throw new IllegalArgumentException("The file '" + file.getAbsolutePath() + "' can't be created");
         }

         final float staticTime = Float.parseFloat(args[2]);
         final float transitionTime = Float.parseFloat(args[3]);

         //

         bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

         bufferedWriter.write("<background>");
         bufferedWriter.newLine();

         bufferedWriter.write("   <starttime>");
         bufferedWriter.newLine();
         {
            final GregorianCalendar gregorianCalendar = new GregorianCalendar();

            bufferedWriter.write("      <year>");
            bufferedWriter.write(String.valueOf(gregorianCalendar.get(Calendar.YEAR)));
            bufferedWriter.write("</year>");
            bufferedWriter.newLine();

            bufferedWriter.write("      <month>");
            bufferedWriter.write(String.valueOf(gregorianCalendar.get(Calendar.MONTH) + 1));
            bufferedWriter.write("</month>");
            bufferedWriter.newLine();

            bufferedWriter.write("      <day>");
            bufferedWriter.write(String.valueOf(gregorianCalendar.get(Calendar.DAY_OF_MONTH)));
            bufferedWriter.write("</day>");
            bufferedWriter.newLine();

            bufferedWriter.write("      <hour>");
            bufferedWriter.write(String.valueOf(gregorianCalendar.get(Calendar.HOUR_OF_DAY)));
            bufferedWriter.write("</hour>");
            bufferedWriter.newLine();

            bufferedWriter.write("      <minute>");
            bufferedWriter.write(String.valueOf(gregorianCalendar.get(Calendar.MINUTE)));
            bufferedWriter.write("</minute>");
            bufferedWriter.newLine();

            bufferedWriter.write("      <second>");
            bufferedWriter.write(String.valueOf(gregorianCalendar.get(Calendar.SECOND)));
            bufferedWriter.write("</second>");
            bufferedWriter.newLine();
         }
         bufferedWriter.write("   </starttime>");
         bufferedWriter.newLine();
         //

         final Stack<File> stack = new Stack<File>();
         stack.push(directory);
         String actual;
         String first = null;
         String previous = null;
         File[] children;

         while(stack.isEmpty() == false)
         {
            directory = stack.pop();

            children = directory.listFiles((java.io.FileFilter) fileFilter);
            if(children != null)
            {
               Scramble.scramble(children);

               for(final File child : children)
               {
                  if(child.isDirectory() == true)
                  {
                     stack.push(child);
                  }
                  else
                  {
                     actual = child.getAbsolutePath();

                     if(previous != null)
                     {
                        bufferedWriter.write("   <transition>");
                        bufferedWriter.newLine();
                        {
                           bufferedWriter.write("      <duration>");
                           bufferedWriter.write(String.valueOf(transitionTime));
                           bufferedWriter.write("</duration>");
                           bufferedWriter.newLine();

                           bufferedWriter.write("      <from>");
                           bufferedWriter.write(previous);
                           bufferedWriter.write("</from>");
                           bufferedWriter.newLine();

                           bufferedWriter.write("      <to>");
                           bufferedWriter.write(actual);
                           bufferedWriter.write("</to>");
                           bufferedWriter.newLine();
                        }
                        bufferedWriter.write("   </transition>");
                        bufferedWriter.newLine();
                     }

                     previous = actual;
                     if(first == null)
                     {
                        first = actual;
                     }

                     bufferedWriter.write("   <static>");
                     bufferedWriter.newLine();
                     {
                        bufferedWriter.write("      <duration>");
                        bufferedWriter.write(String.valueOf(staticTime));
                        bufferedWriter.write("</duration>");
                        bufferedWriter.newLine();

                        bufferedWriter.write("      <file>");
                        bufferedWriter.write(actual);
                        bufferedWriter.write("</file>");
                        bufferedWriter.newLine();
                     }
                     bufferedWriter.write("   </static>");
                     bufferedWriter.newLine();
                  }
               }
            }
         }

         if(previous != null && first != null)
         {
            bufferedWriter.write("   <transition>");
            bufferedWriter.newLine();
            {
               bufferedWriter.write("      <duration>");
               bufferedWriter.write(String.valueOf(transitionTime));
               bufferedWriter.write("</duration>");
               bufferedWriter.newLine();

               bufferedWriter.write("      <from>");
               bufferedWriter.write(previous);
               bufferedWriter.write("</from>");
               bufferedWriter.newLine();

               bufferedWriter.write("      <to>");
               bufferedWriter.write(first);
               bufferedWriter.write("</to>");
               bufferedWriter.newLine();
            }
            bufferedWriter.write("   </transition>");
            bufferedWriter.newLine();
         }

         bufferedWriter.write("</background>");
         bufferedWriter.newLine();
      }
      catch(final Exception exception)
      {
         Debug.printException(exception);

         GenerateUnbuntuSlideShowBackground.printUsage();
      }
      finally
      {
         if(bufferedWriter != null)
         {
            try
            {
               bufferedWriter.flush();
            }
            catch(final Exception exception)
            {
               // Nothing to do
            }

            try
            {
               bufferedWriter.close();
            }
            catch(final Exception exception)
            {
               // Nothing to do
            }
         }
      }
   }
}