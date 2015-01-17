package nars.logic.entity;

import nars.core.Parameters;
import nars.util.bag.Bag;

import java.util.List;
import java.util.Map;

/**
* Created by me on 1/17/15.
*/
public class TermLinkBuilder implements Bag.BagSelector<String,TermLink> {

    public final Concept concept;

    final Map<Term,TermLinkTemplate> template;

    /** cache of non-transform termlinks for fast iteration without iterator */
    final List<TermLinkTemplate> nonTransforms;

    private final CompoundTerm host;

    private Term from = null;
    private Term to = null;
    private Term other = null;

    private TermLinkTemplate temp;
    private boolean incoming;
    private BudgetValue budget = new BudgetValue(0,0,0);

    public TermLinkBuilder(Concept c) {
        this.concept = c;

        host = (CompoundTerm)c.getTerm();

        int complexity = host.getComplexity();

        template = Parameters.newHashMap(complexity + 1);
        nonTransforms = Parameters.newArrayList(complexity/2);

        host.prepareComponentLinks(this);
    }

    /** count how many termlinks are non-transform */
    public List<TermLinkTemplate> getNonTransforms() {
        return nonTransforms;
    }


    public void addTemplate(TermLinkTemplate tl) {
        template.put(tl.target, tl);

        tl.setConcept(host);

        if (tl.type!=TermLink.TRANSFORM)
            nonTransforms.add(tl);
    }


    @Override public BudgetValue getBudget() {
        return budget;
    }

    public String name(Term from) {
        return temp.name( from!= concept.term );
    }

    /** configures this selector's current budget for the next bag operation */
    public BudgetValue set(float subBudget, float durability, float quality) {
        budget.setPriority(subBudget);
        budget.setDurability(durability);
        budget.setQuality(quality);
        return budget;
    }

    /** configures this selector's current bag key for the next bag operation */
    public TermLinkBuilder set(Term source, Term target) {
        if ((this.from == source) && (this.to == target)) return this;
        this.incoming = !source.equals(concept.term);
        this.other = incoming ? source : target;
        this.temp = template.get(other);
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
        return new TermLink(incoming, concept.getTerm(), temp, name(), getBudget());
    }

    public int size() {
        return template.size();
    }

    public void clear() {
        template.clear();
        nonTransforms.clear();
    }

}
