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

import java.util.Map;
import java.util.TreeMap;

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
    
    /* special characors in argument list */
    public static final char ARGUMENT_SEPARATOR = ',';
    public static final char IMAGE_PLACE_HOLDER = '_';


    
    public static enum Operator {
        /* CompountTerm operators, length = 1 */
        INTERSECTION_EXT { @Override public String toString() { return "&"; } },
        INTERSECTION_INT { @Override public String toString() { return "|"; } },
        DIFFERENCE_EXT { @Override public String toString() { return "-"; } },
        DIFFERENCE_INT { @Override public String toString() { return "~"; } },
        PRODUCT { @Override public String toString() { return "*"; } },
        IMAGE_EXT { @Override public String toString() { return "/"; } },
        IMAGE_INT { @Override public String toString() { return "\\"; } },

        /* CompoundStatement operators, length = 2 */        
        NEGATION { @Override public String toString() { return "--"; } },
        DISJUNCTION { @Override public String toString() { return "||"; } },
        CONJUNCTION { @Override public String toString() { return "&&"; } },    
        SEQUENCE { @Override public String toString() { return "&/"; } },    
        PARALLEL { @Override public String toString() { return "&|"; } }    
        
    }
    
    /* CompountTerm operators, length = 1 */
    public static final String INTERSECTION_EXT_OPERATOR = "&";
    public static final char INTERSECTION_EXT_OPERATORc = '&';
    public static final String INTERSECTION_INT_OPERATOR = "|";
    public static final char INTERSECTION_INT_OPERATORc = '|';
    public static final String DIFFERENCE_EXT_OPERATOR = "-";
    public static final char DIFFERENCE_EXT_OPERATORc = '-';
    public static final String DIFFERENCE_INT_OPERATOR = "~";
    public static final char DIFFERENCE_INT_OPERATORc = '~';
    public static final String PRODUCT_OPERATOR = "*";
    public static final char PRODUCT_OPERATORc = '*';
    public static final String IMAGE_EXT_OPERATOR = "/";        
    public static final char IMAGE_EXT_OPERATORc = '/';
    public static final String IMAGE_INT_OPERATOR = "\\";
    public static final char IMAGE_INT_OPERATORc = '\\';

    /* CompoundStatement operators, length = 2 */
    public static final String NEGATION_OPERATOR = "--";
    public static final char NEGATION_OPERATORc = '-';    
    public static final String DISJUNCTION_OPERATOR = "||";
    public static final char DISJUNCTION_OPERATORc = '|';
    public static final String CONJUNCTION_OPERATOR = "&&";
    public static final char CONJUNCTION_OPERATORc = '&';
    public static final String SEQUENCE_OPERATOR = "&/";
    public static final String PARALLEL_OPERATOR = "&|";

    public static enum Relation {
        INHERITANCE { @Override public String toString() { return "-->"; } },
        SIMILARITY { @Override public String toString() { return "<->"; } },
        INSTANCE { @Override public String toString() { return "{--"; } },
        PROPERTY { @Override public String toString() { return "--]"; } },
        INSTANCE_PROPERTY { @Override public String toString() { return "{-]"; } },
        IMPLICATION { @Override public String toString() { return "==>"; } },
        IMPLICATION_AFTER { @Override public String toString() { return "=/>"; } },
        IMPLICATION_WHEN { @Override public String toString() { return "=|>"; } },
        IMPLICATION_BEFORE { @Override public String toString() { return "=\\>"; } },
        EQUIVALENCE { @Override public String toString() { return "<=>"; } },
        EQUIVALENCE_AFTER { @Override public String toString() { return "</>"; } },
        EQUIVALENCE_WHEN { @Override public String toString() { return "<|>"; } }
    }
    

    protected static final Map<String,Relation> stringToRelation = new TreeMap();    
    static {
        for (final Relation r : Relation.values())
            stringToRelation.put(r.toString(), r);
    }
    
    public static Relation getRelation(String s) {
        return stringToRelation.get(s);
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
}
