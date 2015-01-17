package nars.logic.entity;

import nars.core.Parameters;
import nars.util.bag.Bag;

import java.util.Map;

/**
* Created by me on 1/17/15.
*/
public class TermLinkBuilder implements Bag.BagSelector<String,TermLink> {

    public final Concept concept;

    final Map<Term,TermLinkTemplate> template;

    private final CompoundTerm host;

    private Term from = null;
    private Term to = null;
    private Term other = null;

    private TermLinkTemplate temp;
    private boolean incoming;
    private BudgetValue budget = new BudgetValue(0,0,0);
    private int nonTransforms;

    public TermLinkBuilder(Concept c) {
        this.concept = c;

        host = (CompoundTerm)c.getTerm();
        nonTransforms = -1;

        int complexity = host.getComplexity();

        template = Parameters.newHashMap(complexity + 1);

        host.prepareComponentLinks(this);
    }

    /** count how many termlinks are non-transform */
    public int getNonTransforms() {
        if (nonTransforms == -1) {
            synchronized (template) {
                int count = 0;
                for (TermLinkTemplate tlt : template.values()) {
                    if (tlt.type != TermLink.TRANSFORM)
                        count++;
                }
                nonTransforms = count;
            }
        }
        return nonTransforms;
    }


    public void addTemplate(TermLinkTemplate tl) {
        template.put(tl.target, tl);

        tl.setConcept(host);

        nonTransforms = -1;
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
        nonTransforms = -1;
    }

}
