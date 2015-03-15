package nars.build;

import nars.control.ConceptActivator;
import nars.core.Core;
import nars.core.Memory;
import nars.core.NAR;
import nars.io.TextOutput;
import nars.logic.BudgetFunctions;
import nars.logic.entity.*;
import nars.logic.reason.ConceptProcess;
import nars.logic.reason.DirectProcess;
import nars.util.bag.impl.CacheBag;
import nars.util.bag.impl.CurveBag;

import java.util.Iterator;

/** processes every concept fairly, according to priority, in each cycle */
public class Solid extends Default {


    private final int maxConcepts;
    private final int maxTasks;
    private final int minFires;
    private final int maxFires;
    private Memory memory;

    public Solid(int maxConcepts, int minFires, int maxFires) {
        super();
        this.maxConcepts = maxConcepts;
        this.maxTasks = maxConcepts * 100;
        this.maxFires = maxFires;
        this.minFires = minFires;
    }

    @Override
    public void init(NAR n) {
        super.init(n);
        this.memory = n.memory;

    }

    final ConceptActivator activator = new ConceptActivator() {
        @Override
        public Memory getMemory() {
            return memory;
        }

        @Override
        public CacheBag<Term, Concept> getSubConcepts() {
            return null;
        }

        @Override
        public ConceptBuilder getConceptBuilder() {
            return Solid.this.getConceptBuilder();
        }
    };

    @Override
    public Core newCore() {
        return new Core() {

            CurveBag<Concept,Term> concepts = new CurveBag(maxConcepts, true);
            CurveBag<Task<CompoundTerm>,Sentence<CompoundTerm>> tasks = new CurveBag(maxTasks, true);

            @Override
            public Iterator<Concept> iterator() {
                return concepts.iterator();
            }

            @Override
            public void addTask(Task t) {
                tasks.put(t);
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public void cycle() {
                System.out.println("\ncycle " + memory.time() + " : " + concepts.size() + " concepts");

                getMemory().perceiveNext();

                //1. process all new tasks
                for (Task t : tasks) {
                    new DirectProcess(getMemory(), t).run();
                }
                tasks.clear();

                //2. fire all cocnepts
                for (Concept c : concepts) {

                    float p = c.getPriority();
                    int fires = Math.round(p * (maxFires - minFires) + minFires);

                    System.out.println("  firing " + c + " x " + fires);
                    for (int i = 0; i < fires; i++) {
                        TaskLink tl = c.taskLinks.forgetNext(param.taskLinkForgetDurations, getMemory());
                        if (tl==null) break;
                        new ConceptProcess(c, tl) {
                            @Override protected void beforeFinish() {

                            }
                        }.run();
                    }
                }
            }

            @Override
            public void reset() {
                tasks.clear();
                concepts.clear();
            }

            @Override
            public Concept concept(Term term) {
                return concepts.get(term);
            }

            @Override
            public Concept conceptualize(BudgetValue budget, Term term, boolean createIfMissing) {
                synchronized(activator) {
                    activator.set(term, budget, true, getMemory().time());
                    return concepts.update(activator);
                }
            }

            @Override
            @Deprecated public void activate(Concept c, BudgetValue b, BudgetFunctions.Activating mode) {

            }

            @Override
            public Concept nextConcept() {
                return concepts.peekNext();
            }

            @Override
            public void init(Memory m) {

            }

            @Override
            public void conceptRemoved(Concept c) {

            }

            @Override
            public Memory getMemory() {
                return memory;
            }
        };
    }



    public static void main(String[] args) {

        Solid s = new Solid(128, 20, 2);
        NAR n = new NAR(s);
        n.input("<a --> b>. :\\:");
        n.input("<b <-> c>.");
        n.input("<c <-> d>? :/:");
        n.input("<(*,d,c) </> a>.");

        TextOutput.out(n);
        n.step(64);

    }


    
}
