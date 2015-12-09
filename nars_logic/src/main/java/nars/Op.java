package nars;


import nars.nal.nal7.Order;
import nars.util.utf8.Utf8;

import java.io.IOException;
import java.io.Serializable;

/**
 * NAL symbol table
 */
public enum Op implements Serializable {


    //TODO include min/max arity for each operate, if applicable

    /**
     * an atomic term (includes interval and variables); this value is set if not a compound term
     */
    ATOM(".", Op.ANY, OpType.Other),
    //        public final Atom get(String i) {
//            return Atom.the(i);
//        }}
//
    VAR_INDEPENDENT(Symbols.VAR_INDEPENDENT, 6 /*NAL6 for Indep Vars */, OpType.Variable),

    VAR_DEPENDENT(Symbols.VAR_DEPENDENT, Op.ANY, OpType.Variable),
    VAR_QUERY(Symbols.VAR_QUERY, Op.ANY, OpType.Variable),

    OPERATOR("^", 8),

    NEGATION("--", 5) {

    },

    /* Relations */
    INHERITANCE("-->", 1, OpType.Relation),
    SIMILARITY("<->", true, 2, OpType.Relation),


    /* CompountTerm operators, length = 1 */
    INTERSECTION_EXT("&", true, 3),
    INTERSECTION_INT("|", true, 3),

    DIFFERENCE_EXT("-", 3),
    DIFFERENCE_INT("~", 3),

    PRODUCT("*", 4),

    IMAGE_EXT("/", 4),
    IMAGE_INT("\\", 4),

    /* CompoundStatement operators, length = 2 */
    DISJUNCTION("||", true, 5),
    CONJUNCTION("&&", true, 5),

    SEQUENCE("&/", 7),
    PARALLEL("&|", true, 7),


    /* CompountTerm delimiters, must use 4 different pairs */
    SET_INT_OPENER("[", true, 3), //OPENER also functions as the symbol for the entire compound
    SET_EXT_OPENER("{", true, 3), //OPENER also functions as the symbol for the entire compound


    IMPLICATION("==>", 5, OpType.Relation),

    /* Temporal Relations */
    IMPLICATION_AFTER("=/>", 7, OpType.Relation),
    IMPLICATION_WHEN("=|>", true, 7, OpType.Relation),
    IMPLICATION_BEFORE("=\\>", 7, OpType.Relation),

    EQUIVALENCE("<=>", true, 5, OpType.Relation),
    EQUIVALENCE_AFTER("</>", 7, OpType.Relation),
    EQUIVALENCE_WHEN("<|>", true, 7, OpType.Relation),


    // keep all items which are invlved in the lower 32 bit structuralHash above this line
    // so that any of their ordinal values will not exceed 31
    //-------------
    NONE('\u2205', Op.ANY),


    VAR_PATTERN(Symbols.VAR_PATTERN, Op.ANY, OpType.Variable),


    INTERVAL(
            //TODO decide what this value should be, it overrides with IMAGE_EXT
            //but otherwise it's not used
            String.valueOf(Symbols.INTERVAL_PREFIX) + '/',
            Op.ANY),

    INSTANCE("{--", 2, OpType.Relation), //should not be given a compact representation because this will not exist internally after parsing
    PROPERTY("--]", 2, OpType.Relation), //should not be given a compact representation because this will not exist internally after parsing
    INSTANCE_PROPERTY("{-]", 2, OpType.Relation); //should not be given a compact representation because this will not exist internally after parsing


    //-----------------------------------------------------


    /**
     * symbol representation of this getOperator
     */
    public final String str;

    /**
     * character representation of this getOperator if symbol has length 1; else ch = 0
     */
    public final char ch;

    public final OpType type;


    /**
     * opener?
     */
    public final boolean opener;

    /**
     * closer?
     */
    public final boolean closer;

    /**
     * minimum NAL level required to use this operate, or 0 for N/A
     */
    public final int minLevel;

    /**
     * should be null unless a 1-character representation is not possible.
     */
    public final byte[] bytes;

    private final boolean commutative;
    private Order temporalOrder;


    Op(char c, int minLevel, int... bytes) {
        this(c, minLevel, OpType.Other);
    }

    Op(String s, boolean commutative, int minLevel) {
        this(s, commutative, minLevel, OpType.Other);
    }


    Op(char c, int minLevel, OpType type) {
        this(Character.toString(c), minLevel, type);
    }

    Op(String string, int minLevel) {
        this(string, minLevel, OpType.Other);
    }

    Op(String string, int minLevel, OpType type) {
        this(string, false, minLevel, type);
    }

    Op(String string, boolean commutative, int minLevel, OpType type) {

        str = string;
        this.commutative = commutative;

        bytes = Utf8.toUtf8(string);

        this.minLevel = minLevel;
        this.type = type;

        ch = string.length() == 1 ? string.charAt(0) : 0;

        opener = name().endsWith("_OPENER");
        closer = name().endsWith("_CLOSER");


    }



    @Override
    public String toString() {
        return str;
    }

    /**
     * alias
     */
    public static final Op SET_EXT = Op.SET_EXT_OPENER;
    public static final Op SET_INT = Op.SET_INT_OPENER;


    /**
     * writes this operator to a Writer in (human-readable) expanded UTF16 mode
     */
    public final void expand(Appendable w) throws IOException {
        if (ch == 0)
            w.append(str);
        else
            w.append(ch);
    }

    public static final int or(Op... o) {
        int bits = 0;
        for (Op n : o) {
            bits |= n.bit();
        }
        return bits;
    }

    public final int bit() {
        return (1 << ordinal());
    }

    public static final int or(int bits, Op o) {
        return bits | o.bit();
    }

    public final boolean levelValid(int nal) {
        return (nal >= minLevel);
    }

    /**
     * specifier for any NAL level
     */
    public static final int ANY = 0;

    public final boolean isVar() {
        return type == Op.OpType.Variable;
    }

    public boolean isCommutative() {
        return commutative;
    }

    public Order getTemporalOrder() {
        return temporalOrder;
    }

    /** top-level Op categories */
    public enum OpType {
        Relation,
        Variable,
        Other
    }


    public static final int VARIABLE_BITS =
        Op.or(Op.VAR_PATTERN,Op.VAR_INDEPENDENT,Op.VAR_DEPENDENT,Op.VAR_QUERY);

}
