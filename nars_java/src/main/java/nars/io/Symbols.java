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
package nars.io;


import nars.core.Events;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Symbols {

    /* sentence type and delimitors */
    public static final char JUDGMENT = '.';
    public static final char QUESTION = '?';
    public static final char GOAL = '!';
    public static final char QUEST = '@';

    
    
    
    /* Tense markers */
    public static final String TENSE_MARK = ":";
    public static final String TENSE_PAST = ":\\:";
    public static final String TENSE_PRESENT = ":|:";
    public static final String TENSE_FUTURE = ":/:";
    
    
    
    
    /* variable type  ------------------ */
   
    public static final char VAR_INDEPENDENT = '$';
    public static final String VAR_INDEPENDENT_DEFAULT = VAR_INDEPENDENT + "it";
    
    //#sth talks about a thinkg without naming it
    public static final char VAR_DEPENDENT = '#';
    public static final String VAR_DEPENDENT_DEFAULT = VAR_DEPENDENT + "sth";
    
    //?any asks for a concrete thing
    public static final char VAR_QUERY = '?';
    //public static final String VAR_QUERY_DEFAULT = VAR_QUERY + "any";

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
    public static final char IMAGE_PLACE_HOLDER = '_';
    
    /* prefix of special Term name */
    public static final char INTERVAL_PREFIX = '+';
    public static final char TERM_PREFIX = 'T';
    public static final char QUOTE = '\"';


    public enum NALOperator {
        
        /* CompountTerm operators, length = 1 */
        INTERSECTION_EXT("&", 3, false, true),
        INTERSECTION_INT("|", 3, false, true),
        DIFFERENCE_EXT("-", 3, false, true),
        DIFFERENCE_INT("~", 3, false, true),
        PRODUCT("*", 4, false, true),
        IMAGE_EXT("/", 4, false, true),
        IMAGE_INT("\\", 4, false, true),

        /* CompoundStatement operators, length = 2 */        
        NEGATION("--", 5, false, true),
        DISJUNCTION("||", 5, false, true),
        CONJUNCTION("&&", 5, false, true),
        SEQUENCE("&/", 7, false, true),
        PARALLEL("&|", 7, false, true),
        
        
        /* CompountTerm delimitors, must use 4 different pairs */
        SET_INT_OPENER("[", 3, false, true),
        SET_INT_CLOSER("]", 3, false, false),
        SET_EXT_OPENER("{", 3, false, true),
        SET_EXT_CLOSER("}", 3, false, false),
        
        /* Syntactical, so is neither relation or isNative */
        COMPOUND_TERM_OPENER("(", 0, false, false),
        COMPOUND_TERM_CLOSER(")", 0, false, false),
        STATEMENT_OPENER("<", 0, false, false),
        STATEMENT_CLOSER(">", 0, false, false),
        
        
        /* Relations */
        INHERITANCE("-->", 1, true),
        SIMILARITY("<->", 2, true),
        INSTANCE("{--", 2, true),
        PROPERTY("--]", 2, true),
        INSTANCE_PROPERTY("{-]", 2, true),
        IMPLICATION("==>", 5, true),
        
        /* Temporal Relations */
        IMPLICATION_AFTER("=/>", 7, true),
        IMPLICATION_WHEN("=|>", 7, true),
        IMPLICATION_BEFORE("=\\>", 7, true),
        EQUIVALENCE("<=>", 5, true),
        EQUIVALENCE_AFTER("</>", 7, true),
        EQUIVALENCE_WHEN("<|>", 7, true),

        OPERATION("^", 8),

        /** an atomic term; this value is set if not a compound term */
        ATOM(".", 0, false);
        
        //-----------------------------------------------------
        
        
        /** symbol representation of this getOperator */
        public final String symbol; 
        
        /** character representation of this getOperator if symbol has length 1; else ch = 0 */
        public final char ch;
        
        /** is relation? */
        public final boolean relation;
        
        /** is native */
        public final boolean isNative;
        
        /** opener? */
        public final boolean opener;
        
        /** closer? */
        public final boolean closer;

        /** minimum NAL level required to use this operator, or 0 for N/A */
        public final int level;

        private NALOperator(String string, int minLevel) {
            this(string, minLevel, false);
        }
        
        private NALOperator(String string, int minLevel, boolean relation) {
            this(string, minLevel, relation, !relation);
        }

        private NALOperator(String string, int minLevel, boolean relation, boolean innate) {
            this.symbol = string;
            this.level = minLevel;
            this.relation = relation;
            this.isNative = innate;
            this.ch = string.length() == 1 ? string.charAt(0) : 0;
            
            this.opener = name().endsWith("_OPENER");
            this.closer = name().endsWith("_CLOSER");
        }

        @Override
        public String toString() { return symbol; }
                
        
    }




    private static final Map<String,NALOperator> _stringToOperator
            = new HashMap(NALOperator.values().length * 2);
    private static final Map<Character,NALOperator> _charToOperator
            = new HashMap(NALOperator.values().length * 2);
    static {
        //Setup NativeOperator String index hashtable 
        for (final NALOperator r : NALOperator.values())
            _stringToOperator.put(r.toString(), r);
        
        //Setup NativeOperator Character index hashtable 
        for (final NALOperator r : NALOperator.values()) {
            char c = r.ch;
            if (c!=0)
                _charToOperator.put(c, r);
        }
    }
    protected static final Map<String,NALOperator> stringToOperator
            = Collections.unmodifiableMap(_stringToOperator);
    protected static final Map<Character,NALOperator> charToOperator
            = Collections.unmodifiableMap(_charToOperator);



    public static NALOperator getOperator(final char c) {
        return charToOperator.get(c);
    }
    
    public static NALOperator getOperator(final String s) {
        return stringToOperator.get(s);
    }
    
    public static NALOperator getRelation(final String s) {
        NALOperator o = getOperator(s);
        if (o == null) return null;
        if (o.relation)
            return o;
        return null;
    }

    public static NALOperator getOpener(final char c) {
        NALOperator o = getOperator(c);
        if (o == null) return null;
        if (o.opener)
            return o;
        return null;
    }
    
    public static NALOperator getCloser(final char c) {
        NALOperator o = getOperator(c);
        if (o == null) return null;
        if (o.closer)
            return o;
        return null;
    }
    
    /**
     * Check Statement getRelation symbol, called in StringPaser
     *
     * @param s The String to be checked
     * @return if the given String is a getRelation symbol
     */
    public static boolean isRelation(final String s) {
        return getRelation(s)!=null;
    }
    
    

    /* experience line prefix */
    public static final String INPUT_LINE_PREFIX = Events.IN.class.getSimpleName();
    public static final String OUTPUT_LINE_PREFIX = Events.OUT.class.getSimpleName();

    public static final char PREFIX_MARK = ':';
    public static final char COMMENT_MARK = '/';
    //public static final char URL_INCLUDE_MARK = '`';
    public static final char ECHO_MARK = '\'';
    //public static final char NATURAL_LANGUAGE_MARK = '\"';

    /* control commands */
    public static final String RESET_COMMAND = "*reset";
    public static final String REBOOT_COMMAND = "*reboot";
    public static final String SET_NOISE_LEVEL_COMMAND = "*volume";
    
    
    /* Stamp, display only */
    public static final char STAMP_OPENER = '{';
    public static final char STAMP_CLOSER = '}';
    public static final char STAMP_SEPARATOR = ';';
    public static final char STAMP_STARTER = ':';
    
    /* TermLink type, display and internal uniqueness purposes only */
    public static final String TO_COMPONENT_1 = "@(";
    public static final String TO_COMPONENT_2 = ")_";
    public static final String TO_COMPOUND_1 = "_@(";
    public static final String TO_COMPOUND_2 = ")";



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

    public static final String SELF = "SELF";
}
