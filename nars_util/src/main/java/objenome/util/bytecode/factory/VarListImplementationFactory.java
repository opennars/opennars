/**
 * Copyright (C) 2009 Future Invent Informationsmanagement GmbH. All rights
 * reserved. <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package objenome.util.bytecode.factory;

import objenome.util.bytecode.*;

import java.util.List;

/**
 * Creates an implementation of one ore more interfaces with a predefined number
 * of fields. An appropriate constructor will also be created.
 */
public final class VarListImplementationFactory {

    private final ImplementationFactory factory;

    /**
     * Constructor with class pool.
     * 
     * @param pool
     *            Pool to use.
     */
    public VarListImplementationFactory(SgClassPool pool) {
        factory = new ImplementationFactory(pool);
    }

    /**
     * Creates an implementation of the interface.
     * 
     * @param implPackageName
     *            Name of the implementation package - Cannot be
     *            <code>null</code>.
     * @param implClassName
     *            Name of the implementation class - Cannot be <code>null</code>
     * @param superClass
     *            Parent class or <code>null</code>.
     * @param enclosingClass
     *            Outer class or <code>null</code>.
     * @param vars
     *            List of variables the implementation contains as fields -
     *            Cannot be <code>null</code>.
     * @param listener
     *            Creates the bodies for all methods - Cannot be
     *            <code>null</code>.
     * @param intf
     *            One or more interfaces.
     * 
     * @return New object implementing the interface.
     */
    public SgClass create(String implPackageName, String implClassName,
                          SgClass superClass, SgClass enclosingClass, List<SgVariable> vars,
                          ImplementationFactoryListener listener, Class<?>... intf) {

        return factory.create(implPackageName, implClassName, superClass, enclosingClass,
                new VarListImplFactoryListener(vars, listener), intf);
    }

    /**
     * Creates all fields and the appropriate constructor.
     */
    private static final class VarListImplFactoryListener implements ImplementationFactoryListener {

        private final ImplementationFactoryListener listener;

        private final List<SgVariable> vars;

        /**
         * Constructor with arguments.
         * 
         * @param vars
         *            List of variables the implementation contains as fields -
         *            Cannot be <code>null</code>.
         * @param listener
         *            Creates the bodies for all methods - Cannot be
         *            <code>null</code>.
         */
        public VarListImplFactoryListener(List<SgVariable> vars,
                                          ImplementationFactoryListener listener) {

            if (vars == null) {
                throw new IllegalArgumentException("The argument 'args' cannot be null!");
            }
            this.vars = vars;

            if (listener == null) {
                throw new IllegalArgumentException("The argument 'listener' cannot be null!");
            }
            this.listener = listener;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void afterClassCreated(SgClass clasz) {

            // Add all arguments as fields and to the constructor
            SgConstructor constructor = new SgConstructor(clasz);
            for (SgVariable var : vars) {
                clasz.addField(new SgField(clasz, "private", var.getType(), var.getName(), ""));
                SgArgument constructorArg = new SgArgument(constructor, var.getModifiers(),
                        var.getType(), var.getName());
                constructor.addArgument(constructorArg);
                constructor.addBodyLine("this." + var.getName() + '=' + var.getName() + ';');
            }

            // Call user defined listener
            listener.afterClassCreated(clasz);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<String> createBody(SgMethod method, Class<?>... intf) {
            // Call user defined listener
            return listener.createBody(method, intf);
        }

    }

}
