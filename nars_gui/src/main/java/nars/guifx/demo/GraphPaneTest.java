package nars.guifx.demo;

import javafx.scene.Scene;
import nars.$;
import nars.guifx.NARfx;
import nars.guifx.graph2.GraphSource;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.impl.BlurCanvasEdgeRenderer;
import nars.guifx.graph2.scene.DefaultVis;
import nars.guifx.graph2.source.DefaultGrapher;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.nal.DerivationRules;
import nars.nal.nal1.Inheritance;
import nars.term.Term;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;

import static nars.$.$;

/**
 * Created by me on 8/15/15.
 */
public class GraphPaneTest {

    static {
        DerivationRules.maxVarArgsToMatch = 2;
    }

    public static SpaceGrapher newGrapher() {


        Graph<Term, Inheritance> gg = newExampleGraph();

        SpaceGrapher<?,?> g = new DefaultGrapher(

                new GraphSource<Term>() {


                    @Override
                    public void start(SpaceGrapher<Term, ? extends TermNode<Term>> s) {

                    }

                    @Override public void accept(SpaceGrapher<Term, TermNode<Term>> s) {

                    }


                },


                128,

                new DefaultVis.HexagonVis(),


                new BlurCanvasEdgeRenderer());


        return g;
    }

    private static Graph<Term,Inheritance> newExampleGraph() {
        DirectedGraph<Term,Inheritance> g = new SimpleDirectedGraph<Term,Inheritance>(new EdgeFactory<Term,Inheritance>() {
            @Override public Inheritance createEdge(Term s, Term t) {
                return $.inh(s,t);
            }
        });

        g.addVertex($("x"));
        g.addVertex($("y"));
        g.addEdge($("x"), $("y"));

        return g;

    }

    public static void main(String[] args)  {



        NARfx.run((a, b)-> {
            b.setScene(
                new Scene(newGrapher(), 800, 800)
            );
            b.show();

//            n.spawnThread(250, x -> {
//
//            });
        });





//        TextOutput.out(n);
//        new Thread(() -> n.loop(185)).start();


    }

}
