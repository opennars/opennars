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
import nars.nal.nal7.AbstractInterval;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Termed;
import nars.term.Variable;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


public class TermLinkBuilder extends BagActivator<TermLinkKey,TermLink> implements TermLinkKey, Serializable {

    transient public final Termed concept;

    final List<TermLinkTemplate> template;

    transient TermLinkTemplate currentTemplate;
    transient boolean incoming;
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

            template = Global.newArrayList(/* initial size estimate */complexity + 1);

            prepareComponentLinks((Compound)host);

        }
        else {
            template = Collections.emptyList();
        }
    }

    /**
     * Collect TermLink templates into a list, go down one level except in
     * special cases
     * <p>
     *
     * @param t The CompoundTerm for which to build links
     */
    void prepareComponentLinks(final Compound t) {

        /** add self link for structural transform: */
        addTemplate(new TermLinkTemplate(concept, t));


        boolean tEquivalence = (t instanceof Equivalence);
        boolean tImplication = (t instanceof Implication);

        for (int i = 0; i < t.term.length; i++) {
            Term ti = t.term[i].normalized();
            if (!growComponent(ti)) {
                continue;
            }

            if (ti == null) {
                throw new RuntimeException("prepareComponentLinks: " + t.term[i] + " normalized to null in superterm " + t);
                //System.err.println("prepareComponentLinks: " + t + " normalized to null");// in superterm " + t);
                //continue;
            }

            if (!(ti instanceof Variable)) {
                addTemplate(new TermLinkTemplate(concept, ti));
            }

            if ((tEquivalence || (tImplication && (i == 0))) && ((ti instanceof Conjunction) || (ti instanceof Negation))) {

                prepareComponentLinks((Compound) ti);

            } else if (ti instanceof Compound) {
                final Compound cti = (Compound)ti;

                boolean t1Grow = growLevel1(ti);

                for (int j = 0; j < cti.term.length; j++) {
                    Term tj = cti.term[j].normalized();

                    if (!(tj instanceof Variable)) {
                        if (t1Grow) {
                            addTemplate(new TermLinkTemplate(concept, tj));
                        }
                    }

                    if (growLevel2(tj)) {
                        Term ctj = (Term)tj;

                        if(ctj instanceof Compound) {
                            for (int k = 0; k < ((Compound) ctj).term.length; k++) {
                                final Term tk = ((Compound) ctj).term[k].normalized();

                                if (!(tk instanceof Variable)) {
                                    addTemplate(new TermLinkTemplate(concept, tk));
                                }
                            }
                        }
                    }

                }
            }


        }
    }

    /** determines whether to grow a 1st-level termlink to a subterm */
    protected static boolean growComponent(Term t) {
        if /*Global.DEBUG ... */ (t instanceof AbstractInterval) {
            throw new RuntimeException("interval terms should not exist at this point");
        }
        return true;
    }

    final static boolean growLevel1(final Term t) {
        return growComponent(t) /*&&
                ( growProductOrImage(t) || (t instanceof SetTensional)) */;
    }

    /** original termlink growth policy */
    static boolean growProductOrImage(Term t) {
        return (t instanceof Product) || (t instanceof Image);
    }


    final static boolean growLevel2(final Term t) {
        return growComponent(t); //growComponent(t); leads to failures, why?
        //return growComponent(t) && growProductOrImage(t);
        //if ((t instanceof Product) || (t instanceof Image) || (t instanceof SetTensional)) {
        //return (t instanceof Product) || (t instanceof Image) || (t instanceof SetTensional) || (t instanceof Junction);
    }



    /** count how many termlinks are non-transform */
    public final List<TermLinkTemplate> templates() {
        return template;
    }


    public void addTemplate(TermLinkTemplate tl) {
        template.add(tl);
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


    protected final void validate() {
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
        return new TermLink(getTerm(),  getBudget());
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
        currentTemplate = null;
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

    public int size() {
        return templates().size();
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
