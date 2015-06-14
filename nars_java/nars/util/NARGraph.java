package nars.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
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

    @Override
    public Object clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * determines which NARS term can result in added graph features
     */
    public static interface Filter {

        boolean includePriority(float l);

        boolean includeConcept(Concept c);
    }

    
    public final static Filter IncludeEverything = new Filter() {
        @Override public boolean includePriority(float l) { return true;  }
        @Override public boolean includeConcept(Concept c) { return true;  }
    };
    public final static class ExcludeBelowPriority implements Filter { 

        final float thresh;
        
        public ExcludeBelowPriority(float l) { this.thresh = l;         }        
        @Override public boolean includePriority(float l) { return l >= thresh;  }
        @Override public boolean includeConcept(Concept c) { return true;  }
    }

    /**
     * creates graph features from NARS term
     */
    public static interface Graphize {

        /**
         * called at beginning of operation
         * @param g
         * @param time 
         */
        void onTime(NARGraph g, long time);


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

        @Override
        public Object getSource() { return super.getSource();         }
        @Override
        public Object getTarget() { return super.getTarget();         }

        @Override
        public Object clone() {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
            

    public static class TermBelief extends NAREdge  {
        @Override public String toString() { return "belief"; }        

        @Override
        public Object clone() {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }
    public static class TermQuestion extends NAREdge  {
        @Override public String toString() { return "question"; }        

        @Override
        public Object clone() {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }
    public static class TermDerivation extends NAREdge  {
        @Override public String toString() { return "derives"; }

        @Override
        public Object clone() {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }
    public static class TermContent extends NAREdge  {
        @Override public String toString() { return "has"; }        

        @Override
        public Object clone() {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }
    public static class TermType extends NAREdge  {
        @Override public String toString() { return "type"; }        

        @Override
        public Object clone() {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }
    public static class SentenceContent extends NAREdge  {
        @Override public String toString() { return "sentence"; }

        @Override
        public Object clone() {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }
        
    
    public NARGraph() {
        super(DefaultEdge.class);
    }

    public List<Concept> currentLevel = new ArrayList();
    
    public void add(NAR n, Filter filter, Graphize graphize) {
        graphize.onTime(this, n.getTime());

        //TODO support AbstractBag
        Collection<? extends Concept> cc = n.memory.getConcepts();

        for (Concept c : cc) {
            
            //TODO use more efficient iterator so that the entire list does not need to be traversed when excluding ranges
            
            float p = c.getPriority();
            
            if (!filter.includePriority(p)) continue;

            //graphize.preLevel(this, p);


            if (!filter.includeConcept(c)) continue;

            graphize.onConcept(this, c);

            //graphize.postLevel(this, level);
            
        }
        
        graphize.onFinish(this);

    }

    public boolean addEdge(Object sourceVertex, Object targetVertex, NAREdge e) {
        return addEdge(sourceVertex,targetVertex,e,false);
    }

    public boolean addEdge(Object sourceVertex, Object targetVertex, NAREdge e, boolean allowMultiple) {
        if (!allowMultiple) {
            Set existing = getAllEdges(sourceVertex, targetVertex);       
            if (existing != null)                
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


        public final Set<Term> terms = new HashSet();
        public final Map<Sentence,Term> sentenceTerms = new HashMap();
        
        private final boolean includeTermContent;
        private final boolean includeDerivations;
        private int includeSyntax; //how many recursive levels to decompose per Term
        
        public DefaultGraphizer(boolean includeBeliefs, boolean includeDerivations, boolean includeQuestions, boolean includeTermContent, boolean includeSyntax) {
            this(includeBeliefs, includeDerivations, includeQuestions, includeTermContent, includeSyntax ? 2 : 0);
        }
        
        public DefaultGraphizer(boolean includeBeliefs, boolean includeDerivations, boolean includeQuestions, boolean includeTermContent, int includeSyntax) {
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

            final Term term = c.term;
            addTerm(g, term);

            if (includeBeliefs) {
                for (final Sentence belief : c.beliefs) {
                    onBelief(belief);

                    sentenceTerms.put(belief, term);
                    g.addVertex(belief);
                    g.addEdge(belief, term, new SentenceContent());
                    
                    //TODO extract to onBelief                    

                    
                    //TODO check if kb.getContent() is never distinct from c.getTerm()
                    if (term.equals(belief.content))
                        continue;
                    
                    addTerm(g, belief.content);                    
                    g.addEdge(term, belief.content, new TermBelief());                    
                }
            }
            
            
            if (includeQuestions) {
                for (final Task q : c.getQuestions()) {
                    if (term.equals(q.getContent()))
                        continue;
                    
                    //TODO extract to onQuestion
                    
                    addTerm(g, q.getContent());
                    
                    //TODO q.getParentBelief()
                    //TODO q.getParentTask()                    
                            
                    g.addEdge(term, q.getContent(), new TermQuestion());
                    
                    onQuestion(q);
                }
            }
            
        }
        
        void recurseTermComponents(NARGraph g, CompoundTerm c, int level) {

            for (Term b : c.term) {
                if (!g.containsVertex(b))
                    g.addVertex(b);
                
                if (!includeTermContent)
                    g.addEdge(c, b, new TermContent());

                if ((level > 1) && (b instanceof CompoundTerm)) {                
                    recurseTermComponents(g, (CompoundTerm)b, level-1);
                }
            }
        }
        
        @Override
        public void onFinish(NARGraph g) {
            if (includeSyntax > 0) {
                for (final Term a : terms) {
                    if (a instanceof CompoundTerm) {
                        CompoundTerm c = (CompoundTerm)a;
                        g.addVertex(c.operator());
                        g.addEdge(c.operator(), c, new TermType());            

                        if (includeSyntax-1 > 0)
                            recurseTermComponents(g, c, includeSyntax-1);
                    }
                  }            

            }
                        
            if (includeTermContent) {
                for (final Term a : terms) {

                      for (final Term b : terms) {
                          if (a == b) continue;

                          if (a.containsTerm(b)) {
                              g.addEdge(a, b, new TermContent());
                          }
                          if (b.containsTerm(a)) {
                              g.addEdge(b, a, new TermContent());
                          }
                      }
                  }            

            }
            
            if (includeDerivations && includeBeliefs) {
                for (final Entry<Sentence,Term> s : sentenceTerms.entrySet()) {
                    
                    final Collection<Term> schain = s.getKey().stamp.getChain();
                    final Term derived = s.getValue();

                    for (final Entry<Sentence,Term> t : sentenceTerms.entrySet()) {
                        if (s == t) continue;

                        final Term deriver = t.getValue();
                        if (derived==deriver) //avoid loops
                            continue;

                        final Collection<Term> tchain = s.getKey().stamp.getChain();                        
                        final Sentence deriverSentence = t.getKey();
                        
                        if (schain.contains(deriverSentence.content)) {
                            g.addEdge(deriver, derived, new TermDerivation());
                        }
                        if (tchain.contains(derived)) {
                            g.addEdge(derived, deriver, new TermDerivation());
                        }
                    }
                }                
            }
        }

        public void setShowSyntax(boolean showSyntax) {
            this.includeSyntax = showSyntax ? 1 : 0;
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
