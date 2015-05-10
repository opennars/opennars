package org.projog.core.parser;

import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.isWhitespace;
import static org.projog.core.parser.Delimiters.isDelimiter;
import static org.projog.core.parser.Delimiters.isListOpenBracket;
import static org.projog.core.parser.TokenType.ANONYMOUS_VARIABLE;
import static org.projog.core.parser.TokenType.ATOM;
import static org.projog.core.parser.TokenType.FLOAT;
import static org.projog.core.parser.TokenType.INTEGER;
import static org.projog.core.parser.TokenType.QUOTED_ATOM;
import static org.projog.core.parser.TokenType.SYMBOL;
import static org.projog.core.parser.TokenType.VARIABLE;

import java.io.BufferedReader;
import java.io.Reader;

import org.projog.core.Operands;

/**
 * Parses an input stream into discrete 'tokens' that are used to represent Prolog queries and rules.
 * 
 * @see SentenceParser
 */
class TokenParser {
   private final CharacterParser parser;
   private final Operands operands;
   private Token lastParsedToken;
   private boolean rewound;

   TokenParser(Reader reader, Operands operands) {
      BufferedReader br = new BufferedReader(reader);
      this.parser = new CharacterParser(br);
      this.operands = operands;
   }

   /** @return {@code true} if there are more tokens to be parsed, else {@code false} */
   boolean hasNext() {
      if (rewound) {
         return true;
      } else {
         skipWhitespaceAndComments();
         return !isEndOfStream(parser.peek());
      }
   }

   /**
    * Parse and return the next {@code Token}.
    * 
    * @return the token that was parsed as a result of this call
    * @throws ParserException if there are no more tokens to parse (i.e. parser has reached the end of the underlying
    * input stream)
    */
   Token next() {
      if (rewound) {
         rewound = false;
      } else {
         lastParsedToken = parseToken();
      }
      return lastParsedToken;
   }

   private Token parseToken() {
      skipWhitespaceAndComments();
      final int c = parser.getNext();
      if (isEndOfStream(c)) {
         throw newParserException("Unexpected end of stream");
      } else if (isUpperCase(c)) {
         return parseText(c, VARIABLE);
      } else if (isAnonymousVariable(c)) {
         return parseText(c, ANONYMOUS_VARIABLE);
      } else if (isLowerCase(c)) {
         return parseText(c, ATOM);
      } else if (isQuote(c)) {
         return parseQuotedText();
      } else if (isDigit(c)) {
         return parseNumber(c);
      } else {
         return parseSymbol(c);
      }
   }

   /**
    * Rewinds the parser (i.e. "pushes-back" the last parsed token).
    * <p>
    * The last parsed value will remain after the next call to {@link #next()}
    * 
    * @param value the value to rewind
    * @throws IllegalArgumentException if already in a rewound state (i.e. have already called
    * {@link TokenParser#rewind(String)} since the last call to {@link #next()}), or {@code value} is not equal to
    * {@link #getValue()}
    */
   void rewind(Token value) {
      if (lastParsedToken != value) {
         throw new IllegalArgumentException();
      }
      rewound = true;
   }

   /** Does the next value to be parsed represent a term (rather than a delimiter) */
   boolean isFollowedByTerm() {
      skipWhitespaceAndComments();
      int nextChar = parser.peek();
      return isListOpenBracket(nextChar) || !isDelimiter(nextChar);
   }

   /** Returns a new {@link ParserException} with the specified message. */
   ParserException newParserException(String message) {
      throw new ParserException(message, parser);
   }

   private void skipWhitespaceAndComments() {
      while (true) {
         final int c = parser.getNext();
         if (isEndOfStream(c)) {
            return;
         } else if (isWhitespace(c)) {
            skipWhitespace();
         } else if (isSingleLineComment(c)) {
            parser.skipLine(); // skip comment
         } else if (isMultiLineCommentStart(c, parser.peek())) {
            skipMultiLineComment();
         } else {
            parser.rewind();
            return;
         }
      }
   }

   /** @param c the first, already parsed, character of the token. */
   private Token parseText(int c, TokenType t) {
      StringBuilder sb = new StringBuilder();

      do {
         sb.append((char) c);
         c = parser.getNext();
      } while (isValidForAtom(c));
      parser.rewind();

      return createToken(sb, t);
   }

   /**
    * Reads a {@code String} consisting of all characters read from the parser up to the next {@code '}.
    * <p>
    * If an atom's name is enclosed in quotes (i.e. {@code '}) then it may contain any character.
    * </p>
    */
   private Token parseQuotedText() {
      StringBuilder sb = new StringBuilder();
      do {
         int c = parser.getNext();
         if (isQuote(c)) {
            c = parser.getNext();
            // If we reach a ' that is not immediately followed by another '
            // we assume we have reached the end of the string.
            // If we find a ' that is immediately followed by another ' (i.e. '') 
            // we treat it as a single ' - this is so the ' character can be included in strings. 
            // e.g. 'abc''def' will be treated as  a single string with the value abc'def
            if (!isQuote(c)) {
               // found closing '
               parser.rewind();
               return createToken(sb, QUOTED_ATOM);
            }
         } else if (isEndOfStream(c)) {
            throw newParserException("No closing ' on quoted string");
         }
         sb.append((char) c);
      } while (true);
   }

   /**
    * Parses a number, starting with the specified character, read from the parser.
    * <p>
    * Deals with numbers of the form {@code 3.4028235E38}.
    */
   private Token parseNumber(final int startChar) {
      StringBuilder sb = new StringBuilder();

      boolean keepGoing = true;
      boolean readDecimalPoint = false;
      boolean readExponent = false;
      boolean wasLastCharExponent = false;
      int c = startChar;
      do {
         sb.append((char) c);
         c = parser.getNext();
         if (c == '.') {
            if (readDecimalPoint) {
               // can't have more than one decimal point per number
               keepGoing = false;
            } else if (isDigit(parser.peek())) {
               readDecimalPoint = true;
            } else {
               // must be a digit after . for it to be a decimal number
               keepGoing = false;
            }
         } else if (c == 'e' || c == 'E') {
            if (readExponent) {
               throw newParserException("unexpected: " + (char) c);
            }
            readExponent = true;
            wasLastCharExponent = true;
         } else if (!isDigit(c)) {
            keepGoing = false;
         } else {
            wasLastCharExponent = false;
         }
      } while (keepGoing);
      parser.rewind();
      if (wasLastCharExponent) {
         throw newParserException("expected digit after e");
      }

      return createToken(sb, readDecimalPoint ? FLOAT : INTEGER);
   }

   private Token parseSymbol(int c) {
      StringBuilder sb = new StringBuilder();
      do {
         sb.append((char) c);
         c = parser.getNext();
      } while (!isAlphabetic(c) && !isDigit(c) && !isWhitespace(c) && !isEndOfStream(c));
      parser.rewind();

      if (isValidParseableElement(sb.toString())) {
         return createToken(sb, SYMBOL);
      }

      int length = sb.length();
      int idx = length;
      while (--idx > 0) {
         final String substring = sb.substring(0, idx);
         if (isValidParseableElement(substring)) {
            parser.rewind(length - idx);
            return createToken(substring, SYMBOL);
         }
      }

      for (int i = 1; i < length; i++) {
         final String substring = sb.substring(i);
         if (isValidParseableElement(substring) || isDelimiter(sb.charAt(i))) {
            parser.rewind(length - i);
            return createToken(sb.substring(0, i), SYMBOL);
         }
      }

      return createToken(sb, SYMBOL);
   }

   private void skipWhitespace() {
      while (isWhitespace(parser.peek())) {
         parser.getNext();
      }
   }

   private void skipMultiLineComment() {
      parser.getNext(); // skip * after /
      int previous = parser.getNext();
      while (true) {
         int current = parser.getNext();
         if (isEndOfStream(current)) {
            throw newParserException("Missing */ to close multi-line comment");
         } else if (isMultiLineCommentEnd(previous, current)) {
            return;
         } else {
            previous = current;
         }
      }
   }

   private boolean isEndOfStream(int c) {
      return c == -1;
   }

   private boolean isSingleLineComment(int c) {
      return c == '%';
   }

   private boolean isMultiLineCommentStart(int c1, int c2) {
      return c1 == '/' && c2 == '*';
   }

   private boolean isMultiLineCommentEnd(int c1, int c2) {
      return c1 == '*' && c2 == '/';
   }

   private boolean isValidForAtom(int c) {
      return isAlphabetic(c) || isDigit(c) || isAnonymousVariable(c);
   }

   private boolean isAnonymousVariable(int c) {
      return c == '_';
   }

   private boolean isQuote(int c) {
      return c == '\'';
   }

   private boolean isValidParseableElement(String commandName) {
      return isDelimiter(commandName) || operands.isDefined(commandName);
   }

   private Token createToken(StringBuilder value, TokenType type) {
      return createToken(value.toString(), type);
   }

   private Token createToken(String value, TokenType type) {
      return new Token(value, type);
   }
}
