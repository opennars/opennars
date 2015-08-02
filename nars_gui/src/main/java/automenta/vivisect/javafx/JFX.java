package automenta.vivisect.javafx;

import com.sun.javafx.tk.Toolkit;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by me on 2/22/15.
 */
public class JFX {
    public static void popup(Parent n) {
        Platform.runLater(() -> {
            Stage st = new Stage();

            st.setScene(new Scene(n));
            st.show();
        });
    }

    static {
        Toolkit.getToolkit().init();
    }

   public static void popup(Application a) {

       Platform.runLater(() -> {
           Stage st = new Stage();

           BorderPane root = new BorderPane();
           st.setScene(new Scene(root));
           try {
               a.start(st);
           } catch (Exception ex) {
               //Logger.getLogger(NetentionJFX.class.getName()).log(Level.SEVERE, null, ex);
               ex.printStackTrace();
           }

           st.show();
       });
    }

    public static Button newIconButton(FontAwesomeIcon i) {
        Button b = GlyphsDude.createIconButton(i);
        b.getStyleClass().addAll("menubutton");
        b.getGraphic().getStyleClass().addAll("menubuttonLabel");
        return b;
    }

    public static ToggleButton newToggleButton(FontAwesomeIcon i) {
        ToggleButton b = GlyphsDude.createIconToggleButton(i, "", "", ContentDisplay.CENTER);
        b.getStyleClass().addAll("menubutton");
        b.getGraphic().getStyleClass().addAll("menubuttonLabel");
        return b;
    }
}
