/*
 * Symbols.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars;


import com.gs.collections.impl.map.mutable.primitive.CharObjectHashMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


abstract public class Symbols {

    /* sentence type and delimitors
       TODO use an enum
    */
    public static final char JUDGMENT = '.';
    public static final char QUESTION = '?';
    public static final char GOAL = '!';
    public static final char QUEST = '@';
    public static final char COMMAND = ';';

    
    
    
    /* Tense markers */
    public static final String TENSE_SEPARATOR = ":";

    public static final String TENSE_PAST = ":\\:";
    public static final String TENSE_PRESENT = ":|:";
    public static final String TENSE_FUTURE = ":/:";
    public static final String TENSE_ETERNAL = ":-:"; //ascii infinity symbol

    public static final String TASK_RULE_FWD = "|-";
    
    /* variable type  ------------------ */
   
    public static final char VAR_INDEPENDENT = '$';

    //#sth talks about a thinkg without naming it
    public static final char VAR_DEPENDENT = '#';

    //?any asks for a concrete thing
    public static final char VAR_QUERY = '?';


    /** used in MetaNAL structure matching */
    public static final char VAR_PATTERN = '%';

    /*
        $it $eth $1 $sth,   #sth #1    ?what      
        $it #sth ?what
    */
    
    
    
    

    
    
    /* numerical value delimitors, must be different from the Term delimitors */
    public static final char BUDGET_VALUE_MARK = '$';
    public static final char TRUTH_VALUE_MARK = '%';
    public static final char VALUE_SEPARATOR = ';';

    /* special characters in argument list */
    public static final char ARGUMENT_SEPARATOR = ',';
    public static final byte ARGUMENT_SEPARATORbyte = (byte)',';
    public static final char IMAGE_PLACE_HOLDER = '_';
    
    /* prefix of special Term name */
    public static final char INTERVAL_PREFIX = '/'; //TODO switch to: ????
    public static final char TERM_PREFIX = 'T';
    public static final char QUOTE = '\"';




    /** index of operators which are encoded by 1 byte: must be less than 31 because this is the range for control characters */
    final static int numByteSymbols = 15;
    private static final Op[] byteSymbols = new Op[numByteSymbols];


    public final static char SET_INT_CLOSER = ']';
    public final static char SET_EXT_CLOSER = '}';
    public final static char COMPOUND_TERM_OPENER = '(';
    public final static char COMPOUND_TERM_CLOSER = ')';
    public final static byte COMPOUND_TERM_CLOSERbyte = (byte)COMPOUND_TERM_CLOSER;
    public final static char STATEMENT_OPENER = '<';
    public final static char STATEMENT_CLOSER = '>';


//    public static Op symbol(final byte b) {
//        if (b > byteSymbols.length)
//            throw new RuntimeException("value of " + b + " exceeds special character range");
//        return byteSymbols[b];
//    }

//
//
//    public static void compact(final ByteBuf b, final Op n) {
//        if (n.has8BitRepresentation()) {
//            b.append(n.byt);
//        }
//        else {
//            b.append(n.str); //ordinary character
//        }
//    }

    private static final Map<String,Op> _stringToOperator
            = new HashMap(Op.values().length * 2);

    private static final CharObjectHashMap<Op> _charToOperator
            = new CharObjectHashMap(Op.values().length * 2);


    static {
        //Setup NativeOperator String index hashtable 
        for (final Op r : Op.values()) {
            _stringToOperator.put(r.toString(), r);


            if (r.has8BitRepresentation()) {
                //store the 8bit representation in the table
                byteSymbols[r.byt] = r;
            }
        }

        //System.out.println(Arrays.toString(byteSymbols));

        //VERIFICATION: Look for any empty holes in the byteSymbols table, indicating that the representation is not contigous
        //index 0 is always 0 to maintain \0's semantics
        //if # of operators are reduced in the future, then this will report that the table size should be reduced (avoiding unnecessary array lookups)
        for (int i = 1; i < byteSymbols.length; i++) {
            if (null == byteSymbols[i])
                throw new RuntimeException("Invalid byteSymbols encoding: index " + i + " is null");
        }

        //Setup NativeOperator Character index hashtable 
        for (final Op r : Op.values()) {
            char c = r.ch;
            if (c!=0)
                _charToOperator.put(c, r);
        }
    }

    protected static final Map<String,Op> stringToOperator
            = Collections.unmodifiableMap(_stringToOperator);
    protected static final CharObjectHashMap<Op> charToOperator
            = (_charToOperator);


    /** separates prefix from the term in a termlink or tasklink */
    final public static char TLinkSeparator = ':';


    public static Op getOperator(final char c) {
        return charToOperator.get(c);
    }
    
    public static Op getOperator(final String s) {
        return stringToOperator.get(s);
    }


//    public static Op getOpener(final char c) {
//        Op o = getOperator(c);
//        if (o == null) return null;
//        if (o.opener)
//            return o;
//        return null;
//    }
//
//    public static Op getCloser(final char c) {
//        Op o = getOperator(c);
//        if (o == null) return null;
//        if (o.closer)
//            return o;
//        return null;
//    }

    
    /* Stamp, display only */
    public static final char STAMP_OPENER = '{';
    public static final char STAMP_CLOSER = '}';
    public static final char STAMP_SEPARATOR = ';';
    public static final char STAMP_STARTER = ':';


    public static boolean isPunctuation(final char c) {
        switch (c) {
            case Symbols.JUDGMENT:
            case Symbols.GOAL:
            case Symbols.QUEST:
            case Symbols.QUESTION:
                return true;
        }
        return false;
    }


//    public static String getPunctuationWord(char punctuation) {
//        switch (punctuation) {
//            case '.': return "BELIEF";
//            case '!': return "GOAL";
//            case '@': return "QUEST";
//            case '?': return "QUESTION";
//        }
//        return "UNKNOWN";
//    }

    /** expands a byte to multi-char representation, for output.
     //     * if a special character, it prints the expanded string and returns true.
     //     * otherwise it does nothing and returns false.;
     //     *  */
//    public static boolean expand(final PrintWriter p, final byte b) {
//        if (b < numByteSymbols) {
//            p.write(symbol(b).str);
//            return true;
//        }
//        else {
//            //ordinary character,
//            return false;
//        }
//    }


    /*
    @Deprecated public static NativeOperator opInnate(final String op) {
        NativeOperator i = getOperator(op);
        if (i == null) return null;

        final int length = op.length();
        if (length == 1) {
            final char c = op.charAt(0);
            switch (c) {
                case Symbols.SET_EXT_OPENER:
                case Symbols.SET_INT_OPENER:
                case Symbols.INTERSECTION_EXT_OPERATORc:
                case Symbols.INTERSECTION_INT_OPERATORc:
                case Symbols.DIFFERENCE_EXT_OPERATORc:
                case Symbols.DIFFERENCE_INT_OPERATORc:
                case Symbols.PRODUCT_OPERATORc:
                case Symbols.IMAGE_EXT_OPERATORc:
                case Symbols.IMAGE_INT_OPERATORc:
                    return true;
            }
        }
        else if (length == 2) {
            //since these symbols are the same character repeated, we only need to compare the first character
            final char c1 = op.charAt(0);
            final char c2 = op.charAt(1);
            if (c1 == c2) {
                switch (c1) {
                    case Symbols.NEGATION_OPERATORc:
                    case Symbols.DISJUNCTION_OPERATORc:
                    case Symbols.CONJUNCTION_OPERATORc:
                        return true;
                }
            } else if ((op.equals(Symbols.SEQUENCE_OPERATOR)) || (op.equals(Symbols.PARALLEL_OPERATOR))) {
                return true;
            }

        }

        return false;
    }
    */

}
