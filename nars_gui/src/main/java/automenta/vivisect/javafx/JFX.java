package automenta.vivisect.javafx;

import com.sun.javafx.tk.Toolkit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

}
