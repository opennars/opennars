package nars.guifx;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;
import javafx.scene.layout.FlowPane;
import nars.guifx.util.NSlider;

import java.util.Map;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 9/13/15.
 */
public class NSliderSet<K> extends FlowPane {
    protected Map<K, NSlider> data = new ConcurrentHashMap<>();


    public NSliderSet() {
    }

    public final double normalized(K k, int dimension) {
        NSlider s = data.computeIfAbsent(k, this::newControl);
        if (s == null) return Double.NaN;
        return s.normalized()[dimension];
    }

    public final NSlider value(K k, double... newValue) {
        NSlider s = data.computeIfAbsent(k, this::newControl);
        s.value(newValue);
        return s;
    }

    public NSlider newControl(K k) {
        NSlider s = new NSlider(k.toString(), 80, 25, NSlider.NotchSlider, 0.5);
        runLater(() -> {
            getChildren().add(s);
            setNeedsLayout(true);
            layout();
        } );

        return s;
    }

//    protected void update() {
//
////        getChildren().clear();
////        addRow(0, data.values().toArray(new Node[0]));
//    }


}
