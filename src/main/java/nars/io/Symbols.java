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

import nars.io.Output.ERR;
import nars.io.Output.IN;
import nars.io.Output.OUT;

import java.util.HashMap;
import java.util.Map;


public class Symbols {

    /* sentence type and delimitors */
    public static final char JUDGMENT_MARK = '.';
    public static final char QUESTION_MARK = '?';
    public static final char GOAL_MARK = '!';
    public static final char QUEST_MARK = '@';
    public static final char TERM_NORMALIZING_WORKAROUND_MARK = 'T';
    
    
    
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
    public static final String VAR_QUERY_DEFAULT = VAR_QUERY + "any";

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
    public static final char OPERATOR_PREFIX = '^';
    public static final char TERM_PREFIX = 'T';
    public static final char QUOTE = '\"';
    

    
    public enum NativeOperator {
        
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
        
        
        /* CompountTerm delimitors, must use 4 different pairs */
        SET_INT_OPENER("[", false, true),
        SET_INT_CLOSER("]", false, false),
        SET_EXT_OPENER("{", false, true),
        SET_EXT_CLOSER("}", false, false),    
        
        /* Syntactical, so is neither relation or isNative */
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
        
        /* Temporal Relations */
        IMPLICATION_AFTER("=/>", true),
        IMPLICATION_WHEN("=|>", true),
        IMPLICATION_BEFORE("=\\>", true),
        EQUIVALENCE("<=>", true),
        EQUIVALENCE_AFTER("</>", true),
        EQUIVALENCE_WHEN("<|>", true),

        /** an atomic term; this value is set if not a compound term */
        ATOM(".", false);
        
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

        private NativeOperator(String string) {
            this(string, false);
        }
        
        private NativeOperator(String string, boolean relation) {
            this(string, relation, !relation);
        }

        private NativeOperator(String string, boolean relation, boolean innate) {            
            this.symbol = string;
            this.relation = relation;
            this.isNative = innate;
            this.ch = string.length() == 1 ? string.charAt(0) : 0;
            
            this.opener = name().endsWith("_OPENER");
            this.closer = name().endsWith("_CLOSER");
        }

        @Override
        public String toString() { return symbol; }
                
        
    }    

    protected static final Map<String,NativeOperator> stringToOperator 
            = new HashMap(NativeOperator.values().length * 2);
    protected static final Map<Character,NativeOperator> charToOperator 
            = new HashMap(NativeOperator.values().length * 2);
            
    static {
        //Setup NativeOperator String index hashtable 
        for (final NativeOperator r : NativeOperator.values())
            stringToOperator.put(r.toString(), r);
        
        //Setup NativeOperator Character index hashtable 
        for (final NativeOperator r : NativeOperator.values()) {
            char c = r.ch;
            if (c!=0)
                charToOperator.put(c, r);
        }
    }    

    public static NativeOperator getOperator(final char c) {
        return charToOperator.get(c);
    }
    
    public static NativeOperator getOperator(final String s) {
        return stringToOperator.get(s);
    }
    
    public static NativeOperator getRelation(final String s) {
        NativeOperator o = getOperator(s);
        if (o == null) return null;
        if (o.relation)
            return o;
        return null;
    }

    public static NativeOperator getOpener(final char c) {
        NativeOperator o = getOperator(c);
        if (o == null) return null;
        if (o.opener)
            return o;
        return null;
    }
    
    public static NativeOperator getCloser(final char c) {
        NativeOperator o = getOperator(c);
        if (o == null) return null;
        if (o.closer)
            return o;
        return null;
    }
    
    /**
     * Check Statement getRelation symbol, called in StringPaser
     *
     * @param s0 The String to be checked
     * @return if the given String is a getRelation symbol
     */
    public static boolean isRelation(final String s) {
        return getRelation(s)!=null;
    }
    
    

    /* experience line prefix */
    public static final String INPUT_LINE_PREFIX = IN.class.getSimpleName();
    public static final String OUTPUT_LINE_PREFIX = OUT.class.getSimpleName();
    public static final String ERROR_LINE_PREFIX = ERR.class.getSimpleName();

    public static final char PREFIX_MARK = ':';
    public static final char COMMENT_MARK = '/';
    //public static final char URL_INCLUDE_MARK = '`';
    public static final char ECHO_MARK = '\'';
    //public static final char NATURAL_LANGUAGE_MARK = '\"';

    /* control commands */
    public static final String RESET_COMMAND = "*reset";
    public static final String REBOOT_COMMAND = "*reboot";
    public static final String STOP_COMMAND = "*stop";
    public static final String START_COMMAND = "*start";
    public static final String SET_NOISE_LEVEL_COMMAND = "*volume";
    public static final String SET_DECISION_LEVEL_COMMAND = "*decisionthreshold";
    
    /* Stamp, display only */
    public static final char STAMP_OPENER = '{';
    public static final char STAMP_CLOSER = '}';
    public static final char STAMP_SEPARATOR = ';';
    public static final char STAMP_STARTER = ':';
    
    /* TermLink type, display only */
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

    public static String SELF = "SELF";
}
