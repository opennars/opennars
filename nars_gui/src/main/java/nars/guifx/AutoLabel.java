package nars.guifx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;


public abstract class AutoLabel<T> extends Label implements ChangeListener {


    protected T obj;
    protected float lastPri = -1;
    public final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);
    protected String text;

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public AutoLabel(T obj) {

        this.obj = obj;
        text = null;



        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);




    }

    protected abstract String getText(T t);

    public abstract void update();

    //TODO use a DoubleProperty
    protected abstract float getPriority(T obj);

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        if (text == null)
            setText(text = getText(obj));


        /* parent changed */
        //getChildren().clear();

        setGraphicTextGap(0);
        //getStylesheets().setAll();
        //getStyleClass().setAll();


        //setTooltip(new Tooltip().on);

        //setText(text);


        //label.getStyleClass().add("tasklabel_text");
        //setMouseTransparent(true);
        //label.setCacheHint(CacheHint.SCALE);
        //setPickOnBounds(false);
        //setSmooth(false);
        //setCache(true);


        setCenterShape(false);
        setPickOnBounds(true);

        setTextAlignment(TextAlignment.LEFT);

        float pri = getPriority(obj);
//        if (Precision.equals(lastPri, pri, 0.07)) {
//            return;
//        }
        lastPri = pri;
        setStyle(JFX.fontSize( ((1.0f + pri)*100.0f) ) );

        setTextFill(JFX.grayscale.get(pri*0.5+0.5));

        update();
        layout();
        //setCache(true);

    }
}
