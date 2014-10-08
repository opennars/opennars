package nars.util.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nars.core.Memory;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.entity.Stamp;
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



public class ImplicationGraph extends SentenceItemGraph {

    Map<Sentence, List<Sentence>> components = new HashMap();
    

    public static final double DEACTIVATED_EDGE_WEIGHT = 10000f;
    
    /** threshold for an edge to be active; 0 will include all edges to some degree */
    double minEdgeStrength = 0.1;
    
    float minConfidence = 0.1f;
    float minFreq = 0.1f;
    
    /** how much a completely dormant concept's priority will contribute to the weight calculation.
     *  any value between 0 and 1.0 is valid.  
     *  factor = dormantConceptInfluence + (1.0 - dormantConceptInfluence) * concept.priority  */
    float dormantConceptInfluence = 0.1f; 

    public ImplicationGraph(NAR nar) {
        this(nar.memory);
    }
    public ImplicationGraph(Memory memory) {
        super(memory);
    }
    
    /** creates a clone, optionally with or without preconditions */
    public ImplicationGraph(ImplicationGraph g, boolean includePrecondition) {
        super(null);
        if (includePrecondition) {
            throw new RuntimeException("Use .clone()");
        }
        else {
            for (Term s : g.vertexSet()) {
                if ((s instanceof Interval) || (s instanceof Operation) || (s instanceof PostCondition))
                    super.addVertex(s);                    
            }
            for (Sentence s : g.edgeSet()) {
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
        private final String id;
        
        public UniqueOperation(Implication parent, Operation o, Term previous) {
            super(o.name(), o.term);
            if (previous != null)
                this.id = parent.name() +"/"+ previous.name() + "/" + o.name();
            else
                this.id = parent.name() +"/"+ o.name();
            this.parent = parent;
        }

 
        
        @Override public int hashCode() {  return id.hashCode();        }

        @Override public boolean equals(Object that) {
            if (that == this) return true;
            
            if (that instanceof UniqueOperation)
                return (((UniqueOperation)that).id.equals(id));
            
            return false;
        }
        
    }
    
    public static class UniqueInterval extends Interval {
        
        public final Implication parent;        
        private final String id;
    
        public UniqueInterval(Implication parent, Interval i, Term previous) {
            super(i.magnitude, true);    
            this.id = parent.name() + "/" + previous.name() + "/" + i.name();
            this.parent = parent;
        }

        @Override public int hashCode() {  return id.hashCode();        }

        @Override public boolean equals(Object that) {
            if (that == this) return true;
            
            if (that instanceof UniqueInterval)
                return (((UniqueInterval)that).id.equals(id));
            
            return false;
        }        
        
    }
    
    public static class PostCondition extends Negation {
        public PostCondition(final Term t) {
            super("~" + t.name(), t);
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
    
    @Override
    public boolean add(final Sentence s, final CompoundTerm ct, final Item c, boolean specialAdd) {

        
        if (!(ct instanceof Implication)) {
            return false;
        }
        
        final Implication st = (Implication)ct;
        
        if ((st.getTemporalOrder() == TemporalRules.ORDER_NONE) || (st.operator() == NativeOperator.IMPLICATION_BEFORE))
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
        final Term predicatePost = new PostCondition(predicatePre);
        
        addVertex(predicatePre);
        addVertex(predicatePost);
        
        

        if (subject instanceof Conjunction) {
            Conjunction seq = (Conjunction)subject;
            if (seq.operator() == Symbols.NativeOperator.SEQUENCE) {
                Term prev = predicatePre;
                boolean addedNonInterval = false;
                
                
                /*List<Term>al = Arrays.asList(seq.term);
                if (reverse)
                     Collections.reverse(al);*/
                
                
                for (int i = 0; i < seq.term.length; i++) {
                    
                    Term a = seq.term[i];
                    
                    if (Executive.isPlanTerm(a)) {
                        //make a unique Term if an Interval or if an Operation in the middle of a sequence
                        if (((i > 0) && (i < seq.term.length-1)) || (a instanceof Interval))
                            a = newExecutableVertex(st, a, prev);
                        else {
                            addVertex(a);
                        }
                            
                        if (prev!=null)  {
                            newImplicationEdge(prev, a, c, s);
                        }
                        
                        if (!(a instanceof Interval))
                            addedNonInterval = true;
                        
                        prev = a;
                    }
                    else {
                        //separate the term into a disconnected pre and post condition
                        Term aPre = a;
                        Term aPost = new PostCondition(a);
                        addVertex(aPre);
                        addVertex(aPost);
                        
                        addVertex(prev);
                        newImplicationEdge(prev, aPre, c, s); //leading edge from previous only         
                        if (!(a instanceof Interval))
                            addedNonInterval = true;                   
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
        }

        return true;
    }
    
    protected void addComponents(final Sentence parentSentence, final Sentence edge) {
        List<Sentence> componentList = components.get(parentSentence);
        if (componentList == null) {
            componentList = new ArrayList(1);
            components.put(parentSentence, componentList);
        }
        componentList.add(edge);        
    }
    
    protected boolean removeComponents(final Sentence parentSentence) {
        List<Sentence> componentList = components.get(parentSentence);
        if (componentList!=null) {
            for (Sentence s : componentList) {
                if (!containsEdge(s))
                    continue;
                Term source = getEdgeSource(s);
                Term target = getEdgeTarget(s);
                removeEdge(s);
                ensureTermConnected(source);
                ensureTermConnected(target);
            }
            componentList.clear();
            components.remove(parentSentence);        
            return true;
        }
        return false;
    }
    

    public Term newExecutableVertex(Implication st, Term t, Term prev) {
        Term r;
        if (t instanceof Operation) {
            r = new UniqueOperation(st, (Operation)t, prev);
        }
        else if (t instanceof Interval) {
            r = new UniqueInterval(st, (Interval)t, prev);
        }
        else
            throw new RuntimeException("Not executable vertex: " + t);
        
        addVertex(r);
        return r;
    }
    
    public Sentence newImplicationEdge(final Term source, final Term target, final Item c, final Sentence parent) {
        Implication impParent = (Implication)parent.content;
        Implication impFinal = new Implication(source, target, impParent.getTemporalOrder());                    
        //System.out.println("new impl edge: " + impFinal + " " + parent.truth + " , parent=" + parent);
        
        Sentence impFinalSentence = new Sentence(impFinal, '.', parent.truth, parent.stamp);
        
//        try {
            addEdge(source, target, impFinalSentence);
            concepts.put(impFinalSentence, c);
//        }
//        catch (IllegalArgumentException e) {
//            //throw new RuntimeException(this + " Unable to create edge: source=" + source + ", target=" + target);
//            return null;
//        }
  
        addComponents(parent, impFinalSentence);
        return impFinalSentence;
    }
    
    
    @Override
    public boolean allow(final Sentence s) {        
        if(s.stamp.getOccurrenceTime()!=Stamp.ETERNAL) {
            return false;
        }
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
        else {
            //System.err.println("ImplicationGraph disallow " + st);
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

    /** returns (no relevancy) 0..1.0 (high relevancy) */
    public double getSentenceRelevancy(final Sentence e) {
        if (!containsEdge(e))
            return 0;
        
        //transitions to PostCondition vertices are free or low-cost        
        if (getEdgeTarget(e) instanceof PostCondition) {
            return 1.0;
        }
        
        Concept c=memory.concept(e.content);
        double strength = 0;
        if(c!=null) {
            strength*=c.getPriority() * e.truth.getExpectation();
        }
        
        return strength;
    }

    @Override
    public boolean remove(final Sentence s) {
        if (!removeComponents(s))
            return false;
        return super.remove(s);
    }
    
    
    
    
}
