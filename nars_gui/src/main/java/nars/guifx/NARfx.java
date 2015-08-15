package nars.guifx;

import automenta.vivisect.javafx.graph2.NARGraph1;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.nar.Default;
import nars.nar.NewDefault;
import nars.task.Task;

import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 *
 * @author me
 */
public class NARfx extends Application {

    public static final String css = NARfx.class.getResource("narfx.css").toExternalForm();

//    static {
//        Video.themeInvert();
//    }

    /** NAR instances -> GUI windows */
    public static Map<Region, Stage> window = Global.newHashMap();

    public static final javafx.scene.control.ScrollPane scrolled(Node n) {
        return scrolled(n, true, true);
    }

    public static final javafx.scene.control.ScrollPane scrolled(Node n, boolean stretchwide, boolean stretchhigh) {
        javafx.scene.control.ScrollPane s = new javafx.scene.control.ScrollPane();
        s.setHbarPolicy(stretchwide ? javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED : javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        s.setVbarPolicy(stretchwide ? javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED : javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);

        s.setContent(n);

        if (stretchhigh) {
            s.setMaxHeight(Double.MAX_VALUE);
        }
        s.setFitToHeight(true);

        if (stretchwide) {
            s.setMaxWidth(Double.MAX_VALUE);
        }
        s.setFitToWidth(true);

        //s.autosize();
        return s;
    }

//    public void start_(Stage primaryStage) {
//        primaryStage.setTitle("Tree View Sample");
//
//        CheckBoxTreeItem<String> rootItem =
//                new CheckBoxTreeItem<String>("View Source Files");
//        rootItem.setExpanded(true);
//
//        final TreeView tree = new TreeView(rootItem);
//        tree.setEditable(true);
//
//        tree.setCellFactory(CheckBoxTreeCell.<String>forTreeView());
//        for (int i = 0; i < 8; i++) {
//            final CheckBoxTreeItem<String> checkBoxTreeItem =
//                    new CheckBoxTreeItem<String>("Sample" + (i+1));
//            rootItem.getChildren().add(checkBoxTreeItem);
//        }
//
//        tree.setRoot(rootItem);
//        tree.setShowRoot(true);
//
//        StackPane root = new StackPane();
//        root.getChildren().add(tree);
//        primaryStage.setScene(new Scene(root, 300, 250));
//        primaryStage.show();
//    }




//    static {
//        Toolkit.getToolkit().init();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Application.launch(NARfx.class);
//            }
//        }).start();
//
//    }

    @Override
    public void start(Stage primaryStage) {


        //Default d = new Default();

        /*Default d = new Equalized(1024,4,5);
        d.setCyclesPerFrame(4);
        d.setTermLinkBagSize(96);
        d.setTaskLinkBagSize(96);*/

        Default d = new NewDefault();

        NAR n = new NAR(d);

        NARPane w = NARfx.window(n);


        for (String s : getParameters().getRaw()) {
            try {
                n.input(new File(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            n.input(new File("/tmp/h.nal")); //temporary
        } catch (IOException e) {
            e.printStackTrace();
        }

        primaryStage.setOnCloseRequest(e -> {
            n.stop();
        });

        {

            NARGraph1 g = new NARGraph1(w.nar);

//            final TilePane lp = new TilePane(4,4,
////                        new LinePlot("Total Priority", () ->
////                            nar.memory.getActivePrioritySum(true, true, true)
////                        , 128),
//                    new LinePlot("Concept Priority", () -> {
//                        int c = nar.memory.getControl().size();
//                        if (c == 0) return 0;
//                        else return nar.memory.getActivePrioritySum(true, false, false) / (c);
//                    }, 128),
//                    new LinePlot("Link Priority", () ->
//                            nar.memory.getActivePrioritySum(false, true, false)
//                            , 128),
//                    new LinePlot("TaskLink Priority", () ->
//                            nar.memory.getActivePrioritySum(false, false, true)
//                            , 128)
//            );
//            lp.setPrefColumns(2);
//            lp.setPrefRows(2);
//
//            new CycleReaction(w.nar) {
//
//                @Override
//                public void onCycle() {
//                    for (Object o : lp.getChildren()) {
//                        if (o instanceof LinePlot)
//                            ((LinePlot)o).update();
//                    }
//                }
//            };
//
//            lp.setOpacity(0.5f);
//            lp.setPrefSize(200,200);
//            lp.maxWidth(Double.MAX_VALUE);
//            lp.maxHeight(Double.MAX_VALUE);
//            lp.setMouseTransparent(true);
//            lp.autosize();


//                StackPane s = new StackPane(lp);
//                s.maxWidth(Double.MAX_VALUE);
//                s.maxHeight(Double.MAX_VALUE);


            w.content.getTabs().add(new TabX("Terminal", new TerminalPane(w.nar) ));
            w.content.getTabs().add(new TabX("Graph", g ));
        }
        //startup defaults
        w.console(true);


        //JFX.popup(new NodeControlPane());

        /*
        WebBrowser w = new WebBrowser();


        primaryStage.setTitle("title");
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });

        try {
            w.start(primaryStage);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/

    }


    public static void main(String[] arg) {


//        Platform.runLater(new Runnable() {
//
//            @Override
//            public void run() {
//
//            }
//        });

        Application.launch(NARfx.class, arg);

    }

    public static Stage getStage(String title, Region n) {
        Stage s = new Stage();

        Scene scene = new Scene(n);
        s.setTitle(title);


        scene.getStylesheets().setAll(NARfx.css, "dark.css" );


        s.setScene(scene);

        n.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        return s;
    }


    public static NARPane window(NAR nar) {
        NARPane wn = new NARPane(nar);

        Stage s;
        wn.setPrefSize(900, 600);
        Stage removed = window.put(wn, s = wn.newStage());

        s.show();

        if (removed!=null)
            removed.close();

        return wn;
    }

    public static void window(NAR nar, Concept c) {
        ConceptPane wn = new ConceptPane(nar, c);

        Stage st;
        Stage removed = window.put(wn, st = getStage(c.toString(), wn));

        st.show();

        if (removed!=null)
            removed.close();
    }

    public static void window(NAR nar, Task c) {
        TaskPane wn = new TaskPane(nar, c);

        Stage st;
        Stage removed = window.put(wn, st = getStage(c.toString(), wn));

        st.show();

        if (removed!=null)
            removed.close();
    }

    //final static public Font monospace = new Font("Monospace", 14);

    final static int fontPrecision = 4; //how many intervals per 1.0 to round to
    static final IntObjectHashMap<Font> monoFonts = new IntObjectHashMap();

    public static Font mono(double v) {
        //[Dialog, SansSerif, Serif, Monospaced, DialogInput]
        if (v < 1) v = 1;

        final int i = (int)Math.round(v * fontPrecision);

        final double finalV = v;

        return monoFonts.getIfAbsentPut(i, () -> {
            return Font.font("Monospaced", finalV);
        });
    }

//   static void popup(Core core, Parent n) {
//        Stage st = new Stage();
//
//        st.setScene(new Scene(n));
//        st.show();
//    }
//   static void popup(Core core, Application a) {
//        Stage st = new Stage();
//
//        BorderPane root = new BorderPane();
//        st.setScene(new Scene(root));
//        try {
//            a.start(st);
//        } catch (Exception ex) {
//            Logger.getLogger(NARfx.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        st.show();
//    }
//
//    static void popupObjectView(Core core, NObject n) {
//        Stage st = new Stage();
//
//        BorderPane root = new BorderPane();
//
//        WebView v = new WebView();
//        v.getEngine().loadContent(ObjectEditPane.toHTML(n));
//
//        root.setCenter(v);
//
//        st.setTitle(n.id);
//        st.setScene(new Scene(root));
//        st.show();
//    }



    /* https://macdevign.wordpress.com/2014/03/27/running-javafx-application-instance-in-the-main-method-of-a-class/ */
    public static void run(AppLaunch appLaunch, String... sArArgs) {
        DummyApplication.appLaunch = appLaunch;
        DummyApplication.launch(DummyApplication.class, sArArgs);
    }

    // This must be public in order to instantiate successfully
    public static class DummyApplication extends Application {

        private static AppLaunch appLaunch;

        @Override
        public void start(Stage primaryStage) throws Exception {

            if (appLaunch != null) {
                appLaunch.start(this, primaryStage);
            }
        }

        @Override
        public void init() throws Exception {
            if (appLaunch != null) {
                appLaunch.init(this);
            }
        }

        @Override
        public void stop() throws Exception {
            if (appLaunch != null) {
                appLaunch.stop(this);
            }
        }
    }

    @FunctionalInterface
    public static interface AppLaunch {
        void start(Application app, Stage stage) throws Exception;
        // Remove default keyword if you need to run in Java7 and below
        default void init(Application app) throws Exception {
        }

        default void stop(Application app) throws Exception {
        }
    }

}
