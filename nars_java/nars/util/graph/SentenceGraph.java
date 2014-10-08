package nars.util.graph;

import java.util.ArrayList;
import nars.core.EventEmitter;
import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.Memory;
import nars.entity.Concept;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.language.CompoundTerm;
import nars.language.Statement;
import nars.language.Term;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;



abstract public class SentenceGraph extends DirectedMultigraph<Term, Sentence> implements Observer {
    public final Memory memory;

    public static class GraphChange { }
    
    private boolean needInitialConcepts;
    private boolean started;
    public final EventEmitter event = new EventEmitter( GraphChange.class );
    
    public SentenceGraph(Memory memory) {
        super(/*null*/new EdgeFactory() {

            @Override public Object createEdge(Object v, Object v1) {
                return null;
            }
            
        });
        
        this.memory = memory;
        
        reset();
        
        start();
        
    }
    
    private void setEvents(boolean n) {
        if (memory!=null) {
            memory.event.set(this, n, 
                    Events.FrameEnd.class, 
                    Events.ConceptRemove.class, 
                    Events.ConceptBeliefAdd.class, 
                    Events.ConceptBeliefRemove.class, 
                    Events.ConceptGoalAdd.class, 
                    Events.ConceptGoalRemove.class, 
                    Events.ResetEnd.class);
        }
    }
    
    public void start() {
        if (started) return;        
        started = true;
        setEvents(true);        
    }
    
    public void stop() {
        if (!started) return;
        started = false;
        setEvents(false);
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
            Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            add(s, c);
        }
        else if (event == Events.ConceptBeliefRemove.class) {
            Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            remove(s);
        }
        else if (event == Events.ConceptGoalAdd.class) {
            Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            add(s, c);
        }
        else if (event == Events.ConceptGoalRemove.class) {
            Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            remove(s);
        }
        else if (event == Events.FrameEnd.class) {
            if (needInitialConcepts)
                getInitialConcepts();
        }
        else if (event == Events.ResetEnd.class) {
            reset();
        }
    }    
    
    public void reset() {
        this.removeAllEdges( new ArrayList(edgeSet()) );
        this.removeAllVertices( new ArrayList(vertexSet()) );
        
        needInitialConcepts = true;
    }
    
    private void getInitialConcepts() {
        needInitialConcepts = false;

        for (final Concept c : memory.getConcepts()) {
            for (final Sentence s : c.beliefs) {                
                add(s, c);
            }
        }        
    }
    
    protected final void ensureTermConnected(final Term t) {
        if (inDegreeOf(t)+outDegreeOf(t) == 0)  removeVertex(t);        
    }
    
        
    abstract public boolean allow(Sentence s);
    
    abstract public boolean allow(CompoundTerm st);    
    
    public boolean remove(final Sentence s) {
        if (!containsEdge(s))
            return false;
        
        Term from = getEdgeSource(s);
        Term to = getEdgeTarget(s);
        
        
        boolean r = removeEdge(s);
        
        
        ensureTermConnected(from);
        ensureTermConnected(to);

        if (r)
            event.emit(GraphChange.class, null, s);
        return true;
    }
    
    public boolean add(final Sentence s, final Item c) { 


        if (!allow(s))
            return false;
            
        
        if (s.content instanceof CompoundTerm) {
            CompoundTerm cs = (CompoundTerm)s.content;
        
            if (cs instanceof Statement) {
                
                
                Statement st = (Statement)cs;
                if (allow(st)) {
                                
                    if (add(s, st, c)) {
                        event.emit(GraphChange.class, st, null);
                        return true;
                    }
                }
            }
                
        }        
        
        return false;
    }    
    
    /** default behavior, may override in subclass */
    public boolean add(final Sentence s, final CompoundTerm ct, final Item c) {
        
        if (ct instanceof Statement) {
            Statement st = (Statement)ct;
            Term subject = st.getSubject();
            Term predicate = st.getPredicate();
            addVertex(subject);
            addVertex(predicate);
            addEdge(subject, predicate, s);        
            return true;
        }
        return false;
        
    }
    
}
