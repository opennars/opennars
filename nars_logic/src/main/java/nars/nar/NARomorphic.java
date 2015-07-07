package nars.nar;

import com.google.common.collect.MapMaker;
import nars.AbstractMemory;
import nars.Param;
import nars.bag.Bag;
import nars.bag.impl.CurveBag;
import nars.budget.Budget;
import nars.clock.Clock;
import nars.concept.BeliefTable;
import nars.concept.Concept;
import nars.concept.TaskTable;
import nars.link.*;
import nars.narsese.NarseseParser;
import nars.process.ConceptProcess;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.util.data.random.XorShift1024StarRandom;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * like HAT-trie libs for java, say if i wanted to create one big term tree around which to organize all system objects as leaves
  and potentially manage them as byte bufers that are operated on
  manage the leaves as self contained contexts
  where all the data is mapped to bytebuffers and avoid object allocation
  your parser could mainline the data directly into this but first i think grappa is sufficient
  this design should be able to runfully asynchronous at unlimited parallelization
  with no duplicate data
  and minimal heap allocation / GC
  it would not need to distniguish between terms and concepts
  and sentences would just be array elements
  tasks i mean
  turn the design inside out
  and ignore OOP sensibility
  without sacrificing simplcity
  now that we have a clear idea of how it works
  and what doesnt
  all terms would be nodes in one big tree
  as xjrn_ suggests
  ordinary references would still be used and allow GC
  there wouldnt need to be the laborious process of creating a term, normalizing it, creating a sentence, processing it.. et
  etc
  just insert the item into the tree and let it go
  we can even do this using the existing code to some extent
  termlinks and tasklinks would work as they do now
  but preferably without the bag system
  maybe just usnig the termlink and tasklink to navigate like the ant model does
  each ant being a thread
  budget would guide them
 */
public class NARomorphic extends Default  {

    public final static Random RNG = new XorShift1024StarRandom(1);

    private final static short CONCURRENCY = 4;


    /** combines Memory and NAR */
    public static class Mem implements Node<Term,Node>, AbstractMemory {

        final static MapMaker indexBuilder = new MapMaker().weakValues().concurrencyLevel(CONCURRENCY);
        public final Map<Term,Node> index;
        final NarseseParser narsese = NarseseParser.newParser();

        /**
         * Constructor, called in Memory.getConcept only
         *
         * @param t
         */
        public Mem() {
            super();

            //super(term, b, taskLinks, termLinks, rb, memory);

            index = indexBuilder.makeMap();
            //sub = new ConcurrentHashMap(initialSubCapacity);
        }

        public void print(PrintStream out) {
            out.print(index);

        }

        @Override
        public Concept concept(Term t) {
            return null;
        }

        @Override
        public Concept conceptualize(Budget budget, Term term) {
            return null;
        }

        @Override
        public Clock getClock() {
            return null;
        }

        @Override
        public Param getParam() {
            return null;
        }

        @Override
        public void emit(Class c, Object... ars) {

        }

        @Override
        public void removed(Task task, String reason) {

        }

        @Override
        public Term self() {
            return null;
        }

        @Override
        public long newStampSerial() {
            return 0;
        }


        @FunctionalInterface
        public interface InputReaction {
            public void onInput(Task t, Node n);
        }

        public class DerivationResponse {
            public final ConceptProcess premise;
            public final Task derivation;

            public DerivationResponse(ConceptProcess premise, Task derivation) {
                this.premise = premise;
                this.derivation = derivation;
            }
        }


        public void input(String task, @Nullable InputReaction callback) {
            Task t = narsese.parseTask(task);
            input(t, callback);
        }

        public void input(Task t, @Nullable InputReaction callback) {
            Node n;
            if (t!=null) {
                n = get(t.getTerm(), t.getBudget());
            }
            else {
                n = null;
            }

            if (callback!=null)
                callback.onInput(t, n);

        }


        @Override
        public Iterator<Node> iterator() {
            return index.values().iterator();
        }

        /** return the subterm matching 't' */
        public Node get(final Term t) {
            return index.get(t);
        }

        /** return existing subterm matching 't' or add it if doesnt exist */
        public Node get(final Term t, final Budget b) {
            Node n = get(t);
            if (n != null)
                return n;

            //something was inserted since we decided to, so use that one instead of the one unnecessarily created here here
            //this avoids needing synchrnized(), maybe
            final Node newNode = newNode(t);
            if (newNode!=null) {
                final Node existing = index.putIfAbsent(t, newNode);
                if (existing != null)
                    return existing;
            }

            return newNode;
        }

        private Node newNode(Term t) {
            if (t instanceof Compound)
                return new CompoundNode((Compound) t);
            /*else if (t instanceof Atom)
                return new TermNode*/

            return null;
        }


        public void clear() {
            index.clear();
        }

    }


    public static interface Node<X,Y> extends Iterable<Y> {



    }

//
//    public static class OpNode extends EnumMap<Op,Node> implements Node<Op,Node> {
//
//        public OpNode() {
//            super(Op.class);
//        }
//
//        @Override
//        public Iterator<Node> iterator() {
//            return values().iterator();
//        }
//    }
//


    abstract public static class TermNode<T extends Term, K,V> extends Budget implements Node, Concept {

        private final T term;
        final TermLinkBuilder linker;
        private Bag<TermLinkKey, TermLink> termLinks;

        public TermNode(T t) {
            this.term = t;
            linker = new TermLinkBuilder(term);
            termLinks = new CurveBag(RNG, 32);
        }

        @Override
        public String toString() {
            return term.toString();
        }

        @Override
        public TermLinkBuilder getTermLinkBuilder() {
            return linker;
        }

        @Override
        public Bag<Sentence, TaskLink> getTaskLinks() {
            return null;
        }

        @Override
        public Bag<TermLinkKey, TermLink> getTermLinks() {
            return termLinks;
        }

        @Override
        public Map<Object, Meta> getMeta() {
            return null;
        }

        @Override
        public void setMeta(Map<Object, Meta> meta) {

        }

        @Override
        public AbstractMemory getMemory() {
            return null;
        }

        @Override
        public TaskLink activateTaskLink(TaskLinkBuilder taskLinkBuilder) {
            return null;
        }

        @Override
        public boolean linkTerms(Budget budgetRef, boolean b) {
            return false;
        }

        @Override
        public TermLink activateTermLink(TermLinkBuilder termLinkBuilder) {
            return null;
        }

        @Override
        public void updateTermLinks() {

        }

        @Override
        public void setUsed(long time) {

        }


        @Override
        public boolean link(Task currentTask) {
            return false;
        }

        @Override
        public boolean isConstant() {
            return false;
        }

        @Override
        public boolean setConstant(boolean b) {
            return false;
        }

        @Override
        public BeliefTable getBeliefs() {
            return null;
        }

        @Override
        public BeliefTable getGoals() {
            return null;
        }

        @Override
        public TaskTable getQuestions() {
            return null;
        }

        @Override
        public TaskTable getQuests() {
            return null;
        }

        @Override
        public State getState() {
            return State.Active;
        }

        @Override
        public Concept setState(State nextState) {
            return this;
        }

        @Override
        public long getCreationTime() {
            return 0;
        }

        @Override
        public long getDeletionTime() {
            return 0;
        }

        @Override
        public void delete() {
        }

        @Override
        public boolean processBelief(TaskProcess nal, Task task) {
            return false;
        }

        @Override
        public boolean processGoal(TaskProcess nal, Task task) {
            return false;
        }

        @Override
        public Task processQuestion(TaskProcess nal, Task task) {
            return null;
        }

        @Override
        public T name() {
            return term;
        }

        @Override
        public T getTerm() {
            return term;
        }

        public T t() {
            return term;
        }

        @Override
        public Budget getBudget() {
            return this;
        }

        public Budget b() {
            return this;
        }
    }

    /** node in the global term tree - associates a Term to all related objects
     * and provides access to them  */
    public static class CompoundNode<C extends Compound> extends TermNode<C, Integer,Node>  {



        /**
         * Constructor, called in Memory.getConcept only
         *
         * @param term      A term corresponding to the concept
         * @param b
         * @param taskLinks
         * @param termLinks
         * @param rb
         * @param memory    A reference to the memory
         */
        public CompoundNode(C t) {
            super(t);
        }


        @Override
        public Iterator iterator() {
            return getTerm().iterator();
        }

    }


    public static void main(String[] args) {

        Mem m = new Mem();
        m.input("<a --> b>.", (t,c) -> {
            System.out.println("IN: " + t + " -> " + c);
        });
        m.print(System.out);

    }
}
