package nars.util.graph;

import nars.Events;
import nars.Memory;
import nars.event.AbstractReaction;
import nars.nal.*;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//TODO extends a new abstract ReactionGraph
abstract public class SentenceGraph<E> extends DirectedMultigraph<Term, E>  {
    public final Memory memory;
    private final AbstractReaction reaction;

    public static class GraphChange { }
    
    private boolean needInitialConcepts;
    private boolean started;
    
    public final Map<Sentence, List<E>> components = new HashMap();
    

    public SentenceGraph(Memory memory) {
        super(/*null*/new NullEdgeFactory());
        
        this.memory = memory;
        this.reaction = new AbstractReaction(memory.event, false,
                Events.FrameEnd.class,
                Events.ConceptForget.class,
                Events.ConceptBeliefAdd.class,
                Events.ConceptBeliefRemove.class,
                Events.ConceptGoalAdd.class,
                Events.ConceptGoalRemove.class,
                Events.Restart.class) {

            @Override
            public void event(Class event, Object[] args) {
                react(event,args);
            }
        };


        reset();
        
        start();
        
    }
    

    public void start() {
        if (started) return;        
        started = true;
        reaction.setActive(true);
    }
    
    public void stop() {
        if (!started) return;
        started = false;
        reaction.setActive(false);
    }

    public void react(final Class event, final Object[] a) {
//        if (event!=FrameEnd.class)
//            System.out.println(event + " " + Arrays.toString(a));
        
        if (event == Events.ConceptForget.class) {
            //remove all associated beliefs
            Concept c = (Concept)a[0];
            
            //create a clone of the list for thread safety

            for (Sentence b : c.beliefs)
                remove(b);
        }
        else if (event == Events.ConceptBeliefAdd.class) {
            Concept c = (Concept)a[0];
            Sentence s = ((Task)a[1]).sentence;
            add(s, c);
        }
        else if (event == Events.ConceptBeliefRemove.class) {
            //Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            remove(s);
        }
        else if (event == Events.ConceptGoalAdd.class) {
            Concept c = (Concept)a[0];
            Sentence s = ((Task)a[1]).sentence;
            add(s, c);
        }
        else if (event == Events.ConceptGoalRemove.class) {
            //Concept c = (Concept)a[0];
            Sentence s = (Sentence)a[1];
            remove(s);
        }
        else if (event == Events.FrameEnd.class) {
            if (needInitialConcepts)
                getInitialConcepts();
        }
        else if (event == Events.Restart.class) {
            reset();
        }
    }    
    

    
        
    protected /*synchronized*/ boolean remove(Sentence s) {
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

        this.removeAllEdges( new ArrayList(edgeSet()) );
        this.removeAllVertices( new ArrayList(vertexSet()) );

        if (!edgeSet().isEmpty()) {
            throw new RuntimeException(this + " edges not empty after reset()");
        }
        if (!vertexSet().isEmpty()) {
            throw new RuntimeException(this + " vertices not empty after reset()");
        }
            
        needInitialConcepts = true;
    }
    
    private void getInitialConcepts() {
        needInitialConcepts = false;

        memory.concepts.forEach(c -> {
            for (final Sentence s : c.beliefs)
                add(s, c);
        });

    }
    
    protected final void ensureTermConnected(final Term t) {
        if (inDegreeOf(t)+outDegreeOf(t) == 0)  removeVertex(t);        
    }
    
        
    abstract public boolean allow(Sentence s);
    
    abstract public boolean allow(Compound st);
    
    public boolean remove(final E s) {
        if (!containsEdge(s))
            return false;
        
        Term from = getEdgeSource(s);
        Term to = getEdgeTarget(s);
        
        
        boolean r = removeEdge(s);
        
        
        ensureTermConnected(from);
        ensureTermConnected(to);

        //if (r)
            //memory.event.emit(GraphChange.class, null, s);
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
    
    public /*synchronized*/ boolean add(final Sentence s, final Item c) {

        if (!allow(s))
            return false;               
        
        Compound cs = s.term;

        if (cs instanceof Statement) {


            Statement st = (Statement) cs;
            if (allow(st)) {

                if (add(s, st, c)) {
                    //event.emit(GraphChange.class, st, null);
                    return true;
                }
            }
        }
        
        return false;
    }    
    
    /** default behavior, may override in subclass */
    abstract public boolean add(final Sentence s, final Compound ct, final Item c);


    private static class NullEdgeFactory implements EdgeFactory {

        @Override public Object createEdge(Object v, Object v1) {
            return null;
        }

    }
}
