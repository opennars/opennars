package nars.cycle;

import nars.Memory;
import nars.bag.impl.CacheBag;
import nars.concept.Concept;
import nars.concept.ConceptActivator;
import nars.process.CycleProcess;
import nars.term.Term;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Basic CycleProcess that can buffer perceptions
 */
public abstract class AbstractCycle<C extends CacheBag<Term,Concept>> extends ConceptActivator implements CycleProcess<Memory> {



    protected Memory memory;

    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final C concepts;

    public AbstractCycle(C concepts) {
        super();
        this.concepts = concepts;
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }

    @Override
    public void conceptPriorityHistogram(double[] bins) {
        if (bins!=null)
            concepts.getPriorityHistogram(bins);
    }

    @Override
    public Concept put(Concept concept) {
        return concepts.put(concept);
    }

    @Override
    public Concept remove(Term key) {
        return remove(concepts.get(key));
    }

    @Override
    public Consumer<Concept> getOnRemoval() {
        return concepts.getOnRemoval();
    }

    @Override
    public void setOnRemoval(Consumer<Concept> onRemoval) {
        concepts.setOnRemoval(onRemoval);
    }

    @Override
    public void clear() {
        concepts.clear();
    }

    @Override
    public Concept get(final Term key) {
        return concepts.get(key);
    }


    @Override
    public void reset(Memory m) {
        clear();
        memory = m;
    }

    @Override
    public final Memory getMemory() {
        return memory;
    }


    @Override
    protected boolean active(Term t) {
        return concepts.get(t)!=null;
    }

    @Override
    public void on(Concept c) {
        Concept overflown = concepts.put(c);
        if (overflown!=null)
            overflow(overflown);

        getMemory().eventConceptActive.emit(c);
    }

    @Override
    public void off(Concept c) {
    }

    @Override
    public final int size() {
        return concepts.size();
    }
    @Override public void delete() {
        concepts.delete();
    }


}
