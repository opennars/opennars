//package nars.gui.output;
//
//import com.mxgraph.layout.mxCompactTreeLayout;
//import com.mxgraph.layout.mxFastOrganicLayout;
//import com.mxgraph.layout.mxGraphLayout;
//import com.mxgraph.swing.mxGraphComponent;
//import com.mxgraph.util.mxConstants;
//import org.jgrapht.Graph;
//import org.jgrapht.ext.JGraphXAdapter;
//
//import java.util.Map;
//
///**
// *
// * https://github.com/jgrapht/jgrapht/blob/master/jgrapht-demo/src/main/java/org/jgrapht/demo/JGraphXAdapterDemo.java
// */
//public class JGraphXGraphPanel extends mxGraphComponent {
//
//
//    public enum LayoutMode {
//        Organic,
//        Tree
//    }
//
//    public JGraphXGraphPanel(Graph g) {
//        this(g, LayoutMode.Organic);
//    }
//
//    public JGraphXGraphPanel(Graph g, LayoutMode l) {
//        super(new JGraphXAdapter(g));
//
//        setOpaque(false);
//
//        getGraph().setGridEnabled(false);
//
//
//        getGraph().setMultigraph(true);
//        getGraph().setEdgeLabelsMovable(false);
//        getGraph().setVertexLabelsMovable(false);
//        getGraph().setAutoOrigin(true);
//        getGraph().setLabelsClipped(false);
//
//        //System.out.println(jgxAdapter.getStylesheet().getDefaultEdgeStyle());
//
//        //{perimeter=com.mxgraph.view.mxPerimeter$1@7b3300e5, shape=rectangle, fontColor=#774400, strokeColor=#6482B9, fillColor=#C3D9FF, align=center, verticalAlign=middle}
//        Map<String, Object> vstyle = getGraph().getStylesheet().getDefaultVertexStyle();
//        vstyle.put(mxConstants.STYLE_SHAPE, "hexagon");
//        vstyle.put(mxConstants.STYLE_FONTFAMILY, "Monospace");
//        vstyle.put(mxConstants.STYLE_FONTSIZE, 16);
//        vstyle.put("fontColor", "#222");
//        vstyle.put("fillColor", "#CCCCCC");
//
//        //{endArrow=classic, shape=connector, fontColor=#446299, strokeColor=#6482B9, align=center, verticalAlign=middle}
//        Map<String, Object> estyle = getGraph().getStylesheet().getDefaultEdgeStyle();
//        estyle.put("strokeColor", "#555");
//        estyle.put("fontColor", "#222");
//        estyle.put(mxConstants.STYLE_STROKEWIDTH, 3);
//        estyle.put(mxConstants.STYLE_FONTFAMILY, "Monospace");
//        estyle.put(mxConstants.STYLE_FONTSIZE, 16);
//
//
//
//        setAntiAlias(true);
//        setConnectable(false);
//        setExportEnabled(false);
//        setFoldingEnabled(false);
//        setPanning(true);
//        setTextAntiAlias(true);
//
//        mxGraphLayout theLayout = null;
//
//        switch (l) {
//            case Organic:
//                mxFastOrganicLayout og =
//                        //new mxCompactTreeLayout(jgxAdapter);
//                        new mxFastOrganicLayout(getGraph());
//                //new mxCircleLayout(jgxAdapter);
//                og.setForceConstant(600);
//                og.setMaxIterations(1500);
//                og.setUseBoundingBox(true);
//                theLayout = og;
//                break;
//            case Tree:
//                mxCompactTreeLayout ot =
//                        new mxCompactTreeLayout(getGraph());
//
//                //new mxCircleLayout(jgxAdapter);
//                ot.setLevelDistance(40);
//                ot.setNodeDistance(50);
//                ot.setEdgeRouting(true);
//                ot.setHorizontal(false);
//                ot.setMoveTree(true);
//                ot.setResizeParent(false);
//                theLayout = ot;
//                break;
//        }
//
//        theLayout.execute(getGraph().getDefaultParent());
//
//
//        getGraph().setConnectableEdges(false);
//        getGraph().setCellsDisconnectable(false);
//        getGraph().setEdgeLabelsMovable(false);
//        //jgxAdapter.setCellsLocked(true);
//
//    }
//
//
//
////    public JGraphXGraphPanel(NAR n) {
////        this(new NARGraph().add(n, IncludeEverything, new DefaultGrapher(true,true,true,true,3,false, false)));
////    }
//
////    public static void main(String[] args) {
////        NAR n = new NAR();
////
////        /*
////        new TextInput(n, "<a --> b>.");
////        new TextInput(n, "<b --> c>.");
////        new TextInput(n, "<d <-> c>. %0.75;0.90%");
////        new TextInput(n, "<a --> c>?");
////        new TextInput(n, "<a --> d>?");
////        n.run(12);
////        */
////
////        n.addInput("<0 --> num>. %1.00;0.90% {0 : 1}");
////        n.addInput("<<$1 --> num> ==> <(*,$1) --> num>>. %1.00;0.90% {0 : 2}");
////        n.addInput("<(*,(*,(*,0))) --> num>?  {0 : 3}");
////
////        n.run(220);
////
////
////
////        Window w = new Window("GraphPanel", new JGraphXGraphPanel(n)) {
////
////            @Override           protected void close() {            }
////
////        };
////        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////        w.setSize(1200,900);
////        w.setVisible(true);
////    }
// }
