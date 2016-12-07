/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output.graph;

import automenta.vivisect.dimensionalize.AbegoTreeLayout;
import automenta.vivisect.graph.AnimatingGraphVis;
import nars.gui.util.NARGraph;
import nars.gui.util.NARGraph.UniqueEdge;
import nars.language.CompoundTerm;
import nars.language.Term;
import org.jgrapht.Graph;

/**
 *
 * @author me
 */
public class TermSyntaxVis extends AnimatingGraphVis {
    private NARGraph syntaxGraph;

    public TermSyntaxVis(Term... t) {
        super(new NARGraph(), new NARGraphDisplay().setTextSize(0.25f,64)
                , new AbegoTreeLayout());
        
        update(t);
        updateGraph();
    }
    
    @Override
    public Graph getGraph() {
        if (syntaxGraph == null)
            syntaxGraph = new NARGraph();
        return syntaxGraph;
    }
        
    
    public static Term addSyntax(NARGraph g, Term t) {
        if (t instanceof CompoundTerm) {
            CompoundTerm ct = (CompoundTerm)t;
            g.addVertex(ct);
            int n = 0;
            for (Term s : ct.term) {
                Term v = addSyntax(g, s);
                g.addEdge(ct, v, new UniqueEdge(ct.operator() + ":" + n));
                n++;
            }
            return ct;
        }
        else {
            g.addVertex(t);
            return t;
        }
    }


    
    protected void update(Term... t) {
        
        NARGraph g = new NARGraph();
        
        for (Term x : t) {
            addSyntax(g, x);
        }
        
        this.syntaxGraph = g;

    }
    
    
}
