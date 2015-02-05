package nars.logic.entity.tlink;

import nars.logic.entity.Term;
import nars.logic.entity.TermLink;

/** contains most of the essential data to populate new TermLinks */
public class TermLinkTemplate {

    /** The linked Term */
    public final Term target;

    /** The type of tlink, one of the above */
    public final short type;

    /** The index of the component in the component list of the compound, may have up to 4 levels */
    public final short[] index;

    /** term of the concept where this template exists, ie. the host */
    private Term concept;

    //cached names for new TermLinks
    protected String outgoing;
    protected String incoming;


    /**
     * TermLink template
     * <p>
     * called in CompoundTerm.prepareComponentLinks only
     * @param target Target Term
     * @param type Link type
     * @param indices Component indices in compound, may be 1 to 4
     */
    public TermLinkTemplate(final Term target, final short type, final short... indices) {
        super();
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


    public TermLinkTemplate(final short type, final Term target, final int i0) {
        this(target, type, (short)i0);
    }

    public TermLinkTemplate(final short type, final Term target, final int i0, final int i1) {
        this(target, type, (short)i0, (short)i1);
    }

    public TermLinkTemplate(final short type, final Term target, final int i0, final int i1, final int i2) {
        this(target, type, (short)i0, (short)i1, (short)i2);
    }

    public TermLinkTemplate(final short type, final Term target, final int i0, final int i1, final int i2, final int i3) {
        this(target, type, (short)i0, (short)i1, (short)i2, (short)i3);
    }

    /** creates a new TermLink key consisting of:
     *      type
     *      target
     *      index array
     *
     * determined by the current template ('temp')
     */
    public String name(boolean incoming, Term other) {
        short t = this.type;
        if (!incoming) {
            t--; //point to component
        }

        StringBuilder sb = new StringBuilder(64);
        //use compact 1-char representation for type and each index component
        sb.append((char)('A' + t));
        for (short s : index) {
            sb.append((char)('a' + s));
        }
        sb.append(other.name());
        return sb.toString();
    }


    protected void setConcept(Term conceptTerm) {
        if (this.concept == null || !this.concept.equals( conceptTerm) ) {
            //reset, concept has changed (if this instance is ever used with a different concept)
            this.concept = conceptTerm;
            incoming = outgoing = null;
        }
    }

    public String name(boolean in) {
        setConcept(concept);
        if (in) {
            if (incoming == null)
                incoming = name(true, concept);
            return incoming;
        }
        else {
            if (outgoing == null)
                outgoing = name(false, target);
            return outgoing;
        }
    }


    @Override
    public String toString() {
        return name(true) + "|" + name(false);
    }
}
