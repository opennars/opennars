package nars.guifx;

import javafx.scene.Node;
import nars.NAR;
import nars.guifx.util.CodeInput;
import nars.guifx.util.Windget;
import nars.guifx.wikipedia.NARWikiBrowser;
import nars.nar.AbstractNAR;
import nars.nar.Default;

import java.util.function.Function;

/**
 * Created by me on 10/2/15.
 */
public class NARspace extends Spacegraph {

	private final NAR nar;

	public NARspace(NAR n) {

        nar = n;

        //BrowserWindow.createAndAddWindow(space, "http://www.google.com");


        Windget cc = new Windget("Edit", new CodeInput("ABC"), 300, 200).move(-10, -10);
        /*cc.addOverlay(new Windget.RectPort(cc, true, 0, 1, 20, 20));
        cc.addOverlay(new Windget.RectPort(cc, true, 0, 0, 20, 20));
        cc.addOverlay(new Windget.RectPort(cc, true, 1, 0, 20, 20));
        cc.addOverlay(new Windget.RectPort(cc, true, 1, 1, 20, 20));*/


        //Region jps = new FXForm(new NAR(new Default()));  // create the FXForm node for your bean



        AbstractNAR b = new Default();
        IOPane np = new IOPane(b);

        Windget nd = new Windget("NAR",
                np, 200, 200
        ).move(-200, 300);

        Function<Node, Node> wrap = (x) -> x;
        //addNodes(wrap, cc, nd);

        setNodes(
                new Windget("Web",
                        new NARWikiBrowser("Software"), 200, 200
                ).move(-200, 300)
        );
    }
}
