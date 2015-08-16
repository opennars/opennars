/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fge.grappa.transform.process;

import com.github.fge.grappa.transform.CodeBlock;
import me.qmx.jitescript.util.CodegenUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import com.github.fge.grappa.run.context.Context;
import com.github.fge.grappa.run.context.ContextAware;
import com.github.fge.grappa.misc.AsmUtils;
import com.github.fge.grappa.transform.base.InstructionGraphNode;
import com.github.fge.grappa.transform.base.InstructionGroup;
import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;

public abstract class GroupClassGenerator
    implements RuleMethodProcessor
{
    private final boolean forceCodeBuilding;
    protected ParserClassNode classNode;
    protected RuleMethod method;

    protected GroupClassGenerator(final boolean forceCodeBuilding)
    {
        this.forceCodeBuilding = forceCodeBuilding;
    }

    @Override
    public final void process(@Nonnull final ParserClassNode classNode,
        @Nonnull final RuleMethod method)
    {
        this.classNode = Objects.requireNonNull(classNode, "classNode");
        this.method = Objects.requireNonNull(method, "method");

        for (final InstructionGroup group: method.getGroups())
            if (appliesTo(group.getRoot()))
                loadGroupClass(group);
    }

    protected abstract boolean appliesTo(InstructionGraphNode group);

    private void loadGroupClass(final InstructionGroup group)
    {
        createGroupClassType(group);
        final String className = group.getGroupClassType().getClassName();
        final ClassLoader classLoader
            = classNode.getParentClass().getClassLoader();

        final Class<?> groupClass;
        synchronized (AsmUtils.class) {
            groupClass = AsmUtils.findLoadedClass(className, classLoader);
            if (groupClass == null || forceCodeBuilding) {
                final byte[] groupClassCode = generateGroupClassCode(group);
                group.setGroupClassCode(groupClassCode);
                if (groupClass == null)
                    AsmUtils.loadClass(className, groupClassCode, classLoader);
            }
        }
    }

    private void createGroupClassType(final InstructionGroup group)
    {
        final String s = classNode.name;
        /*
         * If the parser has no package, the group will be an embedded class
         * to the parser class
         */
        final int lastSlash = classNode.name.lastIndexOf('/');
        final String groupName = group.getName();
        final String pkg = lastSlash >= 0 ? s.substring(0, lastSlash) : s;
        final String groupClassInternalName = pkg  + '/' + groupName;
        group.setGroupClassType(Type.getObjectType(groupClassInternalName));
    }

    protected final byte[] generateGroupClassCode(final InstructionGroup group)
    {
        final ClassWriter classWriter
            = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        generateClassBasics(group, classWriter);
        generateFields(group, classWriter);
        generateConstructor(classWriter);
        generateMethod(group, classWriter);
        return classWriter.toByteArray();
    }

    private void generateClassBasics(final InstructionGroup group,
        final ClassWriter cw)
    {
        cw.visit(Opcodes.V1_7, ACC_PUBLIC + ACC_FINAL + ACC_SYNTHETIC,
            group.getGroupClassType().getInternalName(), null,
            getBaseType().getInternalName(), null);
        cw.visitSource(classNode.sourceFile, null);
    }

    protected abstract Type getBaseType();

    private static void generateFields(final InstructionGroup group,
        final ClassWriter cw)
    {
        // TODO: fix the below comment; those "two members" should be split
        // CAUTION: the FieldNode has illegal access flags and an illegal
        // value field since these two members are reused for other
        // purposes, so we need to write out the field "manually" here
        // rather than just call "field.accept(cw)"
        for (final FieldNode field: group.getFields())
            cw.visitField(ACC_PUBLIC + ACC_SYNTHETIC, field.name, field.desc,
                null, null);

    }

    private void generateConstructor(final ClassWriter cw)
    {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>",
            CodegenUtils.sig(void.class, String.class), null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, getBaseType().getInternalName(),
            "<init>", CodegenUtils.sig(void.class, String.class), false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // trigger automatic computing
    }

    protected abstract void generateMethod(InstructionGroup group,
        ClassWriter cw);

    protected static void insertSetContextCalls(final InstructionGroup group,
        int localVarIx)
    {
        final InsnList instructions = group.getInstructions();
        final CodeBlock block = CodeBlock.newCodeBlock();

        for (final InstructionGraphNode node: group.getNodes()) {
            if (!node.isCallOnContextAware())
                continue;

            final AbstractInsnNode insn = node.getInstruction();

            if (node.getPredecessors().size() > 1) {
                // store the target of the call in a new local variable
                final AbstractInsnNode loadTarget = node.getPredecessors()
                    .get(0).getInstruction();

                block.clear().dup().astore(++localVarIx);
                instructions.insert(loadTarget, block.getInstructionList());

                // immediately before the call get the target from the local var
                // and set the context on it
                instructions.insertBefore(insn, new VarInsnNode(ALOAD,
                    localVarIx));
            } else {
                // if we have only one predecessor the call does not take any
                // parameters and we can skip the storing and loading of the
                // invocation target
                instructions.insertBefore(insn, new InsnNode(DUP));
            }

            block.clear()
                .aload(1)
                .invokeinterface(CodegenUtils.p(ContextAware.class),
                    "setContext", CodegenUtils.sig(void.class, Context.class));

            instructions.insertBefore(insn, block.getInstructionList());
        }
    }

    protected static void convertXLoads(final InstructionGroup group)
    {
        final String owner = group.getGroupClassType().getInternalName();

        InsnList insnList;

        for (final InstructionGraphNode node : group.getNodes()) {
            if (!node.isXLoad())
                continue;

            final VarInsnNode insn = (VarInsnNode) node.getInstruction();
            final FieldNode field = group.getFields().get(insn.var);
            final FieldInsnNode fieldNode = new FieldInsnNode(GETFIELD, owner,
                field.name, field.desc);

            insnList = group.getInstructions();

            // insert the correct GETFIELD after the xLoad
            insnList.insert(insn, fieldNode);
            // change the load to ALOAD 0
            insnList.set(insn, new VarInsnNode(ALOAD, 0));
        }
    }
}
