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

import objenome.util.bytecode.SgClass;
import objenome.util.bytecode.SgMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to cache implementation's new methods.
 */
final class ImplementedMethod {

    private final SgMethod method;

    private final List<Class<?>> interfaces;

    /**
     * Constructor with method.
     * 
     * @param method
     *            Method - Cannot be <code>null</code>.
     */
    public ImplementedMethod(SgMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("The argument 'method' cannot be null!");
        }
        this.method = method;

        interfaces = new ArrayList<>();
    }

    /**
     * Returns the Method.
     * 
     * @return Method - Always non <code>null</code>.
     */
    public SgMethod getMethod() {
        return method;
    }

    /**
     * Adds a new interface that has this method.
     * 
     * @param intf
     *            Interface to add - Cannot be <code>null</code> and must be an
     *            interface.
     */
    public void addInterface(Class<?> intf) {
        if (intf == null) {
            throw new IllegalArgumentException("The argument 'intf' cannot be null!");
        }
        if (!intf.isInterface()) {
            throw new IllegalArgumentException("The argument 'intf' [" + intf.getName()
                    + "] is not an interface!");
        }
        interfaces.add(intf);
    }

    /**
     * Returns the list of methods as array.
     * 
     * @return Copy of the internal method list.
     */
    public Class<?>[] getInterfaces() {
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }

    /**
     * Returns the "type" signature of the method.
     * 
     * @return Method name and argument types (like
     *         "methodXY(String, int, boolean)").
     */
    public String getTypeSignature() {
        return method.getTypeSignature();
    }

    /**
     * Returns the return type of the method.
     * 
     * @return Type - Always non-null.
     */
    public SgClass getReturnType() {
        return method.getReturnType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return method.getTypeSignature() + " => " + interfaces;
    }
    
}
