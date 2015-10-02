package nars.guifx.demo;

import nars.Global;
import nars.NAR;
import nars.guifx.IOPane;
import nars.guifx.NARide;
import nars.guifx.graph2.CanvasEdgeRenderer;
import nars.guifx.graph2.HyperassociativeMapLayout;
import nars.guifx.graph2.NARGraph1;
import nars.guifx.graph2.NARGrapher;
import nars.guifx.util.TabX;
import nars.nar.Default;

import java.io.IOException;

/**
 * Created by me on 8/15/15.
 */
public class NARGraph1Test {

    public static NARGraph1 newGraph(NAR n) {
        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.5f;


        n.memory.conceptForgetDurations.set(5);

        //n.input(new File("/tmp/h.nal"));
        n.input("<a-->b>.");
        n.input("<b-->c>.");
        n.input("<a-->(c,b)>. %0.25;0.90%");
        //n.input("<(&&,a,b,ca)-->#x>?");

        n.frame(5);

        NARGraph1 g = new NARGraph1(n);
        //g.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        g.setUpdater(new NARGrapher(32));
        g.setEdgeRenderer(new CanvasEdgeRenderer());
        //g.setEdgeRenderer(new QuadPolyEdgeRenderer());

        //g.setLayout(new CircleLayout<>());
        g.setLayout(new HyperassociativeMapLayout());
        //g.setLayout(new TimelineLayout());

        return g;
    }

    public static void main(String[] args) throws IOException {


        NAR n = new Default(256, 2,3,4);

        NARide.show(n, ide -> {

            ide.content.getTabs().setAll(new TabX("Graph", newGraph(n)));
            ide.addView(new IOPane(n));


            n.frame(5);

        });

//        NARfx.run((a,b)-> {
//            b.setScene(
//                new Scene(newGraph(n), 600, 600)
//            );
//            b.show();
//
//            n.spawnThread(250, x -> {
//
//            });
//        });





//        TextOutput.out(n);
//        new Thread(() -> n.loop(185)).start();


    }
}
