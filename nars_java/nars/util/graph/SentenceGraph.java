package nars.util.graph;

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
    
    public void start() {
        if (started) return;        
        started = true;
        memory.event.on(Events.CycleEnd.class, this);
        memory.event.on(Events.ConceptRemove.class, this);
        memory.event.on(Events.ConceptBeliefAdd.class, this);
        memory.event.on(Events.ConceptBeliefRemove.class, this);        
        memory.event.on(Events.ConceptGoalAdd.class, this);
        memory.event.on(Events.ConceptGoalRemove.class, this);        
    }
    
    public void stop() {
        if (!started) return;
        started = false;
        memory.event.off(Events.CycleEnd.class, this);        
        memory.event.off(Events.ConceptRemove.class, this);
        memory.event.off(Events.ConceptBeliefAdd.class, this);
        memory.event.off(Events.ConceptBeliefRemove.class, this);        
        memory.event.off(Events.ConceptGoalAdd.class, this);
        memory.event.off(Events.ConceptGoalRemove.class, this);        
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
            add(s, c,false);
        }
        else if (event == Events.ConceptBeliefRemove.class) {
            Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            remove(s);
        }
        else if (event == Events.ConceptGoalAdd.class) {
            Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            //add(s, c);
        }
        else if (event == Events.ConceptGoalRemove.class) {
            Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            //remove(s);
        }
        else if (event == Events.CycleEnd.class) {
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

        for (final Concept c : memory.getConcepts()) {
            for (final Sentence s : c.beliefs) {                
                add(s, c,false);
            }
        }        
    }
        
    abstract public boolean allow(Sentence s);
    
    abstract public boolean allow(CompoundTerm st);    
    
    public boolean remove(final Sentence s) {
        if (!containsEdge(s))
            return false;
        
        Term from = getEdgeSource(s);
        Term to = getEdgeTarget(s);
        
        
        boolean r = removeEdge(s);
        
        
        if (inDegreeOf(from)+outDegreeOf(from) == 0)  removeVertex(from);
        if (inDegreeOf(to)+outDegreeOf(to) == 0)  removeVertex(to);                

        if (r)
            event.emit(GraphChange.class, null, s);
        return true;
    }
    
    public boolean add(final Sentence s, final Item c, boolean specialAdd) {
        
        //specialAdd is a debug variable, which makes it possible to select from where the system is allowed to build up the tree easily
        if(!specialAdd)
           return false;
        
        /*if (containsEdge(s))
            return false;*/
        
        if (!allow(s))
            return false;
            
        
        if (s.content instanceof CompoundTerm) {
            CompoundTerm cs = (CompoundTerm)s.content;
        
            if (cs instanceof Statement) {
                
                
                Statement st = (Statement)cs;
                if (allow(st)) {
                                
                    if (add(s, st, c, specialAdd)) {
                        event.emit(GraphChange.class, st, null);
                        return true;
                    }
                }
            }
                
        }        
        
        return false;
    }    
    
    /** default behavior, may override in subclass */
    public boolean add(final Sentence s, final CompoundTerm ct, final Item c, boolean specialAdd) {
        
        if(!specialAdd) {
            return false;
        }
        
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
