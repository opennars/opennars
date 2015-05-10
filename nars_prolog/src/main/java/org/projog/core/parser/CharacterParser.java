package org.projog.core.parser;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Reads characters from a {@code BufferedReader}.
 * <p>
 * Provides details of current line and column number being parsed.
 * </p>
 * 
 * @see SentenceParser#getInstance(java.io.Reader, org.projog.core.Operands)
 */
final class CharacterParser {
   private static final int END_OF_STREAM = -1;

   private final BufferedReader br;
   private String currentLine;
   /**
    * The line number of the current line being parsed.
    * <p>
    * Required in order to provide useful information if a {@link ParserException} is thrown.
    */
   private int lineNumber;
   /**
    * The position, within the current line, of the character being parsed.
    * <p>
    * Required in order to provide useful information if a {@link ParserException} is thrown.
    */
   private int columnNumber;

   CharacterParser(BufferedReader br) {
      this.br = br;
   }

   /**
    * Reads a single character.
    * <p>
    * Every call to {@code getNext()} causes the parser to move forward one character - meaning that by making repeated
    * calls to {@code getNext()} all characters in the the underlying stream represented by this object will be
    * returned.
    * 
    * @return The character read, as an integer in the range 0 to 65535 (<tt>0x00-0xffff</tt>), or -1 if the end of the
    * stream has been reached
    * @exception ParserException if an I/O error occurs
    * @see #peek()
    */
   int getNext() {
      try {
         // proceed to next line
         if (currentLine == null || columnNumber > currentLine.length()) {
            String nextLine = br.readLine();
            if (nextLine == null) {
               return END_OF_STREAM;
            }
            currentLine = nextLine;
            lineNumber++;
            columnNumber = 0;
         }

         // if reached end of a line return the new line character
         if (columnNumber == currentLine.length()) {
            columnNumber++;
            return '\n';
         } else {
            return currentLine.charAt(columnNumber++);
         }
      } catch (IOException e) {
         throw new ParserException("Unexpected exception getting next character", this, e);
      }
   }

   /**
    * Reads a single character but does not consume it.
    * 
    * @return The character read, as an integer in the range 0 to 65535 (<tt>0x00-0xffff</tt>), or -1 if the end of the
    * stream has been reached
    * @exception ParserException if an I/O error occurs
    * @see #getNext()
    */
   int peek() {
      int i = getNext();
      if (i != END_OF_STREAM) {
         rewind();
      }
      return i;
   }

   /**
    * Moves the parser back one character.
    * 
    * @throws ParserException if attempting to rewind back past the start of the current line
    */
   void rewind() {
      rewind(1);
   }

   /**
    * Moves the parser back by the specified number of characters.
    * 
    * @throws ParserException if attempting to rewind back past the start of the current line
    */
   void rewind(int numberOfCharacters) {
      if (numberOfCharacters > columnNumber) {
         throw new ParserException("Cannot rewind past start of current line", this);
      }
      columnNumber -= numberOfCharacters;
   }

   /**
    * Skips the remainder of the line currently being parsed.
    */
   void skipLine() {
      columnNumber = currentLine.length() + 1;
   }

   /**
    * Returns the entire contents of the line currently being parsed.
    */
   String getLine() {
      return currentLine;
   }

   /**
    * Returns the line number of the line currently being parsed.
    */
   int getLineNumber() {
      return lineNumber;
   }

   /**
    * Returns the index, in the line currently being parsed, of the character that will be returned by the next call to
    * {@link #getNext()}.
    */
   int getColumnNumber() {
      return columnNumber;
   }
}