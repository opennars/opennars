package nars.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.xml.transform.TransformerConfigurationException;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import org.jgrapht.ext.GmlExporter;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringEdgeNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.xml.sax.SAXException;

/**
 * Stores the contents of some, all, or of multiple NAR memory snapshots.
 *
 * @author me
 */
public class NARGraph extends DirectedMultigraph {

    @Override
    public Object clone() {
        return super.clone();
    }

    /**
     * determines which NARS term can result in added graph features
     */
    public static interface Filter {

        boolean includePriority(float l);

        boolean includeConcept(Concept c);
    }

    public final static Filter IncludeEverything = new Filter() {
        @Override
        public boolean includePriority(float l) {
            return true;
        }

        @Override
        public boolean includeConcept(Concept c) {
            return true;
        }
    };

    public final static class ExcludeBelowPriority implements Filter {

        final float thresh;

        public ExcludeBelowPriority(float l) {
            this.thresh = l;
        }

        @Override
        public boolean includePriority(float l) {
            return l >= thresh;
        }

        @Override
        public boolean includeConcept(Concept c) {
            return true;
        }
    }

    /**
     * creates graph features from NARS term
     */
    public static interface Graphize {

        /**
         * called at beginning of operation
         *
         * @param g
         * @param time
         */
        void onTime(NARGraph g, long time);

        /**
         * called per concept
         *
         * @param g
         * @param c
         */
        void onConcept(NARGraph g, Concept c);

        /**
         * called at end of operation
         *
         * @param g
         */
        void onFinish(NARGraph g);
    }

    public static class NAREdge extends DefaultEdge {

        @Override
        public Object getSource() {
            return super.getSource();
        }

        @Override
        public Object getTarget() {
            return super.getTarget();
        }

        @Override
        public Object clone() {
            return super.clone();
        }

    }

    public static class TermBelief extends NAREdge {

        @Override
        public String toString() {
            return "belief";
        }

        @Override
        public Object clone() {
            return super.clone();
        }
    }

    public static class TermLinkEdge extends NAREdge {
        public final TermLink termLink;

        public TermLinkEdge(TermLink t) {  this.termLink = t;         }
        
        @Override public String toString() { return "termlink";         }

        @Override public Object clone() {  return super.clone();        }
    }
    
    public static class TaskLinkEdge extends NAREdge {
        public final TaskLink taskLink;

        public TaskLinkEdge(TaskLink t) {  this.taskLink = t;         }
        
        @Override public String toString() { return "tasklink";         }

        @Override public Object clone() {  return super.clone();        }
    }
    
    public static class TermQuestion extends NAREdge {

        @Override
        public String toString() {
            return "question";
        }

        @Override
        public Object clone() {
            return super.clone();
        }
    }

    public static class TermDerivation extends NAREdge {

        @Override
        public String toString() {
            return "derives";
        }

        @Override
        public Object clone() {
            return super.clone();
        }
    }

    public static class TermContent extends NAREdge {

        @Override
        public String toString() {
            return "has";
        }

        @Override
        public Object clone() {
            return super.clone();
        }
    }

    public static class TermType extends NAREdge {

        @Override
        public String toString() {
            return "type";
        }

        @Override
        public Object clone() {
            return super.clone();
        }
    }

    public static class SentenceContent extends NAREdge {

        @Override
        public String toString() {
            return "sentence";
        }

        @Override
        public Object clone() {
            return super.clone();
        }
    }

    public NARGraph() {
        super(DefaultEdge.class);
    }

    public List<Concept> currentLevel = new ArrayList();

    public NARGraph add(NAR n, Filter filter, Graphize graphize) {
        graphize.onTime(this, n.time());

        //TODO support AbstractBag
        Collection<? extends Concept> cc = n.memory.getConcepts();

        for (Concept c : cc) {

            //TODO use more efficient iterator so that the entire list does not need to be traversed when excluding ranges
            float p = c.getPriority();

            if (!filter.includePriority(p)) {
                continue;
            }

            //graphize.preLevel(this, p);
            if (!filter.includeConcept(c)) {
                continue;
            }

            graphize.onConcept(this, c);

            //graphize.postLevel(this, level);
        }

        graphize.onFinish(this);
        return this;

    }

    public boolean addEdge(Object sourceVertex, Object targetVertex, NAREdge e) {
        return addEdge(sourceVertex, targetVertex, e, false);
    }

    public boolean addEdge(Object sourceVertex, Object targetVertex, NAREdge e, boolean allowMultiple) {
        if (!allowMultiple) {
            Set existing = getAllEdges(sourceVertex, targetVertex);
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


    public void toGraphML(Writer writer) throws SAXException, TransformerConfigurationException {
        GraphMLExporter gme = new GraphMLExporter(new IntegerNameProvider(), new StringNameProvider(), new IntegerEdgeNameProvider(), new StringEdgeNameProvider());
        gme.export(writer, this);
    }

    public void toGraphML(String outputFile) throws SAXException, TransformerConfigurationException, IOException {
        toGraphML(new FileWriter(outputFile, false));
    }

    public void toGML(Writer writer) {
        GmlExporter gme = new GmlExporter(new IntegerNameProvider(), new StringNameProvider(), new IntegerEdgeNameProvider(), new StringEdgeNameProvider());
        gme.setPrintLabels(GmlExporter.PRINT_EDGE_VERTEX_LABELS);
        gme.export(writer, this);
    }

    public void toGML(String outputFile) throws IOException {
        toGML(new FileWriter(outputFile, false));
    }

}
