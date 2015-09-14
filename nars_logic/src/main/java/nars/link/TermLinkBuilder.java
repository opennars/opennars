package nars.link;

import nars.Global;
import nars.Memory;
import nars.bag.tx.BagActivator;
import nars.nal.nal1.Negation;
import nars.nal.nal4.Image;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.term.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


public class TermLinkBuilder extends BagActivator<TermLinkKey,TermLink> implements TermLinkKey, Serializable {

    transient public final Termed concept;

    final List<TermLinkTemplate> template;

    transient int nonTransforms;
    transient TermLinkTemplate currentTemplate;
    transient boolean incoming;
    private byte[] prefix;
    private int hash;
    private float forgetCycles;
    private long now;

    public TermLinkBuilder(Termed c) {
        super();

        this.concept = c;

        setBudget(null);

        Term host = c.getTerm();
        if (host instanceof Compound) {


            int complexity = host.complexity();


            template = Global.newArrayList(complexity + 1);
            nonTransforms = 0;

            prepareComponentLinks((Compound)host);
        }
        else {
            template = Collections.emptyList();
        }



    }

    final void prepareComponentLinks(Compound ct) {
        short type = (ct instanceof Statement) ? TermLink.COMPOUND_STATEMENT : TermLink.COMPOUND;   // default
        prepareComponentLinks(type, ct);
    }

    /**
     * Collect TermLink templates into a list, go down one level except in
     * special cases
     * <p>
     *
     * @param type The type of TermLink to be built
     * @param t The CompoundTerm for which to build links
     */
    void prepareComponentLinks(final short type, final Compound t) {

        boolean tEquivalence = (t instanceof Equivalence);
        boolean tImplication = (t instanceof Implication);


        for (short i = 0; i < t.term.length; ) {
            Term ti = t.term[i].normalized();

            if (ti == null) {
                throw new RuntimeException("prepareComponentLinks: " + t.term[i] + " normalized to null in superterm " + t);
            }

            if (!(ti instanceof Variable)) {
                addTemplate(new TermLinkTemplate(concept, type, ti, i));
            }

            if ((tEquivalence || (tImplication && (i == 0))) && ((ti instanceof Conjunction) || (ti instanceof Negation))) {

                prepareComponentLinks(TermLink.COMPOUND_CONDITION, (Compound) ti);

            } else if (ti instanceof Compound) {
                final Compound cti = (Compound)ti;

                boolean t1ProductOrImage = (ti instanceof Product) || (ti instanceof Image);

                final short tiSize = (short)cti.term.length;
                for (short j = 0; j < tiSize; ) {
                    Term tj = cti.term[j].normalized();

                    if (!(tj instanceof Variable)) {
                        TermLinkTemplate a;
                        if (t1ProductOrImage) {
                            if (type == TermLink.COMPOUND_CONDITION) {
                                a = new TermLinkTemplate(concept, TermLink.TRANSFORM, tj, 0, i, j);
                            } else {
                                a = new TermLinkTemplate(concept, TermLink.TRANSFORM, tj, i, j);
                            }
                        } else {
                            a = new TermLinkTemplate(concept, type, tj, i, j);
                        }
                        addTemplate(a);
                    }

                    if ((tj instanceof Product) || (tj instanceof Image)) {
                        Compound ctj = (Compound)tj;

                        final short tjSize = (short) ctj.term.length;
                        for (short k = 0; k < tjSize; ) {
                            final Term tk = ctj.term[k].normalized();

                            if (!(tk instanceof Variable)) {
                                TermLinkTemplate b;
                                if (type == TermLink.COMPOUND_CONDITION) {
                                    b = new TermLinkTemplate(concept, TermLink.TRANSFORM, tk, 0, i, j, k);
                                } else {
                                    b = new TermLinkTemplate(concept, TermLink.TRANSFORM, tk, i, j, k);
                                }
                                addTemplate(b);
                            }

                            k++; //increment at end in case it's the last iteration we want to use max n-1, not n
                        }
                    }

                    j++; //increment at end in case it's the last iteration we want to use max n-1, not n
                }
            }

            i++; //increment at end in case it's the last iteration we want to use max n-1, not n
        }
    }



    /** count how many termlinks are non-transform */
    public final List<TermLinkTemplate> templates() {
        return template;
    }


    public void addTemplate(TermLinkTemplate tl) {
        template.add(tl);

        if (tl.type!= TermLink.TRANSFORM)
            nonTransforms++;
    }


    @Override
    public final float getForgetCycles() {
        return forgetCycles;
    }

    @Override
    public final long time() {
        return now;
    }


    /** configures this selector's current bag key for the next bag operation */
    public final TermLinkBuilder set(final TermLinkTemplate temp, boolean initialDirection, final Memory m) {
        if ((temp != currentTemplate) || (this.incoming != initialDirection)) {
            this.currentTemplate = temp;
            this.incoming = initialDirection;
            super.setBudget(/*(Budget)*/temp);
            validate();
        }

        this.forgetCycles = m.durationToCycles(
                m.termLinkForgetDurations.floatValue()
        );
        this.now = m.time();
        return this;
    }

    public final TermLinkBuilder setIncoming(final boolean b) {
        if (this.incoming!=b) {
            this.incoming = b;
            validate();
        }
        return this;
    }

    @Override
    public final byte[] bytes() {
        return prefix;
    }


    @Override
    public final void setBytes(byte[] b) {
        this.prefix = b;
    }

    protected final void validate() {
        this.prefix = currentTemplate.prefix(incoming);
        this.hash = currentTemplate.hash(incoming);
    }

    @Override
    public final Term getTerm() {
        return incoming ? concept.getTerm() : currentTemplate.getTarget();
    }

//    public final Term getSource() {
//        return incoming ? currentTemplate.getTarget() : concept.getTerm();
//    }

    @Override
    public final boolean equals(final Object obj) {
        return TermLinkKey.termLinkEquals(this, (TermLinkKey) obj);
    }



    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public final TermLink newItem() {
        //this.prefix = null;
        return new TermLink(getTerm(), currentTemplate, getBudget(), prefix, hash);
    }

//    public final TermLink out(TermLinkTemplate tlt) {
//        return new TermLink(tlt.getTarget(), tlt, getBudget(),
//                tlt.prefix(false),
//                tlt.hash(false));
//    }


/*    public int size() {
        return template.size();
    }*/

    public void delete() {
        template.clear();
        nonTransforms = 0;
        currentTemplate = null;
    }

    /** count of how many templates are non-transforms */
    public final int getNonTransforms() {
        return nonTransforms;
    }



    @Override
    public final String toString() {
        //return new StringBuilder().append(newKeyPrefix()).append(target!=null ? target.name() : "").toString();
        return name().toString();
    }

    @Override
    public final TermLinkKey name() {
        return this;
    }

    //    public TermLink get(boolean createIfMissing) {
//        TermLink t = concept.termLinks.GET(name());
//        if ((t == null) && createIfMissing) {
//            t = newItem();
//        }
//        return t;
//    }

//    public final Term getOther() {
//        return currentTemplate.getTerm();
//    }



}
