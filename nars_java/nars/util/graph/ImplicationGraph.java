package nars.util.graph;

import nars.core.Memory;
import nars.core.NAR;
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


public class ImplicationGraph extends SentenceGraph {

    float minConfidence = 0.01f;

    public ImplicationGraph(NAR nar) {
        this(nar.memory);
    }
    public ImplicationGraph(Memory memory) {
        super(memory);
    }
    
    public static class UniqueInterval extends Interval {
        public final Implication parent;
    
        public UniqueInterval(Implication parent, Interval i) {
            super(i.magnitude, true);    
            this.parent = parent;
        }

        @Override
        public int hashCode() {
            return parent.hashCode() * 37  + super.hashCode();
        }

        @Override
        public boolean equals(Object that) {
            if (that == this) return true;
            
            if (that instanceof UniqueInterval) {
                UniqueInterval ui = (UniqueInterval)that;
                return (ui.parent.equals(parent) && ui.magnitude == magnitude);
            }
            return false;
        }
        
        
    }
    
    @Override
    public boolean add(Sentence s, CompoundTerm ct) {
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
                            Implication imp = Implication.make(prev,a, TemporalRules.ORDER_FORWARD, memory);
                            Sentence impSent = new Sentence(imp, '.', s.truth, s.stamp);
                            addEdge(prev, a, impSent);
                        }
                        prev = a;
                    }
                    addVertex(predicate);
                    
                    Implication impFinal = Implication.make(prev, predicate, TemporalRules.ORDER_FORWARD, memory);
                    addEdge(prev, predicate, new Sentence(impFinal, '.', s.truth, s.stamp));
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
        if (conf > minConfidence)
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
    
    
    
}
