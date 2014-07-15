package nars.gui.graph;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import java.awt.BorderLayout;
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
        g.add(n, IncludeEverything, new NARGraph.DefaultGraphizer(true,true,true,true));        

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

        
        String v1 = "v1";
        String v2 = "v2";
        String v3 = "v3";
        String v4 = "v4";

        // add some sample data (graph manipulated via JGraphX)
        g.addVertex(v1);
        g.addVertex(v2);
        g.addVertex(v3);
        g.addVertex(v4);

        g.addEdge(v1, v2);
        g.addEdge(v2, v3);
        g.addEdge(v3, v1);
        g.addEdge(v4, v3);

        // positioning via jgraphx layouts
        mxGraphLayout layout = 
                //new mxCompactTreeLayout(jgxAdapter);
                new mxFastOrganicLayout(jgxAdapter);
                //new mxCircleLayout(jgxAdapter);
        
        
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
        new TextInput(n, "<a --> c>?");
        
        
        n.run(6);
        
        Window w = new Window("GraphPanel", new GraphPanel(n)) {

            @Override
            protected void close() {
            }
            
        };
        w.setSize(1200,900);
        w.setVisible(true);
    }
}
