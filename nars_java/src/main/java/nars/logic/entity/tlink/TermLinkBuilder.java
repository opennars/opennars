package nars.logic.entity.tlink;

import nars.core.Parameters;
import nars.logic.entity.*;
import nars.logic.nal1.Negation;
import nars.logic.nal4.Image;
import nars.logic.nal4.Product;
import nars.logic.nal5.Conjunction;
import nars.logic.nal5.Equivalence;
import nars.logic.nal5.Implication;
import nars.util.bag.select.BagActivator;

import java.util.List;


public class TermLinkBuilder extends BagActivator<TermLinkKey,TermLink> implements TermLinkKey {

    public final Concept concept;

    List<TermLinkTemplate> template;


    int nonTransforms;

    TermLinkTemplate currentTemplate;
    boolean incoming;

    public TermLinkBuilder(Concept c) {
        super();

        this.concept = c;

        setBudget(null);

        Term host = c.getTerm();
        if (host instanceof CompoundTerm) {


            int complexity = host.getComplexity();

            template = Parameters.newArrayList(complexity + 1);
            nonTransforms = 0;

            prepareComponentLinks((CompoundTerm)host);
        }
        else {
        }


        setKey(this);
    }

    void prepareComponentLinks(CompoundTerm ct) {
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
    void prepareComponentLinks(final short type, final CompoundTerm t) {

        boolean tEquivalence = (t instanceof Equivalence);
        boolean tImplication = (t instanceof Implication);


        for (short i = 0; i < t.term.length; ) {
            final Term ti = t.term[i];

            if (ti.isConstant()) {
                addTemplate(new TermLinkTemplate(concept, type, ti, i));
            }
            if ((tEquivalence || (tImplication && (i == 0))) && ((ti instanceof Conjunction) || (ti instanceof Negation))) {

                prepareComponentLinks(TermLink.COMPOUND_CONDITION, (CompoundTerm) ti);

            } else if (ti instanceof CompoundTerm) {
                final CompoundTerm cti = (CompoundTerm)ti;

                boolean t1ProductOrImage = (ti instanceof Product) || (ti instanceof Image);

                final short tiSize = (short)cti.term.length;
                for (short j = 0; j < tiSize; ) {
                    Term tj = cti.term[j];

                    if (tj.isConstant()) {
                        TermLinkTemplate a;
                        if (t1ProductOrImage) {
                            if (type == TermLink.COMPOUND_CONDITION) {
                                //WARNING: COVERAGE FOUND THIS CONDITION NEVER BEING CALLED, TODO check if this is still the case
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
                        CompoundTerm ctj = (CompoundTerm)tj;

                        final short tjSize = (short) ctj.term.length;
                        for (short k = 0; k < tjSize; ) {
                            final Term tk = ctj.term[k];

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

    public BudgetValue set(BudgetValue b) {
        this.budget = b;
        return b;
    }

    /** configures this selector's current budget for the next bag operation */
    public BudgetValue set(float subBudget, float durability, float quality) {
        if (budget == null)
            this.budget = new BudgetValue(subBudget, durability, quality);
        else
            this.budget.set(subBudget, durability, quality);
        return budget;
    }

    /** configures this selector's current bag key for the next bag operation */
    public TermLinkBuilder set(TermLinkTemplate temp, Term source) {
        this.currentTemplate = temp;
        this.incoming = !source.equals(concept.term);
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
        return currentTemplate.prefix(incoming);
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
        return termLinkHashCode();
    }

    @Override
    public TermLink newItem() {
        return new TermLink(incoming, concept.getTerm(), currentTemplate, getPrefix(), getBudgetRef());
    }

    public int size() {
        return template.size();
    }

    public void clear() {
        template.clear();
        nonTransforms = 0;
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




}
