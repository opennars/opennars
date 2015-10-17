package nars.guifx;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import nars.Global;
import nars.NAR;
import nars.budget.Budget;
import nars.budget.Budgeted;
import nars.clock.FrameClock;
import nars.clock.RealtimeMSClock;
import nars.concept.Concept;
import nars.guifx.graph2.ConceptsSource;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.layout.Grid;
import nars.guifx.graph2.source.DefaultNARGraph;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.guifx.nars.LoopPane;
import nars.guifx.remote.VncClientApp;
import nars.guifx.terminal.LocalTerminal;
import nars.guifx.util.NControl;
import nars.guifx.util.TabPaneDetacher;
import nars.guifx.util.TabX;
import nars.nar.Default;
import nars.term.Atom;
import nars.term.Term;
import nars.util.NARLoop;
import nars.util.data.Util;
import nars.video.WebcamFX;
import org.jewelsea.willow.browser.WebBrowser;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static javafx.application.Platform.runLater;
import static nars.guifx.NARfx.scrolled;

/**
 * NAR ide panel
 */
public class NARide extends BorderPane {


    //private final TabPane taskBar = new TabPane();

    public final TabPane content = new TabPane();

    public final NARMenu controlPane;
    private final ScrollPane spp;
    public final PluginPanel pp;

    public final Map<Class, Function<Object,Node>> nodeBuilders = Global.newHashMap();
    private Map<Term, Supplier<? extends Node>> tools = new HashMap();


    public static void show(NARLoop loop, Consumer<NARide> ide) {

        //SizeAwareWindow wn = NARide.newWindow(nar, ni = new NARide(nar));

        NARfx.run((a, b) -> {

            NAR nar = loop.nar;
            NARide ni = new NARide(loop);

            {
                ni.addView(new IOPane(nar));

                /*ni.addView(new UDPPane(new UDPNetwork(
                        10001+(int)(Math.random()*5000) //HACK
                ).connect(nar)));*/

                /*ni.addIcon(() -> {
                    return new InputPane(nar);
                });*/
                ni.addIcon(() -> {
                    return new ConceptSonificationPanel(nar);
                });
                //ni.addView(additional components);
            }

            ni.addTool("I/O", () -> new IOPane(nar));
            ni.addTool("Active Concepts", () -> new LogPane2(nar));
            ni.addTool("Task Tree", () -> new TreePane(nar));
            ni.addTool("Concept Network", () -> new DefaultNARGraph(64, new ConceptsSource(nar)));
            ni.addTool("Fractal Workspace", () -> new NARspace(nar));


            ni.addTool("Webcam", WebcamFX::new);


            ni.addTool("Terminal (bash)", LocalTerminal::new);
            ni.addTool("Status", () -> new StatusPane(nar, 320));
            ni.addTool("VNC/RDP Remote", () -> (VncClientApp.newView()));
            ni.addTool("Web Browser", WebBrowser::new);

            ni.addTool("HTTP Server", Pane::new);

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

            scene.getStylesheets().setAll(NARfx.css);
            b.setScene(scene);


            b.setScene(scene);

            b.show();

            if (ide != null)
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


//    private static class UDPPane extends Pane {
//        public UDPPane(UDPNetwork n) {
//
//
//            //p = n.peer.getPeers();
//        }
//    }

    public class ToolDialog extends BorderPane {

        public ToolDialog(Consumer<Collection<Node>> results) {
            super();

            Set<Term> selected = new HashSet();


            SpaceGrapher<Term, TermNode<Term>> chooser =
                    SpaceGrapher.forCollection(tools.keySet(),
                            t -> t,
                            (t, tn) -> {
                                ToggleButton tb = new ToggleButton(t.toString());
                                tb.selectedProperty().addListener((c, p, v) -> {
                                    if (v) selected.add(t);
                                    else selected.remove(t);
                                });
                                tn.getChildren().add(tb);

                            }, new Grid());


//            chooser.minWidth(500);
//            chooser.prefWidth(500);
//            chooser.minHeight(500);
//            chooser.prefHeight(500);


            Button addButton = new Button("ADD");
            addButton.setDefaultButton(true);
            addButton.setOnMouseClicked((e) -> {
                results.accept(
                        selected.stream().map(s -> tools.get(s).get()).collect(Collectors.toList())
                );

                hide();
            });

            Button cancelButton = new Button("CANCEL");
            cancelButton.setOnMouseClicked((e) -> {
                hide();
            });

            FlowPane bottom = new FlowPane(cancelButton, addButton);
            bottom.setAlignment(Pos.CENTER_RIGHT);

            setCenter(new BorderPane(chooser));
            setBottom(bottom);
        }


        public void hide() {
            getChildren().clear();
            getScene().getWindow().hide();
        }
    }


    public void popupToolDialog(Consumer<Collection<Node>> x) {
        NARfx.popup(new ToolDialog(x));
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
        /* depr */
        controlPane.tool.getItems().add(mi);
        tools.put(Atom.the(name, true), builder);
    }

    public void addTool(Menu submenu) {
        /* depr */
        controlPane.tool.getItems().add(submenu);
    }


    public final NAR nar;


    public NARide(NARLoop l) {
        super();


        this.nar = l.nar;


        //default node builders
        //TODO make these Function<Object,Node>, not a supplier interface
        icon(FrameClock.class, (c) -> new NARMenu.CycleClockPane(nar));
        icon(RealtimeMSClock.class, (c) -> new NARMenu.RTClockPane(nar));
        icon(NARLoop.class, (ll) -> new LoopPane(l));
        icon(Default.DefaultCycle.class, (c) ->
                new DefaultCyclePane((Default.DefaultCycle)c) //cast is hack
        );


        spp = scrolled(pp = new PluginPanel(this));

        controlPane = new NARMenu(nar);
        Button addIcon = new Button("++");
        addIcon.setOnMouseClicked(e -> {
            popupToolDialog(
                    pp.getChildren()::addAll);
        });
        controlPane.getChildren().add(addIcon);


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

        runLater(() -> {
            TabPaneDetacher tabDetacher = new TabPaneDetacher();
            tabDetacher.makeTabsDetachable(content);
            /*tabDetacher.stylesheets(
                    getScene().getStylesheets().toArray(
                            new String[getScene().getStylesheets().size()]));*/
        });

        //autosize();

    }

    public <C extends Object> NARide icon(Class<C> c, Function<Object,Node> iconBuilder) {
        nodeBuilders.put(c, iconBuilder);
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

    private class DefaultCyclePane extends BorderPane {

        private final NAR nar;
        private final Default.DefaultCycle cycle;

        public DefaultCyclePane(Default.DefaultCycle l) {
            this.cycle = l;
            this.nar = l.nar;

            Button sleep = new Button("Sleep");
            sleep.setOnAction((e) -> {
                System.out.println("BEFORE CLEAR # concepts: " + cycle.concepts().size());
                cycle.concepts().clear();

                System.out.println(" AFTER CLEAR # concepts: " + cycle.concepts().size());
            });

            Button wake = new Button("Wake");
            wake.setOnAction((e) -> {
                System.out.println("Subconcepts to sample from: " + nar.concepts().size());
                System.out.println(" BEFORE WAKE # concepts: " + cycle.concepts().size());
                cycle.concepts().clear();

                Budget b = new Budget(0.1f, 0.1f, 0.1f);
                for (Concept c : nar.concepts()) {
                    cycle.activate(c.getTerm(), b);
                }

                System.out.println(" AFTER WAKE # concepts: " + cycle.concepts().size());

                cycle.concepts().forEach(x -> {
                    x.print(System.out);
                });

            });
            //TODO sample randomly from main nar index

            Button bake = new Button("Bake");
            //TODO scramble concept memory, replace random % with subconcepts

//            BudgetScatterPane b = new BudgetScatterPane(() -> cycle.concepts());
//            nar.onEachFrame((n) -> b.redraw());
//            setCenter(b);

            setBottom( new FlowPane(sleep, wake, bake) );
        }


    }
    public static class BudgetScatterPane<X extends Budgeted> extends NControl {
        private final Supplier<Iterable<X>> source;

        public BudgetScatterPane(Supplier<Iterable<X>> source) {
            super(350,250);
            this.source = source;
        }

        @Override
        protected void redraw() {

            GraphicsContext g = canvas.getGraphicsContext2D();
            double w = canvas.getWidth(), h = canvas.getHeight();
            g.clearRect(0, 0, w, h);

            final double iw = 6;
            final double ih = 6;

            if (source!=null) {
                Iterable<X> si = source.get();

                for (X i : si) {
                    Budget b = i.getBudget();

                    int c = i.hashCode();
                    Color f = NARfx.hashColor(c, b.summary(), Plot2D.ca);
                    g.setFill(f);

                    float p = b.getPriorityIfNaNThenZero();
                    double x = w * Math.abs(c % Util.PRIME2) / Util.PRIME2;
                    double y = h * p;
                    g.fillRect(x - iw / 2, y - ih / 2, iw, ih);
                }
            }
        }
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

//    public static SizeAwareWindow show(NAR n, Parent main) {
//
//
//        BorderPane summary;
//        {
//            LinePlot bp = new LinePlot(
//                    "Concepts",
//                    () -> (n.memory.getConcepts().size()),
//                    300
//            );
//
//            new FrameReaction(n) {
//
//                @Override
//                public void onFrame() {
//                                /*for (Object o : lp.getChildren()) {
//                                    if (o instanceof LinePlot)
//                                        ((LinePlot) o).update();
//                                }*/
//
//                    bp.draw();
//
//                }
//            };
//
//            summary = new BorderPane(bp);
//            bp.widthProperty().bind(summary.widthProperty());
//            bp.heightProperty().bind(summary.heightProperty());
//        }
//
//
//        return new SizeAwareWindow((d) -> {
//            double W = d[0];
//            double H = d[1];
//            if (W < 150 && H < 150) {
//                return () -> summary;
//            } else {
//                return () -> main;
//            }
//        });
//    }


}
