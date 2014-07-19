package nars.graph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.transform.TransformerConfigurationException;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.language.CompoundTerm;
import nars.language.Term;
import nars.storage.ConceptBag;
import org.jgrapht.ext.GmlExporter;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringEdgeNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.xml.sax.SAXException;

/**
 * Stores the contents of some, all, or of multiple NAR memory snapshots.
 *
 * @author me
 */
public class NARGraph extends DirectedMultigraph {

    /**
     * determines which NARS components can result in added graph features
     */
    public static interface Filter {

        boolean includeLevel(int l);

        boolean includeConcept(Concept c);
    }

    
    public final static Filter IncludeEverything = new Filter() {
        @Override public boolean includeLevel(int l) { return true;  }
        @Override public boolean includeConcept(Concept c) { return true;  }
    };
    public final static class ExcludeLevelsBelow implements Filter { 

        final int thresh;
        
        public ExcludeLevelsBelow(int l) { this.thresh = l;         }        
        @Override public boolean includeLevel(int l) { return l >= thresh;  }
        @Override public boolean includeConcept(Concept c) { return true;  }
    };
            
    /**
     * creates graph features from NARS components
     */
    public static interface Graphize {

        /**
         * called at beginning of operation
         * @param g
         * @param time 
         */
        void onTime(NARGraph g, long time);

        /**
         * called at beginning of each level
         * @param g
         * @param l 
         */
        void preLevel(NARGraph g, int l);
        
        /**
         * called at end of each level
         * @param g
         * @param l 
         */
        void postLevel(NARGraph g, int l);

        /**
         * called per concept
         * @param g
         * @param c 
         */
        void onConcept(NARGraph g, Concept c);
        
        /**
         * called at end of operation
         * @param g 
         */
        void onFinish(NARGraph g);
    }
    
    public static class NAREdge extends DefaultEdge {
        
    }
            

    public static class TermBelief extends NAREdge  {
        @Override public String toString() { return "belief"; }        
        
        
        
    }
    public static class TermQuestion extends NAREdge  {
        @Override public String toString() { return "question"; }        
    }
    public static class TermDerivation extends NAREdge  {
        @Override public String toString() { return "derives"; }
    }
    public static class TermContent extends NAREdge  {
        @Override public String toString() { return "has"; }        
    }
    public static class TermType extends NAREdge  {
        @Override public String toString() { return "type"; }        
    }
        
    
    public NARGraph() {
        super(DefaultEdge.class);
    }

    public List<Concept> currentLevel = new ArrayList();
    
    public void add(NAR n, Filter filter, Graphize graphize) {
        graphize.onTime(this, n.getTime());

        ConceptBag bag = n.memory.concepts;

        for (int level = bag.levels - 1; level >= 0; level--) {

            if (!filter.includeLevel(level)) continue;

            graphize.preLevel(this, level);

            if (!bag.emptyLevel(level)) {

                currentLevel.clear();
                currentLevel.addAll(bag.getLevel(level));
                
                for (final Concept c : currentLevel) {

                    if (!filter.includeConcept(c)) continue;
                    
                    if (c!=null)
                        graphize.onConcept(this, c);
                }

            }
            
            graphize.postLevel(this, level);
            
        }
        
        graphize.onFinish(this);

    }

    public boolean addEdge(Object sourceVertex, Object targetVertex, NAREdge e) {
        return addEdge(sourceVertex,targetVertex,e,false);
    }

    public boolean addEdge(Object sourceVertex, Object targetVertex, NAREdge e, boolean allowMultiple) {
        if (!allowMultiple) {
            Set existing = getAllEdges(sourceVertex, targetVertex);       
            for (Object o : existing) {
                if (o.getClass() == e.getClass()) {
                    return false;
                }
            }
        }
        
        return super.addEdge(sourceVertex, targetVertex, e);
    }
    
    

    public static class DefaultGraphizer implements Graphize {
        private final boolean includeBeliefs;
        private final boolean includeQuestions;


        private Set<Term> terms = new HashSet();
        private Map<Sentence,Term> sentenceTerms = new HashMap();
        
        private final boolean includeTermContent;
        private final boolean includeDerivations;
        private final boolean includeSyntax;
        
        public DefaultGraphizer(boolean includeBeliefs, boolean includeDerivations, boolean includeQuestions, boolean includeTermContent, boolean includeSyntax) {
            this.includeBeliefs = includeBeliefs;
            this.includeQuestions = includeQuestions;
            this.includeTermContent = includeTermContent;
            this.includeDerivations = includeDerivations;
            this.includeSyntax = includeSyntax;
        }
        
        @Override
        public void onTime(NARGraph g, long time) {
            terms.clear();
            sentenceTerms.clear();
        }

        @Override
        public void preLevel(NARGraph g, int l) {
        }

        @Override
        public void postLevel(NARGraph g, int l) {
        }
        

        protected void addTerm(NARGraph g, Term t) {
            if (terms.add(t)) {
                g.addVertex(t);
                onTerm(t);
            }
        }
        
        public void onTerm(Term t) {
            
        }
        
        public void onBelief(Sentence kb) {
            
        }

        public void onQuestion(Task q) {
            
        }
        
        @Override
        public void onConcept(NARGraph g, Concept c) {

            final Term term = c.getTerm();
            addTerm(g, term);

            if (includeBeliefs) {
                for (final Sentence kb : c.beliefs) {
                    //TODO extract to onBelief
                    
                    sentenceTerms.put(kb, term);
                    //TODO check if kb.getContent() is never distinct from c.getTerm()
                    addTerm(g, kb.getContent());
                    
                    g.addVertex(kb);
                    g.addEdge(term, kb, new TermBelief());
                    
                    onBelief(kb);
                }
            }
            
            if (includeQuestions) {
                for (final Task q : c.getQuestions()) {
                    //TODO extract to onQuestion
                    
                    addTerm(g, q.getContent());
                    
                    //TODO q.getParentBelief()
                    //TODO q.getParentTask()
                    
                            
                    g.addVertex(q);                    
                    g.addEdge(term, q, new TermQuestion());
                    
                    onQuestion(q);
                }
            }
            
        }
        
        public void onFinish(NARGraph g) {
            if (includeSyntax) {
                for (final Term a : terms) {
                    if (a instanceof CompoundTerm) {
                        CompoundTerm c = (CompoundTerm)a;
                        
                        g.addVertex(c.operator());
                        g.addEdge(c.operator(), c, new TermType());
                        
                        for (Term b : c.getComponents()) {
                            if (!g.containsVertex(b))
                                g.addVertex(b);
                            g.addEdge(c, b, new TermContent());
                        }
                        
                    }
                  }            

            }
                        
            if (includeTermContent) {
                for (final Term a : terms) {

                      for (final Term b : terms) {
                          if (a == b) continue;

                          if (a.containTerm(b)) {
                              g.addEdge(a, b, new TermContent());
                          }
                      }
                  }            

            }
            
            if (includeDerivations && includeBeliefs) {
                for (final Entry<Sentence,Term> s : sentenceTerms.entrySet()) {
                    
                    final List<Term> chain = s.getKey().getStamp().getChain();
                    final Term derived = s.getValue();

                    for (final Entry<Sentence,Term> t : sentenceTerms.entrySet()) {
                        if (s == t) continue;

                        final Sentence deriverSentence = t.getKey();
                        final Term deriver = t.getValue();

                        if (chain.contains(deriverSentence.getContent())) {
                            if (derived!=deriver) //avoid loops
                                g.addEdge(deriver, derived, new TermDerivation());
                        }

                    }
                }                
            }
        }

        
    }
    
    public void toGraphML(Writer writer) throws SAXException, TransformerConfigurationException {
        GraphMLExporter gme = new GraphMLExporter(new IntegerNameProvider(), new StringNameProvider(), new IntegerEdgeNameProvider(), new StringEdgeNameProvider());
        gme.export(writer, this);
    }
    
    public void toGraphML(String outputFile) throws SAXException, TransformerConfigurationException, IOException {
        toGraphML(new FileWriter(outputFile, false));
    }

    public void toGML(Writer writer)  {
        GmlExporter gme = new GmlExporter(new IntegerNameProvider(), new StringNameProvider(), new IntegerEdgeNameProvider(), new StringEdgeNameProvider());
        gme.setPrintLabels(GmlExporter.PRINT_EDGE_VERTEX_LABELS);
        gme.export(writer, this);
    }
    
    public void toGML(String outputFile) throws IOException  {
        toGML(new FileWriter(outputFile, false));
    }    
    
}
