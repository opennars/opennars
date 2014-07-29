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

import java.util.HashMap;
import java.util.Map;

/**
 * The ASCII symbols used in I/O.
 */
public class Symbols {

    /* sentence type and delimitors */
    public static final char JUDGMENT_MARK = '.';
    public static final char QUESTION_MARK = '?';
    public static final char GOAL_MARK = '!';
    public static final char QUEST_MARK = '@';

    /* Tense markers */
    public static final String TENSE_MARK = ":";
    public static final String TENSE_PAST = ":\\:";
    public static final String TENSE_PRESENT = ":|:";
    public static final String TENSE_FUTURE = ":/:";
    
    /* cached versions of Tense markers with space character appended to end */
    public static final String TENSE_PAST_space = TENSE_PAST + ' ';
    public static final String TENSE_PRESENT_space = TENSE_PRESENT + ' ';
    public static final String TENSE_FUTURE_space = TENSE_FUTURE + ' ';

    /* variable type */
    public static final char VAR_INDEPENDENT = '$';
    public static final char VAR_DEPENDENT = '#';
    public static final char VAR_QUERY = '?';

    /* numerical value delimitors, must be different from the Term delimitors */
    public static final char BUDGET_VALUE_MARK = '$';
    public static final char TRUTH_VALUE_MARK = '%';
    public static final char VALUE_SEPARATOR = ';';

    /* CompountTerm delimitors, must use 4 different pairs */
    public static final char COMPOUND_TERM_OPENER = '(';
    public static final char COMPOUND_TERM_CLOSER = ')';
    public static final char STATEMENT_OPENER = '<';
    public static final char STATEMENT_CLOSER = '>';
    public static final char SET_EXT_OPENER = '{';
    public static final char SET_EXT_CLOSER = '}';
    public static final char SET_INT_OPENER = '[';
    public static final char SET_INT_CLOSER = ']';
    
    /* special characters in argument list */
    public static final char ARGUMENT_SEPARATOR = ',';
    public static final char IMAGE_PLACE_HOLDER = '_';
    
    /* prefix of special Term name */
    public static final char INTERVAL_PREFIX = '+';

    public static final char OPERATOR_PREFIX = '^';

    
    public  enum InnateOperator {
        
        /* CompountTerm operators, length = 1 */
        INTERSECTION_EXT("&", false, true),
        INTERSECTION_INT("|", false, true),
        DIFFERENCE_EXT("-", false, true),
        DIFFERENCE_INT("~", false, true),
        PRODUCT("*", false, true),
        IMAGE_EXT("/", false, true),
        IMAGE_INT("\\", false, true),

        /* CompoundStatement operators, length = 2 */        
        NEGATION("--", false, true),
        DISJUNCTION("||", false, true),
        CONJUNCTION("&&", false, true),    
        SEQUENCE("&/", false, true),    
        PARALLEL("&|", false, true),        
        
        
        /* Set Int/Ext Opener/Closer */
        SET_INT_OPENER("[", false, true),
        SET_INT_CLOSER("]", false, false),
        SET_EXT_OPENER("{", false, true),
        SET_EXT_CLOSER("}", false, false),
        
        COMPOUND_TERM_OPENER("(", false, false),
        COMPOUND_TERM_CLOSER(")", false, false),
        STATEMENT_OPENER("<", false, false),
        STATEMENT_CLOSER(">", false, false),
        
        
        /* Relations */
        INHERITANCE("-->", true),
        SIMILARITY("<->", true),
        INSTANCE("{--", true),
        PROPERTY("--]", true),
        INSTANCE_PROPERTY("{-]", true),
        IMPLICATION("==>", true),
        IMPLICATION_AFTER("=/>", true),
        IMPLICATION_WHEN("=|>", true),
        IMPLICATION_BEFORE("=\\>", true),
        EQUIVALENCE("<=>", true),
        EQUIVALENCE_AFTER("</>", true),
        EQUIVALENCE_WHEN("<|>", true);

        
        /** string representation of this operator */
        public final String string; 
        
        /** character representation of this operator if string has length 1; else ch = 0 */
        public final char ch;
        
        /** is relation? */
        public final boolean relation;
        
        /** "innate"? (according to the old CompoundTerm.isOperator() method) */
        public final boolean innate;

        private InnateOperator(String string) {
            this(string, false);
        }
        
        private InnateOperator(String string, boolean relation) {
            this(string, relation, !relation);
        }

        private InnateOperator(String string, boolean relation, boolean innate) {            
            this.string = string;
            this.relation = relation;
            this.innate = innate;
            this.ch = string.length() == 1 ? string.charAt(0) : 0;
        }
                
        public String toString() { return string; }
                
        
    }
    
    /* CompountTerm operators, length = 1 */     
    //@Deprecated public static final String INTERSECTION_EXT_OPERATOR = "&";
    @Deprecated public static final char INTERSECTION_EXT_OPERATORc = '&';
    //@Deprecated public static final String INTERSECTION_INT_OPERATOR = "|";
    @Deprecated public static final char INTERSECTION_INT_OPERATORc = '|';
    //@Deprecated public static final String DIFFERENCE_EXT_OPERATOR = "-";
    @Deprecated public static final char DIFFERENCE_EXT_OPERATORc = '-';
    //@Deprecated public static final String DIFFERENCE_INT_OPERATOR = "~";
    @Deprecated public static final char DIFFERENCE_INT_OPERATORc = '~';
    //@Deprecated public static final String PRODUCT_OPERATOR = "*";
    @Deprecated public static final char PRODUCT_OPERATORc = '*';
    //@Deprecated public static final String IMAGE_EXT_OPERATOR = "/";        
    @Deprecated public static final char IMAGE_EXT_OPERATORc = '/';
    //@Deprecated public static final String IMAGE_INT_OPERATOR = "\\";
    @Deprecated public static final char IMAGE_INT_OPERATORc = '\\';
    

    /* CompoundStatement operators, length = 2 */
    //@Deprecated public static final String NEGATION_OPERATOR = "--";
    @Deprecated public static final char NEGATION_OPERATORc = '-';    
    //@Deprecated public static final String DISJUNCTION_OPERATOR = "||";
    @Deprecated public static final char DISJUNCTION_OPERATORc = '|';
    //@Deprecated public static final String CONJUNCTION_OPERATOR = "&&";
    @Deprecated public static final char CONJUNCTION_OPERATORc = '&';
    @Deprecated public static final String SEQUENCE_OPERATOR = "&/";
    @Deprecated public static final String PARALLEL_OPERATOR = "&|";
    

    

    protected static final Map<String,InnateOperator> stringToOperator 
            = new HashMap(InnateOperator.values().length*2);    
    protected static final Map<Character,InnateOperator> charToOperator 
            = new HashMap(InnateOperator.values().length*2);
            
    static {
        for (final InnateOperator r : InnateOperator.values())
            stringToOperator.put(r.toString(), r);
        for (final InnateOperator r : InnateOperator.values()) {
            char c = r.ch;
            if (c!=0)
                charToOperator.put(c, r);
        }
    }
    

    public static InnateOperator operator(final char c) {
        return charToOperator.get(c);
    }
    
    public static InnateOperator operator(final String s) {
        return stringToOperator.get(s);
    }
    
    static InnateOperator opRelation(String s) {
        //r = r.trim();
        InnateOperator o = operator(s);
        if (o == null) return null;
        if (o.relation)
            return o;
        return null;
    }
    
    /**
     * Check Statement opRelation symbol, called in StringPaser
     *
     * @param s0 The String to be checked
     * @return if the given String is a opRelation symbol
     */
    public static boolean isRelation(final String s) {
        return opRelation(s)!=null;
    }
    
    

    /* experience line prefix */
    public static final String INPUT_LINE = "IN";
    public static final String OUTPUT_LINE = "OUT";
    public static final String ERROR_LINE = "ERR"; 

    public static final char PREFIX_MARK = ':';
    public static final char COMMENT_MARK = '/';
    public static final char URL_INCLUDE_MARK = '`';
    public static final char ECHO_MARK = '\'';
    public static final char NATURAL_LANGUAGE_MARK = '\"';

    /* control commands */
    public static final String RESET_COMMAND = "*reset";
    public static final String STOP_COMMAND = "*stop";
    public static final String START_COMMAND = "*start";
    public static final String SILENCE_COMMAND = "*silence";
    
    
    /* Stamp, display only */
    public static final char STAMP_OPENER = '{';
    public static final char STAMP_CLOSER = '}';
    public static final char STAMP_SEPARATOR = ';';
    public static final char STAMP_STARTER = ':';
    
    /* TermLink type, display only */
    public static final String TO_COMPONENT_1 = " @(";
    public static final String TO_COMPONENT_2 = ")_ ";
    public static final String TO_COMPOUND_1 = " _@(";
    public static final String TO_COMPOUND_2 = ") ";



    /*
    @Deprecated public static InnateOperator opInnate(final String op) {
        InnateOperator i = operator(op);
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
