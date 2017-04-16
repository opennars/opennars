package nars.util.experimental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.AbstractGraph;

/**
 * EXPERIMENTAL
 * @author me
 */
public class CachedObjectGraph extends AbstractGraph<Object, Object> implements DirectedGraph<Object,Object> {

    
    private final Set<Object> items;
    private final Map<Object, Object> in = new HashMap();
    private final Map<Object, Object> out = new HashMap();
    
    public CachedObjectGraph(Collection<Object> items) {
        super();
        
        this.items = new HashSet(items);
        
        update();
    }
    
    protected void update() {
        in.clear();
        out.clear();
        
        for (Object i : items) {
            if (i instanceof Concept)
                addConceptTermLinks((Concept)i);
        }

    }

    @Override
    public boolean addVertex(Object v) {
        return items.add(v);
    }

    @Override
    public boolean addEdge(Object src, Object target, Object e) {
        if (in.containsKey(e))
            return false;
        
        in.put(e, src);
        out.put(e, target);
        return true;
    }
    
    public void addConceptTermLinks(Concept c) {
            final Term source = c.getTerm();
            
            if (!containsVertex(c)) {
                addVertex(c);

                /*if (includeConceptTermLinks)*/ {
                    for (TermLink t : c.termLinks.nameTable.values()) {
                        Term target = t.getTarget();
                        if (!containsVertex(target))  {
                            addVertex(target);
                        }
                        addEdge(source, target, t);
                    }
                }

                /*if (includeConceptTaskLinks)*/ {
                    for (TaskLink t : c.taskLinks.nameTable.values()) {
                        Term target = t.getTarget();
                        if (!containsVertex(target))  {
                            addVertex(target);
                        }        
                        addEdge(source, target, t);                    
                    }            
                }

                /*if (includeConceptBeliefs)*/ {
                    for (Sentence s : c.beliefs) {
                        Term target = s.getContent();
                        if (!containsVertex(target))
                            addVertex(target);
                        addEdge(source, target, s);
                    }
                }
                
                /*if (includeConceptQuestoins)*/ {
                    for (Task t : c.questions) {
                        Term target = t.getContent();
                        if (!containsVertex(target))
                            addVertex(target);
                        addEdge(source, target, t);
                    }
                }
                
                
            }
        
    }
    
    public interface IConcept {
        public Term getTerm();
        public List<Task> getQuestions();
        public List<Sentence> getBeliefs();
        public List<TermLink> getTermLinks();
        public List<TaskLink> getTaskLinks();
    }
    
    public interface ITerm {
        public ArrayList<Term> getComponents();
    
        /** Set of all contained components, recursively */
        public Set<Term> getContainedTerms();
    }
    
    
    /** form a concept from the surrounding neighborhood of any vertex */
    public IConcept concept(String id) {
        return null;
    }
    
    /** form a Term from the surrounding neighborhood of any vertex */
    public ITerm term(String term) {
        return null;
    }
    
    @Override
    public Set<Object> getAllEdges(Object source, Object target) {
        Set<Object> edges = new HashSet();
        for (final Map.Entry<Object, Object> e : in.entrySet()) {
            if (e.getValue().equals(source)) {
                if (out.get(e.getKey()).equals(target))
                    edges.add(e.getKey());
            }
        }
        return edges;
    }

    @Override
    public Object getEdge(Object source, Object target) {
        
       throw new UnsupportedOperationException("Not supported yet.");         
    }



    @Override
    public boolean containsEdge(Object e) {
        return in.containsKey(e);
    }

    @Override
    public boolean containsVertex(Object v) {
        return items.contains(v);
    }

    @Override
    public Set<Object> edgeSet() {
        return in.keySet();
    }

    @Override
    public Set<Object> edgesOf(Object v) {
        Set<Object> e = incomingEdgesOf(v);
        e.addAll(outgoingEdgesOf(v));
        return e;
    }



    @Override
    public Set<Object> vertexSet() {
        return items;
    }

    @Override
    public Object getEdgeSource(Object e) {
        return in.get(e);
    }

    @Override
    public Object getEdgeTarget(Object e) {
        return out.get(e);
    }

    @Override
    public double getEdgeWeight(Object e) {
        return 1.0;
    }

    @Override
    public int inDegreeOf(Object v) {
        return incomingEdgesOf(v).size();
    }

    @Override
    public Set<Object> incomingEdgesOf(Object v) {
        Set<Object> s = new HashSet();
        for (final Map.Entry<Object, Object> e : in.entrySet()) {
            if (e.getValue().equals(v))
                s.add(e.getKey());
        }
        return s;
    }

    @Override
    public int outDegreeOf(Object v) {
        return outgoingEdgesOf(v).size();
    }

    @Override
    public Set<Object> outgoingEdgesOf(Object v) {
        Set<Object> s = new HashSet();
        for (final Map.Entry<Object, Object> e : out.entrySet()) {
            if (e.getValue().equals(v))
                s.add(e.getKey());
        }
        return s;
    }
    
    
    
    
    
    //UNMODIFIABLE -------

    @Override
    public EdgeFactory<Object, Object> getEdgeFactory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object removeEdge(Object v, Object v1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeEdge(Object e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeVertex(Object v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object addEdge(Object v, Object v1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    
}
