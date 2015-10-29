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

import objenome.solver.evolve.GPContainer.GPContainerAware;
import objenome.solver.evolve.GPContainer.GPKey;
import objenome.solver.evolve.event.ConfigEvent;
import objenome.solver.evolve.event.Listener;

/**
 * <code>ProxyComponent</code> provides a mechanism to wrap objects and view
 * them as {@link PopulationProcess}s. The wrapped object is specified by a
 * <code>ConfigKey</code> and changes in the configuration are monitored.
 */
public abstract class ProxyComponent<T> implements Listener<ConfigEvent>, GPContainerAware {

    /**
     * The <code>ConfigKey</code> of the proxied object.
     */
    protected GPKey key;
    protected GPContainer config;

    /**
     * The proxied object.
     */
    //protected T handler;
    /**
     * Constructs a <code>ProxyComponent</code>.
     *
     * @param key the <code>ConfigKey</code> of the proxied object.
     */
    public ProxyComponent(GPKey key) {
        this.key = key;
    }

    @Override
    public void setConfig(GPContainer config) {
        this.config = config;
        setup();
        config.on(ConfigEvent.class, this);
    }
    
    

    /**
     * Receives configuration events and refresh its configuration if the
     * <code>ConfigEvent</code> is for the parameter specified by the
     * <code>key</code> object.
     *
     * @param event the fired event object.
     */
    @Override
    public void onEvent(ConfigEvent event) {
//        if (event.isKindOf(ProblemSTGP.PROBLEM, key)) {
//            setup();
//        }
    }

    protected void setup() {

    }

    public T getHandler() {
        return (T)config.get(key);
    }

    public GPContainer getConfig() {
        return config;
    }

    
}
