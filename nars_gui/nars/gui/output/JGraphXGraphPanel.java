package nars.gui.output;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import java.awt.BorderLayout;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nars.main.NAR;
import nars.gui.util.DefaultGraphizer;
import nars.gui.util.NARGraph;
import static nars.gui.util.NARGraph.IncludeEverything;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;

/**
 *
 * https://github.com/jgrapht/jgrapht/blob/master/jgrapht-demo/src/main/java/org/jgrapht/demo/JGraphXAdapterDemo.java
 */
public class JGraphXGraphPanel extends JPanel {
    
    private final JGraphXAdapter jgxAdapter;
    

    public JGraphXGraphPanel(Graph g) {
        super(new BorderLayout());
        

        // create a visualization using JGraph, via an adapter
        jgxAdapter = new JGraphXAdapter(g) {


          
        };
        jgxAdapter.setMultigraph(true);
        jgxAdapter.setEdgeLabelsMovable(false);
        jgxAdapter.setVertexLabelsMovable(false);
        jgxAdapter.setAutoOrigin(true);
        jgxAdapter.setLabelsClipped(true);
        
        //System.out.println(jgxAdapter.getStylesheet().getDefaultEdgeStyle());
        
        //{perimeter=com.mxgraph.view.mxPerimeter$1@7b3300e5, shape=rectangle, fontColor=#774400, strokeColor=#6482B9, fillColor=#C3D9FF, align=center, verticalAlign=middle}
        Map<String, Object> vstyle = jgxAdapter.getStylesheet().getDefaultVertexStyle();

        vstyle.put("fillColor", "#CCCCCC");
        
        //{endArrow=classic, shape=connector, fontColor=#446299, strokeColor=#6482B9, align=center, verticalAlign=middle}
        Map<String, Object> estyle = jgxAdapter.getStylesheet().getDefaultEdgeStyle();
        estyle.put("strokeColor", "#333333");
        estyle.put("fontColor", "#333333");
        estyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
        
        

        mxGraphComponent mxc = new mxGraphComponent(jgxAdapter) {
            
        };
        mxc.setAntiAlias(true);
        mxc.setConnectable(false);        
        mxc.setExportEnabled(false);
        mxc.setFoldingEnabled(false);
        mxc.setPanning(true);
        mxc.setTextAntiAlias(true);

        
                
        add(mxc, BorderLayout.CENTER);


        /*
        

        
        mxOrganicLayout layout = 
                new mxCompactTreeLayout(jgxAdapter);
                new mxOrganicLayout(jgxAdapter);
                //new mxCircleLayout(jgxAdapter);        
        layout.setEdgeLengthCostFactor(0.001);
        */
        /*
        {
        mxCompactTreeLayout layout = 
                new mxCompactTreeLayout(jgxAdapter);
        
        layout.setLevelDistance(40);
        layout.setNodeDistance(50);
        layout.setEdgeRouting(true);
        layout.setHorizontal(false);
        layout.setMoveTree(true);
        layout.setResizeParent(false);
        layout.execute(jgxAdapter.getDefaultParent());
        }
        */
        {
        mxFastOrganicLayout layout = 
                //new mxCompactTreeLayout(jgxAdapter);
                new mxFastOrganicLayout(jgxAdapter);
                //new mxCircleLayout(jgxAdapter);                
        layout.setForceConstant(400);
        layout.setMaxIterations(2000);
        layout.setUseBoundingBox(true);
        layout.execute(jgxAdapter.getDefaultParent());
            
        }
        

        
        jgxAdapter.setConnectableEdges(false);
        jgxAdapter.setCellsDisconnectable(false);
        jgxAdapter.setEdgeLabelsMovable(false);
        //jgxAdapter.setCellsLocked(true);
        
    }
    
    public JGraphXGraphPanel(NAR n) {
        this(new NARGraph().add(n, IncludeEverything, new DefaultGraphizer(true,true,true,true,3,false, false, null, null, null, null)));
    }
    
//    public static void main(String[] args) {
//        NAR n = new NAR();
//        
//        /*
//        new TextInput(n, "<a --> b>.");
//        new TextInput(n, "<b --> c>.");
//        new TextInput(n, "<d <-> c>. %0.75;0.90%");
//        new TextInput(n, "<a --> c>?");      
//        new TextInput(n, "<a --> d>?");
//        n.run(12);
//        */
//        
//        n.addInput("<0 --> num>. %1.00;0.90% {0 : 1}");
//        n.addInput("<<$1 --> num> ==> <(*,$1) --> num>>. %1.00;0.90% {0 : 2}"); 
//        n.addInput("<(*,(*,(*,0))) --> num>?  {0 : 3}");
//       
//        n.run(220);
//        
//
//        
//        Window w = new Window("GraphPanel", new JGraphXGraphPanel(n)) {
//
//            @Override           protected void close() {            }
//            
//        };
//        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        w.setSize(1200,900);
//        w.setVisible(true);
//    }
}
