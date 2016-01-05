package objenome.util.bean;

import objenome.util.bean.anno.Initializer;
import objenome.util.bean.anno.InitializerMethod;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InitializerTest {

    @Initializer(InitTestBean.Init.class)
    public interface InitTestBean {

        enum Init {
            ;

            @InitializerMethod
            public static void initialize(InitTestBean bean) {
                bean.setValueTrue(true);
                bean.setNonNullValue("not null"); //$NON-NLS-1$
            }

        }

        boolean isValueTrue();

        void setValueTrue(boolean value);

        String getNonNullValue();

        void setNonNullValue(String value);

    }

    @Test
    public void testInit() {
        InitTestBean bean = BeanProxyBuilder.on(InitTestBean.class).build();
        assertTrue(bean.isValueTrue());
        assertNotNull(bean.getNonNullValue());
    }

}
