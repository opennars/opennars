package objenome.util.bean;

import org.junit.Test;

public class BooleanIFaceTest {

    public interface BooleanGetterGet {

        void setMyBool(boolean xy);

        boolean getMyBool();
    }

    public interface BooleanGetterIs {

        void setMyBool(boolean xy);

        boolean isMyBool();
    }

    @Test
    public void testBooleanGetter() {
        BeanProxyBuilder.on(BooleanGetterGet.class).build();
        BeanProxyBuilder.on(BooleanGetterIs.class).build();
    }

}
