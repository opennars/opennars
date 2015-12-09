package objenome.util.bean;

import org.junit.Test;

public class InheritanceTest {

    private interface A {

        int getFoo();

        void setBar(int bar);
    }

    private interface B extends A {

        void setFoo(int foo);

        int getBar();
    }

    @Test
    public void testInheritance() {
        B b = BeanProxyBuilder.on(B.class).build();
        System.out.println(b);
    }

}
