package jhelp.util.debug;

/**
 * Debug level
 * 
 * @author JHelp
 */
public enum DebugLevel
{
   /** Debug level */
   DEBUG(3, "DEBUG : "),
   /** Error level */
   ERROR(0, "$@! ERROR !@$ : "),
   /** Information level */
   INFORMATION(2, "INFORMATION : "),
   /** Verbose level */
   VERBOSE(4, "VERBOSE : "),
   /** Warning level */
   WARNING(1, "/!\\ WARNING /!\\ : ");

   /** Header */
   private String header;
   /** Level */
   private int    level;

   /**
    * Create a new instance of DebugLevel
    * 
    * @param level
    *           Level
    * @param header
    *           Header
    */
   DebugLevel(final int level, final String header)
   {
      this.level = level;
      this.header = header;
   }

   /**
    * Header
    * 
    * @return Header
    */
   public String getHeader()
   {
      return this.header;
   }

   /**
    * Level
    * 
    * @return Level
    */
   public int getLevel()
   {
      return this.level;
   }
}