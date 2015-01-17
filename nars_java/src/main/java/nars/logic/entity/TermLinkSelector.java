package nars.logic.entity;

import nars.core.Parameters;
import nars.util.bag.Bag;

import java.util.Collection;
import java.util.Map;

/**
* Created by me on 1/17/15.
*/
public class TermLinkSelector implements Bag.BagSelector<String,TermLink> {

    public final Concept concept;

    final Map<Term,TermLink.TermLinkTemplate> template;

    private Term from = null;
    private Term to = null;
    private Term other = null;
    private TermLink.TermLinkTemplate temp;
    private boolean incoming;
    private BudgetValue budget = null;


    public TermLinkSelector(Concept c, Collection<TermLink.TermLinkTemplate> templates) {
        this.concept = c;



        //int complexity = c.getTerm().getComplexity();
        int complexity = templates.size();
        template = Parameters.newHashMap(complexity + 1);

        for (TermLink.TermLinkTemplate tl : templates) {
            template.put(tl.target, tl);
        }
    }


    @Override
    public BudgetValue getBudget() {
        return budget;
    }

    public String name(Term from, Term to) {
        return temp.name( from!= concept.term );
    }

    public void set(BudgetValue budget) {
        this.budget = budget;
    }
    
    public TermLinkSelector set(Term source, Term target) {
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
        return name(this.from, this.to);
    }


    @Override
    public TermLink newInstance() {
        return new TermLink(incoming, concept.getTerm(), temp,name(), getBudget());
    }


    public int size() {
        return template.size();
    }

    public void clear() {
        template.clear();
    }
}
