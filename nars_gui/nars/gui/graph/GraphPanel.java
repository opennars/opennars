package nars.gui.graph;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.swing.mxGraphComponent;
import java.awt.BorderLayout;
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
public class GraphPanel extends JPanel {
    
    private JGraphXAdapter jgxAdapter;
    

    public GraphPanel(NAR n) {
        super(new BorderLayout());

        
        NARGraph g = new NARGraph();
        g.add(n, IncludeEverything, new NARGraph.DefaultGraphizer(false,false,false,true,true));        

        // create a visualization using JGraph, via an adapter
        jgxAdapter = new JGraphXAdapter(g);
        

        mxGraphComponent mxc = new mxGraphComponent(jgxAdapter) {
            
        };
        mxc.setAntiAlias(true);
        mxc.setConnectable(false);
        mxc.setExportEnabled(false);
        mxc.setFoldingEnabled(false);
        mxc.setPanning(true);
        mxc.setTextAntiAlias(true);

        
        add(new JScrollPane(mxc), BorderLayout.CENTER);


        
        /*
        mxOrganicLayout layout = 
                //new mxCompactTreeLayout(jgxAdapter);
                new mxOrganicLayout(jgxAdapter);
                //new mxCircleLayout(jgxAdapter);        
        layout.setEdgeLengthCostFactor(0.001);*/
        
        mxCompactTreeLayout layout = 
                new mxCompactTreeLayout(jgxAdapter);
        
        layout.setLevelDistance(40);
        layout.setNodeDistance(50);
        layout.execute(jgxAdapter.getDefaultParent());
        
        
        jgxAdapter.setConnectableEdges(false);
        jgxAdapter.setCellsDisconnectable(false);
        jgxAdapter.setEdgeLabelsMovable(false);
        //jgxAdapter.setCellsLocked(true);
    }
    
    public static void main(String[] args) {
        NAR n = new NAR();
        
        new TextInput(n, "<a --> b>.");
        new TextInput(n, "<b --> c>.");
        new TextInput(n, "<d <-> c>. %0.75;0.90%");
        new TextInput(n, "<a --> c>?");                
        
        n.run(12);
        
        new TextInput(n, "<a --> d>?");

        n.run(12);
        
        Window w = new Window("GraphPanel", new GraphPanel(n)) {

            @Override           protected void close() {            }
            
        };
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setSize(1200,900);
        w.setVisible(true);
    }
}
