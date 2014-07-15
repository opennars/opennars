package nars.gui.output;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nars.core.NAR;
import nars.graph.NARGraph;
import static nars.graph.NARGraph.IncludeEverything;
import nars.gui.Window;
import nars.io.TextInput;
import org.jgrapht.ext.JGraphXAdapter;

/**
 *
 * https://github.com/jgrapht/jgrapht/blob/master/jgrapht-demo/src/main/java/org/jgrapht/demo/JGraphXAdapterDemo.java
 */
public class JGraphXGraphPanel extends JPanel {
    
    private JGraphXAdapter jgxAdapter;
    

    public JGraphXGraphPanel(NAR n) {
        super(new BorderLayout());

        
        NARGraph g = new NARGraph();
        g.add(n, IncludeEverything, new NARGraph.DefaultGraphizer(true,true,true,true,true));        

        // create a visualization using JGraph, via an adapter
        jgxAdapter = new JGraphXAdapter(g) {


          
        };
        

        mxGraphComponent mxc = new mxGraphComponent(jgxAdapter) {
            
        };
        mxc.setAntiAlias(true);
        mxc.setConnectable(false);
        mxc.setExportEnabled(false);
        mxc.setFoldingEnabled(false);
        mxc.setPanning(true);
        mxc.setTextAntiAlias(true);

        
        add(new JScrollPane(mxc), BorderLayout.CENTER);


        
        mxFastOrganicLayout layout = 
                //new mxCompactTreeLayout(jgxAdapter);
                new mxFastOrganicLayout(jgxAdapter);
                //new mxCircleLayout(jgxAdapter);        
        layout.setForceConstant(150);
        layout.execute(jgxAdapter.getDefaultParent());
        

        /*
        mxOrganicLayout layout = 
                //new mxCompactTreeLayout(jgxAdapter);
                new mxOrganicLayout(jgxAdapter);
                //new mxCircleLayout(jgxAdapter);        
        layout.setEdgeLengthCostFactor(0.001);*/
        
        
        /*
        mxCompactTreeLayout layout = 
                new mxCompactTreeLayout(jgxAdapter);
        
        layout.setLevelDistance(40);
        layout.setNodeDistance(50);
        layout.execute(jgxAdapter.getDefaultParent());*/
        
        
        jgxAdapter.setConnectableEdges(false);
        jgxAdapter.setCellsDisconnectable(false);
        jgxAdapter.setEdgeLabelsMovable(false);
        //jgxAdapter.setCellsLocked(true);
    }
    
    public static void main(String[] args) {
        NAR n = new NAR();
        
        /*
        new TextInput(n, "<a --> b>.");
        new TextInput(n, "<b --> c>.");
        new TextInput(n, "<d <-> c>. %0.75;0.90%");
        new TextInput(n, "<a --> c>?");      
        new TextInput(n, "<a --> d>?");
        n.run(12);
        */
        
        new TextInput(n, "<0 --> num>. %1.00;0.90% {0 : 1}");
        new TextInput(n, "<<$1 --> num> ==> <(*,$1) --> num>>. %1.00;0.90% {0 : 2}"); 
        new TextInput(n, "<(*,(*,(*,0))) --> num>?  {0 : 3}");
       
        n.run(220);
        

        
        Window w = new Window("GraphPanel", new JGraphXGraphPanel(n)) {

            @Override           protected void close() {            }
            
        };
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setSize(1200,900);
        w.setVisible(true);
    }
}
