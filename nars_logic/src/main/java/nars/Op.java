package nars;


import nars.util.utf8.Utf8;

import java.io.IOException;
import java.io.Serializable;

/** NAL symbol table */
public enum Op implements Serializable {




    //TODO include min/max arity for each operate, if applicable

    /** an atomic term (includes interval and variables); this value is set if not a compound term */
    ATOM(".", Op.ANY, OpType.Other),

    VAR_INDEPENDENT(Symbols.VAR_INDEPENDENT, Op.ANY, OpType.Variable),
    VAR_DEPENDENT(Symbols.VAR_DEPENDENT, Op.ANY, OpType.Variable),
    VAR_QUERY(Symbols.VAR_QUERY,Op.ANY, OpType.Variable),

    OPERATOR("^", 8),

    NEGATION("--", 5, 1),

    /* Relations */
    INHERITANCE("-->", 1, OpType.Relation, 2),
    SIMILARITY("<->", 2, OpType.Relation, 3),


    /* CompountTerm operators, length = 1 */
    INTERSECTION_EXT("&", 3),
    INTERSECTION_INT("|", 3),
    DIFFERENCE_EXT("-", 3),
    DIFFERENCE_INT("~", 3),

    PRODUCT("*", 4),

    IMAGE_EXT("/", 4),
    IMAGE_INT("\\", 4),

    /* CompoundStatement operators, length = 2 */
    DISJUNCTION("||", 5, 4),
    CONJUNCTION("&&", 5, 5),

    SEQUENCE("&/", 7, 6),
    PARALLEL("&|", 7, 7),


    /* CompountTerm delimiters, must use 4 different pairs */
    SET_INT_OPENER("[", 3), //OPENER also functions as the symbol for the entire compound
    SET_EXT_OPENER("{", 3), //OPENER also functions as the symbol for the entire compound



    IMPLICATION("==>", 5, OpType.Relation, 8),

    /* Temporal Relations */
    IMPLICATION_AFTER("=/>", 7, OpType.Relation, 9),
    IMPLICATION_WHEN("=|>", 7, OpType.Relation, 10),
    IMPLICATION_BEFORE("=\\>", 7, OpType.Relation, 11),

    EQUIVALENCE("<=>", 5, OpType.Relation, 12),
    EQUIVALENCE_AFTER("</>", 7, OpType.Relation, 13),
    EQUIVALENCE_WHEN("<|>", 7, OpType.Relation, 14),




    // keep all items which are invlved in the lower 32 bit structuralHash above this line
    // so that any of their ordinal values will not exceed 31
    //-------------
    NONE('\u2205', Op.ANY),


    INTERVAL(
            //TODO decide what this value should be, it overrides with IMAGE_EXT
            //but otherwise it's not used
            String.valueOf(Symbols.INTERVAL_PREFIX) + "/",
            Op.ANY),

    INSTANCE("{--", 2, OpType.Relation), //should not be given a compact representation because this will not exist internally after parsing
    PROPERTY("--]", 2, OpType.Relation), //should not be given a compact representation because this will not exist internally after parsing
    INSTANCE_PROPERTY("{-]", 2, OpType.Relation), //should not be given a compact representation because this will not exist internally after parsing



    VAR_PATTERN(Symbols.VAR_PATTERN, Op.ANY, OpType.Variable);


    //-----------------------------------------------------



    /** symbol representation of this getOperator */
    public final String str;

    /** character representation of this getOperator if symbol has length 1; else ch = 0 */
    public final char ch;

    public final OpType type;


    /** opener? */
    public final boolean opener;

    /** closer? */
    public final boolean closer;

    /** minimum NAL level required to use this operate, or 0 for N/A */
    public final int minLevel;

    /** should be null unless a 1-character representation is not possible. */
    public final byte[] bytes;

    /** 1-character representation, or 0 if a multibyte must be used */
    public final byte byt;


    Op(char c, int minLevel, int... bytes) {
        this(c, minLevel, OpType.Other, bytes);
    }

    Op(char c, int minLevel, OpType type, int... bytes) {
        this(Character.toString(c), minLevel, type, bytes);
    }

    Op(String string, int minLevel, int... ibytes) {
        this(string, minLevel, OpType.Other, ibytes);
    }

    Op(String string, int minLevel, OpType type, int... ibytes) {

        this.str = string;

        final byte bb[];

        final boolean hasCompact = (ibytes.length == 1);
        if (!hasCompact) {
            bb = Utf8.toUtf8(string);
        } else {
            bb = new byte[ibytes.length];
            for (int i = 0; i < ibytes.length; i++)
                bb[i] = (byte) ibytes[i];
        }

        this.bytes = bb;

        if (hasCompact) {
            int p = bb[0];
            if (p < 31) //do not assign if it's an ordinary non-control char
                this.byt = (byte)(p);
            else
                this.byt = (byte)0;
        }
        else {
            //multiple ibytes, use the provided array
            this.byt = (byte)0;
        }

        this.minLevel = minLevel;
        this.type = type;

        this.ch = string.length() == 1 ? string.charAt(0) : 0;

        this.opener = name().endsWith("_OPENER");
        this.closer = name().endsWith("_CLOSER");
    }

    @Override
    public String toString() { return str; }

    /** alias */
    public static final Op SET_EXT = Op.SET_EXT_OPENER;
    public static final Op SET_INT = Op.SET_INT_OPENER;


    /** writes this operator to a Writer in (human-readable) expanded UTF16 mode */
    public final void expand(final Appendable w) throws IOException {
        if (this.ch == 0)
            w.append(str);
        else
            w.append(ch);
    }

    public boolean has8BitRepresentation() {
        return byt!=0;
    }


    public static int bitStructure(final Op... o) {
        int bits = 0;
        for (Op n : o) {
            bits |= (1 << n.ordinal());
        }
        return bits;
    }

    public boolean levelValid(final int nal) {
        final int l = minLevel;

        //if a minimum level is specified for the term..
        if (l > ANY) {
            //if nal is below the required minimum level
            if (nal < l)
                return false;
        }

        return true;
    }

    /** specifier for any NAL level */
    public final static int ANY = 0;

    public boolean isVar() {
        return type == Op.OpType.Variable;
    }

    public enum OpType {
        Relation,
        Variable,
        Other
    }
}
