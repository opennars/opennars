package nars.guifx.annotation;

import javafx.beans.property.SimpleObjectProperty;

/**
 * Annotate with @Implementation
 */
public class ImplementationProperty<C> extends SimpleObjectProperty<Class<? extends C>> {

    public C getInstance() {
        Class<? extends C> lc = get();
        if (lc != null) {
            try {
                C il = lc.newInstance();
                return il;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }


}
