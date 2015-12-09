package nars;


import com.gs.collections.api.tuple.primitive.IntIntPair;
import com.gs.collections.impl.tuple.primitive.PrimitiveTuples;
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
    VAR_INDEP(Symbols.VAR_INDEPENDENT, 6 /*NAL6 for Indep Vars */, OpType.Variable),
    VAR_DEP(Symbols.VAR_DEPENDENT, Op.ANY, OpType.Variable),
    VAR_QUERY(Symbols.VAR_QUERY, Op.ANY, OpType.Variable),

    OPERATOR("^", 8, Args.OneArg),

    NEGATE("--", 5, Args.OneArg) {

    },

    /* Relations */
    INHERIT("-->", 1, OpType.Relation, Args.TwoArgs),
    SIMILAR("<->", true, 2, OpType.Relation, Args.TwoArgs),


    /* CompountTerm operators */
    INTERSECT_EXT("&", true, 3, Args.TwoArgs),
    INTERSECT_INT("|", true, 3, Args.TwoArgs),

    DIFF_EXT("-", 3, Args.TwoArgs),
    DIFF_INT("~", 3, Args.TwoArgs),

    PRODUCT("*", 4, Args.GTEZeroArgs),

    IMAGE_EXT("/", 4, Args.GTEOneArgs),
    IMAGE_INT("\\", 4, Args.GTEOneArgs),

    /* CompoundStatement operators, length = 2 */
    DISJUNCT("||", true, 5, Args.GTEOneArgs),
    CONJUNCT("&&", true, 5, Args.GTEOneArgs),

    SEQUENCE("&/", 7, Args.GTEOneArgs),
    PARALLEL("&|", true, 7, Args.GTEOneArgs),


    /* CompountTerm delimiters, must use 4 different pairs */
    SET_INT_OPENER("[", true, 3, Args.GTEOneArgs), //OPENER also functions as the symbol for the entire compound
    SET_EXT_OPENER("{", true, 3, Args.GTEOneArgs), //OPENER also functions as the symbol for the entire compound


    IMPLICATION("==>", 5, OpType.Relation, Args.TwoArgs),

    /* Temporal Relations */
    IMPLICATION_AFTER("=/>", 7, OpType.Relation, Args.TwoArgs),
    IMPLICATION_WHEN("=|>", true, 7, OpType.Relation, Args.TwoArgs),
    IMPLICATION_BEFORE("=\\>", 7, OpType.Relation, Args.TwoArgs),

    EQUIV("<=>", true, 5, OpType.Relation, Args.TwoArgs),
    EQUIV_AFTER("</>", 7, OpType.Relation, Args.TwoArgs),
    EQUIV_WHEN("<|>", true, 7, OpType.Relation, Args.TwoArgs),


    // keep all items which are invlved in the lower 32 bit structuralHash above this line
    // so that any of their ordinal values will not exceed 31
    //-------------
    NONE('\u2205', Op.ANY, null),

    VAR_PATTERN(Symbols.VAR_PATTERN, Op.ANY, OpType.Variable),

    INTERVAL(
            //TODO decide what this value should be, it overrides with IMAGE_EXT
            //but otherwise it's not used
            String.valueOf(Symbols.INTERVAL_PREFIX) + '/',
            Op.ANY, Args.NoArgs),

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

    /** arity limits, range is inclusive >= <=
     *  -1 for unlimited */
    public final int minSize, maxSize;

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


//    Op(char c, int minLevel) {
//        this(c, minLevel, Args.NoArgs);
//    }

    Op(char c, int minLevel, OpType type) {
        this(c, minLevel, type, Args.NoArgs);
    }

    Op(String s, boolean commutative, int minLevel) {
        this(s, minLevel, OpType.Other, Args.NoArgs);
    }
    Op(String s, boolean commutative, int minLevel, IntIntPair size) {
        this(s, commutative, minLevel, OpType.Other, size);
    }

    Op(char c, int minLevel, OpType type, IntIntPair size) {
        this(Character.toString(c), minLevel, type, size);
    }

    Op(String string, int minLevel, IntIntPair size) {
        this(string, minLevel, OpType.Other, size);
    }

    Op(String string, int minLevel, OpType type) {
        this(string, false, minLevel, type, Args.NoArgs);
    }
    Op(String string, int minLevel, OpType type, IntIntPair size) {
        this(string, false, minLevel, type, size);
    }

    Op(String string, boolean commutative, int minLevel, OpType type, IntIntPair size) {
        str = string;
        this.commutative = commutative;

        bytes = Utf8.toUtf8(string);

        this.minLevel = minLevel;
        this.type = type;

        ch = string.length() == 1 ? string.charAt(0) : 0;

        opener = name().endsWith("_OPENER");
        closer = name().endsWith("_CLOSER");

        this.minSize= size.getOne();
        this.maxSize = size.getTwo();

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
    public final void append(Appendable w) throws IOException {
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

    public boolean validSize(int length) {
        if (minSize!=-1 && length < minSize) return false;
        if (maxSize!=-1 && length > maxSize) return false;
        return true;
    }

    public boolean isImage() {
        return isA(ordinal(), ImageBits);
    }
    public boolean isStatement() {
        return isA(ordinal(), StatementBits);
    }

    static boolean isA(int needle, int haystack) {
        return (needle | haystack) == needle;
    }

    /** top-level Op categories */
    public enum OpType {
        Relation,
        Variable,
        Other
    }

    public static int StatementBits =
        Op.or(Op.INHERIT, Op.SIMILAR, Op.EQUIV, Op.IMPLICATION);

    public static final int ImageBits =
        Op.or(Op.IMAGE_EXT,Op.IMAGE_INT);

    public static final int VariableBits =
        Op.or(Op.VAR_PATTERN,Op.VAR_INDEP,Op.VAR_DEP,Op.VAR_QUERY);

    static class Args {
        static final IntIntPair NoArgs = PrimitiveTuples.pair(0,0);
        static final IntIntPair OneArg = PrimitiveTuples.pair(1,1);
        static final IntIntPair TwoArgs = PrimitiveTuples.pair(2,2);

        static final IntIntPair GTEZeroArgs = PrimitiveTuples.pair(0,-1);
        static final IntIntPair GTEOneArgs = PrimitiveTuples.pair(1,-1);
        static final IntIntPair GTETwoArgs = PrimitiveTuples.pair(2,-1);

    }

}
