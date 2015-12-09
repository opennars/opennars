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

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Removes all unused labels.
 */
public final class UnusedLabelsRemover
    implements RuleMethodProcessor
{
    @Override
    public boolean appliesTo(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
    {
        return true;
    }

    @Override
    public void process(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
        throws Exception
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        AbstractInsnNode current = method.instructions.getFirst();

        AbstractInsnNode next;
        boolean doRemove;
        while (current != null) {
            next = current.getNext();
            //noinspection SuspiciousMethodCalls
            doRemove = current.getType() == AbstractInsnNode.LABEL
                && !method.getUsedLabels().contains(current);
            if (doRemove)
                method.instructions.remove(current);
            current = next;
        }
    }
}
