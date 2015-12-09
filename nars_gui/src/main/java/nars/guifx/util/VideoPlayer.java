package nars.guifx.util;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class VideoPlayer extends Application {

  public static void main(String[] args) {
    launch(args);
  }
  
  @Override
  public void start(Stage primaryStage) {


    Media media = new Media(
            //"file://tmp/oow2010-2.flv"
            //"http://mirror.bigbuckbunny.de/peach/bigbuckbunny_movies/big_buck_bunny_480p_h264.mov"
            "http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv"
    );

    System.out.println( media.getDuration() );

    BorderPane root = new BorderPane();
    root.maxWidth(Double.MAX_VALUE);
    root.maxHeight(Double.MAX_VALUE);


    MediaPlayer mp = new MediaPlayer(media);
    mp.setAutoPlay(true);


    MediaView mv = new MediaView(mp);

//    final DoubleProperty width = mv.fitWidthProperty();
//    final DoubleProperty height = mv.fitHeightProperty();
//
//    width.bind(scene.widthProperty());
//    height.bind(scene.heightProperty());

    mv.setPreserveRatio(true);

    Scene scene = new Scene(root, 960, 540);
    scene.setFill(Color.BLACK);

    primaryStage.setScene(scene);
    primaryStage.setFullScreen(true);
    primaryStage.show();


    mp.play();
  }
}