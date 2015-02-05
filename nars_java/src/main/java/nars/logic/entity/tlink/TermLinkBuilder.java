package nars.logic.entity.tlink;

import nars.core.Parameters;
import nars.logic.Terms;
import nars.logic.entity.*;
import nars.logic.nal1.Negation;
import nars.logic.nal4.Image;
import nars.logic.nal4.Product;
import nars.logic.nal5.Conjunction;
import nars.logic.nal5.Equivalence;
import nars.logic.nal5.Implication;
import nars.util.bag.select.BagActivator;

import java.util.List;

/**
* Created by me on 2/5/15.
*/
public class TermLinkBuilder extends BagActivator<String,TermLink> {

    public final Concept concept;

    List<TermLinkTemplate> template;
    int nonTransforms;

    final CompoundTerm host;

    Term from = null;

    TermLinkTemplate currentTemplate;
    boolean incoming;

    public TermLinkBuilder(Concept c) {
        super();

        this.concept = c;

        setBudget(new BudgetValue(0,0,0));

        host = (CompoundTerm)c.getTerm();

        int complexity = host.getComplexity();

        template = Parameters.newArrayList(complexity + 1);
        nonTransforms = 0;

        prepareComponentLinks(host);
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

        //componentLinks.ensureCapacity(componentLinks.size() + t.complexity);

        for (short i = 0; i < t.term.length; ) {
            final Term ti = t.term[i];

            if (!ti.hasVar()) {
                addTemplate(new TermLinkTemplate(type, ti, i));
            }

            if ((tEquivalence || (tImplication && (i == 0))) && ((ti instanceof Conjunction) || (ti instanceof Negation))) {

                prepareComponentLinks(TermLink.COMPOUND_CONDITION, (CompoundTerm) ti);

            } else if (ti instanceof CompoundTerm) {
                final CompoundTerm cti = (CompoundTerm)ti;

                boolean t1ProductOrImage = (ti instanceof Product) || (ti instanceof Image);

                final short tiSize = (short)cti.term.length;
                for (short j = 0; j < tiSize; ) {
                    Term tj = cti.term[j];

                    if (!tj.hasVar()) {
                        TermLinkTemplate a;
                        if (t1ProductOrImage) {
                            if (type == TermLink.COMPOUND_CONDITION) {
                                a = new TermLinkTemplate(TermLink.TRANSFORM, tj, 0, i, j);
                            } else {
                                a = new TermLinkTemplate(TermLink.TRANSFORM, tj, i, j);
                            }
                        } else {
                            a = new TermLinkTemplate(type, tj, i, j);
                        }
                        addTemplate(a);
                    }

                    if ((tj instanceof Product) || (tj instanceof Image)) {
                        CompoundTerm ctj = (CompoundTerm)tj;

                        final short tjSize = (short) ctj.term.length;
                        for (short k = 0; k < tjSize; ) {
                            final Term tk = ctj.term[k];

                            if (!tk.hasVar()) {
                                TermLinkTemplate b;
                                if (type == TermLink.COMPOUND_CONDITION) {
                                    b = new TermLinkTemplate(TermLink.TRANSFORM, tk, 0, i, j, k);
                                } else {
                                    b = new TermLinkTemplate(TermLink.TRANSFORM, tk, i, j, k);
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

        tl.setConcept(host);

        if (tl.type!= TermLink.TRANSFORM)
            nonTransforms++;
    }


    @Override public BudgetValue getBudget() {
        return budget;
    }

    public String name(Term from) {
        return currentTemplate.name( !from.equals( concept.term ) );
    }

    /** configures this selector's current budget for the next bag operation */
    public BudgetValue set(float subBudget, float durability, float quality) {
        budget.setPriority(subBudget);
        budget.setDurability(durability);
        budget.setQuality(quality);
        return budget;
    }

    /** configures this selector's current bag key for the next bag operation */
    public TermLinkBuilder set(TermLinkTemplate temp, Term source, Term target) {
        this.currentTemplate = temp;
        this.incoming = !source.equals(concept.term);
        this.from = source;
        //this.to = target;
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

    @Override public String name() {
        return name(this.from);
    }


    @Override
    public TermLink newItem() {
        return new TermLink(incoming, concept.getTerm(), currentTemplate, name(), getBudget());
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
}
