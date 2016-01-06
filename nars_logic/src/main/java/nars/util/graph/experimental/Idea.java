///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.util.graph.experimental;
//
//import com.google.common.base.Objects;
//import com.google.common.collect.Iterables;
//import nars.*;
//import nars.Events.ConceptForget;
//import nars.concept.Concept;
//import nars.nal.nal4.Image;
//
//import nars.task.Task;
//import nars.term.Compound;
//import nars.term.Term;
//import nars.term.Termed;
//import nars.term.Terms;
//import nars.truth.Truthed;
//import nars.util.event.EventEmitter;
//import nars.util.event.Reaction;
//
//import java.util.*;
//
///**
// *each of those rows can be a representation of something like a 'multiconcept' or 'aggregated concept' which combines concept data from related concepts
//and tasks where the only differ by the top-level operate, tense, freq, conf,etc
// EXPERIMENTAL
// */
//public class Idea implements Iterable<Concept> {
//
//    final public Set<Concept> concepts = Collections.synchronizedSet(new HashSet());
//    final CharSequence key;
//    final Set<SentenceType> feature = new HashSet();
//    final Set<Op> operators = new HashSet<Op>();
//
//
//    public static CharSequence getKey(Termed tt) {
//        Term t = tt.getTerm();
//        if (t instanceof Compound) {
//            Compound ct = (Compound)t;
//
//            //TODO use an array -> strong conversion that eliminates the ' ' after comma, saving 1 char each term
//
//            if (!ct.isCommutative()) {
//                //if not commutative (order matters): key = list of subterms
//                String s = Arrays.toString(ct.term).replaceFirst("\\[", "(");
//
//                if (ct instanceof Image) {
//                    int index = ((Image)ct).relationIndex;
//                    s += "." + index;
//                }
//
//                return s;
//            }
//            else {
//                //key = sorted set of subterms
//                return Terms.toSortedSet(ct.term).toString();
//            }
//        }
//        else {
//            return t.toString();
//        }
//    }
//
//
//    public Idea(Concept c) {
//        super();
//        this.key = getKey(c.getTerm());
//        add(c);
//    }
//
//    public Idea(Iterable<Concept> c) {
//        super();
//        this.key = getKey(c.iterator().next());
//        for (Concept x : c)
//            add(x);
//    }
//
//    public Set<Op> operators() {
//        return operators;
//    }
//
//
//    /** returns a sample term (ex: first concept's term);
//        all Concepts will have equal sub-components */
//    public Term getSampleTerm() {
//        return concepts.iterator().next().getTerm();
//    }
//
//    /** number of concepts represented in this Idea */
//    public int size() {
//        return concepts.size();
//    }
//
//    /** # of terms, which will be equal in all Concept terms */
//    public int getArity() {
//        Term sampleTerm = getSampleTerm();
//        if (sampleTerm instanceof Compound) {
//            return ((Compound)sampleTerm).term.length;
//        }
//        return 1;
//    }
//
//    public class SentenceType implements Comparable<SentenceType> {
//
//        public final Op op;
//        public final char punc;
//
//        transient private final int hash;
//        private ArrayList sentences;
//
//        public SentenceType(Op o, char c) {
//            this.op = o;
//            this.punc = c;
//            this.hash = Objects.hashCode(op, punc);
//        }
//
//        @Override
//        public int hashCode() {
//            return hash;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (o == this) return true;
//            if (!(o instanceof SentenceType)) return false;
//            SentenceType x = (SentenceType)o;
//            return x.op == op && x.punc == punc;
//        }
//
//        @Override
//        public int compareTo(SentenceType t) {
//            int i = op.compareTo(t.op);
//            if (i != 0) return i;
//            return Character.compare(punc, t.punc);
//        }
//
//        @Override
//        public String toString() {
//            return op.toString() + " " + punc;
//        }
//
//        public List<Truthed> getSentences() {
//            if (sentences == null) {
//                sentences = new ArrayList();
//                for (Concept c : concepts) {
//                    switch (punc) {
//                        case Symbols.JUDGMENT:
//                            Iterables.addAll(sentences, c.getBeliefs());
//                            break;
//                        case Symbols.QUESTION:
//                            sentences.addAll(Task.getSentences(c.getQuestions()));
//                            break;
//                        case Symbols.QUEST:
//                            sentences.addAll(Task.getSentences(c.getQuests()));
//                            break;
//                        case Symbols.GOAL:
//                            Iterables.addAll(sentences, c.getGoals());
//                            //sentences.addAll(c.getGoals());
//                            break;
//                    }
//                }
//            }
//            return sentences;
//        }
//
//    }
//
////    public Collection<Sentence> getSentences(SentenceType o) {
////        List<Sentence> s = new ArrayList();
////        for (Concept c : this) {
////            if (c.term.operate() == o.op) {
////                s.addAll(c.getSentences(o.punc));
////            }
////        }
////        return s;
////    }
////
//    /** returns the set of all operate+punctuation concatenations */
//    public Set<SentenceType> getSentenceTypes() {
//        return feature;
//    }
//
//    /**
//     * includes the concept in this idea.  it's ok to repeat add a
//     * concept again since they are stored as Set
//     */
//    public boolean add(Concept c) {
//        if (Global.DEBUG)
//            ensureMatchingConcept(c);
//
//        boolean b = concepts.add(c);
//
//        if (b) {
//            update();
//        }
//
//        return b;
//    }
//
//    public void update() {
//
//        operators.clear();
//        feature.clear();
//
//        for (Concept c : this) {
//            Op o = c.operator();
//            operators.add(o);
//
//            if (!c.getBeliefs().isEmpty())
//                feature.add(new SentenceType(o, Symbols.JUDGMENT));
//            if (!c.getQuestions().isEmpty())
//                feature.add(new SentenceType(o, Symbols.QUESTION));
//            if (!c.getGoals().isEmpty())
//                feature.add(new SentenceType(o, Symbols.GOAL));
//            if (!c.getQuests().isEmpty())
//                feature.add(new SentenceType(o, Symbols.QUEST));
//        }
//
//    }
//
//    public boolean remove(Concept c) {
//        if (Global.DEBUG)
//            ensureMatchingConcept(c);
//
//        boolean b = concepts.remove(c);
//        if (b)
//            update();
//        return b;
//    }
//
//    public CharSequence key() {
//        return key;
//    }
//
//    protected void ensureMatchingConcept(Concept c) {
//        CharSequence ckey = getKey(c.getTerm());
//        if (!ckey.equals(key))
//            throw new RuntimeException(c + " does not belong in Idea " + key);          }
//
//    @Override
//    public String toString() {
//        return key() + concepts.toString();
//    }
//
//    @Override
//    public Iterator<Concept> iterator() {
//        return concepts.iterator();
//    }
//
//    @Override
//    public int hashCode() {
//        return key.hashCode();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj==this) return true;
//        if (obj instanceof Idea) {
//            return key.equals(((Idea)obj).key);
//        }
//        return false;
//    }
//
//    //TODO should implement NARReaction probably
//    public static class IdeaSet extends HashMap<CharSequence,Idea> implements Reaction<Class,Object[]> {
//        private final NAR nar;
//        private EventEmitter.Registrations reg;
//
//        public IdeaSet(NAR n) {
//            super();
//            this.nar = n;
//            enable(true);
//        }
//
//        @Override
//        public void event(Class event, Object[] args) {
//            if (event == Events.ConceptActive.class) {
//                add((Concept)args[0]);
//            }
//            else if (event == ConceptForget.class) {
//                remove((Concept) args[0]);
//            }
//            else if (event == TaskProcess.class) {
//                update((Concept) args[2]);
//            }
//        }
//
//
//        public void enable(boolean enabled) {
//
//            if (enabled) {
//                clear();
//
//                reg = nar.memory.event.on(this, ConceptForget.class, Events.ConceptActive.class, TaskProcess.class);
//
//                //add existing
//                for (Concept c : nar.memory.getControl())
//                    add(c);
//           }
//            else {
//                if (reg!=null) {
//                    reg.off();
//                    reg = null;
//                }
//            }
//
//        }
//
//        public Idea get(Termed t) {
//            return get(Idea.getKey(t.getTerm()));
//        }
//
//        public Idea update(Concept c) {
//            Idea existing = get(c);
//            if (existing != null) {
//                existing.update();
//            }
//            return existing;
//        }
//
//        public Idea add(Concept c) {
//            Idea existing = get(c);
//            if (existing == null) {
//                existing = new Idea(c);
//                put(Idea.getKey(c), existing); //calculating getKey() twice can be avoided by caching it when it's uesd to get Idea existing above
//            }
//            else {
//                existing.add(c);
//            }
//            return existing;
//        }
//
//        public Idea remove(Concept c) {
//            Idea existing = get(c);
//            if (existing != null) {
//                existing.remove(c);
//            }
//            return existing;
//        }
//
//    }
//
// }
