package automenta.rdp.cv;

import automenta.rdp.RdesktopCanvas;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class RDPCV  {
    private final RdesktopCanvas canvas;
    private Image backgroundImage;

    ImageView baseView;
    private WritableImage baseImage = null;

    public RDPCV(RdesktopCanvas canvas) {
        this.canvas = canvas;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("CV");
                final JFXPanel fxPanel = new JFXPanel();
                frame.add(fxPanel);
                frame.setSize(300, 200);
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        init(fxPanel);

                        new AnimationTimer() {

                            @Override
                            public void handle(long now) {
                                baseView.setImage(getBase());

                                for (RDPVis v : canvas.vis) {
                                    v.update();
                                }
                            }

                        }.start();
                    }
                });
            }
        });
    }

    private void init(JFXPanel fxPanel) {



        baseView = new ImageView(
                getBase()
        );

        baseView.setOpacity(0.75f);

        Scene scene = createScene();
        fxPanel.setScene(scene);


    }

    public Image getBase() {
        baseImage = SwingFXUtils.toFXImage(canvas.backstore.getImage(), baseImage);

        return baseImage;
    }

    private Scene createScene() {

        // construct the scene contents over a stacked background.
        StackPane layout = new StackPane();
        layout.getChildren().setAll(
                baseView//,
                //b
        );

        //a.setOpacity(0.5f);

        for (RDPVis v : canvas.vis) {
            Node n = v.getNode();
            if (n!=null)
                layout.getChildren().add(n);
        }

        // wrap the scene contents in a pannable scroll pane.
        //ScrollPane scroll = createScrollPane(layout);


        // bind the preferred size of the scroll area to the size of the scene.
        //scroll.prefWidthProperty().bind(scene.widthProperty());
        //scroll.prefHeightProperty().bind(scene.widthProperty());

        // center the scroll contents.
        //scroll.setHvalue(scroll.getHmin() + (scroll.getHmax() - scroll.getHmin()) / 2);
        //scroll.setVvalue(scroll.getVmin() + (scroll.getVmax() - scroll.getVmin()) / 2);

        // show the scene.
        Scene scene = new Scene(layout); //scroll);
        return scene;
    }



    /**
     * @return a ScrollPane which scrolls the layout.
     */
    private ScrollPane createScrollPane(Pane layout) {
        ScrollPane scroll = new ScrollPane();
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setPrefSize(800, 600);
        scroll.setContent(layout);
        return scroll;
    }


}