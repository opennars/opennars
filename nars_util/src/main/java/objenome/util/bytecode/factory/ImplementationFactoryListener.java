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

import java.util.List;

/**
 * Creates the source code for the method implementations.
 */
public interface ImplementationFactoryListener {

    /**
     * The class was just created and implements the interface. The listener may
     * add constructors or fields here.
     * 
     * @param clasz
     *            Class without any method yet.
     */
    void afterClassCreated(SgClass clasz);

    /**
     * Creates the source code for a method.
     * 
     * @param method
     *            Method to create source code for.
     * @param intf
     *            Interfaces the created method belongs to.
     * 
     * @return List of source lines.
     */
    List<String> createBody(SgMethod method, Class<?>... intf);

}
