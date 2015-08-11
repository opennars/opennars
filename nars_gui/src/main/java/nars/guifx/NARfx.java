package nars.guifx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.nar.Default;

import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 *
 * @author me
 */
public class NARfx extends Application {

    static final String css = NARfx.class.getResource("narfx.css").toExternalForm();

//    static {
//        Video.themeInvert();
//    }

    /** NAR instances -> GUI windows */
    public static Map<Region, Stage> window = Global.newHashMap();

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



        NAR n = new NAR(new Default());

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
        scene.getStylesheets().addAll(NARfx.css, "dark.css" );



        s.setScene(scene);

        n.setMaxWidth(Double.MAX_VALUE);
        n.setMaxHeight(Double.MAX_VALUE);

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

    //final static public Font monospace = new Font("Monospace", 14);

    public static Font mono(double v) {
        return Font.font("Monospace", v);
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
    
}
