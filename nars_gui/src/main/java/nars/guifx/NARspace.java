package nars.guifx;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import nars.NAR;
import nars.guifx.util.CodeInput;
import nars.guifx.util.Windget;
import nars.guifx.wikipedia.NARWikiBrowser;
import nars.nar.Default;
import za.co.knonchalant.builder.POJONode;
import za.co.knonchalant.builder.TaggedParameters;
import za.co.knonchalant.sample.pojo.SampleClass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by me on 10/2/15.
 */
public class NARspace extends Spacegraph {

    private final NAR nar;

    public NARspace(NAR n) {
        super();

        this.nar = n;

        //BrowserWindow.createAndAddWindow(space, "http://www.google.com");


        Windget cc = new Windget("Edit", new CodeInput("ABC"), 300, 200).move(-10, -10);
        /*cc.addOverlay(new Windget.RectPort(cc, true, 0, 1, 20, 20));
        cc.addOverlay(new Windget.RectPort(cc, true, 0, 0, 20, 20));
        cc.addOverlay(new Windget.RectPort(cc, true, 1, 0, 20, 20));
        cc.addOverlay(new Windget.RectPort(cc, true, 1, 1, 20, 20));*/


        //Region jps = new FXForm(new NAR(new Default()));  // create the FXForm node for your bean


        TaggedParameters taggedParameters = new TaggedParameters();
        List<String> range = new ArrayList<>();
        range.add("Ay");
        range.add("Bee");
        range.add("See");
        taggedParameters.addTag("range", range);
        Pane jps = POJONode.build(new SampleClass(), taggedParameters);

//        Button button = new Button("Read in");
//        button.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                //SampleClass sample = POJONode.read(mainPane, SampleClass.class);
//                //System.out.println(sample.getTextString());
//            }
//        });

        jps.setStyle("-fx-font-size: 75%");
        Windget wd = new Windget("WTF",
                jps,
                //new Button("XYZ"),
                400, 400);
        //wd.addOverlay(new Windget.RectPort(wc, true, 0, +1, 10, 10));


        final Default b = new Default();
        IOPane np = new IOPane(b);

        Windget nd = new Windget("NAR",
                np, 200, 200
        ).move(-200, 300);

        Function<Node, Node> wrap = (x) -> {
            return x;
        };
        addNodes(wrap, cc, wd, nd);

        addNodes(
                new Windget("Web",
                        new NARWikiBrowser("Software"), 200, 200
                ).move(-200, 300)
        );
    }
}
