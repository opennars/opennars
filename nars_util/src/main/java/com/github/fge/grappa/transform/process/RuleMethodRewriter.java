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

package com.github.fge.grappa.transform.process;

import com.github.fge.grappa.transform.CodeBlock;
import com.github.fge.grappa.transform.LoadingOpcode;
import com.github.fge.grappa.transform.base.InstructionGraphNode;
import com.github.fge.grappa.transform.base.InstructionGroup;
import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.google.common.base.Supplier;
import me.qmx.jitescript.util.CodegenUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.PUTFIELD;

/**
 * Inserts action group class instantiation code at the groups respective
 * placeholders.
 */
public final class RuleMethodRewriter
    implements RuleMethodProcessor
{
    private RuleMethod method;
    private int actionNr = 0;
    private int varInitNr = 0;

    @Override
    public boolean appliesTo(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        return method.containsExplicitActions() || method.containsVars();
    }

    @Override
    public void process(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
        throws Exception
    {
        this.method = Objects.requireNonNull(method, "method");
        actionNr = 0;
        varInitNr = 0;

        for (InstructionGroup group: method.getGroups()) {
            createNewGroupClassInstance(group);
            initializeFields(group);

            InstructionGraphNode root = group.getRoot();
            AbstractInsnNode rootInsn = root.getInstruction();

            if (root.isActionRoot())
                method.instructions.remove(rootInsn);
            else  // if (root.isVarInitRoot())
                // TODO: replace with Supplier
                ((MethodInsnNode) rootInsn).desc = CodegenUtils.sig(void.class,
                    Supplier.class);
        }

        method.setBodyRewritten();
    }

    private void createNewGroupClassInstance(InstructionGroup group)
    {
        String internalName
            = group.getGroupClassType().getInternalName();
        InstructionGraphNode root = group.getRoot();
        AbstractInsnNode rootInsn = root.getInstruction();
        InsnList insnList = method.instructions;
        String constant = method.name + (root.isActionRoot() ? "_Action"
            + ++actionNr : "_VarInit" + ++varInitNr);

        CodeBlock block = CodeBlock.newCodeBlock();

        block.newobj(internalName)
            .dup()
            .ldc(constant)
            .invokespecial(internalName, "<init>",
                CodegenUtils.sig(void.class, String.class));

        if (root.isActionRoot()
            && method.hasSkipActionsInPredicatesAnnotation())
            block.dup().invokevirtual(internalName, "setSkipInPredicates",
                CodegenUtils.sig(void.class));

        insnList.insertBefore(rootInsn, block.getInstructionList());
    }

    private void initializeFields(InstructionGroup group)
    {
        String internalName
            = group.getGroupClassType().getInternalName();

        InsnList insnList;
        AbstractInsnNode rootInsn;
        int opcode;
        VarInsnNode varNode;
        FieldInsnNode fieldNode;

        for (FieldNode field: group.getFields()) {
            insnList = method.instructions;
            rootInsn = group.getRoot().getInstruction();
            // TODO: replace with method in CodeBlock?
            opcode = LoadingOpcode.forType((Type) field.value);
            varNode = new VarInsnNode(opcode, field.access);
            fieldNode = new FieldInsnNode(PUTFIELD, internalName, field.name,
                field.desc);

            insnList.insertBefore(rootInsn, new InsnNode(DUP));
            // the FieldNodes access and value members have been reused for the
            // var index / Type respectively!
            insnList.insertBefore(rootInsn, varNode);
            insnList.insertBefore(rootInsn, fieldNode);
        }
    }
}

