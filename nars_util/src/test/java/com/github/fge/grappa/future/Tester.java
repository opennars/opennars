package com.github.fge.grappa.future;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_7;

public final class Tester
{

    public static CallSite genCallSite(final MethodHandles.Lookup caller,
        final String invokedName, final MethodType invokedType,
        final String methodName)
        throws NoSuchMethodException, IllegalAccessException
    {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final MethodHandle handle
            = lookup.findStatic( Tester.class, methodName, MethodType

                .methodType(void.class, String.class));
        return new ConstantCallSite(handle);
    }

    public static void main(final String[] args) throws Throwable {
        Label line;
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_7, ACC_PUBLIC, "pkg/MyClass", null, "java/lang/Object",
            new String[]{ "java/lang/Runnable" });
        cw.visitSource("(generated)", "(generated)");
        MethodVisitor visitor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        visitor.visitCode();
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V",
            false);
        visitor.visitInsn(RETURN);
        visitor.visitMaxs(1, 1);
        visitor.visitEnd();

        visitor = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
        visitor.visitCode();

        line = new Label(); visitor.visitLabel(line); visitor.visitLineNumber(1, line);
        visitor.visitLdcInsn("Some argument");

        final String desc1 =
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;"
                + "Ljava/lang/invoke/MethodType;Ljava/lang/String;)"
                + "Ljava/lang/invoke/CallSite;";
        final String desc2 = "(Ljava/lang/String;)V";
        final String owner = "com/github/fge/grappa/future/Tester";
        visitor.visitInvokeDynamicInsn("call", desc2, new Handle(H_INVOKESTATIC,
            owner, "genCallSite", desc1), "hello");

        line = new Label(); visitor.visitLabel(line); visitor.visitLineNumber(2, line);
        visitor.visitLdcInsn("Another argument");
        visitor.visitInvokeDynamicInsn("call", "(Ljava/lang/String;)V", new Handle(
            H_INVOKESTATIC, owner, "genCallSite",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;"
                + "Ljava/lang/invoke/MethodType;Ljava/lang/String;)"
                + "Ljava/lang/invoke/CallSite;"),
            "world");

        visitor.visitInsn(RETURN);
        visitor.visitMaxs(1, 1);
        visitor.visitEnd();

        cw.visitEnd();

        new ClassLoader(Tester.class.getClassLoader()) {{
            final byte[] bytes = cw.toByteArray();
            final Class<?> cl = defineClass(null, bytes, 0, bytes.length);
            final Runnable r = (Runnable) cl.newInstance();
            r.run();
        }};
    }

    private static void hello(final String arg) {
        System.err.println("Called hello(): " + arg);
        Thread.dumpStack();
    }

    private static void world(final String arg) {
        System.err.println("Called world():" + arg);
        Thread.dumpStack();
    }
}
