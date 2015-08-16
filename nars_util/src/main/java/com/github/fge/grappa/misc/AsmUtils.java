/*
 * Copyright (c) 2009-2011 Ken Wenzel and Mathias Doenitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.fge.grappa.misc;

import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.transform.ClassCache;
import com.github.fge.grappa.transform.LoadingOpcode;
import me.qmx.jitescript.util.CodegenUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import com.github.fge.grappa.run.context.ContextAware;
import com.github.fge.grappa.support.Var;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class AsmUtils
{
    private static final ClassCache CACHE = ClassCache.INSTANCE;

    private AsmUtils()
    {
    }

    public static String getExtendedParserClassName(
        final String parserClassName)
    {
        Objects.requireNonNull(parserClassName, "parserClassName");
        return parserClassName + "$$grappa";
    }

    /**
     * Get the class equivalent to an ASM {@link Type}
     *
     * @param type the type
     * @return the matching class
     */
    public static Class<?> getClassForType(final Type type)
    {
        Objects.requireNonNull(type, "type");
        switch (type.getSort()) {
            case Type.BOOLEAN:
                return boolean.class;
            case Type.BYTE:
                return byte.class;
            case Type.CHAR:
                return char.class;
            case Type.DOUBLE:
                return double.class;
            case Type.FLOAT:
                return float.class;
            case Type.INT:
                return int.class;
            case Type.LONG:
                return long.class;
            case Type.SHORT:
                return short.class;
            case Type.VOID:
                return void.class;
            case Type.OBJECT:
            case Type.ARRAY:
                return CACHE.loadClass(type.getInternalName());
                //return getClassForInternalName(type.getInternalName());
        }
        throw new IllegalStateException(); // should be unreachable
    }

    public static Field getClassField(final String classInternalName,
        final String fieldName)
    {
        Objects.requireNonNull(classInternalName, "classInternalName");
        Objects.requireNonNull(fieldName, "fieldName");
        final Class<?> c = CACHE.loadClass(classInternalName);
        //final Class<?> c = getClassForInternalName(classInternalName);
        Class<?> current = c;
        while (current != Object.class) {
            for (final Field field: current.getDeclaredFields())
                if (field.getName().equals(fieldName))
                    return field;
            current = current.getSuperclass();
        }
        throw new RuntimeException("Field '" + fieldName + "' not found in '"
            + c.getCanonicalName() + "' or any superclass");
    }

    public static Method getClassMethod(final String classInternalName,
        final String methodName, final String methodDesc)
    {
        Objects.requireNonNull(classInternalName, "classInternalName");
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(methodDesc, "methodDesc");

        final Class<?> c = CACHE.loadClass(classInternalName);
        //final Class<?> c = getClassForInternalName(classInternalName);
        final Type[] types = Type.getArgumentTypes(methodDesc);
        final Class<?>[] argTypes = new Class<?>[types.length];

        for (int i = 0; i < types.length; i++)
            argTypes[i] = getClassForType(types[i]);

        final Method method = findMethod(c, methodName, argTypes);
        if (method == null) {
            throw new RuntimeException("Method '" + methodName
                + "' with descriptor '" + methodDesc + "' not found in '"
                + c + "' or any supertype");
        }
        return method;
    }

    @Nullable
    private static Method findMethod(final Class<?> clazz,
        final String methodName, final Class<?>[] argTypes)
    {
        if (clazz == null)
            return null;

        try {
            return clazz.getDeclaredMethod(methodName, argTypes);
        } catch (NoSuchMethodException ignored) {
            Method ret;
            ret = findMethod(clazz.getSuperclass(), methodName, argTypes);
            if (ret != null)
                return ret;

            for (final Class<?> interfaceClass : clazz.getInterfaces()) {
                ret = findMethod(interfaceClass, methodName, argTypes);
                if (ret != null)
                    return ret;
            }
        }
        // TODO: is this actually reachable?
        return null;
    }

    public static Constructor<?> getClassConstructor(
        final String classInternalName, final String constructorDesc)
    {
        Objects.requireNonNull(classInternalName, "classInternalName");
        Objects.requireNonNull(constructorDesc, "constructorDesc");

        final Class<?> c = CACHE.loadClass(classInternalName);
        //final Class<?> c = getClassForInternalName(classInternalName);
        final Type[] types = Type.getArgumentTypes(constructorDesc);
        final Class<?>[] argTypes = new Class<?>[types.length];

        for (int i = 0; i < types.length; i++)
            argTypes[i] = getClassForType(types[i]);


        try {
            return c.getDeclaredConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Constructor with descriptor '"
                + constructorDesc + "' not found in '" + c, e);
        }
    }

    /**
     * Returns the class with the given name if it has already been loaded by
     * the given class loader. Otherwise the method returns null.
     *
     * @param className the full name of the class to be loaded
     * @param classLoader the class loader to use
     * @return the class instance or null
     */
    // TODO: rework synchronization
    @Nullable
    public static Class<?> findLoadedClass(final String className,
        final ClassLoader classLoader)
    {
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(classLoader, "classLoader");

        final Class<?> c;
        final Method m;

        try {
            c = Class.forName("java.lang.ClassLoader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not determine whether class '"
                + className + "' has already been loaded", e);
        }

        try {
            m = c.getDeclaredMethod("findLoadedClass", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not determine whether class '"
                + className + "' has already been loaded", e);
        }
        m.setAccessible(true);
        try {
            return (Class<?>) m.invoke(classLoader, className);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Could not determine whether class '"
                + className + "' has already been loaded", e);
        } finally {
            m.setAccessible(false);
        }
    }

    /**
     * Loads the class defined with the given name and bytecode using the given
     * class loader
     *
     * <p>Since package and class idendity includes the ClassLoader instance
     * used to load a class, we use reflection on the given class loader to
     * define generated classes.</p>
     *
     * <p>If we used our own class loader (in order  to be able to access the
     * protected "defineClass" method), we would likely still be able to load
     * generated classes; however, they would not have access to package-private
     * classes and members of their super classes.</p>
     *
     * @param className the full name of the class to be loaded
     * @param code the bytecode of the class to load
     * @param classLoader the class loader to use
     * @return the class instance
     */
    public static Class<?> loadClass(final String className, final byte[] code,
        final ClassLoader classLoader)
    {
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(classLoader, "classLoader");

        final Class<?> c;
        final Method m;

        try {
            c = Class.forName("java.lang.ClassLoader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load class '" + className
                + '\'', e);
        }

        try {
            m = c.getDeclaredMethod("defineClass", String.class, byte[].class,
                int.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not load class '" + className
                + '\'', e);
        }

        // protected method invocation
        m.setAccessible(true);
        try {
            return (Class<?>) m.invoke(classLoader, className, code, 0,
                code.length);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Could not load class '" + className
                + '\'', e);
        } finally {
            m.setAccessible(false);
        }
    }

    public static InsnList createArgumentLoaders(final String methodDescriptor)
    {
        Objects.requireNonNull(methodDescriptor, "methodDescriptor");

        final InsnList instructions = new InsnList();
        final Type[] types = Type.getArgumentTypes(methodDescriptor);

        int opcode;
        VarInsnNode node;

        for (int i = 0; i < types.length; i++) {
            opcode = LoadingOpcode.forType(types[i]);
            node = new VarInsnNode(opcode, i + 1);
            instructions.add(node);
        }
        return instructions;
    }

    /**
     * Determines whether the class with the given descriptor is assignable to the given type.
     *
     * @param classInternalName the class descriptor
     * @param type the type
     * @return true if the class with the given descriptor is assignable to the given type
     */
    public static boolean isAssignableTo(final String classInternalName,
        final Class<?> type)
    {
        Objects.requireNonNull(classInternalName, "classInternalName");
        Objects.requireNonNull(type, "type");

        final Class<?> c = CACHE.loadClass(classInternalName);
        //final Class<?> c = getClassForInternalName(classInternalName);
        return type.isAssignableFrom(c);
    }

    public static boolean isBooleanValueOfZ(final AbstractInsnNode insn)
    {
        Objects.requireNonNull(insn, "insn");
        if (insn.getOpcode() != Opcodes.INVOKESTATIC)
            return false;
        final MethodInsnNode mi = (MethodInsnNode) insn;
        return isBooleanValueOfZ(mi.owner, mi.name, mi.desc);
    }

    public static boolean isBooleanValueOfZ(final String methodOwner,
        final String methodName, final String methodDesc)
    {
        Objects.requireNonNull(methodOwner, "methodOwner");
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(methodDesc, "methodDesc");

        return CodegenUtils.p(Boolean.class).equals(methodOwner)
            && "valueOf".equals(methodName)
            && CodegenUtils.sig(Boolean.class, boolean.class)
                .equals(methodDesc);
    }

    public static boolean isActionRoot(final AbstractInsnNode insn)
    {
        Objects.requireNonNull(insn, "insn");
        if (insn.getOpcode() != Opcodes.INVOKESTATIC)
            return false;
        final MethodInsnNode mi = (MethodInsnNode) insn;
        return isActionRoot(mi.owner, mi.name);
    }

    public static boolean isActionRoot(final String methodOwner,
        final String methodName)
    {
        Objects.requireNonNull(methodOwner, "methodOwner");
        Objects.requireNonNull(methodName, "methodName");
        return "ACTION".equals(methodName)
            && isAssignableTo(methodOwner, BaseParser.class);
    }

    public static boolean isVarRoot(final AbstractInsnNode insn)
    {
        Objects.requireNonNull(insn, "insn");
        if (insn.getOpcode() != Opcodes.INVOKESPECIAL)
            return false;
        final MethodInsnNode mi = (MethodInsnNode) insn;
        return isVarRoot(mi.owner, mi.name, mi.desc);
    }

    public static boolean isVarRoot(final String methodOwner,
        final String methodName, final String methodDesc)
    {
        Objects.requireNonNull(methodOwner, "methodOwner");
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(methodDesc, "methodDesc");

        return "<init>".equals(methodName)
            && CodegenUtils.sig(void.class, Object.class).equals(methodDesc)
            && isAssignableTo(methodOwner, Var.class);
    }

    public static boolean isCallOnContextAware(final AbstractInsnNode insn)
    {
        Objects.requireNonNull(insn, "insn");
        if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL
            && insn.getOpcode() != Opcodes.INVOKEINTERFACE)
            return false;
        final MethodInsnNode mi = (MethodInsnNode) insn;
        return isAssignableTo(mi.owner, ContextAware.class);
    }
}
