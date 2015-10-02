package nars.guifx;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import nars.Global;
import nars.NAR;
import nars.clock.CycleClock;
import nars.clock.RealtimeMSClock;
import nars.event.FrameReaction;
import nars.guifx.graph2.CanvasEdgeRenderer;
import nars.guifx.graph2.HyperassociativeMapLayout;
import nars.guifx.graph2.NARGraph;
import nars.guifx.graph2.NARGrapher;
import nars.guifx.remote.VncClientApp;
import nars.guifx.terminal.LocalTerminal;
import nars.guifx.util.CodeInput;
import nars.guifx.util.SizeAwareWindow;
import nars.guifx.util.TabX;
import nars.guifx.util.Windget;
import nars.guifx.wikipedia.NARWikiBrowser;
import nars.nar.Default;
import nars.video.WebcamFX;
import org.jewelsea.willow.browser.WebBrowser;
import za.co.knonchalant.builder.POJONode;
import za.co.knonchalant.builder.TaggedParameters;
import za.co.knonchalant.sample.pojo.SampleClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static javafx.application.Platform.runLater;
import static nars.guifx.NARfx.scrolled;

/**
 * NAR ide panel
 */
public class NARide extends BorderPane {


    //private final TabPane taskBar = new TabPane();

    public final TabPane content = new TabPane();

    public final NARControlFX controlPane;
    private final ScrollPane spp;
    private final PluginPanel pp;

    public final Map<Object,Supplier<Node>> iconNodeBuilders = Global.newHashMap();

    public static void show(NAR nar, Consumer<NARide> ide) {

        //SizeAwareWindow wn = NARide.newWindow(nar, ni = new NARide(nar));

        NARfx.run((a, b) -> {

            NARide ni = new NARide(nar);

            {
                ni.addView(new DemoNARSpace(nar));
                ni.addView(new IOPane(nar));
                /*ni.addIcon(() -> {
                    return new InputPane(nar);
                });*/
                ni.addIcon(()-> {
                   return new ConceptSonificationPanel(nar);
                });
                //ni.addView(additional components);
            }

            ni.addTool("I/O", () -> new IOPane(nar));
            ni.addTool("Task Tree", () -> new TreePane(nar));
            ni.addTool("Concept Network", () -> new DefaultNARGraph(nar));
            ni.addTool("Fractal Workspace", () -> new DemoNARSpace(nar));

            ni.addTool("Webcam", () -> new WebcamFX());


            ni.addTool("Terminal (bash)", () -> new LocalTerminal());
            ni.addTool("Status", () -> new StatusPane(nar));
            ni.addTool("VNC/RDP Remote", () -> (VncClientApp.newView()));
            ni.addTool("Web Browser", () -> new WebBrowser());

            ni.addTool("HTTP Server", () -> new Pane());

            ni.addTool(new Menu("Interface..."));
            ni.addTool(new Menu("Cognition..."));
            ni.addTool(new Menu("Sensor..."));


            //Button summaryPane = new Button(":D");

//            Scene scene = new SizeAwareWindow((d) -> {
//                double w = d[0];
//                double h = d[1];
//                if ((w < 200) && (h < 200)) {
//                    /*
//                    new LinePlot(
//                        "Concepts",
//                        () -> (nar.memory.getConcepts().size()),
//                        300
//                     */
//                    return () -> summaryPane;
//                }/* else if (w < 200) {
//                    return Column;
//                } else if (h < 200) {
//                    return Row;
//                }*/
//                return () -> ni;
//            });
            Scene scene = new Scene(ni, 900, 700,
                    false, SceneAntialiasing.DISABLED);

            scene.getStylesheets().setAll(NARfx.css, "dark.css" );
            b.setScene(scene);


            b.setScene(scene);

            b.show();

            if (ide!=null)
                ide.accept(ni);

            b.setOnCloseRequest((e) -> {
                System.exit(0);
            });
        });
//        SizeAwareWindow wn = NARide.newWindow(nar, ni = new NARide(nar));
//
//        ni.resize(500,500);
//
//        Stage s = new Stage();
//        s.setScene(wn);
//        //s.sizeToScene();
//
//
//
//        s.show();
//
//        Stage removed = window.put(nar, s);
//
//        if (removed!=null)
//            removed.close();
//
//        return ni;
    }


    public static class DemoNARSpace extends Spacegraph {

        private final NAR nar;

        public DemoNARSpace(NAR n) {
            super();

            this.nar = n;

            //BrowserWindow.createAndAddWindow(space, "http://www.google.com");


            Windget cc = new Windget("Edit", new CodeInput("ABC"), 300, 200).move(-10,-10);
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
            ).move(-200,300);

            Function<Node, Node> wrap = (x) -> {
                return x;
            };
            addNodes(wrap, cc, wd, nd);

            addNodes(
                new Windget("Web",
                    new NARWikiBrowser("Software"), 200, 200
                ).move(-200,300)
            );
        }
    }

    public void addIcon(FXIconPaneBuilder n) {
        nar.memory().the(n);
    }

    public void addView(Pane n) {
        nar.memory().the(n);

        content.getTabs().add(new TabX(
            n.getClass().getSimpleName(),
            n));
    }

    public void addTool(String name, Supplier<Pane> builder) {
        MenuItem mi = new MenuItem(name);
        mi.setOnAction((e) -> {
            addView(builder.get());
        });
        controlPane.tool.getItems().add(mi);
    }

    public void addTool(Menu submenu) {
        controlPane.tool.getItems().add(submenu);
    }


    public final NAR nar;


    public NARide(NAR n) {
        super();
        this.nar = n;

        //default node builders
        icon(CycleClock.class, () -> new NARControlFX.CycleClockPane(nar) );
        icon(RealtimeMSClock.class, () -> new NARControlFX.RTClockPane(nar) );

//        runLater(() -> {
//                    TabPaneDetacher tabDetacher = new TabPaneDetacher();
//                    tabDetacher.makeTabsDetachable(content);
//                    tabDetacher.stylesheets(getScene().getStylesheets().toArray(new String[getScene().getStylesheets().size()]));
//        });



        controlPane = new NARControlFX(nar);


        final BorderPane f = new BorderPane();


        /*LinePlot lp = new LinePlot(
                "Concepts",
                () -> (nar.memory.getConcepts().size()),
                300
        );*/
//        LinePlot lp2 = new LinePlot(
//                "Happy",
//                () -> nar.memory.emotion.happy(),
//                300
//        );
//
//        VBox vb = new VBox(lp, lp2);
//        vb.autosize();


        spp = scrolled(pp = new PluginPanel(this));

//        //taskBar.setSide(Side.LEFT);
//        taskBar.getTabs().addAll(
//
//                new TabX("Plugins",
//                        spp).closeable(false),
//
//                new TabX("Tasks",
//                        new TreePane(n)).closeable(false),
//
//                /*new TabX.TabButton("+",
//                        scrolled(new NARReactionPane()))
//                        .button("I/O", (e) -> {
//                        })
//                        .button("Graph", (e) -> {
//                        })
//                        .button("About", (e) -> {
//                        })
//                ,*/
//
//
//                new TabX("Concepts",
//                        new VBox()).closeable(false),
//
//
////                new TabX("Stats",
////                    new VBox()).closeable(false),
//
//                new TabX("InterNAR",
//                        new VBox()).closeable(false)
//
//
//        );

        //taskBar.setRotateGraphic(true);

        //f.setCenter(taskBar);
        f.setCenter(spp);

        f.setTop(controlPane);


        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        //  content.getTabs().add(new Tab("I/O", new TerminalPane(nar)));


        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        f.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        content.setVisible(true);

        SplitPane p = new SplitPane();
        p.getItems().setAll(f, content);
        p.setDividerPositions(0.5f);

        setCenter(p);

        //autosize();

    }

    public NARide icon(Class c, Supplier<Node> iconBuilder) {
        iconNodeBuilders.put(c, iconBuilder);
        return this;
    }

    public Stage show() {

        return NARfx.newWindow(nar.toString(), this);

    }


    public void contentUpdate(boolean show) {

        runLater(() -> {
            if (!show) {
                content.setVisible(false);
            } else {
                content.setVisible(true);
            }

            layout();
            //g.autosize();

            //p.setDividerPosition(0, 0.25);

//                if (!isMaximized())
//                    sizeToScene();
        });

    }


//    public class NARReactionPane extends NARCollectionPane<Reaction> {
//
//        public NARReactionPane() {
//            super(nar, r ->
//                            new Label(r.toString())
//            );
//        }
//
//        @Override
//        public void collect(Consumer<Reaction> c) {
//            nar.memory.exe.forEachReaction(c);
//        }
//    }

    public static SizeAwareWindow show(NAR n, Parent main) {


        BorderPane summary;
        {
            LinePlot bp = new LinePlot(
                    "Concepts",
                    () -> (n.memory.getConcepts().size()),
                    300
            );

            new FrameReaction(n) {

                @Override
                public void onFrame() {
                                /*for (Object o : lp.getChildren()) {
                                    if (o instanceof LinePlot)
                                        ((LinePlot) o).update();
                                }*/

                    bp.draw();

                }
            };

            summary = new BorderPane(bp);
            bp.widthProperty().bind(summary.widthProperty());
            bp.heightProperty().bind(summary.heightProperty());
        }









        return new SizeAwareWindow((d) -> {
            double W = d[0];
            double H = d[1];
            if (W < 150 && H < 150) {
                return () -> summary;
            } else {
                return () -> main;
            }
        });
    }


    /** provides defalut settings for a NARGraph view */
    public static class DefaultNARGraph extends NARGraph {
        public DefaultNARGraph(NAR nar) {
            super(nar);

            setUpdater(new NARGrapher(32));


            setEdgeRenderer(new CanvasEdgeRenderer());
            //g.setEdgeRenderer(new QuadPolyEdgeRenderer());


            //g.setLayout(new CircleLayout<>());
            setLayout(new HyperassociativeMapLayout());
            //g.setLayout(new TimelineLayout());

        }
    }
}
