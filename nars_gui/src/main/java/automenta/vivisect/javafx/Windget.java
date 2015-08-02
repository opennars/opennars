package automenta.vivisect.javafx;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import jfxtras.scene.control.window.Window;

/** window widget */
public class Windget extends Window {

    public Windget(String title, Node content) {
        super(title);
        getContentPane().getChildren().add(content);
        autosize();
    }

    public Windget(String title, Region content, double width, double height) {
        this(title, content);

        //content.setAutoSizeChildren(true);
        content.resize(width,height);
        autosize();
    }

//        public Windget size(final double width, final double height) {
//            getContentPane().setPrefSize(width, height);
//            getContentPane().setMinSize(width, height);
//
//            return this;
//        }
}
