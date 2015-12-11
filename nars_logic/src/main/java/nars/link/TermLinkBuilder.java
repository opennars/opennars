package nars.link;

import nars.Global;
import nars.Memory;
import nars.Op;
import nars.bag.tx.BagActivator;
import nars.nal.nal7.CyclesInterval;
import nars.term.Term;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.term.variable.Variable;

import java.util.Collections;
import java.util.List;
import java.util.Set;


public class TermLinkBuilder extends BagActivator<TermLinkKey,TermLink> implements TermLinkKey {

    public final transient Termed concept;

    protected final List<TermLinkTemplate> template;

    transient TermLinkTemplate currentTemplate;
    transient boolean incoming;

    protected int hash;
    protected float forgetCycles;
    protected long now;

    public TermLinkBuilder(Termed c) {

        concept = c;

        setBudget(null);

        Term host = c.getTerm();
        if (host instanceof Compound) {

            Set<Term> components = Global.newHashSet(host.complexity());
            prepareComponentLinks((Compound)host, components);

            template = Global.newArrayList(components.size());
            components.forEach(t -> template.add(
                    new TermLinkTemplate(host, t))
            );

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
     * @param components set of components being accumulated, to avoid duplicates
     */
    static void prepareComponentLinks(Compound t, Set<Term> components) {

        ///** add self link for structural transform: */
        //components.add(t);

        boolean tEquivalence = t.isAny(Op.EquivalencesBits);
        boolean tImplication = t.isAny(Op.ImplicationsBits);


        int ni = t.size();
        for (int i = 0; i < ni; i++) {
            Term ti = t.term(i).normalized();
            if (!growComponent(ti)) {
                continue;
            }

            if (ti == null) {
                throw new RuntimeException("prepareComponentLinks: " + t.term(i) + " normalized to null in superterm " + t);
                //System.err.println("prepareComponentLinks: " + t + " normalized to null");// in superterm " + t);
                //continue;
            }

            if (!(ti instanceof Variable)) {
                components.add(ti);
            }

            if ((tEquivalence || (tImplication && (i == 0))) &&
                    ((ti.isAny(Op.ConjunctivesBits)) || (ti.isAny(Op.NEGATE)))) {

                prepareComponentLinks((Compound) ti, components);

            } else if (ti instanceof Compound) {
                Compound cti = (Compound)ti;

                if (!growLevel1(ti)) continue;

                int nj = cti.size();
                for (int j = 0; j < nj; j++) {
                    Term tj = cti.term(j).normalized();

                    if (!(tj instanceof Variable)) {
                        components.add(tj);
                    }

                    if (growLevel2(tj)) {

                        if(tj instanceof Compound) {
                            Compound cctj = (Compound) tj;
                            int nk = cctj.size();
                            for (int k = 0; k < nk; k++) {
                                Term tk = cctj.term(k).normalized();

                                if (!(tk instanceof Variable)) {
                                    components.add(tk);
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
        if /*Global.DEBUG ... */ (t instanceof CyclesInterval) {
            throw new RuntimeException("interval terms should not exist at this point");
        }
        return true;
    }

    static final boolean growLevel1(Term t) {
        return growComponent(t) /*&&
                ( growProductOrImage(t) || (t instanceof SetTensional)) */;
    }

//    /** original termlink growth policy */
//    static boolean growProductOrImage(Term t) {
//        return (t instanceof Product) || (t instanceof Image);
//    }


    static final boolean growLevel2(Term t) {
        return growComponent(t); //growComponent(t); leads to failures, why?
        //return growComponent(t) && growProductOrImage(t);
        //if ((t instanceof Product) || (t instanceof Image) || (t instanceof SetTensional)) {
        //return (t instanceof Product) || (t instanceof Image) || (t instanceof SetTensional) || (t instanceof Junction);
    }



    /** count how many termlinks are non-transform */
    public final List<TermLinkTemplate> templates() {
        return template;
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
    public final TermLinkBuilder set(TermLinkTemplate temp, boolean initialDirection, Memory m) {
        if ((temp != currentTemplate) || (incoming != initialDirection)) {
            currentTemplate = temp;
            incoming = initialDirection;
            super.setBudget(/*(Budget)*/temp);
            validate();
        }

        forgetCycles = m.durationToCycles(
                m.termLinkForgetDurations.floatValue()
        );
        now = m.time();
        return this;
    }

    public final TermLinkBuilder setIncoming(boolean b) {
        if (incoming !=b) {
            incoming = b;
            validate();
        }
        return this;
    }


    protected final void validate() {
        hash = currentTemplate.hash(incoming);
    }

    @Override
    public final Term getTerm() {
        return incoming ? concept.getTerm() : currentTemplate.getTarget();
    }

//    public final Term getSource() {
//        return incoming ? currentTemplate.getTarget() : concept.getTerm();
//    }

    @Override
    public final boolean equals(Object obj) {
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
