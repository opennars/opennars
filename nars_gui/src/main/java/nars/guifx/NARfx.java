package nars.guifx;

import javafx.application.Application;
import javafx.stage.Stage;
import nars.Global;
import nars.NAR;
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
    public static Map<NAR, NARWindow> window = Global.newHashMap();

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

        NARWindow w = NARfx.window(n);
        w.show();

        for (String s : getParameters().getRaw()) {
            try {
                n.input(new File(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public static NARWindow window(NAR nar) {
        NARWindow wn = new NARWindow(nar);

        NARWindow removed = window.put(nar, wn);

        if (removed!=null)
            removed.close();

        return wn;
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
