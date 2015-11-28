package jhelp.engine.util;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.io.UtilIO;

import java.io.File;
import java.io.IOException;

/**
 * Install libraries need for communicate with Open GL
 * 
 * @author JHelp
 */
public class LibraryInstaller
{
   /** Indicates that the libraries are all ready installed */
   private static boolean alreadyInstall = true;

   /**
    * Install the libraries
    */
   public static void install()
   {
      if(LibraryInstaller.alreadyInstall == true)
      {
         return;
      }

      // Get the jar name where extract the good libraries for the local OS
      final String jarFileName = NativeLibInfo.getJarFileName();
      Debug.println(DebugLevel.VERBOSE, "jarFileName=", jarFileName);
      if(jarFileName == null)
      {
         return;
      }

      // Open the jar file
      final File file = UtilIO.obtainExternalFile("jar/" + jarFileName);
      Debug.println(DebugLevel.VERBOSE, "file=", file.getAbsolutePath(), " | ", file.exists());
      if(file.exists() == false)
      {
         return;
      }

      Debug.printMark(DebugLevel.VERBOSE, "COPY START");

      try
      {
         UtilIO.unzip(UtilIO.obtainExternalFile("jar"), file);
      }
      catch(final IOException exception)
      {
         Debug.printException(exception, "Issue while extracting the jar : ", file.getAbsolutePath(), " into : ", UtilIO.obtainExternalFile("jar").getAbsolutePath());
      }

      Debug.printMark(DebugLevel.VERBOSE, "COPY END");

      LibraryInstaller.alreadyInstall = true;
   }
}