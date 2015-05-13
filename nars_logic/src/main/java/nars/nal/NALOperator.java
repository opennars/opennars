package nars.nal;


import nars.Symbols;
import nars.util.utf8.Utf8;

public enum NALOperator {

    //TODO include min/max arity for each operate, if applicable

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
    SET_INT_OPENER("[", 3, false, true), //OPENER also functions as the symbol for the entire compound
    SET_INT_CLOSER("]", 3, false, false),
    SET_EXT_OPENER("{", 3, false, true), //OPENER also functions as the symbol for the entire compound
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

    /** an atomic term (includes interval and variables); this value is set if not a compound term */
    ATOM(".", 0, false),

    INTERVAL(String.valueOf(Symbols.INTERVAL_PREFIX), 0, false);

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

    /** minimum NAL level required to use this operate, or 0 for N/A */
    public final int level;
    private final byte[] symbolBytes;

    private NALOperator(String string, int minLevel) {
        this(string, minLevel, false);
    }

    private NALOperator(String string, int minLevel, boolean relation) {
        this(string, minLevel, relation, !relation);
    }

    private NALOperator(String string, int minLevel, boolean relation, boolean innate) {
        this.symbol = string;
        this.symbolBytes = Utf8.toUtf8(string);
        this.level = minLevel;
        this.relation = relation;
        this.isNative = innate;
        this.ch = string.length() == 1 ? string.charAt(0) : 0;

        this.opener = name().endsWith("_OPENER");
        this.closer = name().endsWith("_CLOSER");
    }

    public byte[] toBytes() { return symbolBytes; }

    @Override
    public String toString() { return symbol; }

    public static final NALOperator SET_EXT = NALOperator.SET_EXT_OPENER;
    public static final NALOperator SET_INT = NALOperator.SET_INT_OPENER;
}
