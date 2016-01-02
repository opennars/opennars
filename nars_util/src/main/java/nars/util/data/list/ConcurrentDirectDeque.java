//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nars.util.data.list;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.AbstractCollection;
import java.util.Deque;

public abstract class ConcurrentDirectDeque<E> extends AbstractCollection<E> implements Deque<E>, Serializable {
    private static final Constructor<? extends ConcurrentDirectDeque> CONSTRUCTOR;

    public ConcurrentDirectDeque() {
    }

    public static <K> ConcurrentDirectDeque<K> newInstance() {
        try {
            return (ConcurrentDirectDeque)CONSTRUCTOR.newInstance();
        } catch (Exception var1) {
            throw new IllegalStateException(var1);
        }
    }

    public abstract Object offerFirstAndReturnToken(E var1);

    public abstract Object offerLastAndReturnToken(E var1);

    public abstract void removeToken(Object var1);

    static {
        boolean fast = false;

        try {
            new FastConcurrentDirectDeque();
            fast = true;
        } catch (Throwable var4) {
        }

        Class klazz = fast?FastConcurrentDirectDeque.class
                :
                PortableConcurrentDirectDeque.class;

        try {
            CONSTRUCTOR = klazz.getConstructor();
        } catch (NoSuchMethodException var3) {
            throw new NoSuchMethodError(var3.getMessage());
        }
    }
}
