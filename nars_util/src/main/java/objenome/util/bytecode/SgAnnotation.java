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
package objenome.util.bytecode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A annotation.
 * 
 * TODO Handle annotation arguments TODO Handle different types of annotations
 * (CLASS, RUNTIME; SOURCE)
 */
public final class SgAnnotation {

    private final String packageName;

    private final String simpleName;

    private final Map<String, Object> arguments;

    /**
     * Constructor with package and name of the class.
     * 
     * @param packageName
     *            Package - Cannot be null (but empty for default package).
     * @param simpleName
     *            Name (without package) - Cannot be null.
     */
    public SgAnnotation(String packageName, String simpleName) {
        if (packageName == null) {
            throw new IllegalArgumentException("The argument 'packageName' cannot be null!");
        }
        this.packageName = packageName.trim();

        if (simpleName == null) {
            throw new IllegalArgumentException("The argument 'simpleName' cannot be null!");
        }
        String trimmed = simpleName.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(
                    "The argument 'simpleName' cannot be an empty string!");
        }
        this.simpleName = trimmed;

        arguments = new HashMap<>();
    }

    /**
     * Returns the name of the annotation.
     * 
     * @return Name including package - Always non-null.
     */
    public String getName() {
        if (packageName.isEmpty()) {
            return simpleName;
        }
        return packageName + '.' + simpleName;
    }

    /**
     * Returns the package of the annotation.
     * 
     * @return Package name - Always non-null.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the name of the annotation.
     * 
     * @return Name (without package) - Always non-null.
     */
    public String getSimpleName() {
        return simpleName;
    }

    /**
     * Returns the annotations arguments.
     * 
     * @return Arguments - Always non-null, maybe empty and is unmodifiable.
     */
    public Map<String, Object> getArguments() {
        return Collections.unmodifiableMap(arguments);
    }

    /**
     * Adds an argument.
     * 
     * @param name
     *            Name of the argument - Cannot be null.
     * @param value
     *            Value of the argument - Cannot be null.
     */
    public void addArgument(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException("The argument 'name' cannot be null!");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument 'value' cannot be null!");
        }
        arguments.put(name.trim(), value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('@');
        sb.append(getName());
        if (!arguments.isEmpty()) {
            sb.append('(');
            if (arguments.size() == 1) {
                for (String name : arguments.keySet()) {
                    Object value = arguments.get(name);
                    if (!"value".equals(name)) {
                        sb.append(name);
                        sb.append('=');
                    }
                    sb.append(value);
                }
            } else {
                int count = 0;
                for (String name : arguments.keySet()) {
                    Object value = arguments.get(name);
                    if (count > 0) {
                        sb.append(", ");
                    }
                    sb.append(name);
                    sb.append('=');
                    sb.append(value);
                    count++;
                }
            }
            sb.append(')');
        }
        return sb.toString();
    }

}
