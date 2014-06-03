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

/**
 * The ASCII symbols used in I/O.
 */
public class Symbols {

    /* sentence type and delimitors */
    public static final char JUDGMENT_MARK = '.';
    public static final char QUESTION_MARK = '?';

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

    /* CompountTerm operators, length = 1 */
    public static final String INTERSECTION_EXT_OPERATOR = "&";
    public static final String INTERSECTION_INT_OPERATOR = "|";
    public static final String DIFFERENCE_EXT_OPERATOR = "-";
    public static final String DIFFERENCE_INT_OPERATOR = "~";
    public static final String PRODUCT_OPERATOR = "*";
    public static final String IMAGE_EXT_OPERATOR = "/";
    public static final String IMAGE_INT_OPERATOR = "\\";

    /* CompoundStatement operators, length = 2 */
    public static final String NEGATION_OPERATOR = "--";
    public static final String DISJUNCTION_OPERATOR = "||";
    public static final String CONJUNCTION_OPERATOR = "&&";

    /* built-in relations, length = 3 */
    public static final String INHERITANCE_RELATION = "-->";
    public static final String SIMILARITY_RELATION = "<->";
    public static final String INSTANCE_RELATION = "{--";
    public static final String PROPERTY_RELATION = "--]";
    public static final String INSTANCE_PROPERTY_RELATION = "{-]";
    public static final String IMPLICATION_RELATION = "==>";
    public static final String EQUIVALENCE_RELATION = "<=>";

    /* experience line prefix */
    public static final String INPUT_LINE = "IN";
    public static final String OUTPUT_LINE = "OUT";
    public static final char PREFIX_MARK = ':';
    public static final char RESET_MARK = '*';
    public static final char COMMENT_MARK = '/';

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
