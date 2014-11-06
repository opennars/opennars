package nars.util.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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



abstract public class SentenceGraph<E> extends DirectedMultigraph<Term, E> implements Observer {
    public final Memory memory;

    public static class GraphChange { }
    
    private boolean needInitialConcepts;
    private boolean started;
    
    public final Map<Sentence, List<E>> components = new HashMap();
    
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
                    Events.ConceptForget.class, 
                    Events.ConceptBeliefAdd.class, 
                    Events.ConceptBeliefRemove.class, 
                    Events.ConceptGoalAdd.class, 
                    Events.ConceptGoalRemove.class, 
                    Events.ResetEnd.class
                    );
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
//        if (event!=FrameEnd.class)
//            System.out.println(event + " " + Arrays.toString(a));
        
        if (event == Events.ConceptForget.class) {
            //remove all associated beliefs
            Concept c = (Concept)a[0];
            
            //create a clone of the list for thread safety
            for (Sentence b : new ArrayList<Sentence>(c.beliefs)) {
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
    

    
        
    protected boolean remove(Sentence s) {
        List<E> componentList = components.get(s);
        if (componentList!=null) {
            for (E e : componentList) {
                if (!containsEdge(e))
                    continue;
                Term source = getEdgeSource(e);
                Term target = getEdgeTarget(e);
                removeEdge(e);
                ensureTermConnected(source);
                ensureTermConnected(target);
            }
            componentList.clear();
            components.remove(s);        
            return true;
        }
        return false;
    }
    
    public void reset() {
        try {
            this.removeAllEdges( new ArrayList(edgeSet()) );
        }
        catch (Exception e) {
            System.err.println(e);
        }
        
        try {
            this.removeAllVertices( new ArrayList(vertexSet()) );
        }
        catch (Exception e) {
            System.err.println(e);
        }
        
        if (!edgeSet().isEmpty()) {
            System.err.println(this + " edges not empty after reset()");
            System.exit(1);
        }
        if (!vertexSet().isEmpty()) {
            System.err.println(this + " vertices not empty after reset()");
            System.exit(1);
        }
            
        needInitialConcepts = true;
    }
    
    private void getInitialConcepts() {
        needInitialConcepts = false;

        try {
            for (final Concept c : memory.concepts) {
                for (final Sentence s : c.beliefs) {                
                    add(s, c);
                }
            }        
        }
        catch (NoSuchElementException e) { }
    }
    
    protected final void ensureTermConnected(final Term t) {
        if (inDegreeOf(t)+outDegreeOf(t) == 0)  removeVertex(t);        
    }
    
        
    abstract public boolean allow(Sentence s);
    
    abstract public boolean allow(CompoundTerm st);    
    
    public boolean remove(final E s) {
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
    
   
    protected void addComponents(final Sentence parentSentence, final E edge) {
        List<E> componentList = components.get(parentSentence);
        if (componentList == null) {
            componentList = new ArrayList(1);
            components.put(parentSentence, componentList);
        }
        componentList.add(edge);        
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
    abstract public boolean add(final Sentence s, final CompoundTerm ct, final Item c);

    
}
