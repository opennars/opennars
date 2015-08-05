package org.projog.core.parser;

import org.projog.core.ProjogException;

import java.io.PrintStream;

/**
 * Signals a failure to successfully parse Prolog syntax.
 */
public class ParserException extends ProjogException {
   private static final long serialVersionUID = 1L;

   private final String message;
   private final String line;
   private final int lineNumber;
   private final int columnNumber;

   ParserException(String message, CharacterParser parser) {
      this(message, parser, null);
   }

   ParserException(String message, CharacterParser parser, Throwable t) {
      super(message + " Line: " + parser.getLine(), t);
      this.message = message;
      this.line = parser.getLine();
      this.lineNumber = parser.getLineNumber();
      this.columnNumber = parser.getColumnNumber();
   }

   /**
    * Returns the contents of the line being parsed when the problem occurred.
    */
   public String getLine() {
      return line;
   }

   /**
    * Returns the line number of the line being parsed when the problem occurred.
    */
   public int getLineNumber() {
      return lineNumber;
   }

   /**
    * Returns the index in the line being parsed of the character being parsed when the problem occurred.
    */
   public int getColumnNumber() {
      return columnNumber;
   }

   /**
    * Prints a description of this exception to the specified print stream.
    * <p>
    * The description contains the particular line being parsed when the exception was thrown.
    * 
    * @param out {@code PrintStream} to use for output
    */
   public void getDescription(PrintStream out) {
      out.println(message);
      out.println(getLine());
      for (int c = 0; c < getColumnNumber() - 1; c++) {
         out.print(' ');
      }
      out.println("^");
   }
}