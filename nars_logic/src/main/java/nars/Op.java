package nars;


import nars.util.utf8.Utf8;

import java.io.IOException;
import java.io.Writer;

/** NAL symbol table */
public enum Op {

    //TODO include min/max arity for each operate, if applicable

    /** an atomic term (includes interval and variables); this value is set if not a compound term */
    ATOM(".", 0, false),

    VAR_INDEPENDENT(Symbols.VAR_INDEPENDENT,0,false),
    VAR_DEPENDENT(Symbols.VAR_DEPENDENT,0,false),
    VAR_QUERY(Symbols.VAR_QUERY,0,false),

    OPERATOR("^", 8),

    NEGATION("--", 5, false, true, 1),

    /* Relations */
    INHERITANCE("-->", 1, true, 2),
    SIMILARITY("<->", 2, true, 3),


    /* CompountTerm operators, length = 1 */
    INTERSECTION_EXT("&", 3, false, true),
    INTERSECTION_INT("|", 3, false, true),
    DIFFERENCE_EXT("-", 3, false, true),
    DIFFERENCE_INT("~", 3, false, true),

    PRODUCT("*", 4, false, true),

    IMAGE_EXT("/", 4, false, true),
    IMAGE_INT("\\", 4, false, true),

    /* CompoundStatement operators, length = 2 */
    DISJUNCTION("||", 5, false, true, 4),
    CONJUNCTION("&&", 5, false, true, 5),

    SEQUENCE("&/", 7, false, true, 6),
    PARALLEL("&|", 7, false, true, 7),


    /* CompountTerm delimitors, must use 4 different pairs */
    SET_INT_OPENER("[", 3, false, true), //OPENER also functions as the symbol for the entire compound
    SET_EXT_OPENER("{", 3, false, true), //OPENER also functions as the symbol for the entire compound



    IMPLICATION("==>", 5, true, 8),

    /* Temporal Relations */
    IMPLICATION_AFTER("=/>", 7, true, 9),
    IMPLICATION_WHEN("=|>", 7, true, 10),
    IMPLICATION_BEFORE("=\\>", 7, true, 11),
    EQUIVALENCE("<=>", 5, true, 12),
    EQUIVALENCE_AFTER("</>", 7, true, 13),
    EQUIVALENCE_WHEN("<|>", 7, true, 14),


    INTERVAL(String.valueOf(Symbols.INTERVAL_PREFIX_OLD), 0, false),

    // keep all items which are invlved in the lower 32 bit structuralHash above this line
    // so that any of their ordinal values will not exceed 31
    //-------------

    INSTANCE("{--", 2, true), //should not be given a compact representation because this will not exist internally after parsing
    PROPERTY("--]", 2, true), //should not be given a compact representation because this will not exist internally after parsing
    INSTANCE_PROPERTY("{-]", 2, true), //should not be given a compact representation because this will not exist internally after parsing



    SET_INT_CLOSER("]", 3, false, false),
    SET_EXT_CLOSER("}", 3, false, false),

    /* Syntactical, so is neither relation or isNative */
    COMPOUND_TERM_OPENER("(", 0, false, false),
    COMPOUND_TERM_CLOSER(")", 0, false, false),
    STATEMENT_OPENER("<", 0, false, false),
    STATEMENT_CLOSER(">", 0, false, false),


    VAR_PATTERN(Symbols.VAR_PATTERN,0, false),
    NONE('\u2205', 0, false);

    //-----------------------------------------------------



    /** symbol representation of this getOperator */
    public final String str;

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

    /** minimum NAL level required to use this operate, or 0 for N/A */
    public final int level;

    /** should be null unless a 1-character representation is not possible. */
    public final byte[] bytes;

    /** 1-character representation, or 0 if a multibyte must be used */
    public final byte byt;


    Op(char c, int minLevel, int... bytes) {
        this(Character.toString(c), minLevel, false, bytes);
    }

    Op(String string, int minLevel, int... bytes) {
        this(string, minLevel, false, bytes);
    }

    Op(char c, int minLevel, boolean relation, int... bytes) {
        this(Character.toString(c), minLevel, relation, bytes);
    }
    Op(String string, int minLevel, boolean relation, int... bytes) {
        this(string, minLevel, relation, !relation, bytes);
    }

    Op(String string, int minLevel, boolean relation, boolean innate, int... ibytes) {

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

        this.level = minLevel;
        this.relation = relation;
        this.isNative = innate;
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
    public final void expand(final Writer w) throws IOException {
        if (this.ch == 0)
            w.write(str);
        else
            w.write(ch);
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
}
