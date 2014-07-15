package nars.graph;

import com.sun.javafx.geom.Edge;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.language.Term;
import nars.storage.ConceptBag;
import org.jgrapht.graph.DirectedMultigraph;

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
        void onLevel(NARGraph g, int l);

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

    public enum NAREdge {
        TermBelief, TermQuestion, TermContent, TermDerivation
    }
    
    public NARGraph() {
        super(Object.class);

    }

    public void add(NAR n, Filter filter, Graphize graphize) {
        graphize.onTime(this, n.getTime());

        ConceptBag bag = n.memory.concepts;

        for (int level = bag.levels - 1; level >= 0; level--) {

            if (!filter.includeLevel(level)) continue;

            graphize.onLevel(this, level);

            if (!bag.emptyLevel(level)) {

                for (final Concept c : bag.getLevel(level)) {

                    if (!filter.includeConcept(c)) continue;
                    
                    graphize.onConcept(this, c);
                }

            }
        }
        
        graphize.onFinish(this);

    }

    public static class DefaultGraphizer implements Graphize {
        private final boolean includeBeliefs;
        private final boolean includeQuestions;


        private Set<Term> terms = new HashSet();
        private Map<Sentence,Term> sentenceTerms = new HashMap();
        
        private final boolean includeTermContent;
        private final boolean includeDerivations;
        
        public DefaultGraphizer(boolean includeBeliefs, boolean includeDerivations, boolean includeQuestions, boolean includeTermContent) {
            this.includeBeliefs = includeBeliefs;
            this.includeQuestions = includeQuestions;
            this.includeTermContent = includeTermContent;
            this.includeDerivations = includeDerivations;
        }
        
        @Override
        public void onTime(NARGraph g, long time) {
            terms.clear();
            sentenceTerms.clear();
        }

        @Override
        public void onLevel(NARGraph g, int l) {

        }

        protected void addTerm(NARGraph g, Term t) {
            if (terms.add(t))
                g.addVertex(t);
        }
        
        @Override
        public void onConcept(NARGraph g, Concept c) {

            final Term term = c.getTerm();
            addTerm(g, term);

            if (includeBeliefs) {
                for (final Sentence kb : c.beliefs) {
                    sentenceTerms.put(kb, term);
                    //TODO check if kb.getContent() is never distinct from c.getTerm()
                    addTerm(g, kb.getContent());
                    
                    g.addVertex(kb);
                    g.addEdge(term, kb, NAREdge.TermBelief);
                }
            }
            
            if (includeQuestions) {
                for (final Task q : c.getQuestions()) {
                    addTerm(g, q.getContent());
                    
                    //TODO q.getParentBelief()
                    //TODO q.getParentTask()
                    
                            
                    g.addVertex(q);                    
                    g.addEdge(term, q, NAREdge.TermQuestion);                   
                }
            }
            
        }
        
        public void onFinish(NARGraph g) {
            if (includeTermContent) {
                for (final Term a : terms) {

                      for (final Term b : terms) {
                          if (a == b) continue;

                          if (a.containTerm(b)) {
                              g.addEdge(a, b, NAREdge.TermContent);
                          }
                      }
                  }            

            }
            
            if (includeDerivations && includeBeliefs) {
                for (final Entry<Sentence,Term> s : sentenceTerms.entrySet()) {
                    
                    final List<Term> deriv = s.getKey().getStamp().getChain();
                    final Term elem1 = s.getValue();

                    for (final Entry<Sentence,Term> t : sentenceTerms.entrySet()) {
                        if (s == t) continue;

                        final Sentence ts = t.getKey();
                        final Term elem2 = t.getValue();

                        if (deriv.contains(ts.getContent())) {
                            g.addEdge(elem1, elem2, NAREdge.TermDerivation); 
                        }

                    }
                }                
            }
        }
        
    }
}
