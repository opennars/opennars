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
import nars.clock.FrameClock;
import nars.clock.RealtimeMSClock;
import nars.event.FrameReaction;
import nars.guifx.graph2.DefaultNARGraph;
import nars.guifx.remote.VncClientApp;
import nars.guifx.terminal.LocalTerminal;
import nars.guifx.util.SizeAwareWindow;
import nars.guifx.util.TabPaneDetacher;
import nars.guifx.util.TabX;
import nars.util.NARLoop;
import nars.video.WebcamFX;
import org.jewelsea.willow.browser.WebBrowser;

import java.util.Map;
import java.util.function.Consumer;
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
    public final PluginPanel pp;

    public final Map<Object,Supplier<Node>> iconNodeBuilders = Global.newHashMap();

    public static void show(NARLoop loop, Consumer<NARide> ide) {

        //SizeAwareWindow wn = NARide.newWindow(nar, ni = new NARide(nar));

        NARfx.run((a, b) -> {

            NAR nar = loop.nar;
            NARide ni = new NARide(loop);

            {
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
            ni.addTool("Concept Network", () -> new DefaultNARGraph(nar,64));
            ni.addTool("Fractal Workspace", () -> new NARspace(nar));

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


    public void addIcon(FXIconPaneBuilder n) {
        nar.memory().the(n);
        pp.update();
    }

    public void addView(Pane n) {
        nar.memory().the(n);

        content.getTabs().add(new TabX(
            n.getClass().getSimpleName(),
            n));
        pp.update();
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


    public NARide(NARLoop l) {
        super();

        this.nar = l.nar;

        //default node builders
        icon(FrameClock.class, () -> new NARControlFX.CycleClockPane(nar) );
        icon(RealtimeMSClock.class, () -> new NARControlFX.RTClockPane(nar) );
        icon(NARLoop.class, () -> new NARControlFX.LoopPane(l) );

        runLater(() -> {
                    TabPaneDetacher tabDetacher = new TabPaneDetacher();
                    tabDetacher.makeTabsDetachable(content);
                    tabDetacher.stylesheets(getScene().getStylesheets().toArray(new String[getScene().getStylesheets().size()]));
        });



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


}
