package jhelp.util.preference;

import java.io.File;

/**
 * Preference type
 * 
 * @author JHelp
 */
public enum PreferenceType
{
   /** Type byte[] */
   ARRAY,
   /** Type boolean */
   BOOLEAN,
   /** Type {@link File} */
   FILE,
   /** Type int */
   INTEGER,
   /** Type {@link String} */
   STRING
}