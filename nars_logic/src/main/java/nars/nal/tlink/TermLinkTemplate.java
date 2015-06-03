package nars.nal.tlink;

import nars.budget.Budget;
import nars.nal.concept.Concept;
import nars.nal.term.Term;
import nars.nal.term.Termed;
import nars.util.utf8.Utf8;

import java.io.Serializable;

/** contains most of the essential data to populate new TermLinks */
public class TermLinkTemplate /* extends Budget ?? instead of the pending field */ implements Termed, Serializable {

    /** The linked Term */
    public final Term target;

    /** The type of tlink, one of the above */
    public final short type;

    /** The index of the component in the component list of the compound, may have up to 4 levels */
    public final short[] index;

    /** term of the concept where this template exists, ie. the host */
    private Term concept;

    //cached names for new TermLinks
    protected byte[] outgoing;
    protected byte[] incoming;

    /** accumulates budget to eventually apply to termlinks at a future time */
    public final Budget pending = new Budget(0,0,0);


    /**
     * TermLink template
     * <p>
     * called in CompoundTerm.prepareComponentLinks only
     * @param target Target Term
     * @param type Link type
     * @param indices Component indices in compound, may be 1 to 4
     */
    public TermLinkTemplate(final Concept host, Term target, final short type, final short... indices) {
        super();

        this.concept = host.getTerm();

        target.ensureNormalized("TermLink template");

        this.target = target;
        this.type = type;
        if (type % 2 != 0)
            throw new RuntimeException("template types all point to compound and target is component: " + target);

        if (type == TermLink.COMPOUND_CONDITION) {  // the first index is 0 by default

            index = new short[indices.length + 1];
            //index[0] = 0; //first index is zero, but not necessary to set since index[] was just created

            System.arraycopy(indices, 0, index, 1, indices.length);
        /* for (int i = 0; i < indices.length; i++)
            index[i + 1] = (short) indices[i]; */
        } else {
            index = indices;
        }

    }


    public TermLinkTemplate(final Concept host, final short type, final Term target, final int i0) {
        this(host, target, type, (short)i0);
    }

    public TermLinkTemplate(final Concept host, final short type, final Term target, final int i0, final int i1) {
        this(host, target, type, (short)i0, (short)i1);
    }

    public TermLinkTemplate(final Concept host, final short type, final Term target, final int i0, final int i1, final int i2) {
        this(host, target, type, (short)i0, (short)i1, (short)i2);
    }

    public TermLinkTemplate(final Concept host, final short type, final Term target, final int i0, final int i1, final int i2, final int i3) {
        this(host, target, type, (short)i0, (short)i1, (short)i2, (short)i3);
    }

    final static byte[] emptyBytes = new byte[0];

    /** creates a new TermLink key consisting of:
     *      type
     *      target
     *      index array
     *
     * determined by the current template ('temp')
     */
    public static byte[] key(short type, short[] index, boolean incoming) {
        short t = type;
        if (!incoming) {
            t--; //point to component
        }
        if (!incoming && type == TermLink.SELF && (index == null || index.length ==0))
            return emptyBytes; //empty, avoids constructing useless prefix in this case

        if (index == null)
            throw new RuntimeException("null termlink index");


        byte[] x = new byte[index.length + 1];
        int j = 0;

        //use compact 1-char representation for type and each index component
        x[j++] = (byte) (typeCharOffset + t);

        if (index!=null) {
            for (short s : index) {
                x[j++] = ((byte) (indexCharOffset + s));
            }
        }

        return x;
    }

    public static final byte typeCharOffset = 'A';
    public static final byte indexCharOffset = 'a';

    public byte[] prefix(boolean in) {
        byte[] prefix = null;
        if (in) {
            if (incoming == null)
                incoming = key(type, index, true);
            prefix = incoming;
        }
        else {
            if (outgoing == null)
                outgoing = key(type, index, false);
            prefix = outgoing;
        }

        return prefix;
//        if (includeTerm) {
//            byte[] tname = target.name();
//            return ByteBuf.create(prefix.length + tname.length + 1).
//                    add(prefix).
//                    add((byte) Symbols.TLinkSeparator).
//                    add(tname).
//                    toBytes();
//        }
//        else {
//            return prefix;
//        }
    }


    @Override
    public String toString() {
        return concept.getTerm() + ":" + Utf8.fromUtf8(prefix(true)) + '|' + Utf8.fromUtf8(prefix(false)) + ':' + target;
    }

    @Override
    public Term getTerm() {
        return target;
    }

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("TermLinkTemplates should not need compared to each other");
        //return ((TermLinkTemplate)obj).toString().equals(toString());
    }


    public short getType(Term target) {
        if (this.target.equals(target)) {
            //points in the same direction as to the subterms, so it is a component
            return (short)(type-1);
        }

        return type;
    }
}
