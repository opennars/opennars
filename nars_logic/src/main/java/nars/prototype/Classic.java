package nars.prototype;

import nars.Memory;
import nars.budget.Bag;
import nars.budget.Budget;
import nars.budget.bag.LevelBag;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.concept.Concept;
import nars.nal.concept.DefaultConcept;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;

import static nars.budget.bag.LevelBag.NextNonEmptyLevelMode.Fast;

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
        return new LevelBag(getConceptBagLevels(), getConceptBagSize()).setNextNonEmptyMode(Fast);
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
