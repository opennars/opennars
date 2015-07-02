package nars.nar;

import nars.Memory;
import nars.bag.Bag;
import nars.bag.impl.LevelBag;
import nars.budget.Budget;
import nars.task.Sentence;
import nars.task.Task;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.term.Compound;
import nars.term.Term;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;

import static nars.bag.impl.LevelBag.NextNonEmptyLevelMode.Fast;

/** Classic NARS configuration (most like the Default from 1.6.4 and before)
  * Uses LevelBag for all bags */
public class Classic extends Default {

    int taskLinkBagLevels;
    int termLinkBagLevels;
    int conceptBagLevels;


    public Classic() {
        this(1000, 1, 3);

        setConceptBagLevels(64);
        setTaskLinkBagLevels(16);
        setTermLinkBagLevels(28);
        setNovelTaskBagLevels(16);

    }

    public Classic(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {
        super(maxConcepts, conceptsFirePerCycle, termLinksPerCycle);

        setConceptBagLevels(64);
        setTaskLinkBagLevels(16);
        setTermLinkBagLevels(28);

    }


    @Override
    public Bag<Sentence<Compound>, Task<Compound>> newNovelTaskBag() {

        return new LevelBag(getNovelTaskBagSize(), getNovelTaskBagLevels());
    }


    @Override
    public Bag<Term, Concept> newConceptBag() {
        return new LevelBag(getConceptBagLevels(), getActiveConcepts()).setNextNonEmptyMode(Fast);
    }


    @Override
    public Concept newConcept(Term t, Budget b, Memory m) {
        Bag<Sentence, TaskLink> taskLinks = new LevelBag<>(getTaskLinkBagLevels(), getConceptTaskLinks());
        Bag<TermLinkKey, TermLink> termLinks = new LevelBag<>(getTermLinkBagLevels(), getConceptTermLinks());

        return new DefaultConcept(t, b, taskLinks, termLinks, m);
    }


    public int getConceptBagLevels() { return conceptBagLevels; }
    public Default setConceptBagLevels(int bagLevels) { this.conceptBagLevels = bagLevels; return this;  }

    /**
     * @return the taskLinkBagLevels
     */
    public int getTaskLinkBagLevels() {
        return taskLinkBagLevels;
    }

    public Default setTaskLinkBagLevels(int taskLinkBagLevels) {
        this.taskLinkBagLevels = taskLinkBagLevels;
        return this;
    }
    public int getTermLinkBagLevels() {
        return termLinkBagLevels;
    }

    public Default setTermLinkBagLevels(int termLinkBagLevels) {
        this.termLinkBagLevels = termLinkBagLevels;
        return this;
    }



    public Default setNovelTaskBagLevels(int l) {
        this.taskBufferLevels = l;
        return this;
    }

}
