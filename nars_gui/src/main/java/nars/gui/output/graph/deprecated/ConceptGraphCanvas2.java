package nars.gui.output.graph.deprecated;

//package nars.gui.output.graph;
//
//import automenta.vivisect.graph.AnimatedProcessingGraphCanvas;
//import nars.core.NAR;
//import nars.util.DefaultGraphizer;
//import nars.util.NARGraph;
//import org.jgrapht.Graph;
//import org.jgrapht.graph.DirectedMultigraph;
//
///**
// *
// */
//public class ConceptGraphCanvas2 extends AnimatedProcessingGraphCanvas<Object,Object> {
//    private final NAR nar;
//    
//    boolean taskLinks = true;
//    float minPriority = 0;
//            
//    public ConceptGraphCanvas2(NAR n) {
//        super(null, new NARGraphDisplay());
//        this.nar = n;
//    }
//
//    @Override
//    public Graph<Object, Object> getGraph() {
//        if (nar!=null)
//            return new NARGraph().add(nar, new NARGraph.ExcludeBelowPriority(minPriority), new DefaultGraphizer(false, true, false, false, 0, true, taskLinks));
//        return super.getGraph();
//    }
//    
//
//    public void setTaskLinks(boolean taskLinks) {
//        this.taskLinks = taskLinks;
//    }
//
//    
//    
//    
//    
//}
