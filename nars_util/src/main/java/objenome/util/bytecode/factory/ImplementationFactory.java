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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates an implementation of an interface.
 */
public class ImplementationFactory {

    private final boolean onlyDeclaredMethods;

    private final SgClassPool pool;

    /**
     * Constructor with class pool. All methods will be implemented (declared
     * and super interface methods). If you just want to implement declared
     * methods use the {@link #ImplementationFactory(SgClassPool, boolean)}
     * constructor.
     * 
     * @param pool
     *            Class pool.
     */
    public ImplementationFactory(SgClassPool pool) {
        this(pool, false);
    }

    /**
     * Constructor with class pool and information about methods to implement.
     * 
     * @param pool
     *            Class pool.
     * @param onlyDeclaredMethods
     *            Should only declared methods be implemented?
     */
    public ImplementationFactory(SgClassPool pool, boolean onlyDeclaredMethods) {

        assureNotNull("pool", pool);
        this.pool = pool;

        this.onlyDeclaredMethods = onlyDeclaredMethods;

    }

    /**
     * Returns the "type" signature of the method.
     *
     * @param methodName
     *            Name of the method.
     * @param paramTypes
     *            Argument types.
     *
     * @return Method name and argument types (like
     *         "methodXY(String, int, boolean)").
     */
    public static String createTypeSignature(String methodName, Class<?>[] paramTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append(methodName);
        sb.append('(');
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(paramTypes[i].getSimpleName());
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Creates an implementation of the interface.
     * 
     * @param implPackageName
     *            Name of the implementation package - Cannot be null.
     * @param implClassName
     *            Name of the implementation class - Cannot be null.
     * @param listener
     *            Creates the bodies for all methods - Cannot be null.
     * @param intf
     *            One or more interfaces.
     * 
     * @return New object implementing the interface.
     */
    public final SgClass create(String implPackageName, String implClassName,
                                ImplementationFactoryListener listener, Class<?>... intf) {
        return create(implPackageName, implClassName, null, null, listener, intf);
    }

    /**
     * Creates an implementation of the interface.
     * 
     * @param implPackageName
     *            Name of the implementation package - Cannot be null.
     * @param implClassName
     *            Name of the implementation class - Cannot be null.
     * @param superClass
     *            Parent class or <code>null</code>.
     * @param enclosingClass
     *            Outer class or <code>null</code>.
     * @param listener
     *            Creates the bodies for all methods - Cannot be null.
     * @param intf
     *            One or more interfaces.
     * 
     * @return New object implementing the interface.
     */
    public final SgClass create(String implPackageName, String implClassName,
                                SgClass superClass, SgClass enclosingClass,
                                ImplementationFactoryListener listener, Class<?>... intf) {

        assureNotNull("implPackageName", implPackageName);
        assureNotNull("implClassName", implClassName);
        assureNotNull("listener", listener);
        assureNotNull("intf", intf);
        assureNotEmpty("intf", intf);
        assureAllInterfaces(intf);

        // Create class with all interfaces
        SgClass clasz = new SgClass("public", implPackageName, implClassName, superClass,
                false, enclosingClass);
        for (Class<?> anIntf1 : intf) {
            clasz.addInterface(SgClass.create(pool, anIntf1));
        }
        listener.afterClassCreated(clasz);

        Map<String, ImplementedMethod> implMethods = new HashMap<>();

        // Iterate through interfaces and add methods
        for (Class<?> anIntf : intf) {
            addInterfaceMethods(implMethods, clasz, anIntf, listener);
        }

        // Iterate through methods and create body
        for (String s : implMethods.keySet()) {
            ImplementedMethod implMethod = implMethods.get(s);
            SgMethod method = implMethod.getMethod();
            Class<?>[] interfaces = implMethod.getInterfaces();
            List<String> lines = listener.createBody(method, interfaces);
            for (String line : lines) {
                implMethod.getMethod().addBodyLine(line);
            }
        }

        return clasz;
    }

    private void addInterfaceMethods(Map<String, ImplementedMethod> implMethods,
                                     SgClass clasz, Class<?> intf, ImplementationFactoryListener listener) {

        Method[] methods;
        methods = onlyDeclaredMethods ? intf.getDeclaredMethods() : intf.getMethods();
        for (Method method1 : methods) {

            // Create method signature
            String name = method1.getName();
            String typeSignature = createTypeSignature(name, method1
                    .getParameterTypes());

            // Get return type
            SgClass returnType;
            returnType = method1.getReturnType() == null ? SgClass.VOID : SgClass.create(pool, method1.getReturnType());

            // Check if we already implemented this method
            ImplementedMethod implMethod = implMethods.get(typeSignature);
            if (implMethod == null) {
                SgMethod method = new SgMethod(clasz, "public", returnType, name);
                // Add arguments
                Class<?>[] paramTypes = method1.getParameterTypes();
                for (int k = 0; k < paramTypes.length; k++) {
                    SgClass paramType = SgClass.create(pool, paramTypes[k]);
                    method.addArgument(new SgArgument(method, paramType, ("arg" + k)));
                }
                method.addAnnotations(SgUtils.createAnnotations(method1.getAnnotations()));
                implMethod = new ImplementedMethod(method);
                implMethod.addInterface(intf);
                implMethods.put(typeSignature, implMethod);
            } else {
                implMethod.addInterface(intf);
                if (!returnType.getName().equals(implMethod.getReturnType().getName())) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < implMethod.getInterfaces().length; i++) {
                        if (i > 0) {
                            sb.append("' or '");
                        }
                        sb.append(implMethod.getInterfaces()[i].getName());
                    }
                    throw new IllegalArgumentException("Method '" + typeSignature
                            + "' has different return types for interface '" + intf.getName()
                            + "' and '" + sb + "'!");
                }
            }

            // Add exceptions if missing
            SgMethod method = implMethod.getMethod();
            Class<?>[] exceptionTypes = method1.getExceptionTypes();
            for (Class<?> exceptionType : exceptionTypes) {
                SgClass ex = SgClass.create(pool, exceptionType);
                if (!method.getExceptions().contains(ex)) {
                    method.addException(ex);
                }
            }

        }

    }

    private static void assureNotNull(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + name + "' cannot be null!");
        }
    }

    private static void assureNotEmpty(String name, Object[] value) {
        if (value.length == 0) {
            throw new IllegalArgumentException("The argument '" + name
                    + "' cannot be an empty array!");
        }
    }

    private static void assureAllInterfaces(Class<?>... intf) {
        for (int i = 0; i < intf.length; i++) {
            if (!intf[i].isInterface()) {
                throw new IllegalArgumentException("Expected an interface: " + intf[i].getName()
                        + " [" + i + ']');
            }
        }
    }

}
