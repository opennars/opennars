package nars.guifx;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;
import javafx.scene.layout.Pane;
import nars.guifx.util.NSlider;

import java.util.Map;

/**
 * Created by me on 9/13/15.
 */
public class NSliderSet<K> {
    protected Map<K, NSlider> data = new ConcurrentHashMap<>();


    public final double value(K k) {
        return data.get(k).value.doubleValue();
    }

    public final NSlider value(K k, double newValue) {
        NSlider s = data.computeIfAbsent(k, this::newControl);
        s.value.set(newValue);
        return s;
    }

    public NSlider newControl(K k) {
        return new NSlider(k.toString(), 80, 25);
    }

    public Pane addTo(Pane p) {
        p.getChildren().addAll(data.values());
        return p;
    }

}
