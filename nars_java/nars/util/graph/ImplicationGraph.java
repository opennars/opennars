package nars.util.graph;

import java.util.Objects;
import nars.core.Memory;
import nars.core.NAR;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.TruthValue;
import nars.inference.Executive;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Negation;
import nars.language.Term;
import nars.operator.Operation;
import nars.util.graph.ImplicationGraph.Cause;



public class ImplicationGraph extends SentenceGraph<Cause> {

    float minConfidence = 0.25f;
    
    public static class Cause {
        public final Term cause;
        public final Term effect;
        public final Sentence parent;
        
//        /** strength below which an item will be removed */
//        public final double minStrength = 0.01;
        
        private double activity = 0;
        
        private final int hash;
        

        public Cause(Term cause, Term effect, Sentence parent) {
            this.cause = cause;
            this.effect = effect;
            this.parent = parent;
            this.hash = Objects.hash(cause, effect, parent);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Cause) {
                Cause c = (Cause)obj;
                if (c.hash!=hash)
                    return false;
                
                return (c.cause.equals(cause)) && (c.effect.equals(effect)) && (c.parent.equals(parent));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hash;
        }
        
        public double getActivity() { return activity; }
        public double addActivity(double a) { activity += a; return activity; }
        public double multActivity(double m) { activity *= m; return activity; }
        
        
        
        public TruthValue getTruth() { return parent.truth; }
        
        public Stamp getStamp() { return parent.stamp; }
     
        public int getTemporalOrder() {
            return getImplication().getTemporalOrder();
        }
        
        public Implication getImplication() {
            return (Implication)parent.content;
        }

        @Override
        public String toString() {
            return cause + " =/> " + effect /* +  " [" + relevancy + "] in " + parent*/;
        }

        
    }
    
    

    
    

    public ImplicationGraph(NAR nar) {
        this(nar.memory);
    }
    public ImplicationGraph(Memory memory) {
        super(memory);
    }
    
    /** creates a clone, optionally with or without preconditions */
    public ImplicationGraph(ImplicationGraph g, boolean includePrecondition, double minPriority) {
        super(null);
        if (includePrecondition) {
            throw new RuntimeException("Use .clone()");
        }
        else {
            for (Term s : g.vertexSet()) {
                if ((s instanceof Interval) || (s instanceof Operation) || (s instanceof PostCondition))
                    super.addVertex(s);                    
            }
            for (Cause s : g.edgeSet()) {
                Term src = g.getEdgeSource(s);
                Term tgt = g.getEdgeTarget(s);
                
                boolean containsSrc = containsVertex(src);
                boolean containsTgt = containsVertex(tgt);
                
                //if both are precondition nodes, skip the edge
                if (!containsSrc && !containsTgt)
                    continue;
                
                if (!containsSrc) super.addVertex(src);
                if (!containsTgt) super.addVertex(tgt);
                    
                super.addEdge(src, tgt, s);
            }
        }
        //System.out.println(g.vertexSet().size() + ":" + vertexSet().size() + "," + g.edgeSet().size() + ":" + edgeSet().size());
    }
    
    public static class UniqueOperation extends Operation {
        
        public final Implication parent;        
        private final Term previous;
        private final int hash;
        
        
        public UniqueOperation(Implication parent, Operation o, Term previous) {
            super(o.term);
            this.previous = previous;            
            this.parent = parent;
            this.hash = Objects.hash(previous, parent, o.term);
            init(o.term);
        }

        
        @Override public boolean equals(final Object that) {
            if (that == this) return true;
            if (hashCode()!=that.hashCode()) return false;
            
            if (that instanceof UniqueOperation) {
                UniqueOperation u = (UniqueOperation)that;
                if (!u.parent.equals(parent)) return false;
                if (!Objects.equals(u.previous, previous)) return false;
                return name().equals(u.name());
            }
            return false;
        }        

        @Override
        public int hashCode() {
            return hash;
        }
        
        
    }
    
    public static class UniqueInterval extends Interval {
        
        public final Implication parent;                
        private final Term previous;
        private final int hash;
    
        public UniqueInterval(Implication parent, Term previous, Interval i) {
            super(i.magnitude, true);            
            
            this.previous = previous;
            this.parent = parent;
            this.hash = Objects.hash(i, previous, parent);
        }

        @Override
        public int hashCode() {
            return hash;
        }
        
        @Override public boolean equals(final Object that) {
            if (that == this) return true;           
            if (hashCode()!=that.hashCode()) return false;
            
            if (that instanceof UniqueInterval) {
                UniqueInterval u = (UniqueInterval)that;
                if (magnitude!=u.magnitude) return false;
                if (parent!=u.parent) return false;
                if (!Objects.equals(u.previous, previous)) return false; //handles null value
                return true;
            }
            return false;
        }        
        
    }
    
    public static class PostCondition extends Negation {
        public PostCondition(final Term t) {
            super(t);                
            init(term);
        }

        @Override
        protected CharSequence makeName() {
            return "~" + this.term[0].name();
        }

        @Override
        public boolean equals(Object that) {
            if (!(that instanceof PostCondition)) return false;
            return super.equals(that);
        }

        
    }
    
    protected void meter(Term a) {
        if (a instanceof Interval)
            memory.logic.PLAN_GRAPH_IN_DELAY_MAGNITUDE.commit(((Interval)a).magnitude);
        else if (a instanceof Operation)
            memory.logic.PLAN_GRAPH_IN_OPERATION.commit(1);
        else
            memory.logic.PLAN_GRAPH_IN_OTHER.commit(1);
    }
    
    protected static Term postcondition(Term t) {
        if ((t instanceof Operation) || (t instanceof Interval)) {
            return t;
        }
        return new PostCondition(t);
    }
    
    @Override
    public boolean add(final Sentence s, final CompoundTerm ct, final Item c) {

        
        if (!(ct instanceof Implication)) {
            return false;
        }
        
        final Implication st = (Implication)ct;
        
        if ((st.getTemporalOrder() == TemporalRules.ORDER_NONE) || (st.operator() == NativeOperator.IMPLICATION_BEFORE) || (!s.isEternal()))
            return false;
                
        final Term subject, predicate;
        
        boolean reverse = false;
        if (reverse) {
            return false;
            //reverse temporal order
            //subject = st.getPredicate();
            //predicate = st.getSubject();            
        }
        else {
            subject = st.getSubject();
            predicate = st.getPredicate();            
        }  
        
        if (s.content.hasVarIndep()) {
            return false;
        }
                
        final Term predicatePre = predicate;
        final Term predicatePost = postcondition(predicatePre);
        
        addVertex(predicatePre);
        addVertex(predicatePost);

        if (subject instanceof Conjunction) {
            Conjunction seq = (Conjunction)subject;
            if (seq.operator() == Symbols.NativeOperator.SEQUENCE) {
                
                Term prev = (predicatePre!=predicatePost) ? predicatePre : null;
                if (prev!=null)
                    addVertex(prev);
                                
                for (int i = 0; i < seq.term.length; i++) {
                    
                    Term a = seq.term[i];                    
                    meter(a);                       
                    
                    if (Executive.isPlanTerm(a)) {
                        //make a unique Term if an Interval or if an Operation in the middle of a sequence
                        if (((i > 0) && (i < seq.term.length-1)) || (a instanceof Interval))
                            a = newExecutableVertex(st, a, prev);
                        else {
                            addVertex(a);
                        }

                        if (prev!=null) {
                            newImplicationEdge(prev, a, c, s);                        
                        }                        
                        
                        prev = a;
                    }
                    else {         

                        //separate the term into a disconnected pre and post condition
                        Term aPre = a;
                        Term aPost = postcondition(a);
                        addVertex(aPre);
                        addVertex(aPost);
                        
                        if (prev!=null) {
                            addVertex(prev);
                            newImplicationEdge(prev, aPre, c, s); //leading edge from previous only         
                        }
                        prev = aPost;
                    }

                }

                newImplicationEdge(prev, predicatePost, c, s);
                return true;
            }
            else if (seq.operator() == Symbols.NativeOperator.PARALLEL) {
                //TODO
            }
        }
        else {
            if (Executive.isPlanTerm(subject)) {                
                //newImplicationEdge(predicatePre, subject, c, s);
                //newImplicationEdge(subject, predicatePost, c, s);                                
                addVertex(subject);
                newImplicationEdge(subject, predicatePost, c, s);
            }
            else {
                //separate into pre/post
                PostCondition subjectPost = new PostCondition(subject);
                addVertex(predicatePre);
                addVertex(subject);
                addVertex(subjectPost);
                newImplicationEdge(predicatePre, subject, c, s);
                newImplicationEdge(subjectPost, predicatePost, c, s);
            }
            meter(subject);
        }

        return true;
    }
 

    @Override
    public Term getEdgeSource(final Cause e) {
        return e.cause;
    }

    @Override
    public Term getEdgeTarget(final Cause e) {
        return e.effect;
    }


    public Term newExecutableVertex(Implication st, Term t, Term prev) {
        Term r;
        if (t instanceof Operation) {
            r = new UniqueOperation(st, (Operation)t, prev);
        }
        else if (t instanceof Interval) {
            r = new UniqueInterval(st, prev, (Interval)t);
        }
        else
            throw new RuntimeException("Not executable vertex: " + t);
        
        addVertex(r);
        return r;
    }
    
    /** less costly subclass of Implication */
    public static class LightweightImplication extends Implication {

        public LightweightImplication(Term subject, Term predicate, int order) {
            super(subject, predicate, order);
        }

        
       @Override  protected void init(Term[] components) {
        }    
    }
    
    public Cause newImplicationEdge(final Term source, final Term target, Item i, final Sentence parent) {
        if (source.equals(target))
            return null;

        //System.out.println("cause: " + source +  " -> " + target + " in " + parent);
        
        Cause c = new Cause(source, target, parent);
        try {
            addEdge(source, target, c);
            addComponents(parent, c);
        }
        catch (IllegalArgumentException wc) {
            //"no such vertex in graph"
            return null;
        }
        return c;
    }
    
    
    @Override
    public boolean allow(final Sentence s) {        
        if(s.stamp.getOccurrenceTime()!=Stamp.ETERNAL) {
            return false;
        }
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
        else {
            //System.err.println("ImplicationGraph disallow " + st);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder x = new StringBuilder();
        x.append(getClass().toString()).append("\n");
        x.append("Terms:\n");
        for (Term v : vertexSet())
            x.append("  ").append(v.toString()).append(",");
        x.append("\nImplications:\n");
        for (Cause v : edgeSet())
            x.append("  ").append(v.toString()).append(",");
        x.append("\n\n");
        return x.toString();
    }    
}
