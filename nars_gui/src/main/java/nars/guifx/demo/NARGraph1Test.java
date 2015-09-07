package nars.guifx.demo;

import nars.Global;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.graph2.HyperassociativeMapLayout;
import nars.guifx.graph2.NARGraph1;
import nars.guifx.graph2.NARGrapher;
import nars.guifx.graph2.QuadPolyEdgeRenderer;
import nars.guifx.terminal.LocalTerminal;
import nars.guifx.util.TabX;
import nars.nar.experimental.Equalized;

import java.io.IOException;

/**
 * Created by me on 8/15/15.
 */
public class NARGraph1Test {

    public static void main(String[] args) throws IOException {

        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.5f;
        NAR n = new NAR(
            new Equalized(60, 4, 3).setInternalExperience(null)
        );
        //n.input(new File("/tmp/h.nal"));
        n.input("<a-->b>.");
        n.input("<b-->c>.");

        n.frame(5);

        NARfx.newWindow(n, ide -> {
            NARGraph1 g = new NARGraph1(n);
            //g.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            g.setUpdater(new NARGrapher());
            //g.setEdgeRenderer(new CanvasEdgeRenderer<>());
            g.setEdgeRenderer(new QuadPolyEdgeRenderer());

            //g.setLayout(new CircleLayout<>());
            g.setLayout(new HyperassociativeMapLayout());
            //g.setLayout(new TimelineLayout());


            ide.content.getTabs().add(new TabX("Graph", g));
        });





//        TextOutput.out(n);
//        new Thread(() -> n.loop(185)).start();


    }
}
