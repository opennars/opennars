package nars.nal.tlink;

import nars.Global;
import nars.budget.Budget;
import nars.nal.concept.Concept;
import nars.nal.term.Statement;
import nars.nal.nal1.Negation;
import nars.nal.nal4.Image;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.bag.tx.BagActivator;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import java.util.List;


public class TermLinkBuilder extends BagActivator<TermLinkKey,TermLink> implements TermLinkKey {

    public final Concept concept;

    final List<TermLinkTemplate> template;


    int nonTransforms;

    TermLinkTemplate currentTemplate;

    boolean incoming;
    transient private String prefix = null;

    public TermLinkBuilder(Concept c) {
        super();

        this.concept = c;

        setBudget(null);

        Term host = c.getTerm();
        if (host instanceof Compound) {


            int complexity = host.getComplexity();

            template = Global.newArrayList(complexity + 1);
            nonTransforms = 0;

            prepareComponentLinks((Compound)host);
        }
        else {
            template = null;
        }


        setKey(this);
    }

    void prepareComponentLinks(Compound ct) {
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

            if (ti.isConstant()) {
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

                    if (tj.isConstant()) {
                        TermLinkTemplate a;
                        if (t1ProductOrImage) {
                            if (type == TermLink.COMPOUND_CONDITION) {
                                //WARNING: COVERAGE (at one point) FOUND THIS CONDITION NEVER BEING CALLED, TODO check if this is still the case
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

                            if (tk.isConstant()) {
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
    public List<TermLinkTemplate> templates() {
        return template;
    }


    public void addTemplate(TermLinkTemplate tl) {
        template.add(tl);

        if (tl.type!= TermLink.TRANSFORM)
            nonTransforms++;
    }


    /** configures this selector's current budget for the next bag operation */
    public Budget set(float subBudget, float durability, float quality) {
        return super.set(subBudget, durability, quality);
    }

    /** configures this selector's current bag key for the next bag operation */
    public TermLinkBuilder set(TermLinkTemplate temp) {
        this.prefix = null;
        this.currentTemplate = temp;
        return this;
    }

    public TermLinkBuilder setIncoming(boolean b) {
        this.prefix = null;
        this.incoming = b;
        return this;
    }


    /**
     *
     * @return the amount of remaining budget priority
     */
    /*double invest(Bag<TermLink,String> bag) {
        //TODO move code from Concept here
        //iterate all items, both forward and backward
        return 0;
    }*/

    @Override public TermLinkKey name() {
        return this;
    }

    public String getPrefix() {
        String p = this.prefix;
        if (p == null) {
            p = this.prefix = currentTemplate.prefix(incoming, getTarget());
        }
        return p;
    }

    public Term getTarget() {
        return incoming ? concept.getTerm() : currentTemplate.target;
    }

    @Override
    public Term getSource() {
        return incoming ? currentTemplate.target : concept.getTerm();
    }

    @Override
    public boolean equals(Object obj) {
        return termLinkEquals(obj);
    }


    @Override
    public int hashCode() {
        return getPrefix().hashCode();
    }

    @Override
    public TermLink newItem() {
        return new TermLink(incoming, concept.getTerm(), currentTemplate, getPrefix(), getBudgetRef());
    }

    public int size() {
        return template.size();
    }

    public void delete() {
        if (template!=null)
            template.clear();
        nonTransforms = 0;
        currentTemplate = null;
    }

    /** count of how many templates are non-transforms */
    public int getNonTransforms() {
        return nonTransforms;
    }

    @Override public TermLink updateItem(TermLink termLink) {
        return null;
    }

    @Override
    public String toString() {
        //return new StringBuilder().append(newKeyPrefix()).append(target!=null ? target.name() : "").toString();
        return getPrefix() + ':' + getTarget();
    }


//    public TermLink get(boolean createIfMissing) {
//        TermLink t = concept.termLinks.GET(name());
//        if ((t == null) && createIfMissing) {
//            t = newItem();
//        }
//        return t;
//    }

    public Term getOther() {
        return currentTemplate.getTerm();
    }


}
