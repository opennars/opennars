/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rl;

import automenta.vivisect.dimensionalize.HyperassociativeMap;
import nars.core.EventEmitter;
import nars.core.Events.ConceptForget;
import nars.core.Events.ConceptNew;
import nars.core.NAR;
import nars.core.build.Default;
import nars.entity.Concept;
import nars.io.Texts;
import nars.io.narsese.Narsese;
import nars.language.CompoundTerm;
import nars.language.Image;
import static nars.language.Tense.Eternal;
import nars.language.Term;
import nars.language.Variable;
import nars.util.NARGraph;

/**
 * TODO add existing concepts before it is added
 */
public class TermVectors implements EventEmitter.EventObserver {
    
    NARGraph graph;
    HyperassociativeMap map;
    private final NAR nar;
    
    /** edge with a value indicating relative position of a subterm (0=start, 1=end) */
    public static class ContentPosition {
    
        public final double position;

        public ContentPosition(final double p) {
            this.position = p;
        }

        @Override
        public String toString() {
            return "content[" + Texts.n2(position) + "]";
        }
        
        
    }
    
    public TermVectors(NAR n, int dimensions) {
        this.nar = n;
        this.graph = new NARGraph();
        
        nar.event(this, true, ConceptNew.class, ConceptForget.class);
        
        setDimensions(dimensions);
    }
    
    public void setDimensions(int d) {
        this.map = new HyperassociativeMap(graph, d);
    }
    
    public Term getTerm(Term t) {
        //TODO map variables to common vertex
        if (t instanceof Variable) {            
            t = Term.get(((Variable)t).getType() + "0" );
        }
        
        if (!graph.containsVertex(t)) {
            graph.addVertex(t);
        
            if (t instanceof CompoundTerm) {
                CompoundTerm ct = (CompoundTerm)t;
                float index = 0;
                
                float numSubTerms = ct.term.length;
                
                //handle Image with index as virtual term
                if (ct instanceof Image)  numSubTerms++;
                        
                for (Term s : ct.term) {
                    if ((ct instanceof Image) && (((Image)ct).relationIndex == index)) index++;
                    
                    float p = numSubTerms > 1 ? index / (numSubTerms-1) : 0.5f;
                    graph.addEdge( getTerm(s), ct, new ContentPosition(p));
                    
                    index++;
                }
            }
        }
        
        return t;
    }
    
    public void removeTerm(Term t) {
        graph.removeVertex(t);
    }

    @Override
    public void event(Class event, Object[] args) {
        if (event == ConceptNew.class) {
            getTerm(((Concept)args[0]).term);            
        }
        else if (event == ConceptForget.class) {
            removeTerm(((Concept)args[0]).term);                        
        }
    }
    
    
    
    public static void main(String[] args) throws Narsese.InvalidInputException {
        NAR n = new NAR(new Default());
        TermVectors t = new TermVectors(n, 2);
        
        n.believe("<a --> b>", Eternal, 1f,0.9f);
        n.believe("<b --> a>", Eternal, 1f,0.9f);
        //n.believe("<(*,a,b,c) --> d>", Eternal, 1f,0.9f);
        n.run(10);
     
        t.map.run(100);
        
        System.out.println(t.graph);
        System.out.println(t.map);
    }
}
