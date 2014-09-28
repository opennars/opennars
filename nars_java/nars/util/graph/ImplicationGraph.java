package nars.util.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nars.core.Memory;
import nars.core.NAR;
import nars.entity.Item;
import nars.entity.Sentence;
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
    
    public static class UniqueInterval extends Interval {
        
        public final Implication parent;        
        private final Term prev;
        private final String id;
    
        public UniqueInterval(Implication parent, Interval i, Term previous) {
            super(i.magnitude, true);    
            this.id = parent.name() + "/" + previous.name() + "/" + i.name();
            this.parent = parent;
            this.prev = previous;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object that) {
            if (that == this) return true;
            
            if (that instanceof UniqueInterval) {
                UniqueInterval ui = (UniqueInterval)that;
                return (ui.id.equals(id));
            }
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
        
        if(!specialAdd) {
            return false;
        }
        
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
                
                for (Term a : seq.term) {

                    if (a instanceof Interval) {
                        if (addedNonInterval) {
                            //eliminate prefix intervals
                            a = new UniqueInterval(st, (Interval)a, prev);
                        }
                    }
                    if (Executive.isPlanTerm(a)) {
                        /*if (!prev.equals(a))*/ 
                        addVertex(a);
                            
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
                addVertex(subject);
                //newImplicationEdge(predicatePre, subject, c, s);
                //newImplicationEdge(subject, predicatePost, c, s);                
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
    

    public Sentence newImplicationEdge(final Term source, final Term target, final Item c, final Sentence parent) {
        Implication impParent = (Implication)parent.content;
        Implication impFinal = new Implication(source, target, impParent.getTemporalOrder());                    
        
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

    /** weight = cost = distance */
    @Override public double getEdgeWeight(Sentence e) {
        if (!containsEdge(e))
            return 0;
        
        //transitions to PostCondition vertices are free or low-cost
        
        Item cc = concepts.get(e);
        double conceptPriority = (cc!=null) ? concepts.get(e).getPriority() : 0;
        conceptPriority = (dormantConceptInfluence + (1.0 - dormantConceptInfluence) * conceptPriority);
        
        if (getEdgeTarget(e) instanceof PostCondition) {
            return 1.0 / conceptPriority;
        }
        
        float freq = e.truth.getFrequency();
        float conf = e.truth.getConfidence();        
        
        double strength = (freq * conf * conceptPriority);
        if (strength > minEdgeStrength)
            return 1.0 / strength;
        else
            return DEACTIVATED_EDGE_WEIGHT;
    }

    @Override
    public boolean remove(final Sentence s) {
        if (!removeComponents(s))
            return false;
        return super.remove(s);
    }
    
    
    
    
}
