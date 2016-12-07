/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Sat Nov 10 10:56:08 CET 2012 */
package automenta.vivisect.swing.property;

import java.util.Date;

/**
 * This class provides information gathered from the build environment.
 * 
 * @author JReleaseInfo AntTask
 */
public class Version {


   /** buildDate (set during build process to 1352541368640L). */
   private static Date buildDate = new Date(1352541368640L);

   /**
    * Get buildDate (set during build process to Sat Nov 10 10:56:08 CET 2012).
    * @return Date buildDate
    */
   public static final Date getBuildDate() { return buildDate; }


   /** year (set during build process to "2005-2009"). */
   private static String year = new String("2005-2009");

   /**
    * Get year (set during build process to "2005-2009").
    * @return String year
    */
   public static final String getYear() { return year; }


   /** project (set during build process to "l2fprod-common"). */
   private static String project = new String("l2fprod-common");

   /**
    * Get project (set during build process to "l2fprod-common").
    * @return String project
    */
   public static final String getProject() { return project; }


   /** buildTimestamp (set during build process to "11/10/2012 10:56 AM"). */
   private static String buildTimestamp = new String("11/10/2012 10:56 AM");

   /**
    * Get buildTimestamp (set during build process to "11/10/2012 10:56 AM").
    * @return String buildTimestamp
    */
   public static final String getBuildTimestamp() { return buildTimestamp; }


   /** version (set during build process to "9.2"). */
   private static String version = new String("9.2");

   /**
    * Get version (set during build process to "9.2").
    * @return String version
    */
   public static final String getVersion() { return version; }

}
