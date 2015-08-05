/**
 */
package jhelp.engine.util;

import jhelp.util.debug.Debug;

import java.text.MessageFormat;

/**
 * OpenGL library informations, to know with jar to open and extract libraries.<br>
 * Extract from JOGL API, adapt for my own use <br>
 * <br>
 * Last modification : 23 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
final class NativeLibInfo
{
   /** All native informations */
   private static final NativeLibInfo[] allNativeLibInfo =
                                                         {
         new NativeLibInfo("win", "x86", "windows-i586", "", ".dll"), new NativeLibInfo("win", "amd64", "windows-amd64", "", ".dll"), new NativeLibInfo("win", "x86_64", "windows-amd64", "", ".dll"),
         new NativeLibInfo("mac", "ppc", "macosx-ppc", "lib", ".jnilib"), new NativeLibInfo("mac", "i386", "macosx-universal", "lib", ".jnilib"), new NativeLibInfo("linux", "i386", "linux-i586", "lib", ".so"),
         new NativeLibInfo("linux", "x86", "linux-i586", "lib", ".so"), new NativeLibInfo("linux", "amd64", "linux-amd64", "lib", ".so"), new NativeLibInfo("linux", "x86_64", "linux-amd64", "lib", ".so"),
         new NativeLibInfo("sunos", "sparc", "solaris-sparc", "lib", ".so"), new NativeLibInfo("sunos", "sparcv9", "solaris-sparcv9", "lib", ".so"), new NativeLibInfo("sunos", "x86", "solaris-i586", "lib", ".so"),
         new NativeLibInfo("sunos", "amd64", "solaris-amd64", "lib", ".so"), new NativeLibInfo("sunos", "x86_64", "solaris-amd64", "lib", ".so")
                                                         };

   /** JOGL jar prefix */
   private static final String          PREFIX           = "jogl-natives-";

   /** Jar suffix */
   private static final String          SUFIX            = ".jar";

   /**
    * Get local library information
    * 
    * @return Local library information
    */
   private static NativeLibInfo getNativeLibInfo()
   {
      final String osName = System.getProperty("os.name");
      final String osArch = System.getProperty("os.arch");
      for(final NativeLibInfo nativeLibInfo : NativeLibInfo.allNativeLibInfo)
      {
         if(nativeLibInfo.matchesOSAndArch(osName, osArch) == true)
         {
            return nativeLibInfo;
         }
      }
      return null;
   }

   /**
    * Get the jar name to use depends on the local OS
    * 
    * @return Jar to use
    */
   public final static String getJarFileName()
   {
      // Get local library information
      final NativeLibInfo nativeLibInfo = NativeLibInfo.getNativeLibInfo();
      // If no information, then the OS is not JOGL compatible
      if(nativeLibInfo == null)
      {
         return null;
      }
      // For Mac OS be sure that jawt library is load
      if(nativeLibInfo.isMacOS() == false)
      {
         try
         {
            System.loadLibrary("jawt");
         }
         catch(final Error error)
         {
            Debug.printError(error, "jawt not load !");
         }
      }
      // return the name
      return NativeLibInfo.PREFIX + nativeLibInfo.osNameAndArchPair + NativeLibInfo.SUFIX;
   }

   /** Local native prefix */
   private final String nativePrefix;
   /** Local native suffix */
   private final String nativeSuffix;
   /** OS architecture */
   private final String osArch;
   /** OS name */
   private final String osName;
   /** Pair OS name, architecture */
   private final String osNameAndArchPair;

   /**
    * Constructs NativeLibInfo
    * 
    * @param osName
    *           OS name
    * @param osArch
    *           OS architecture
    * @param osNameAndArchPair
    *           Pair OS name, architecture
    * @param nativePrefix
    *           Native prefix
    * @param nativeSuffix
    *           Native suffix
    */
   private NativeLibInfo(final String osName, final String osArch, final String osNameAndArchPair, final String nativePrefix, final String nativeSuffix)
   {
      this.osName = osName;
      this.osArch = osArch;
      this.osNameAndArchPair = osNameAndArchPair;
      this.nativePrefix = nativePrefix;
      this.nativeSuffix = nativeSuffix;
   }

   /**
    * Format a pattern to correspond this native information
    * 
    * @param nativeJarPattern
    *           Pattern to format
    * @return Formated pattern
    */
   public String formatNativeJarName(final String nativeJarPattern)
   {
      return MessageFormat.format(nativeJarPattern, new Object[]
      {
         this.osNameAndArchPair
      });
   }

   /**
    * Constructs a native library name with this information
    * 
    * @param baseName
    *           Base name
    * @return Constructed name
    */
   public String getNativeLibName(final String baseName)
   {
      return this.nativePrefix + baseName + this.nativeSuffix;
   }

   /**
    * Indicates if the OS is Mac
    * 
    * @return {@code true} if the OS is Mac
    */
   public boolean isMacOS()
   {
      return(this.osName.equals("mac"));
   }

   /**
    * Indicates if a file is native file for this native information
    * 
    * @param fileName
    *           File name
    * @return {@code true} if the file is native file for this native information
    */
   public boolean matchesNativeLib(final String fileName)
   {
      if(fileName.toLowerCase().endsWith(this.nativeSuffix))
      {
         return true;
      }
      return false;
   }

   /**
    * Indicates if the local OS corresponds to the native informations
    * 
    * @param osName
    *           OS name
    * @param osArch
    *           OS architecture
    * @return {@code true} if the local OS corresponds to the native informations
    */
   public boolean matchesOSAndArch(final String osName, final String osArch)
   {
      if(osName.toLowerCase().startsWith(this.osName))
      {
         if((this.osArch == null) || (osArch.toLowerCase().equals(this.osArch)))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Indicates if the OS may need DRI hack
    * 
    * @return {@code true} if the OS may need DRI hack
    */
   public boolean mayNeedDRIHack()
   {
      return(!this.isMacOS() && !this.osName.equals("win"));
   }
}