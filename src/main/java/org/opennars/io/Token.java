package org.opennars.io;

public class Token {
    public String contentString;
    public int contentOperation = 0; // set to 0 for internalerror
    public int contentNumber = 0;

    public EnumType type = EnumType.INTERNALERROR;
    public int line = 0;
    public int column = 0; // Spalte
    // public String Filename;

    /*
    final public String getRealString() {
        if( type == EnumType.OPERATION ) {
            return to!string(this.contentOperation);
        }
        else if( type == EnumType.IDENTIFIER ) {
            return this.contentString;
        }
        else if( type == EnumType.NUMBER ) {
            // TODO< catch exceptions >
            return to!string(contentNumber);
        }
        else if( type == EnumType.STRING ) {
            return contentString;
        }


        return "";
    }
     */

    public Token copy() {
        Token result = new Token();
        result.contentString = this.contentString;
        result.contentOperation = this.contentOperation;
        result.contentNumber = this.contentNumber;
        result.type = this.type;
        result.line = this.line;
        result.column = this.column;
        return result;
    }

    public enum EnumType {
        NUMBER,
        IDENTIFIER,
        KEYWORD,       // example: if do end then
        OPERATION,     // example: := > < >= <=

        ERROR,         // if Lexer found an error
        INTERNALERROR, // if token wasn't initialized by Lexer
        STRING,        // "..."

        EOF,           // end of file
    }
}
