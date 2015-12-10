package nars.guifx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;

/**
 * Created by me on 10/16/15.
 */
public abstract class SummaryIcon extends Canvas implements ChangeListener<Number> {


    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public SummaryIcon() {
    }


    public SummaryIcon size(double w, double h) {
        if (getWidth()!=w && getHeight()!=h) {
            setWidth(w);
            setHeight(h);
            repaint();
        }
        return this;
    }

    public SummaryIcon width(double w) {
        if (getWidth()!=w) {
            setWidth(w);
            repaint();
        }
        return this;
    }


    protected abstract void repaint();


    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        double h = newValue.doubleValue();
        h *= 0.5;

        setHeight(h);
        double w = h * 3;
        setWidth(w);
        prefWidth(w);

        repaint();
    }

}
