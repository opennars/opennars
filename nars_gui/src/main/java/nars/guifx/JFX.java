package nars.guifx;

import com.gs.collections.impl.map.mutable.primitive.FloatObjectHashMap;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import nars.guifx.util.ColorArray;
import org.apache.commons.math3.util.Precision;

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

    final static ColorArray grayscale = new ColorArray(
            128,
            Color.rgb(0, 0, 0, 1.0),
            Color.rgb(255,255,255,1.0)
    );

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

    static final FloatObjectHashMap<String> fontSizeStrings = new FloatObjectHashMap<>(128);

    public static String fontSize(final float v) {
        return fontSizeStrings.getIfAbsentPutWithKey(Precision.round(v, 1), _v -> {
            return "-fx-font-size: " + _v;
        });
    }

}
