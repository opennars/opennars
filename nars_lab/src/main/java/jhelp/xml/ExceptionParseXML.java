/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : ExceptionParseXML<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

import jhelp.util.text.UtilText;

/**
 * Exception on parsing XML <br>
 * <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class ExceptionParseXML
      extends ExceptionXML
{
   /** serialVersionUID */
   private static final long serialVersionUID = 2539638378262270550L;
   /** Column */
   private final int         column;
   /** Line */
   private final int         line;

   /**
    * Constructs ExceptionParseXML
    * 
    * @param line
    *           Line
    * @param column
    *           Column
    */
   public ExceptionParseXML(final int line, final int column)
   {
      this(null, line, column, null);
   }

   /**
    * Constructs ExceptionParseXML
    * 
    * @param line
    *           Line
    * @param column
    *           Column
    * @param cause
    *           Cause
    */
   public ExceptionParseXML(final int line, final int column, final Throwable cause)
   {
      this(null, line, column, cause);
   }

   /**
    * Constructs ExceptionParseXML
    * 
    * @param message
    *           Message
    * @param line
    *           Line
    * @param column
    *           Column
    */
   public ExceptionParseXML(final String message, final int line, final int column)
   {
      this(message, line, column, null);
   }

   /**
    * Constructs ExceptionParseXML
    * 
    * @param message
    *           Message
    * @param line
    *           Line
    * @param column
    *           Column
    * @param cause
    *           Cause
    */
   public ExceptionParseXML(final String message, final int line, final int column, final Throwable cause)
   {
      super(UtilText.concatenate(message == null
            ? "Parse Exception"
            : message, " ON line=", line, " column=", column), cause);

      this.line = line;
      this.column = column;
   }

   /**
    * Return column
    * 
    * @return column
    */
   public int getColumn()
   {
      return this.column;
   }

   /**
    * Return line
    * 
    * @return line
    */
   public int getLine()
   {
      return this.line;
   }
}