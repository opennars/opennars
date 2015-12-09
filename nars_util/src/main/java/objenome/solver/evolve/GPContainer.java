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
package objenome.solver.evolve;

import objenome.Container;
import objenome.op.Node;
import objenome.op.Variable;
import objenome.op.VariableNode;
import objenome.solver.evolve.event.ConfigEvent;
import objenome.solver.evolve.event.Event;
import objenome.solver.evolve.event.EventManager;
import objenome.solver.evolve.event.Listener;
import objenome.solver.evolve.event.stat.AbstractStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a centralised store for configuration parameters. It uses a
 * singleton which is obtainable with the <code>getInstance</code> method. Each
 * parameter is referenced with a {@link GPKey} which is used to both set new
 * parameters and retrieve existing parameter values. the key also constrains
 * the data-type of the parameter value with its generic type.
 *
 * @see GPKey
 *
 * TODO subclass a Container and store properties with NORMAL, THREAD or
 * SINGLETON scope
 */
@Deprecated public class GPContainer<I extends Organism> extends Container {

    /**
     * The key for setting and retrieving the list of components.
     */
    public static final GPKey<ArrayList<PopulationProcess>> COMPONENTS = new GPKey<>();

    public final EventManager events = new EventManager();

    /**
     * stats repository, TODO rename
     */
    public final Map<Class<?>, Object> stat = new HashMap<>();
    protected Pipeline pipeline;
    protected Population<I> population = null;

    /**
     * The key -&gt; value mapping.
     */
    //public final HashMap<GPKey<?>, Object> prop = new HashMap<GPKey<?>, Object>();
    /**
     * No instance are allowed, appart from the singleton.
     *
     */
    public GPContainer() {

    }

    public void reset() {
        pipeline = null;
        population = null;
    }

    /**
     * Performs an evolutionary run. Each component in the pipeline returned by
     * the <code>setupPipeline</code> method is processed in sequence. An empty
     * {@link Population} is provided to the first component, and each
     * succeeding component is supplied with the <code>Population</code>
     * returned by the previous component.
     *
     * @return a <code>Population</code> that is the result of processing the
     * pipeline of components, as returned by the final component in that
     * pipeline
     */
    public Population<I> cycle() {
        if (pipeline == null) {
            pipeline = new Pipeline();
            /* Initialises the supplied <code>Pipeline</code> with the components that
             * an evolutionary run is composed of. The specific list of components used
             * is obtained from the {@link Config}, using the appropriate <code>Class</code> */
            for (PopulationProcess component : (Iterable<PopulationProcess>) the(COMPONENTS)) {
                GPContainer.setContainerAware(this, component);
                pipeline.add(component);
            }
            
            population = newPopulation();
        }

        //config.fire(new StartRun(0));
        
        population = pipeline.process(population);
        
        //config.fire(new EndRun(0, population));
        
        return population;
    }

    protected Population<I> newPopulation() {
        return new Population<>(this);
    }

    public Population<I> getPopulation() {
        return population;
    }

    public I onAdded(Organism i) {
        return (I) i;
    }

    public interface GPContainerAware {

        void setConfig(GPContainer c);
    }

    /**
     * the involved syntax elements
     */
    public Node[] getSyntax() {
        return get(TypedOrganism.SYNTAX);
    }

    /**
     * the involved variables, which are obtained by iterating syntax elements.
     */
    public Variable[] getVariables() {

        List<Variable> variables = new ArrayList();

        for (Node n : getSyntax()) {
            if (n instanceof VariableNode) {
                variables.add(((VariableNode) n).getVariable());
            }
        }

        return variables.toArray(new Variable[variables.size()]);
    }

//    /**
//     * Removes the specified <code>AbstractStat</code> from the repository.
//     *
//     * @param type the class of <code>AbstractStat</code> to be removed.
//     */
//    public <E extends Event> void remove(Class<? extends AbstractStat<E>> type) {
//        super.remove(type);
//        if (stat.containsKey(type)) {
//            AbstractStat<E> stat = type.cast(this.stat.remove(type));
//            off(stat.getEvent(), stat.getListener());
//            off(stat.getClearEvent(), stat.getListener());
//        }
//    }
//    /** define a statistic to collect, creating a singleton component keyed by its class */
//    public <X extends Object & Event> AbstractStat<X> stat(Class<? extends AbstractStat<X>> type) {
//
//        
//        // if the repository already contains an instance of the specified stat,
//        // we do not create a new one; otherwise, we create a new instance and
//        // register its listener in the EventManager
//        if (!stat.containsKey(type)) {
//            try {
//                AbstractStat<X> s = the(type);
//                this.stat.put(type, s);
//                on(s.getEvent(), s.listener);
//                return s;
//            } catch (Exception e) {
//                throw new RuntimeException("Could not create an instance of " + type, e);
//            }
//        }
//        return the(type);
//
//    }
    /**
     * Removes all registered <code>AbstractStat</code> objects from the
     * repository.
     */
    public <E extends Event> void resetStats() {
        Iterable<Class<?>> registered = new ArrayList<>(stat.keySet());

        for (Class<?> type : registered) {
            remove(type);
        }

        stat.clear();
    }

    public <X extends Object & Event> AbstractStat<X> stat(AbstractStat<X> a) {
        if (!stat.containsKey(a.getClass())) {
            stat.put(a.getClass(), a);
            return the(a.getClass(), a);
        }
        return super.the(a.getClass());
    }

    /**
     * Sets the value of the specified configuration key. If the given key
     * already has a value associated with it, then it will be overwritten. The
     * value is constrained to be of the correct object type as defined by the
     * generic type of the key. Calling this method will trigger the firing of a
     * new configuration event after the configuration option has been set.
     *
     * @param key the <code>ConfigKey</code> for the configuration parameter
     * that a new value is to be set for
     * @param value the new value to set for the specified configuration key
     */
    public <T> GPContainer set(GPKey<T> key, T value) {
        setContainerAware(this, value);
        remove(key);
        the(key, value);
        fire(new ConfigEvent(this, key));
        return this;
    }

    @Override
    public <T> T the(Object key, Object value) {
        T x = super.the(key, value);
        setContainerAware(this, value);
        return x;
    }

    public static void setContainerAware(GPContainer config, Object value) {
        if (value instanceof GPContainerAware) {
            ((GPContainerAware) value).setConfig(config);
        }
        if (value instanceof Iterable) {
            Iterable ii = (Iterable) value;
            for (Object i : ii) {
                if (i instanceof GPContainerAware) {
                    ((GPContainerAware) i).setConfig(config);
                }
            }
        }
    }

    /**
     * convenience method
     */
    public <T> GPContainer with(GPKey<T> key, T value) {
        return set(key, value);
    }

//    /**
//     * Retrieves the value of the configuration parameter associated with the
//     * specified key. If no value has been set for the given key then
//     * <code>null</code> is returned.
//     *
//     * @param key the <code>ConfigKey</code> for the configuration parameter to
//     * retrieve
//     * @return the value of the specified configuration parameter, or
//     * <code>null</code> if it has not been set. The object type is defined by
//     * the generic type of the key.
//     */
//    public <T> T get(GPKey<T> key) {
//        return get(key, null);
//    }
//    /**
//     * Retrieves the value of the configuration parameter associated with the
//     * specified key. If the parameter has not been set, the value will be
//     * retrived from the <code>Template</code> object.
//     *
//     * @param key the <code>ConfigKey</code> for the configuration parameter to
//     * retrieve
//     * @param defaultValue the default value to be returned if the parameter has
//     * not been set
//     * @return the value of the specified configuration parameter, or
//     * <code>null</code> if it has not been set. The object type is defined by
//     * the generic type of the key.
//     */
//    @SuppressWarnings("unchecked")
//    public <T> T get(GPKey<T> key, T defaultValue) {
//        T value = (T) prop.get(key);
//
//        if (value == null) {
//            STProblem template = (STProblem) prop.get(STProblem.PROBLEM);
//            return (template == null) ? defaultValue : template.get(key, defaultValue);
//        }
//
//        return value;
//    }
    /**
     * Removes all configuration parameter mapping. The configuration will be
     * empty this call returns.
     */
    @Override
    public void clearCache() {
        resetStats();
        //prop.clear();
    }

    public <T extends Event, V extends T> void fire(T event) {
        events.fire(event);
    }

    public <E extends Object & Event> void on(Class<? extends E> key, Listener<E> listener) {
        events.add(key, listener);
    }

    public <E extends Object & Event> Listener<E> off(Class<? extends E> key, Listener<E> listener) {
        events.remove(key, listener);
        return listener;
    }

    /**
     * Instances of <code>ConfigKey</code> are used to uniquely identify
     * configuration parameters. The generic type <code>T</code> defines a
     * constraint upon the object type of the parameter's value.
     *
     * @param <T> the required object type of values for this parameter
     */
    @Deprecated public static class GPKey<T> {
    }

}
