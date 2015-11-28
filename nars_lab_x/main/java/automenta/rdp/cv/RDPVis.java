package automenta.rdp.cv;

import automenta.rdp.WrappedImage;
import javafx.scene.Node;

/**
 * Created by me on 7/22/15.
 */
public interface RDPVis {

    void redrawn(WrappedImage backstore, int x, int y, int wx, int wy);



    /**
	 * JavaFX component node
	 */
    Node getNode();


    void update();

}
