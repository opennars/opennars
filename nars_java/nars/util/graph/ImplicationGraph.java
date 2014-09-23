package nars.util.graph;

import nars.core.Memory;
import nars.core.NAR;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.inference.GraphExecutive;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Negation;
import nars.language.Term;



public class ImplicationGraph extends SentenceItemGraph {

    float minConfidence = 0.1f;
    float minFreq = 0.6f;
    
    /** how much a completely dormant concept's priority will contribute to the weight calculation.
     *  any value between 0 and 1.0 is valid.  
     *  factor = dormantConceptInfluence + (1.0 - dormantConceptInfluence) * concept.priority
     */
    float dormantConceptInfluence = 0.1f; 

    public ImplicationGraph(NAR nar) {
        this(nar.memory);
    }
    public ImplicationGraph(Memory memory) {
        super(memory);
    }
    
    public static class UniqueInterval extends Interval {

        
        public final Implication parent;
        private final int order;
    
        public UniqueInterval(Implication parent, Interval i, int order) {
            super(i.magnitude, true);    
            this.parent = parent;
            this.order = order;
        }

        @Override
        public int hashCode() {
            return order + parent.hashCode() * 37  + super.hashCode();
        }

        @Override
        public boolean equals(Object that) {
            if (that == this) return true;
            
            if (that instanceof UniqueInterval) {
                UniqueInterval ui = (UniqueInterval)that;
                return (ui.order == order && ui.parent.equals(parent) && ui.magnitude == magnitude);
            }
            return false;
        }
        
        
    }
    
    public static class PostCondition extends Negation {
        public PostCondition(final Term t) {
            super("~" + t.name(), t);
        }
    }
    
    @Override
    public boolean add(final Sentence s, final CompoundTerm ct, final Item c) {
        if (!(ct instanceof Implication)) {
            return false;
        }
        
        final Implication st = (Implication)ct;
        
        final Term subject, predicate;
        
        if (st.operator() == NativeOperator.IMPLICATION_BEFORE) {
            //reverse temporal order
            subject = st.getPredicate();
            predicate = st.getSubject();            
        }
        else {
            subject = st.getSubject();
            predicate = st.getPredicate();            
        }            

        final Term precondition = predicate;
        final Term postcondition = new PostCondition(precondition);
        addVertex(precondition);
        addVertex(postcondition);

        if (subject instanceof Conjunction) {
            Conjunction seq = (Conjunction)subject;
            if (seq.operator() == Symbols.NativeOperator.SEQUENCE) {
                Term prev = precondition;
                boolean addedNonInterval = false;
                int intervalNum = 0;
                
                for (Term a : seq.term) {


                    if (a instanceof Interval) {
                        if (addedNonInterval) {
                            //eliminate prefix intervals
                            a = new UniqueInterval(st, (Interval)a, intervalNum++);
                        }
                    }
                    if (GraphExecutive.validPlanComponent(a)) {
                        if (!prev.equals(a)) {
                            addVertex(prev);                        
                            addVertex(a);
                            newImplicationEdge(prev, a, c, s);
                            if (!(a instanceof Interval))
                                addedNonInterval = true;
                        }
                        prev = a;
                    }
                    else {
                        //separate the term into a disconnected pre and post condition
                        Term pre = a;
                        Term post = new PostCondition(a);
                        addVertex(pre);
                        addVertex(post);
                        
                        addVertex(prev);
                        newImplicationEdge(prev, pre, c, s); //leading edge from previous only         
                        if (!(a instanceof Interval))
                            addedNonInterval = true;                   
                        prev = post;
                    }

                }

                newImplicationEdge(prev, postcondition, c, s);
                return true;
            }
            else if (seq.operator() == Symbols.NativeOperator.PARALLEL) {
                //TODO
            }
        }
        else {
            if (GraphExecutive.validPlanComponent(subject)) {
                addVertex(subject);
                newImplicationEdge(precondition, subject, c, s);
                newImplicationEdge(subject, postcondition, c, s);                
            }
            else {
                //separate into pre/post
                PostCondition postSubject = new PostCondition(subject);
                addVertex(subject);
                addVertex(postSubject);
                newImplicationEdge(precondition, subject, c, s);
                newImplicationEdge(postSubject, postcondition, c, s);
            }
        }

        return true;
    }

    public Sentence newImplicationEdge(final Term source, final Term target, final Item c, final Sentence parent) {
        Implication impFinal = new Implication(source, target, TemporalRules.ORDER_FORWARD);                    
        Sentence impFinalSentence = new Sentence(impFinal, '.', parent.truth, parent.stamp);
        addEdge(source, target, impFinalSentence);
        concepts.put(impFinalSentence, c);
        
        return impFinalSentence;
    }
    
    
    @Override
    public boolean allow(final Sentence s) {        
        float conf = s.truth.getConfidence();
        float freq = s.truth.getFrequency();
        if ((conf > minConfidence) && (freq > minFreq))
            return true;
        return false;
    }


    @Override
    public boolean allow(final CompoundTerm st) {
        Symbols.NativeOperator o = st.operator();
        if ((o == Symbols.NativeOperator.IMPLICATION_WHEN) || (o == Symbols.NativeOperator.IMPLICATION_BEFORE) || (o == Symbols.NativeOperator.IMPLICATION_AFTER)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder x = new StringBuilder();
        x.append(getClass().toString() + "\n");
        x.append("Terms:\n");
        for (Term v : vertexSet())
            x.append("  " + v.toString() + ",");
        x.append("\nImplications:\n");
        for (Sentence v : edgeSet())
            x.append("  " + v.toString() + ",");
        x.append("\n\n");
        return x.toString();
    }

    @Override
    public double getEdgeWeight(Sentence e) {
        //transitions to PostCondition vertices are free or low-cost
        if (getEdgeTarget(e) instanceof PostCondition)
            return 1.0;
        
        float freq = e.truth.getFrequency();
        float conf = e.truth.getConfidence();        
        float conceptPriority = concepts.get(e).getPriority();
        //weight = cost = distance
        //return 1.0 / (freq * conf * conceptPriority);
        
        return 1.0 / (freq * conf * (dormantConceptInfluence + (1.0 - dormantConceptInfluence) * conceptPriority));
    }
    
    
    
}
