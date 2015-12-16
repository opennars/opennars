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

import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

/**
 * Replaces the method code with a simple call to the super method.
 */
public final class SuperCallRewriter
    implements RuleMethodProcessor
{
    @Override
    public boolean appliesTo(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        return method.containsPotentialSuperCalls();
    }

    @Override
    public void process(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
        throws Exception
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        InsnList instructions = method.instructions;
        AbstractInsnNode insn = instructions.getFirst();
        while (insn.getOpcode() != ARETURN) {
            if (insn.getOpcode() == INVOKESPECIAL)
                process(classNode, method, (MethodInsnNode) insn);
            insn = insn.getNext();
        }
    }

    private static void process(ParserClassNode classNode,
                                RuleMethod method, MethodInsnNode insn)
    {
        if ("<init>".equals(insn.name))
            return;
        String superMethodName = getSuperMethodName(method, insn);
        RuleMethod superMethod = classNode.getRuleMethods()
            .get(superMethodName + insn.desc);
        if (superMethod == null)
            return;
        if (!superMethod.isBodyRewritten())
            return;

        // since the super method is rewritten we do need to generate it
        superMethod.dontSkipGeneration();

        // we have a call to a super method that was rewritten, so we need to
        // change the call to the generated method
        insn.setOpcode(INVOKEVIRTUAL);
        insn.name = superMethodName;
        insn.owner = classNode.name;

        method.setBodyRewritten();
    }

    private static String getSuperMethodName(RuleMethod method,
                                             MethodInsnNode insn)
    {
        Class<?> c = method.getOwnerClass();
        StringBuilder sb = new StringBuilder(method.name);
        do {
            //noinspection ConstantConditions
            c = c.getSuperclass();
            // we should find the owner before hitting Object
            Preconditions.checkState(c != null);
            sb.insert(0, '$');
        } while (!Type.getInternalName(c).equals(insn.owner));
        return sb.toString();
    }
}
