package nars.guifx.graph3;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by me on 8/14/15.
 */
public abstract class SpacenetApp extends Application {
    SpaceNet root = new SpaceNet() {
        @Override
        public Xform getRoot() {
            return SpacenetApp.this.getRoot();
        }
    };

    public abstract Xform getRoot();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(root.content, 1024, 768, true);
        root.handleKeyboard(scene);
        root.handleMouse(scene);

        primaryStage.setTitle("?");
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
