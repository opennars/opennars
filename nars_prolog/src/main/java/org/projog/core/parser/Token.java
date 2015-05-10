package org.projog.core.parser;

/** @see TokenParser#next() */
class Token {
   final String value;
   final TokenType type;

   Token(String value, TokenType type) {
      this.value = value;
      this.type = type;
   }

   @Override
   public String toString() {
      return value;
   }
}
