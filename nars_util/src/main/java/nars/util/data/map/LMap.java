package nars.util.data.map;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/** lambda map / instance cache. stores a set of instances produced by lambdas selected by a custom function.
 * if the function is selected again, the cached value is used. */
public class LMap<I,O> implements Function<I, O> {

    final Map<Supplier<O>, O> nodeCache;
    private final Function<I, Supplier<O>> model;

    public LMap(Function<I, Supplier<O>> model) {
        this(new HashMap(), model);
    }

    public LMap(Map<Supplier<O>, O> nodeCache, Function<I, Supplier<O>> model) {
        this.nodeCache = nodeCache;
        this.model = model;
    }

    @Override
    public O apply(I i) {
        return nodeCache.computeIfAbsent(model.apply(i), Supplier::get
        );
    }

    public static <I,O> LMap<I,O> newHash(Function<I, Supplier<O>> model) {
        return new LMap(new HashMap(), model);
    }


    public static <I,O> LMap<I,O> newWeak(Function<I, Supplier<O>> model) {
        return new LMap(new WeakHashMap(), model);
    }

    /** for blending in as a Map */
    public final O get(I i) { return apply(i); }

    /** for resetting or emptying an instance */
    public final O remove(I i) {
        return nodeCache.remove(model.apply(i));
    }



    /*public static class AbstractLMap extends LMap implements Function {

        public AbstractLMap() {
            super(this);
        }
    }*/
}
