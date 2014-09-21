package nars.util.graph;

import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.language.CompoundTerm;
import nars.language.Statement;
import nars.language.Term;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

/**
 *
 * @author me
 */


abstract public class SentenceGraph extends DirectedMultigraph<Term, Sentence> implements Observer {

    private NAR nar;
    private boolean needInitialConcepts;

    public SentenceGraph() {
        super(/*null*/new EdgeFactory() {

            @Override public Object createEdge(Object v, Object v1) {
                return null;
            }
            
        });
    }    

    public SentenceGraph(NAR nar) {
        this();
        
        this.nar = nar;
        
        reset();

        start();
        
    }
    
    public void start() {
        nar.memory.event.on(Events.CycleStop.class, this);
        nar.memory.event.on(Events.ConceptRemove.class, this);
        nar.memory.event.on(Events.ConceptBeliefAdd.class, this);
        nar.memory.event.on(Events.ConceptBeliefRemove.class, this);        
    }
    
    public void stop() {
        nar.memory.event.off(Events.CycleStop.class, this);        
        nar.memory.event.off(Events.ConceptRemove.class, this);
        nar.memory.event.off(Events.ConceptBeliefAdd.class, this);
        nar.memory.event.off(Events.ConceptBeliefRemove.class, this);        
    }

    @Override
    public void event(final Class event, final Object[] a) {
        if (event == Events.ConceptRemove.class) {
            //remove all associated beliefs
            Concept c = (Concept)a[0];
            for (Sentence b : c.beliefs) {
                remove(b);
            }
        }
        else if (event == Events.ConceptBeliefAdd.class) {
            //Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            if (allow(s))
                add(s);
        }
        else if (event == Events.ConceptBeliefRemove.class) {
            //Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            remove(s);
        }
        else if (event == Events.CycleStop.class) {
            if (needInitialConcepts)
                getInitialConcepts();
        }
    }    
    
    public void reset() {
        this.removeAllEdges(edgeSet());
        this.removeAllVertices(vertexSet());
        
        needInitialConcepts = true;
    }
    
    private void getInitialConcepts() {
        needInitialConcepts = false;

        for (Concept c : nar.memory.getConcepts()) {
            for (Sentence s : c.beliefs) {
                if (allow(s))
                    add(s);
            }
        }        
    }
        
    abstract public boolean allow(Sentence s);
    
    abstract public boolean allow(Statement st);    
    
    public void remove(final Sentence s) {
        removeEdge(s);
    }
    
    public void add(final Sentence s) {
        boolean include = false;
        
        
        if (s.content instanceof CompoundTerm) {
            CompoundTerm cs = (CompoundTerm)s.content;
        
            if (cs instanceof Statement) {
                Statement st = (Statement)cs;
                if (allow(st)) {
                    
                    Term subject = st.getSubject();
                    Term predicate = st.getPredicate();
                    addVertex(subject);
                    addVertex(predicate);
                    addEdge(subject, predicate, s);                    
                }
            }
                
        }        
        
    }    
    
}
