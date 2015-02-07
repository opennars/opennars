package nars.jprolog.lang;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.Serializable;
/**
 * The <code>Token</code> class contains methods
 * for character input/output.<br>
 * <font color="red">This document is under construction.</font>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.2.5
 */
public class Token implements Serializable {

    public static boolean isSolo(int c) {
	return (c =='!' || c ==';');
    }

    public static boolean isSymbol(int c) {
	switch (c) {
	case '+':
	case '-':
	case '*':
	case '/':
	case '\\':
	case '^':
	case '<':
	case '>':
	case '=':
	case '`':
	case '~':
	case ':':
	case '.':
	case '?':
	case '@':
	case '#':
	case '$':
	case '&':
	    return true;
	default:
	    return false;
	}
    }

    /*
      public static int read_token(StringBuffer s, PushbackReader in) 

      This method reads one token from the input "in", sets the string, 
      and returns the token type.
      
      Type		String
      -2		"error message"
      -1		"end_of_file"
      '.'		"."		full stop
      ' '		" "		space or comment or unknown chars
      ','		","
      '('		"("
      ')'		")"
      '['		"["
      ']'		"]"
      '{'		"{"
      '}'		"}"
      ','		","
      '|'		"|"
      'I'		"decimal"	positive integer
      'D'		"decimal"	positive double 
      'A'		"atom name"
      'V'		"variable name"
      'S'		"string"
    */
    public static int read_token(StringBuffer s, PushbackReader in) 
	throws IOException {

	int c, c1;
	int rc;

	c = in.read(); // get 1st. char
	if(c == -1) {
	    s.append("end_of_file");
	    return -1;
	}
	if (Character.isDigit((char)c)) {
	    rc = read_number(c, s, in);
	    if (rc == 1) 
		rc = 'I';
	    else if (rc == 2) 
		rc = 'D';
	    return rc;
	}
	if (Character.isLowerCase((char)c)) {
	    rc = read_word(c, s, in);
	    if (rc > 0)
		rc = 'A';
	    return rc;
	}
	if (Character.isUpperCase((char) c) || c == '_') {
	    rc = read_word(c, s, in);
	    if (rc > 0)
		rc = 'V';
	    return rc;
	}
	switch (c) {
	case '(':
	case ')':
	case '[':
	case ']':
	case '{':
	case '}':
	case ',':
	case '|':
	    s.append((char)c);
	    return c;
	case '.':		/* full stop or symbol */
	    c1 = in.read();
	    if (c1 == -1 || c1 <= ' ') {
		s.append('.');
		return '.';
	    }
	    in.unread(c1);
	    break;
	case '%':		/* one line comment */
	    s.append(' ');
	    while ((c1 = in.read()) != '\n') {
		if (c1 == -1)
		    return ' ';
	    }
	    return ' ';
	case '/':		/* start of comment or symbol */
	    if ((c1 = in.read()) == '*') {
		s.append(' ');
		while (true) {
		    while ((c1 = in.read()) != '*') {
			if(c1 == -1) {
			    s.append("unexpected end_of_file");
			    return -2;
			}
		    }
		    if ((c1 = in.read()) == '/')
			return ' ';
		    in.unread(c1);
		}
	    }
	    in.unread(c1);
	    break;
	case '\'':
	    rc = read_quoted(c, s, in);
	    if (rc > 0)
		rc = 'A';
	    return rc;
	case '"':
	    rc = read_quoted(c, s, in);
	    if (rc > 0)
		rc = 'S';
	    return rc;
	default:
	    break;
	}
	if (isSolo(c)) {
	    s.append((char)c);
	    return 'A';
	}
	if (isSymbol(c)) {
	    rc = read_symbol(c, s, in);
	    if (rc > 0)
		rc = 'A';
	    return rc;
	}
	s.append(' ');
	return ' ';
    }

    public static int read_number(int c, StringBuffer s, PushbackReader in) 
	throws IOException {

	int c1, c2, c3;
	in.unread(c);
	for (;;) {
	    c1 = in.read();
	    if (! Character.isDigit((char)c1))
		break;
	    s.append((char) c1);
	}
	if (c1 != '.'){
	    in.unread(c1);
	    return 1;
	}
	c2 = in.read();
	if (! Character.isDigit((char)c2)){
	    in.unread(c2);
	    in.unread(c1);
	    return 1;
	}
	s.append((char)c1);
	in.unread(c2);
	for (;;) {
	    c1 = in.read();
	    if (! Character.isDigit((char) c1))
		break;
	    s.append((char) c1);
	}
	//	in.unread(c1);
	//	return 2;
	if (c1 != 'E' && c1 != 'e'){
	    in.unread(c1);
	    return 2;
	}
	c2 = in.read();
	if (c2 == '-' || c2 == '+') {
	    c3 = in.read();
	    if (! Character.isDigit((char)c3)){
		in.unread(c3);
		in.unread(c2);
		in.unread(c1);
		return 2;
	    }
	    s.append((char)c1);
	    s.append((char)c2);
	    in.unread(c3);
	} else if (Character.isDigit((char)c2)){
	    s.append((char)c1);
	    in.unread(c2);
	} else {
	    in.unread(c2);
	    in.unread(c1);
	    return 2;
	}
	for (;;) {
	    c1 = in.read();
	    if (! Character.isDigit((char) c1))
		break;
	    s.append((char) c1);
	}
	in.unread(c1);
	return 2;
    }

    public static int read_word(int c, StringBuffer s, PushbackReader in)
	throws IOException {
	int c1;
	
	in.unread(c);
	for (;;) {
	    c1 = in.read();
	    if (! Character.isLetterOrDigit((char)c1) && c1 != '_')
		break;
	    s.append((char)c1);
	}
	in.unread(c1);
	return 1;
    }

    public static int read_quoted(int quote, StringBuffer s, PushbackReader in) 
	throws IOException {
	int rc;
	int c1;
	
	for (;;) {
	    c1 = in.read();
	    if (c1 == -1 || c1 == '\n') {
		in.unread(c1);
		return -2;
	    } else if (c1 == quote){
		c1 = in.read();
		if (c1 != quote) {
		    in.unread(c1);
		    return 1;
		}
		c1 = quote;
	    } 
	    else if (c1 == '\\') {
		rc = escapeSequences(c1, s, in);
		if (rc > 0)
		    continue;
		else
		    return -2;
	    }
	    s.append((char)c1);
	}
    }

    public static int escapeSequences(int backslash, StringBuffer s, PushbackReader in)	
	throws IOException {

	int c;
	c = in.read();
	switch (c) {
	case 'b': // backspace
	    s.append((char) 8); break; 
	case 't': // horizontal tab 
	    s.append((char) 9); break;
	case 'n': // newline
	    s.append((char)10); break;
	case 'v': // vertical tab
	    s.append((char)11); break;
	case 'f': // form feed
	    s.append((char)12); break;
	case 'r': // carriage return
	    s.append((char)13); break;
	case 'e': // escape
	    s.append((char)27); break;
	case 'd':  // delete
	    s.append((char)127); break;
	case 'a': // alarm
	    s.append((char)7); break;
	default:
	    s.append((char)c); 
	    return 2;
	}
	return 1;
    }


    public static int read_symbol(int c, StringBuffer s, PushbackReader in) 
	throws IOException {
	int c1;
	s.append((char)c);
	//	in.unread(c); 
	for (;;) {
	    c1 = in.read();
	    if (! isSymbol(c1))
		break;
	    s.append((char)c1);
	}
	in.unread(c1);
	return 1;
    }


    /* Write */
    public static void write_string(String s, PrintWriter out) {
	out.print(s); 
    }

    public static void writeq_string(String s, PrintWriter out) {
	char[] ch;

	ch = s.toCharArray();
	if ((getStringType(s) == 3)){
	    out.print("\'");
	    for (int i=0; i<ch.length; i++) {
		if (ch[i] == '\'')
		    out.print("\\\'");
		else if (ch[i] == '\\')
		    out.print("\\\\");
		else if (ch[i] == 8)  // backspace
		    out.print("\\b");
		else if (ch[i] == 9)  // horizontal tab 
		    out.print("\\t");
		else if (ch[i] == 10) // newline
		    out.print("\\n");
		else if (ch[i] == 11) // vertical tab
		    out.print("\\v");
		else if (ch[i] == 12) // form feed
		    out.print("\\f");
		else if (ch[i] == 13) // carriage return
		    out.print("\\r");
		else if (ch[i] == 27) // escape
		    out.print("\\e");
		else if (ch[i] == 127) // delete
		    out.print("\\d");
		else if (ch[i] == 7) // alarm
		    out.print("\\a");
		else 
		    out.print(ch[i]);
	    }
	    out.print("\'");
	} else {
	    write_string(s, out);
	}
    }

    public static String toQuotedString(String s) {
	StringBuilder quoted = new StringBuilder(s.length() * 2);
	char[] ch;

	ch = s.toCharArray();
	if ((getStringType(s) == 3)){
	    quoted.append('\'');
	    for (int i=0; i<ch.length; i++) {
		if (ch[i] == '\'')
		    quoted.append("\\\'");
		else if (ch[i] == '\\')
		    quoted.append("\\\\");
		else if (ch[i] == 8)  // backspace
		    quoted.append("\\b");
		else if (ch[i] == 9)  // horizontal tab 
		    quoted.append("\\t");
		else if (ch[i] == 10) // newline
		    quoted.append("\\n");
		else if (ch[i] == 11) // vertical tab
		    quoted.append("\\v");
		else if (ch[i] == 12) // form feed
		    quoted.append("\\f");
		else if (ch[i] == 13) // carriage return
		    quoted.append("\\r");
		else if (ch[i] == 27) // escape
		    quoted.append("\\e");
		else if (ch[i] == 127) // delete
		    quoted.append("\\d");
		else if (ch[i] == 7) // alarm
		    quoted.append("\\a");
		else 
		    quoted.append(ch[i]);
	    }
	    quoted.append('\'');
	    return quoted.toString();
	} else {
	    return s;
	}
    }


    /*
     * return value:
     *   0 : if string is a lower case alphnum
     *   1 : if string is a symbol
     *   2 : if string is a solo
     *   3 : others
     */
    public static int getStringType(String s) {
	char[] p;

	if (s.equals("[]") || s.equals("{}")) 
	    return 0;
	if (s.isEmpty() || s.equals("."))
	    return 3;
	if (s.equals("!")  || s.equals(";"))
	    return 2;
	p = s.toCharArray(); // string --> chars[]
	if (Character.isLowerCase(p[0])){
	    for (int i=1; i<p.length; i++){
		if (! Character.isLetterOrDigit(p[i]) && ((int)p[i]) != '_')
		    return 3;
	    }
	    return 0;
	}
	if (isSymbol((int) p[0])){
	    for (int i=1; i<p.length; i++){
		if (! isSymbol((int) p[i]))
		    return 3;
	    }
	    return 1;
	}
	return 3;
    }
}
