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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GOTO;

/**
 * Replaces all "non-last" return instructions with goto instructions to the
 * last return instruction. If a method contains only one return instruction
 * the transformer does nothing.
 */
public final class ReturnInstructionUnifier
    implements RuleMethodProcessor
{
    @Override
    public boolean appliesTo(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
    {
        return method.getNumberOfReturns() > 1;
    }

    @Override
    public void process(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
        throws Exception
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");

        AbstractInsnNode current = method.instructions.getLast();

        // find last return
        while (current.getOpcode() != ARETURN)
            current = current.getPrevious();


        LabelNode lastReturnLabel = new LabelNode();
        method.instructions.insertBefore(current, lastReturnLabel);

        // iterate backwards up to first instructions
        while ((current = current.getPrevious()) != null) {

            // replace returns with gotos
            if (current.getOpcode() != ARETURN)
                continue;

            JumpInsnNode insn = new JumpInsnNode(GOTO, lastReturnLabel);
            method.instructions.set(current, insn);
            current = insn;
        }
    }
}
