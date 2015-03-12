package nars.gui;


import ca.nengo.model.SimulationException;
import ca.nengo.test.TestAgentNode;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.model.node.UINetwork;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import nars.core.NAR;
import nars.guifx.NARWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;


public class NARSim extends Application {


        static {
            NARSwing.themeInvert();
        }

        /** NAR instances -> GUI windows */
        public static Map<NAR, NARWindow> window = new HashMap();

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


    @Override
    public void start(Stage primaryStage) {
        //NARWindow w = NARfx.window(new NAR(new Default()));
        //w.show();

        Stage st = new Stage();

        StackPane root = new StackPane();

        SwingNode nnengo = new SwingNode();
        nnengo.setContent(new NARSim.NNengrow());

        root.getChildren().add(nnengo);

        BorderPane atmosphere = new BorderPane(); //above nengo's sky
        //atmosphere.setMouseTransparent(true);
        atmosphere.setPickOnBounds(false);

        TextArea tf = new TextArea();

        tf.setOpacity(0.75);

        tf.setStyle("-fx-background-color: transparent;");

        atmosphere.setBottom(tf);

        root.getChildren().add(atmosphere);

        st.setScene(new Scene(root));
        st.show();
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

        public static void main(String[] arg) {

//        Platform.runLater(new Runnable() {
//
//            @Override
//            public void run() {
//
//            }
//        });
            //new NARSim(new NAR(new Default()));

            Application.launch(NARSim.class);

        }

        private static NARWindow window(NAR nar) {
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

    //}


    public static class NNengrow extends Nengrow {


        @Override
        public void init() throws Exception {
            UINetwork networkUI = (UINetwork) addNodeModel(TestAgentNode.newAgentNodeDemo());
            networkUI.doubleClicked();


            new Timer(250, new ActionListener() {

                float time = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        float dt = getSimulationDT();
                        networkUI.node().run(time, time + dt);
                        time += dt;

                    } catch (SimulationException e1) {
                        e1.printStackTrace();
                    }
                    //cycle();
                }
            }).start();
        }

    }




}
