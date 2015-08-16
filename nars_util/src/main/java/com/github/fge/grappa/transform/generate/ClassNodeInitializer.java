/*
 * Copyright (c) 2009-2010 Ken Wenzel and Mathias Doenitz
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

package com.github.fge.grappa.transform.generate;

import com.github.fge.grappa.exceptions.InvalidGrammarException;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.transform.ParserAnnotation;
import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.google.common.io.Closer;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import static com.github.fge.grappa.transform.ParserAnnotation.recordAnnotation;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static com.github.fge.grappa.misc.AsmUtils.getExtendedParserClassName;

/**
 * Initializes the basic ParserClassNode fields and collects all methods.
 */
public final class ClassNodeInitializer
    extends ClassVisitor
{
    private static final Set<ParserAnnotation> CLASS_FLAGS_CLEAR = EnumSet.of(
        ParserAnnotation.EXPLICIT_ACTIONS_ONLY,
        ParserAnnotation.DONT_LABEL,
        ParserAnnotation.SKIP_ACTIONS_IN_PREDICATES
    );

    private ParserClassNode classNode;
    private Class<?> ownerClass;
    private final Set<ParserAnnotation> annotations
        = EnumSet.noneOf(ParserAnnotation.class);

    public ClassNodeInitializer()
    {
        super(Opcodes.ASM5);
    }

    public void process(final ParserClassNode classNode)
        throws IOException
    {
        this.classNode = Objects.requireNonNull(classNode, "classNode");

        // walk up the parser parent class chain
        ownerClass = classNode.getParentClass();
        Closer closer;
        ClassReader reader;
        InputStream in;
        while (!Object.class.equals(ownerClass)) {
            annotations.removeAll(CLASS_FLAGS_CLEAR);

            closer = Closer.create();
            try {
                in = getInputStream(ownerClass);
                if (in == null)
                    throw new IOException(ownerClass + " not found");
                reader = new ClassReader(closer.register(in));
                reader.accept(this, ClassReader.SKIP_FRAMES);
            } finally {
                closer.close();
            }
            ownerClass = ownerClass.getSuperclass();
        }

        for (final RuleMethod method: classNode.getRuleMethods().values()) {
            // move all flags from the super methods to their overriding methods
            if (!method.isSuperMethod())
                continue;

            final String overridingMethodName
                = method.name.substring(1) + method.desc;

            final RuleMethod overridingMethod
                = classNode.getRuleMethods().get(overridingMethodName);

            method.moveFlagsTo(overridingMethod);
        }
    }

    @Override
    public void visit(final int version, final int access, final String name,
        final String signature, final String superName,
        final String[] interfaces)
    {
        if (ownerClass == classNode.getParentClass()) {
            if ((access & ACC_PRIVATE) != 0)
                throw new InvalidGrammarException("a parser class cannot be "
                    + "private");
            if ((access & ACC_FINAL) != 0)
                throw new InvalidGrammarException("a parser class cannot be "
                    + "final");
            final String className = getExtendedParserClassName(name);
            classNode.visit(Opcodes.V1_7, ACC_PUBLIC, className, null,
                classNode.getParentType().getInternalName(), null);
        }
    }

    @Nullable
    @Override
    public AnnotationVisitor visitAnnotation(final String desc,
        final boolean visible)
    {
        if (recordAnnotation(annotations, desc))
            return null;

        // only keep visible annotations on the parser class
        if (!visible)
            return null;

        return ownerClass == classNode.getParentClass()
            ? classNode.visitAnnotation(desc, true)
            : null;
    }

    @Override
    public void visitSource(final String source, final String debug)
    {
        classNode.visitSource(null, null);
    }

    @Nullable
    @Override
    public MethodVisitor visitMethod(final int access, String name,
        final String desc, final String signature, final String[] exceptions)
    {
        if ("<init>".equals(name)) {
            // do not add constructors from super classes or private constructors
            if (ownerClass != classNode.getParentClass())
                return null;
            if ((access & ACC_PRIVATE) > 0)
                return null;

            final MethodNode constructor = new MethodNode(access, name, desc,
                signature, exceptions);
            classNode.getConstructors().add(constructor);
             // return the newly created method in order to have it "filled"
             // with the method code
            return constructor;
        }

        // only add non-native, non-abstract methods returning Rules
        if (!Type.getReturnType(desc).equals(Type.getType(Rule.class)))
            return null;
         if ((access & (ACC_NATIVE | ACC_ABSTRACT)) > 0)
            return null;


        if ((access & ACC_PRIVATE) != 0)
            throw new InvalidGrammarException("rule methods cannot be private");
        if ((access & ACC_FINAL) != 0)
            throw new InvalidGrammarException("rule methods cannot be final");

        // check, whether we do not already have a method with that name and
        // descriptor; if we do we add the method with a "$" prefix in order
        // to have it processed and be able to reference it later if we have to
        String methodKey = name + desc;
        while (classNode.getRuleMethods().containsKey(methodKey)) {
            name = '$' + name;
            methodKey = name + desc;
        }

        final RuleMethod method = new RuleMethod(ownerClass, access, name, desc,
            signature, exceptions, annotations);
        classNode.getRuleMethods().put(methodKey, method);
        // return the newly created method in order to have it "filled" with the
        // actual method code
        return method;
    }

    @Override
    public void visitEnd()
    {
        classNode.visitEnd();
    }

    private static InputStream getInputStream(final Class<?> c)
    {
        Objects.requireNonNull(c);
        final String name = c.getName().replace('.', '/') + ".class";
        final ClassLoader me = ClassNodeInitializer.class.getClassLoader();
        final ClassLoader context
            = Thread.currentThread().getContextClassLoader();
        final ClassLoader system = ClassLoader.getSystemClassLoader();

        InputStream ret = me.getResourceAsStream(name);

        if (ret == null)
            ret = context.getResourceAsStream(name);

        if (ret == null)
            ret = system.getResourceAsStream(name);

        if (ret == null)
            throw new IllegalStateException("unable to load parser class??");

        return ret;
    }
}
