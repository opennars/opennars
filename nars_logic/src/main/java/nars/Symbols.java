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


public enum Symbols {
    ;

    /* sentence type and delimitors
       TODO use an enum
    */
    public static final char JUDGMENT = '.';
    public static final char QUESTION = '?';
    public static final char GOAL = '!';
    public static final char QUEST = '@';
    public static final char COMMAND = ';';


    
    /* Tense markers */


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
    public static final String ARGUMENT_SEPARATOR_PRETTY = ", ";
    public static final char IMAGE_PLACE_HOLDER = '_';
    
    /* prefix of special Term name */
    public static final char INTERVAL_PREFIX = '/'; //TODO switch to: ????
    public static final char TERM_PREFIX = 'T';
    public static final char QUOTE = '\"';





    /** index of operators which are encoded by 1 byte: must be less than 31 because this is the range for control characters */
    static final int numByteSymbols = 15;
    private static final Op[] byteSymbols = new Op[numByteSymbols];


    public static final char SET_INT_CLOSER = ']';
    public static final char SET_EXT_CLOSER = '}';
    public static final char COMPOUND_TERM_OPENER = '(';
    public static final char COMPOUND_TERM_CLOSER = ')';
    public static final char STATEMENT_OPENER = '<';
    public static final char STATEMENT_CLOSER = '>';


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
        for (Op r : Op.values()) {
            _stringToOperator.put(r.toString(), r);
            int ordinal = r.ordinal();
            if (ordinal < 15)
                byteSymbols[ordinal] = r;
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
        for (Op r : Op.values()) {
            char c = r.ch;
            if (c!=0)
                _charToOperator.put(c, r);
        }
    }

    protected static final Map<String,Op> stringToOperator
            = Collections.unmodifiableMap(_stringToOperator);

//    protected static final CharObjectHashMap<Op> charToOperator
//            = (_charToOperator);
//    public static Op getOperator(final char c) {
//        return charToOperator.get(c);
//    }
    
    public static Op getOperator(String s) {
        return stringToOperator.get(s);
    }

    /* Stamp, display only */
    public static final char STAMP_OPENER = '{';
    public static final char STAMP_CLOSER = '}';
    public static final char STAMP_SEPARATOR = ';';
    public static final char STAMP_STARTER = ':';


}
