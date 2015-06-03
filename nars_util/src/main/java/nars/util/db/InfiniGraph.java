package nars.util.db;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.*;

import java.io.Serializable;
import java.util.*;

/**
 * Blueprints Graph interface implemented with Infinispan collections
 */
public class InfiniGraph<X> implements Graph {

    public final String id;
    //protected Long currentId = 0l;
    protected Map<X, Vertex> vertices;
    protected Map<X, Edge> edges;
    //protected Map<String, TinkerIndex> indices = new HashMap<String, TinkerIndex>();

    //protected TinkerKeyIndex<InfiniVertex> vertexKeyIndex = new TinkerKeyIndex<InfiniVertex>(InfiniVertex.class, this);
    //protected TinkerKeyIndex<InfiniEdge> edgeKeyIndex = new TinkerKeyIndex<InfiniEdge>(InfiniEdge.class, this);


    private static final Features FEATURES = new Features();


    static {
        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.supportsSerializableObjectProperty = true;
        FEATURES.supportsBooleanProperty = true;
        FEATURES.supportsDoubleProperty = true;
        FEATURES.supportsFloatProperty = true;
        FEATURES.supportsIntegerProperty = true;
        FEATURES.supportsPrimitiveArrayProperty = true;
        FEATURES.supportsUniformListProperty = true;
        FEATURES.supportsMixedListProperty = true;
        FEATURES.supportsLongProperty = true;
        FEATURES.supportsMapProperty = true;
        FEATURES.supportsStringProperty = true;

        FEATURES.ignoresSuppliedIds = false;
        FEATURES.isPersistent = false;
        FEATURES.isWrapper = false;

        FEATURES.supportsIndices = false;
        FEATURES.supportsKeyIndices = false;
        FEATURES.supportsVertexKeyIndex = false;
        FEATURES.supportsEdgeKeyIndex = false;
        FEATURES.supportsVertexIndex = false;
        FEATURES.supportsEdgeIndex = false;
        FEATURES.supportsTransactions = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;
        FEATURES.supportsThreadedTransactions = false;
        FEATURES.supportsThreadIsolatedTransactions = false;

    }

    public enum FileType {
        JAVA,
        GML,
        GRAPHML,
        GRAPHSON
    }



    public InfiniGraph(String id) {
        super();

        this.id = id;

        init();
    }

    protected void init() {

    }

//    public Iterable<Vertex> getVertices(final String key, final Object value) {
//        if (vertexKeyIndex.getIndexedKeys().contains(key)) {
//            return (Iterable) vertexKeyIndex.get(key, value);
//        } else {
//            return new PropertyFilteredIterable<Vertex>(key, value, this.getVertices());
//        }
//    }
//
//    public Iterable<Edge> getEdges(final String key, final Object value) {
//        if (edgeKeyIndex.getIndexedKeys().contains(key)) {
//            return (Iterable) edgeKeyIndex.get(key, value);
//        } else {
//            return new PropertyFilteredIterable<Edge>(key, value, this.getEdges());
//        }
//    }

    public Vertex addVertex(final Object id) {

        InfiniVertex<X> vertex;
        if (null != id) {
            vertex = (InfiniVertex<X>) this.vertices.get(id);
            if (null != vertex) {
                throw ExceptionFactory.vertexWithIdAlreadyExists(id);
            }
        } else {
            throw new RuntimeException("id must be non-null");
//            boolean done = false;
//            while (!done) {
//                idString = this.getNextId();
//                vertex = this.vertices.get(idString);
//                if (null == vertex)
//                    done = true;
//            }
        }

        this.vertices.put(vertex.getId(), vertex = new InfiniVertex(id, this));
        return vertex;
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();

        String idString = id.toString();
        return this.vertices.get(idString);
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();

        String idString = id.toString();
        return this.edges.get(idString);
    }


    public Iterable<Vertex> getVertices() {
        //Unmodifiable?
        return this.vertices.values();
    }

    @Override
    public Iterable<Vertex> getVertices(String s, Object o) {
        return null;
    }

    public Iterable<Edge> getEdges() {
        ////Unmodifiable?
        return this.edges.values();
    }

    @Override
    public Iterable<Edge> getEdges(String s, Object o) {
        return null;
    }

    public void removeVertex(final Vertex vertex) {
        if (null == this.vertices.remove(vertex.getId().toString()))
            throw ExceptionFactory.vertexWithIdDoesNotExist(vertex.getId());

        for (Edge edge : vertex.getEdges(Direction.BOTH)) {
            this.removeEdge(edge);
        }

        this.vertices.remove(vertex.getId().toString());
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        /*if (label == null)
            throw ExceptionFactory.edgeLabelCanNotBeNull();*/

        String idString = null;
        InfiniEdge<X> edge;
        if (null != id) {
            idString = id.toString();
            edge = (InfiniEdge<X>) this.edges.get(idString);
            if (null != edge) {
                throw ExceptionFactory.edgeWithIdAlreadyExist(id);
            }
        } else {
            throw new RuntimeException("ID must be non-null");
//            boolean done = false;
//            while (!done) {
//                idString = this.getNextId();
//                edge = this.edges.get(idString);
//                if (null == edge)
//                    done = true;
//            }
        }

        this.edges.put(edge.getId(),
                edge = new InfiniEdge(idString,
                        (InfiniVertex)outVertex,
                        (InfiniVertex)inVertex,
                        label, this));
        
        final InfiniVertex out = (InfiniVertex) outVertex;  //(InfiniVertex) outVertex;
        final InfiniVertex in = (InfiniVertex) inVertex; //(InfiniVertex) inVertex;
        out.addOutEdge(label, edge);
        in.addInEdge(label, edge);
        return edge;

    }

    public void removeEdge(final Edge edge) {

        if (null == this.edges.remove(edge.getId().toString())) {
            return;
        }


        InfiniVertex<X> outVertex = ((InfiniEdge) edge).outVertex;
        InfiniVertex<X> inVertex = ((InfiniEdge) edge).inVertex;
        if (null != outVertex && null != outVertex.outEdges) {
            /*final Set<Edge> edges = */outVertex.outEdges.remove(edge.getLabel());
        }
        if (null != inVertex && null != inVertex.inEdges) {
            /*final Set<Edge> edges = */inVertex.inEdges.remove(edge.getLabel());
        }



    }

    public GraphQuery query() {
        return new DefaultGraphQuery(this);
    }


    public String toString() {
        return StringFactory.graphString(this, "vertices:" + this.vertices.size() + " edges:" + this.edges.size());
    }

    public void clear() {
        this.vertices.clear();
        this.edges.clear();
    }

    public void shutdown() {
    }

//    private String getNextId() {
//        String idString;
//        while (true) {
//            idString = this.currentId.toString();
//            this.currentId++;
//            if (null == this.vertices.get(idString) || null == this.edges.get(idString) || this.currentId == Long.MAX_VALUE)
//                break;
//        }
//        return idString;
//    }

    public Features getFeatures() {
        return FEATURES;
    }

//    protected class TinkerKeyIndex<T extends TinkerElement> extends TinkerIndex<T> implements Serializable {
//
//        private final Set<String> indexedKeys = new HashSet<String>();
//        private TinkerGraph graph;
//
//        public TinkerKeyIndex(final Class<T> indexClass, final TinkerGraph graph) {
//            super(null, indexClass);
//            this.graph = graph;
//        }
//
//        public void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
//            if (this.indexedKeys.contains(key)) {
//                if (oldValue != null)
//                    this.remove(key, oldValue, element);
//                this.put(key, newValue, element);
//            }
//        }
//
//        public void autoRemove(final String key, final Object oldValue, final T element) {
//            if (this.indexedKeys.contains(key)) {
//                this.remove(key, oldValue, element);
//            }
//        }
//
//        public void createKeyIndex(final String key) {
//            if (this.indexedKeys.contains(key))
//                return;
//
//            this.indexedKeys.add(key);
//
//            if (InfiniVertex.class.equals(this.indexClass)) {
//                KeyIndexableGraphHelper.reIndexElements(graph, graph.getVertices(), new HashSet<String>(Arrays.asList(key)));
//            } else {
//                KeyIndexableGraphHelper.reIndexElements(graph, graph.getEdges(), new HashSet<String>(Arrays.asList(key)));
//            }
//        }
//
//        public void dropKeyIndex(final String key) {
//            if (!this.indexedKeys.contains(key))
//                return;
//
//            this.indexedKeys.remove(key);
//            this.index.remove(key);
//
//        }
//
//        public Set<String> getIndexedKeys() {
//            if (null != this.indexedKeys)
//                return new HashSet<String>(this.indexedKeys);
//            else
//                return Collections.emptySet();
//        }
//    }

    abstract protected static class InfiniElement<X> implements Element, Serializable {

        public final Map<String, Serializable> properties = new LinkedHashMap();
        public final X id;
        public final String graphID;
        transient private final InfiniGraph<X> graph;

        protected InfiniElement(final X id, final InfiniGraph<X> graph) {
            this.graph = graph;
            this.graphID = graph.id;
            this.id = id;
        }

        public InfiniGraph<X> graph() {
            if (graph == null) {
                //TODO lookup the graph by graph ID
            }
            return graph;
        }

        public Set<String> getPropertyKeys() {
            return this.properties.keySet();
        }

        public <T> T getProperty(final String key) {
            return (T) this.properties.get(key);
        }

        public void setProperty(final String key, final Object value) {
            Serializable v = (Serializable)value;
            ElementHelper.validateProperty(this, key, value);
            Object oldValue = this.properties.put(key, v);
            afterAdd(key, v);
        }

        abstract protected void afterAdd(final String key, final Serializable value);

        public <T> T removeProperty(final String key) {
            Serializable oldValue = this.properties.remove(key);
            if (oldValue!=null) {
                beforeRemove(key, oldValue);
                return (T) oldValue;
            }
            return null;
        }

        abstract protected void beforeRemove(final String key, final Serializable oldValue);

        public int hashCode() {
            return this.id.hashCode();
        }

        public X getId() {
            return this.id;
        }

        public boolean equals(final Object object) {
            return ElementHelper.areEqual(this, object);
        }

    }

    protected static class InfiniVertex<X> extends InfiniElement<X> implements Vertex, Serializable {

        public final Map<X, Set<Edge>> outEdges = new LinkedHashMap();
        public final Map<X, Set<Edge>> inEdges = new LinkedHashMap();

        protected InfiniVertex(final X id, final InfiniGraph graph) {
            super(id, graph);
        }

        public Iterable<Edge> getEdges(final Direction direction, final X... labels) {
            if (direction.equals(Direction.OUT)) {
                return this.getOutEdges(labels);
            } else if (direction.equals(Direction.IN))
                return this.getInEdges(labels);
            else {
                return Iterables.concat(this.getInEdges(labels), this.getOutEdges(labels));
            }
        }

        @Override
        public Iterable<Edge> getEdges(Direction direction, String... strings) {
            return null;
        }

        public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
            return new VerticesFromEdgesIterable(this, direction, labels);
        }

        private Iterable<Edge> getEdges(final Map<X, Set<Edge>> e, final X... labels) {
            if (labels.length == 0) {
                return Iterables.concat(e.values());
            } else if (labels.length == 1) {
                final Set<Edge> edges = e.get(labels[0]);
                if (null == edges) {
                    return Collections.emptyList();
                } else {
                    return edges;
                }
            } else {
                final Set<X> labelSet = Sets.newHashSet(labels);
                return Iterables.concat(Iterables.transform(e.entrySet(), x -> {
                    if (labelSet.contains(x.getKey()))
                        return x.getValue();
                    return Collections.emptyList();
                }));
            }
        }

        private Iterable<Edge> getInEdges(final X... labels) {
            return getEdges(inEdges, labels);
        }
        private Iterable<Edge> getOutEdges(final X... labels) {
            return getEdges(outEdges, labels);
        }

        public VertexQuery query() {
            return new DefaultVertexQuery(this);
        }

        public String toString() {
            return StringFactory.vertexString(this);
        }

        public Edge addEdge(final String label, final Vertex vertex) {
            return graph().addEdge(null, this, vertex, label);
        }


        //TODO make addInEdge and addOutEdge call a common function parameterized by a collection reference (in or out)
        protected boolean addOutEdge(final X label, final Edge edge) {
            Set<Edge> edges = this.outEdges.get(label);
            if (null == edges) {
                this.outEdges.put(label, edges = newEdgeSet(1));
            }
            return edges.add(edge);
        }

        protected boolean addInEdge(final X label, final Edge edge) {
            Set<Edge> edges = this.inEdges.get(label);
            if (null == edges) {
                this.inEdges.put(label, edges = newEdgeSet(1));
            }
            return edges.add(edge);
        }


        private Set<Edge> newEdgeSet(int size) {
            return new LinkedHashSet<Edge>(size);
        }



        @Override
        protected void afterAdd(String key, Serializable value) {
            //this.graph.vertexKeyIndex.autoUpdate(key, value, oldValue, (InfiniVertex) this);
        }

        @Override
        protected void beforeRemove(String key, Serializable oldValue) {
               //this.graph.vertexKeyIndex.autoRemove(key, oldValue, (InfiniVertex) this);
        }


        @Override
        public void remove() {
            graph().removeVertex(this);
        }


    }

    public static class InfiniEdge<X> extends InfiniElement<X> implements Edge, Serializable {

        public final String label;
        public final InfiniVertex<X> inVertex;
        public final InfiniVertex<X> outVertex;

        protected InfiniEdge(final X id, final InfiniVertex<X> outVertex, final InfiniVertex<X> inVertex, final String label, final InfiniGraph<X> graph) {
            super(id, graph);
            this.label = label;
            this.outVertex = outVertex;
            this.inVertex = inVertex;
        }

        public String getLabel() {
            return this.label;
        }

        public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
            if (direction.equals(Direction.IN))
                return this.inVertex;
            else if (direction.equals(Direction.OUT))
                return this.outVertex;
            else
                throw ExceptionFactory.bothIsNotSupported();
        }

        public String toString() {
            return StringFactory.edgeString(this);
        }

        @Override
        protected void afterAdd(String key, Serializable value) {
            //this.graph.edgeKeyIndex.autoUpdate(key, value, oldValue, (InfiniEdge) this);*/
        }

        @Override
        protected void beforeRemove(String key, Serializable oldValue) {
            //this.graph.edgeKeyIndex.autoRemove(key, oldValue, (InfiniEdge) this);*/
        }

        @Override
        public void remove() {
            graph().removeEdge(this);
        }
    }
}