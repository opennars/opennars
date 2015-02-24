package automenta.vivisect.javafx;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by me on 2/22/15.
 */
public class JFX {
    public static void popup(Parent n) {
        Stage st = new Stage();

        st.setScene(new Scene(n));
        st.show();
    }
   public static void popup(Application a) {
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
    }

}
