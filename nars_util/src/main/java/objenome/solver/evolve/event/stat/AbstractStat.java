/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.solver.evolve.event.stat;

import objenome.solver.evolve.GPContainer;
import objenome.solver.evolve.GPContainer.GPContainerAware;
import objenome.solver.evolve.event.Event;
import objenome.solver.evolve.event.EventManager;
import objenome.solver.evolve.event.Listener;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The <code>AbstractStat</code> represent the base class for classes that
 * gathers data and statistics about events. It also works as a central
 * repository for registering, removing and retrieving stat objects.
 *
 * @see Event
 */
public abstract class AbstractStat<T extends Event> implements GPContainerAware {

    /**
     * An empty list of dependencies.
     */
    public static final List<Class<? extends AbstractStat<?>>> NO_DEPENDENCIES = new ArrayList<>(
            0);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    
    /**
     * This is the stat listener. When the stat is registered, its listener is
     * added to the {@link EventManager}.
     */
    public Listener<T> listener = this::refresh;

    /**
     * The event that trigger the stat to clear its values.
     * TODO make private
     */
    private Class<T> clearOnEvent = null;

    
    /**
     * This is the clear listener. This is only created if the event to trigger
     * the {@link #clear()} method is specified.
     */
    private Listener<T> clearOnListener;
    private GPContainer config;
    private final List<Class<? extends AbstractStat<?>>> dependencies;

    /**
     * Constructs an <code>AbstractStat</code>.
     */
    public AbstractStat() {
        this(NO_DEPENDENCIES);
    }

    /**
     * Constructs an <code>AbstractStat</code>.
     *
     * @param dependency the dependency of this stat.
     */
    @SuppressWarnings("unchecked")
    public AbstractStat(Class<? extends AbstractStat<?>> dependency) {
        this(Collections.<Class<? extends AbstractStat<?>>>singletonList(dependency));
    }

    /**
     * Constructs an <code>AbstractStat</code>. The array of dependencies can be
     * empty, in case this stat has no dependencies.
     *
     * @param dependencies the array of dependencies of this stat.
     */
    @SafeVarargs
    public AbstractStat(Class<? extends AbstractStat<?>>... dependencies) {
        this(Arrays.asList(dependencies));
    }

    /**
     * Constructs an <code>AbstractStat</code>. The list of dependencies can be
     * empty, in case this stat has no dependencies.
     *
     * @param dependencies the list of dependencies of this stat.
     */
    public AbstractStat(List<Class<? extends AbstractStat<?>>> dependencies) {
        this.dependencies = dependencies;
    }

    public Class<? extends T> getClearEvent() { return clearOnEvent; }
    /**
     * Constructs an <code>AbstractStat</code>.
     *
     * @param clearOn the event that trigger the stat to clear its values.
     * @param dependency the dependency of this stat.
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> AbstractStat(Class<T> clearOn, Class<? extends AbstractStat<?>> dependency) {
        this(clearOn, Collections.<Class<? extends AbstractStat<?>>>singletonList(dependency));
    }

    /**
     * Constructs an <code>AbstractStat</code>. The array of dependencies can be
     * empty, in case this stat has no dependencies.
     *
     * @param clearOn the event that trigger the stat to clear its values.
     * @param dependencies the array of dependencies of this stat.
     */
    @SafeVarargs
    public <E extends Event> AbstractStat(Class<T> clearOn, Class<? extends AbstractStat<?>>... dependencies) {
        this(clearOn, Arrays.asList(dependencies));
    }
    
    /**
     * Constructs an <code>AbstractStat</code>. The list of dependencies can be
     * empty, in case this stat has no dependencies.
     *
     * @param clearOn the event that trigger the stat to clear its values.
     * @param dependencies the list of dependencies of this stat.
     */
    @SuppressWarnings("unchecked")
    public <E extends Event> AbstractStat(Class<T> clearOn, List<Class<? extends AbstractStat<?>>> dependencies) {
        
        this.dependencies = dependencies;
        clearOnEvent = clearOn;

    }


    @Override
    public void setConfig(GPContainer c) {
        config = c;
        for (Class<? extends AbstractStat<?>> dependency : dependencies) {
            config.get(dependency);
        }
        if (clearOnEvent!=null) {
            Listener<T> trigger = event -> clear();
            config.on(clearOnEvent, trigger);

            clearOnListener = trigger;
        }
        
    }

    public GPContainer getConfig() {
        return config;
    }

    
    public Listener<T> getListener() {
        return listener;
    }
    
//    public <X extends Object & Event> AbstractStat<X> put(Class<? extends AbstractStat<X>> type) {
//        return getConfig().put(type);
//    }
//
    
    
    /**
     * Returns the class of the generic type T.
     */
    @SuppressWarnings("unchecked")
    public Class<T> getEvent() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Gathers the information about the event.
     *
     * @param event the event
     */
    public abstract void refresh(T event);

    /**
     * Clears the cached values. This method is automatically called when a
     * clear on event is specified.
     */
    public void clear() {
    }

//    /**
//     * Registers the specified <code>AbstractStat</code> in the repository, if
//     * it is not already registered.
//     *
//     * @param type the class of <code>AbstractStat</code> to be registered.
//     */
//    @SuppressWarnings("unchecked")
//    public static <E extends Event, V extends AbstractStat<?>> void register(Config config, Class<V> type) {
//		// if the repository already contains an instance of the specified stat,
//        // we do not create a new one; otherwise, we create a new instance and
//        // register its listener in the EventManager
//        if (!config.REPOSITORY.containsKey(type)) {
//            try {
//                AbstractStat<E> stat = (AbstractStat<E>) type.newInstance();
//                config.REPOSITORY.put(type, stat);
//                config.on(stat.getEvent(), stat.listener);
//            } catch (Exception e) {
//                throw new RuntimeException("Could not create an instance of " + type, e);
//            }
//        }
//    }

//    /**
//     * Returns the <code>AbstractStat</code> object of the specified class. If
//     * the <code>AbstractStat</code> has been registered, it returns
//     * <code>null</code>.
//     *
//     * @return the <code>AbstractStat</code> object of the specified class;
//     * <code>null</code> if the <code>AbstractStat</code> has not been
//     * registered.
//     */
//    public static <V extends AbstractStat<?>> V get(Config config, Class<V> type) {
//        return type.cast(config.REPOSITORY.get(type));
//    }


}
