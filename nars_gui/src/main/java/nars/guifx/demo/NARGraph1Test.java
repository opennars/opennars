package nars.guifx.demo;

import nars.Global;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.graph2.CircleLayout;
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

    public static void main(String[] args) throws IOException {

        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.5f;
        NAR n = new NAR(
            new Default(60, 1, 1).setInternalExperience(null)
        );
        //n.input(new File("/tmp/h.nal"));
        n.input("<a-->b>.");
        n.input("<b-->c>.");

        n.frame(500);

        NARfx.newWindow(n, ide -> {
            NARGraph1 g = new NARGraph1(n);
            //g.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            g.setUpdater(new NARGrapher());

            //g.setLayout(new CircleLayout<>());
            g.setLayout(new HyperassociativeMapLayout());
            //g.setLayout(new TimelineLayout());

            ide.content.getTabs().add(new TabX("Graph", g));
        });




//        TextOutput.out(n);
//        new Thread(() -> n.loop(185)).start();


    }
}
