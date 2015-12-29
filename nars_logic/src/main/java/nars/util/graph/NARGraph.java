package nars.util.graph;

import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores the contents of some, all, or of multiple NAR memory snapshots.
 *
 * @author me
 */
public class NARGraph<V,E> extends DirectedMultigraph<V,E> {

    public <X> Set<X> vertices(Class<? extends X> type) {
        Set<X> s = Global.newHashSet(vertexSet().size());
        s.addAll(vertexSet().stream().filter(type::isInstance).map(o -> (X) o).collect(Collectors.toList()));
        return s;
    }
    
    /**
     * determines which NARS term can result in added graph features
     */
    public interface Filter {

        boolean includeConcept(Concept c);
    }

    public static final Filter IncludeEverything = new Filter() {

        @Override
        public boolean includeConcept(Concept c) {
            return true;
        }
    };

    public static final class ExcludeBelowPriority implements Filter {

        final float thresh;




        public ExcludeBelowPriority(float l) {
            thresh = l;
        }


        @Override
        public boolean includeConcept(Concept c) {
            return true;
        }
    }

    /**
     * creates graph features from NARS term
     */
    public interface Grapher {

        Grapher on(NARGraph g, Object o);

        /**
         * called at beginning of operation
         *
         * @param g
         * @param time
         */
        @Deprecated void onTime(NARGraph g, long time);


        /**
         * called at end of operation
         *
         */
        void finish();

        void setMinPriority(float minPriority);
    }

    public abstract static class NAREdge<X> extends DefaultEdge {

        private final X object;
        private final int hash;

        protected NAREdge(X x) {
            object = x;
            hash = getHash();
        }

        protected NAREdge() {
            object = (X)getClass();
            hash = getHash();
        }


        public X name() {
            return object;
        }

        private int getHash() {
            return Objects.hash(object.hashCode(), getSource(), getTarget());
        }

        @Override
        public int hashCode() {
            return hash;
        }

        public X getObject() {
            return object;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == object) {
                return true;
            }
            if (obj instanceof NAREdge) {
                NAREdge e = (NAREdge) obj;
                return ((getSource() == e.getSource()) && //need .equals?
                        (getTarget() == e.getTarget()) && //need .equals?
                        (object.equals(((NAREdge) obj).object)));
            }
            return false;
        }

        @Override
        public Object getSource() {
            return super.getSource();
        }

        @Override
        public Object getTarget() {
            return super.getTarget();
        }


    }

//    public static class TermBelief extends NAREdge {
//
//        @Override
//        public String toString() {
//            return "belief";
//        }
//
//        @Override
//        public Object clone() {
//            return super.clone();
//        }
//    }
//
//
//    public static class TermQuestion extends NAREdge {
//
//        @Override
//        public String toString() {
//            return "question";
//        }
//
//        @Override
//        public Object clone() {
//            return super.clone();
//        }
//    }
//
//    public static class TermDerivation extends NAREdge {
//
//        @Override
//        public String toString() {
//            return "derives";
//        }
//
//        @Override
//        public Object clone() {
//            return super.clone();
//        }
//    }
//
//    public static class TermContent extends NAREdge {
//
//        @Override
//        public String toString() {
//            return "has";
//        }
//
//        @Override
//        public Object clone() {
//            return super.clone();
//        }
//    }
//
//    public static class TermType extends NAREdge {
//
//        @Override
//        public String toString() {
//            return "type";
//        }
//
//        @Override
//        public Object clone() {
//            return super.clone();
//        }
//    }
//
//    public static class SentenceContent extends NAREdge {
//
//        @Override
//        public String toString() {
//            return "sentence";
//        }
//
//        @Override
//        public Object clone() {
//            return super.clone();
//        }
//    }

    public NARGraph() {
        super(new MyEdgeFactory());
    }


    public NARGraph add(NAR n, Filter filter, Grapher graphize) {
        graphize.onTime(this, n.time());

        //TODO support AbstractBag
        n.forEachConcept(c -> {

            //graphize.preLevel(this, p);
            if (!filter.includeConcept(c)) {
                return;
            }

            graphize.on(this, c);

            //graphize.postLevel(this, level);
        });

        graphize.finish();
        return this;

    }

    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e) {
        return addEdge(sourceVertex, targetVertex, e, false);
    }

    public boolean addEdge(V sourceVertex, V targetVertex, E e, boolean allowMultiple) {
        if (!allowMultiple) {
            Set<E> existing = getAllEdges(sourceVertex, targetVertex);
            if (existing != null) {
                for (Object o : existing) {
                    if (o.getClass() == e.getClass()) {
                        return false;
                    }
                }
            }
        }

        return super.addEdge(sourceVertex, targetVertex, e);
    }

    //THESE REQUIRE JGRAPHX LIBRARY WHICH WE OTHERWISE DO NOT NEED IN NARS_CORE
    //    public void toGraphML(Writer writer) throws SAXException, TransformerConfigurationException {
    //        GraphMLExporter gme = new GraphMLExporter(new IntegerNameProvider(), new StringNameProvider(), new IntegerEdgeNameProvider(), new StringEdgeNameProvider());
    //        gme.export(writer, this);
    //    }
    //
    //    public void toGraphML(String outputFile) throws SAXException, TransformerConfigurationException, IOException {
    //        toGraphML(new FileWriter(outputFile, false));
    //    }
    //
    //    public void toGML(Writer writer) {
    //        GmlExporter gme = new GmlExporter(new IntegerNameProvider(), new StringNameProvider(), new IntegerEdgeNameProvider(), new StringEdgeNameProvider());
    //        gme.setPrintLabels(GmlExporter.PRINT_EDGE_VERTEX_LABELS);
    //        gme.export(writer, this);
    //    }
    //
    //    public void graphMLWrite(String filename) throws Exception {
    //        new GraphMLExporter(new IntegerNameProvider(), new StringNameProvider(), new IntegerEdgeNameProvider(), new StringEdgeNameProvider()).export(new FileWriter(filename), this);
    //    }
    //
    //    public void toGML(String outputFile) throws IOException {
    //        toGML(new FileWriter(outputFile, false));
    //    }

    @Override
    public Graph clone() {
        return (Graph) super.clone();
    }



    public static class TimeNode {

        private final long time;

        public TimeNode(long t) {
            time = t;
        }

        @Override
        public int hashCode() {
            return (int) time;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TimeNode)) {
                return false;
            }
            return ((TimeNode) obj).time == time;
        }

        @Override
        public String toString() {
            return "t" + time;
        }

    }

    public static class UniqueEdge {

        private final String label;

        public UniqueEdge(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

    }

    @Deprecated public void at(V x, long t) {
        at(x, t, "c"); //c=creation time
    }
    @Deprecated public void at(V x, long t, String edgeLabel) {
        at(x, t, (E) new UniqueEdge(edgeLabel));
    }

    @Deprecated public void at(V x, long t, E edge) {
        TimeNode timeNode = new TimeNode(t);
        addVertex((V) timeNode);
        addEdge((V) timeNode, x, edge);
    }

    private static class MyEdgeFactory implements EdgeFactory {
        @Override
        public Object createEdge(Object sourceVertex, Object targetVertex) {
            return null;
        }
    }
}
