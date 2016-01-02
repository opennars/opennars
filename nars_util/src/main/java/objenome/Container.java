package objenome;

import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.set.mutable.UnifiedSet;
import nars.util.data.list.FasterList;
import objenome.solution.dependency.*;
import objenome.util.InjectionUtils;
import objenome.util.InjectionUtils.Provider;
import objenome.util.bean.BeanProxyBuilder;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The deterministic implementation of of IoC container.
 *
 * @author sergio.oliveira.jr@gmail.com
 */
public class Container extends AbstractPrototainer implements AbstractContainer {


    private final Map singletonsCache;

    private final Map<String, ThreadLocal<Object>> threadLocalsCache;

    
    
    public Container() {
        this(false);        
    }
    
    public Container(boolean concurrent) {
        super(concurrent);
        singletonsCache = concurrent ? new ConcurrentHashMap() : new UnifiedMap(0);
        threadLocalsCache = concurrent ? new ConcurrentHashMap() : new UnifiedMap(0);
    }

    public Container(AbstractPrototainer parent) {
        super(
                //TODO clone according to concurrent implementation:                
                new UnifiedMap(parent.builders),
                new UnifiedMap(parent.scopes),
                new UnifiedSet(parent.setterDependencies),
                new UnifiedSet(parent.constructorDependencies),
                new UnifiedSet(parent.forConstructMethod));
        
        singletonsCache = parent.concurrent ? new ConcurrentHashMap() : new UnifiedMap(0);
        threadLocalsCache = parent.concurrent ? new ConcurrentHashMap() : new UnifiedMap(0);
    }
    
    
    
    
    public final <T> T get(Object key, T defaultValue) {
        T existing = get(key);
        if (existing == null)
            return defaultValue;
        return existing;
    }
    

    @Override
    public <T> T get(Object key) {

        String name = InjectionUtils.getKeyName(key);

        if (!builders.containsKey(name)) {
            if (key instanceof Class)
                return (T) get((Class)key);
            return null;
        }

        Builder c = builders.get(name);

        Scope scope = scopes.get(name);

        try {

            Object target = null;
            //noinspection IfStatementWithTooManyBranches
            if (scope == Scope.SINGLETON) {

                boolean needsToCreate = false;

                synchronized (singletonsCache) {

                    target = singletonsCache.get(name);
                    if (target!=null) {
                        return (T) target; // no need to wire again...

                    } else {
                        needsToCreate = true;
                    }
                }

                if (needsToCreate) {

                    // instance needs to be in a non-synchronized block
                    target = c.instance(this);

                    checkInterceptable(c, target);

                    //synchronized (this) {

                        singletonsCache.put(name, target);
                    //}
                }

            } else if (scope == Scope.THREAD) {

                //synchronized (this) {

                ThreadLocal<Object> t = threadLocalsCache.get(name);
                boolean needsToAddToCache = false;
                boolean needsToCreate = false;
                if (t!=null) {

                        //t = threadLocalsCache.get(name);

                        target = t.get();

                        if (target == null) { // different thread...

                            needsToCreate = true;

                            // don't return... let it be wired...
                        } else {

                            return (T) target; // no need to wire again...

                        }

                    } else {

                        t = new ThreadLocal<>();

                        needsToCreate = true;

                        needsToAddToCache = true;

                        // let it be wired...
                    }


                if (needsToCreate) {

                    // instance needs to be in a non-synchronized block
                    target = c.instance(this);

                    checkInterceptable(c, target);

                    t.set(target);
                }

                if (needsToAddToCache) {

                    //synchronized (this) {

                        threadLocalsCache.put(name, t);
                    //}
                }

            } else if (scope == Scope.NONE) {

                target = c.instance(this);

                checkInterceptable(c, target);

            } else {

                throw new UnsupportedOperationException("Don't know how to handle scope: " + scope);
            }

            if (target != null) {

                for (SetterDependency d : setterDependencies) {

                    // has dependency ?
                    Method m = d.check(target.getClass());

                    if (m != null) {

                        String sourceKey = d.getSource();

                        if (sourceKey.equals(name)) {

                            // cannot depend on itself... also avoid recursive StackOverflow...
                            continue;

                        }

                        Object source = get(sourceKey);

                        try {

                            // apply
                            m.invoke(target, source);

                        } catch (Exception e) {

                            throw new RuntimeException("Cannot inject dependency: method = " + (m != null ? m.getName() : "NULL") + " / source = "
                                    + (source != null ? source : "NULL") + " / target = " + target, e);

                        }
                    }
                }
            }

            return (T) target; // return target nicely with all of dependencies

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    private static void checkInterceptable(Builder f, Object value) {
        if (f instanceof Interceptor) {
            //Interceptor i = (Interceptor) f;
            ((Interceptor) f).onCreated(value);
        }
    }

    
    public <T> T the(Object key, Builder builder) {        
        if (builder!=null)
            usable(key, Scope.SINGLETON, builder);
        return get(key);
    }    
    public <T> T the(Class<? extends T> c) {
        T existing = get((Object)c);
        if (existing == null) {
            return the(c, new ClassBuilder(this, c).instance(this));
        }
        return existing;        
    }
    public <T> T the(Object key, Object value) {
        if (value == null) {
            return remove(key);
        }
        return the(key, new SingletonBuilder(value));
    }

    public <T> T the(Object value) {    
        T existing = get(value);
        if (existing == null) {
            return the(value.getClass(), new SingletonBuilder(value));
        }
        return existing;
    }

    public Map<String, Object> getSingletons() {
        return singletonsCache;
    }


    
    @Override
    public <T> T get(Class<? extends T> c) {
        //if c is actually a key and not an arbitrary class this container has never been told about:
        String name = InjectionUtils.getKeyName(c);
        if (builders.containsKey(name)) {
            return get(name);
        }
        
        ClassBuilder f = getClassBuilder(c);
        return (T) f.instance(this);
    }

    @Override
    public <C> C apply(C instance) {

        Provider p = new Provider() {

            @Override
            public Object get(String key) {

                return Container.this.get(key);
            }

            @Override
            public boolean hasValue(String key) {

                return contains(key);
            }

        };

        try {

            InjectionUtils.getObject(instance, p, false, null, true, false, true);

        } catch (Exception e) {

            throw new RuntimeException("Error populating bean: " + instance, e);
        }
        
        return instance;
    }

    @Override
    public Builder usable(Object key, Scope scope, Builder factory) {
        Builder b = super.usable(key, scope, factory);

        String keyString = InjectionUtils.getKeyName(key);
        //singletonsCache.remove(keyString); // just in case we are overriding a previous singleton bean...
        ThreadLocal<Object> threadLocal = threadLocalsCache.remove(keyString); // just in case we are overriding a previous thread local...
        if (threadLocal != null) {
            threadLocal.remove();
        }
        return b;
    }

    
    @Override
    public void remove(Scope scope) {
        if (scope == Scope.SINGLETON) {
            Collection<ClearableHolder> listToClear = new FasterList<>();
            synchronized (this) {
                singletonsCache.forEach((k,value) -> {
                    Builder factory = builders.get(k);
                    if (factory instanceof Interceptor) {
                        Interceptor c = (Interceptor) factory;
                        listToClear.add(new ClearableHolder(c, value));
                    }

                });
                singletonsCache.clear();
            }
            // remove everything inside a non-synchronized block...
            listToClear.forEach(ClearableHolder::clear);
        } else if (scope == Scope.THREAD) {
            Collection<ClearableHolder> listToClear = new FasterList<>();
            synchronized (this) {
                for (Map.Entry<String, ThreadLocal<Object>> stringThreadLocalEntry : threadLocalsCache.entrySet()) {
                    Builder factory = builders.get(stringThreadLocalEntry.getKey());
                    if (factory instanceof Interceptor) {
                        Interceptor c = (Interceptor) factory;
                        ThreadLocal<Object> t = stringThreadLocalEntry.getValue();
                        Object value = t.get();
                        // we are ONLY clearing if this thread has something in of threadlocal, in other words,
                        // if of thread has previously requested this key...
                        if (value != null) {
                            listToClear.add(new ClearableHolder(c, value));
                        }
                    }
                }
                // and now we remove all thread locals belonging to this thread...
                // this will only remove of instances related to this thread...
                threadLocalsCache.values().forEach(ThreadLocal::remove);
            }
            // remove everything inside a non-synchronized block...
            listToClear.forEach(ClearableHolder::clear);
        }
    }

    public void clear() {
        builders.clear();
        scopes.clear();
        constructorDependencies.clear();
        setterDependencies.clear();
        forConstructMethod.clear();        
        clearCache();
    }
    public void clearCache() {
        singletonsCache.clear();
        threadLocalsCache.clear();        
    }
    
    @Override
    public <T> T remove(Object k) {
        String key = InjectionUtils.getKeyName(k);
        if (!builders.containsKey(key)) {
            return null;
        }
        Scope scope = scopes.get(key);
        //noinspection IfStatementWithTooManyBranches
        if (scope == Scope.SINGLETON) {
            ClearableHolder cp = null;
            Object value = null;
            synchronized (this) {
                value = singletonsCache.remove(key);
                if (value != null) {
                    Builder factory = builders.get(key);                    
                    if (factory instanceof Interceptor) {
                        Interceptor c = (Interceptor) factory;
                        cp = new ClearableHolder(c, value);
                    }
                }
            }
            if (cp != null) {
                cp.c.onRemoved(cp.value);
            }
            return (T) value;
        } else if (scope == Scope.THREAD) {
            ClearableHolder cp = null;
            Object retVal = null;
            synchronized (this) {
                ThreadLocal<Object> t = threadLocalsCache.get(key);
                if (t != null) {
                    Object o = t.get();
                    if (o != null) {
                        Builder factory = builders.get(key);                    
                        if (factory instanceof Interceptor) {
                            Interceptor c = (Interceptor) factory;
                            cp = new ClearableHolder(c, o);
                        }
                        t.remove();
                        retVal = o;
                    }
                }
            }
            if (cp != null) {
                cp.c.onRemoved(cp.value);
            }
            return (T) retVal;
        } else if (scope == Scope.NONE) {
            return null; // always...
        } else {
            throw new UnsupportedOperationException("Scope not supported: " + scope);
        }
    }


    @Override
    public boolean contains(Object obj) {
        String key = InjectionUtils.getKeyName(obj);
        if (!builders.containsKey(key)) {
            return false;
        }
        Scope scope = scopes.get(key);
        //noinspection IfStatementWithTooManyBranches
        if (scope == Scope.NONE) {
            return false; // always...
        } else if (scope == Scope.SINGLETON) {
            return singletonsCache.containsKey(key);
        } else if (scope == Scope.THREAD) {
            ThreadLocal<Object> t = threadLocalsCache.get(key);
            if (t != null) {
                return t.get() != null;
            }
            return false;
        } else {
            throw new UnsupportedOperationException("This scope is not supported: " + scope);
        }
    }

    public <X> X bean(Class<? extends X> intrface) {
        //TODO see if caching the builder's (result of on()) performs best
        return BeanProxyBuilder.on(intrface).build();
    }


}
