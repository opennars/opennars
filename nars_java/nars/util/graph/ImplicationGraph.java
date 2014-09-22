package nars.util.graph;

import nars.core.Memory;
import nars.core.NAR;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Term;
import nars.operator.Operator;

/**
 *
 * @author me
 */


public class ImplicationGraph extends SentenceItemGraph {

    float minConfidence = 0.1f;
    float minFreq = 0.6f;

    public ImplicationGraph(NAR nar) {
        this(nar.memory);
    }
    public ImplicationGraph(Memory memory) {
        super(memory);
    }
    
    public static class UniqueInterval extends Interval {
        private static int _id = 0;
        
        public final Implication parent;
        private final int id;
    
        public UniqueInterval(Implication parent, Interval i) {
            super(i.magnitude, true);    
            this.parent = parent;
            this.id = _id++;
        }

        @Override
        public int hashCode() {
            return id + parent.hashCode() * 37  + super.hashCode();
        }

        @Override
        public boolean equals(Object that) {
            if (that == this) return true;
            
            if (that instanceof UniqueInterval) {
                UniqueInterval ui = (UniqueInterval)that;
                return (ui.id == id && ui.parent.equals(parent) && ui.magnitude == magnitude);
            }
            return false;
        }
        
        
    }
    
    @Override
    public boolean add(Sentence s, CompoundTerm ct, Item c) {
//        if (ct.operator() == NativeOperator.SEQUENCE) {
//            Conjunction c = (Conjunction)ct;
//            System.out.println(c);
//            return true;
//        }
        if (ct instanceof Implication) {
            Implication st = (Implication)ct;
            Term subject, predicate;
            if (st.operator() == NativeOperator.IMPLICATION_BEFORE) {
                //reverse temporal order
                subject = st.getPredicate();
                predicate = st.getSubject();            
            }
            else {
                subject = st.getSubject();
                predicate = st.getPredicate();            
            }            
            
            if (subject instanceof Conjunction) {
                Conjunction seq = (Conjunction)subject;
                if (seq.operator() == Symbols.NativeOperator.SEQUENCE) {
                    Term prev = null;
                    for (Term a : seq.term) {
                        
                        if (!((a instanceof Operator) || (a instanceof Interval))) {
                            //..
                        }
                        if (a instanceof Interval) {
                            a = new UniqueInterval(st, (Interval)a);
                        }
                        
                        addVertex(a);
                        if (prev!=null) {
                            Implication imp = new Implication(prev, a, TemporalRules.ORDER_FORWARD);
                            Sentence impSent = new Sentence(imp, '.', s.truth, s.stamp);
                            addEdge(prev, a, impSent);
                            concepts.put(impSent, c);
                        }
                        prev = a;
                    }
                    addVertex(predicate);
                    
                    Implication impFinal = new Implication(prev, predicate, TemporalRules.ORDER_FORWARD);                    
                    Sentence impFinalSentence = new Sentence(impFinal, '.', s.truth, s.stamp);
                    addEdge(prev, predicate, impFinalSentence);
                    concepts.put(impFinalSentence, c);
                    return true;
                }
            }
            else if (predicate instanceof Conjunction) {
                //TODO?
            }
                        
            addVertex(subject);
            addVertex(predicate);
            addEdge(subject, predicate, s);
            return true;
        }
        return false;
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
        float freq = e.truth.getFrequency();
        float conf = e.truth.getConfidence();        
        float conceptPriority = concepts.get(e).getPriority();
        //weight = cost = distance
        return 1.0 / (freq * conf * conceptPriority);
    }
    
    
    
}
