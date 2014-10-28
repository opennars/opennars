package nars.util.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
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
import nars.language.Variables;
import nars.operator.Operation;
import nars.util.graph.ImplicationGraph.Cause;



public class ImplicationGraph extends SentenceGraph<Cause> {

    public static class Cause {
        public final Term cause;
        public final Term effect;
        public final Sentence parent;
        
        /** strength below which an item will be removed */
        public final double minStrength = 0.01;
        
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
    
    

    float minConfidence = 0.1f;
    

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
                
                if (s.getTruth()!=null)
                    if (s.getTruth().getExpectation() < minPriority)
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
        private final String id;
        
        public UniqueOperation(Implication parent, Operation o, Term previous) {
            super(o.term);
            if (previous != null)
                this.id = parent.name() +"/"+ previous.name() + "/" + o.name();
            else
                this.id = parent.name() +"/"+ o.name();
            this.parent = parent;
        }

        @Override
        public CharSequence name() {
            return id;
        }
        
        @Override
        protected CharSequence makeName() {
            return id;
        }
        
        @Override public boolean equals(final Object that) {
            if (that == this) return true;           
            if (that instanceof UniqueOperation)
                return (((UniqueOperation)that).id.equals(id));            
            return false;
        }        
        
    }
    
    public static class UniqueInterval extends Interval {
        
        public final Implication parent;        
        private final String id;
    
        public UniqueInterval(Implication parent, Term previous, Interval i) {
            super(i.magnitude, true);    
            this.id = parent.name() + "/" + (previous!=null ? previous.name() : "") + "/" + i.name();
            this.parent = parent;
        }
        
        @Override public CharSequence name() {  return id;        }
    
        @Override public boolean equals(final Object that) {
            if (that == this) return true;           
            if (that instanceof UniqueInterval)
                return (((UniqueInterval)that).id.equals(id));            
            return false;
        }        
        
    }
    
    public static class PostCondition extends Negation {
        public PostCondition(final Term t) {
            super(t);
            setName("~" + t.name());
        }

        
        //fast override to avoid unnecessary calculations
        @Override protected boolean setName(CharSequence name) {
            this.name = name;
            return true;
        }

        //fast override to avoid unnecessary calculations
        @Override protected short calcComplexity() {
            return term[0].getComplexity();
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
        
        if(Variables.containVarIndep(s.content.toString())) {
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
    
        @Override
        protected boolean calcContainedVariables() { return false; }            
        @Override
        protected short calcComplexity() { return -1; }
    }
    
    public Cause newImplicationEdge(final Term source, final Term target, Item i, final Sentence parent) {
        if (source.equals(target))
            return null;
                    
        Cause c = new Cause(source, target, parent);
        addEdge(source, target, c);
        addComponents(parent, c);
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
