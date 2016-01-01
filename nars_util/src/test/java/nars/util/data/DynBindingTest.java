package nars.util.data;

import com.gs.collections.api.block.function.primitive.BooleanFunction;
import org.apache.commons.math3.primes.Primes;
import org.junit.Test;

import static java.lang.Math.random;

/**
 * Created by me on 12/31/15.
 */
public class DynBindingTest {

    public static final BooleanFunction<Integer> even = (i) -> i % 2 == 0;
    public static final BooleanFunction<Integer> positive = (i) -> i>0;
    public static final BooleanFunction<Integer> prime = Primes::isPrime;

    @Test public void testMHChain() throws Throwable {
//        MethodHandle evenOnly = Util.mh("booleanValueOf", even);
//        assertEquals(true, evenOnly.invokeExact(2));
//        assertEquals(false, evenOnly.invokeExact(1));

    }


    public static boolean rng() {
        int i = (int) (random() * 10);
        System.out.println(i);
        return i != 1;
    }

    public static void print() {
        System.out.println("loop ");
    }
/*
    @Test
    public void testBranch() throws Throwable {
        MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();

        MethodHandle w = MethodHandles.whileLoop(
                null,
                Binder.from(boolean.class).invokeStatic(
                        LOOKUP,
                        DynBindingTest.class,
                        "rng"
                ),
                Binder.from(void.class).invokeStatic(
                        LOOKUP,
                        DynBindingTest.class,
                        "print"
                )
        );
        System.out.println(w);
        w.invokeExact();

//        MethodHandle handle = Binder
//                .from(String.class, String.class)
//                .branch(
//                        Binder
//                                .from(boolean.class, String.class)
//                                .invokeStatic(LOOKUP, DynBindingTest.class, "isStringFoo"),
//                        Binder
//                                .from(String.class, String.class)
//                                .invokeStatic(LOOKUP, DynBindingTest.class, "addBar"),
//                        Binder
//                                .from(String.class, String.class)
//                                .invokeStatic(LOOKUP, DynBindingTest.class, "addBaz")
//                );
//
//        assertEquals(MethodType.methodType(String.class, String.class), handle.type());
//        assertEquals("foobar", (String)handle.invokeExact("foo"));
//        assertEquals("quuxbaz", (String)handle.invokeExact("quux"));
    }*/
}
