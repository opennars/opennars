package nars.logic.entity;

import nars.core.Parameters;
import nars.util.bag.Bag;

import java.util.List;

/**
* Created by me on 1/17/15.
*/
public class TermLinkBuilder implements Bag.BagSelector<String,TermLink> {

    public final Concept concept;

    List<TermLinkTemplate> template;
    int nonTransforms;

    private final CompoundTerm host;

    private Term from = null;
    private Term to = null;
    private Term other = null;

    private TermLinkTemplate currentTemplate;
    private boolean incoming;
    private BudgetValue budget = new BudgetValue(0,0,0);

    public TermLinkBuilder(Concept c) {
        this.concept = c;

        host = (CompoundTerm)c.getTerm();

        int complexity = host.getComplexity();

        template = Parameters.newArrayList(complexity + 1);
        nonTransforms = 0;

        host.prepareComponentLinks(this);
    }

    /** count how many termlinks are non-transform */
    public List<TermLinkTemplate> templates() {
        return template;
    }


    public void addTemplate(TermLinkTemplate tl) {
        template.add(tl);

        tl.setConcept(host);

        if (tl.type!=TermLink.TRANSFORM)
            nonTransforms++;
    }


    @Override public BudgetValue getBudget() {
        return budget;
    }

    public String name(Term from) {
        return currentTemplate.name( from!= concept.term );
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
        if ((this.from == source) && (this.to == target)) return this;
        this.currentTemplate = temp;
        this.incoming = !source.equals(concept.term);
        this.other = incoming ? source : target;
        this.from = source;
        this.to = target;
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
    public TermLink newInstance() {
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
}
