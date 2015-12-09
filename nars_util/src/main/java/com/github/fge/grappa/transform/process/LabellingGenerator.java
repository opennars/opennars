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

import com.github.fge.grappa.annotations.Label;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.transform.CodeBlock;
import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.google.common.base.Preconditions;
import me.qmx.jitescript.util.CodegenUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import javax.annotation.Nonnull;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.ARETURN;

/**
 * Adds automatic labelling code before the return instruction.
 */
public final class LabellingGenerator
    implements RuleMethodProcessor
{
    @Override
    public boolean appliesTo(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        return !method.hasDontLabelAnnotation();
    }

    @Override
    public void process(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
        throws Exception
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        // super methods have flag moved to the overriding method
        Preconditions.checkState(!method.isSuperMethod());

        InsnList instructions = method.instructions;

        AbstractInsnNode retInsn = instructions.getLast();
        while (retInsn.getOpcode() != ARETURN)
            retInsn = retInsn.getPrevious();

        LabelNode label = new LabelNode();
        CodeBlock block = CodeBlock.newCodeBlock()
            .dup()
            .ifnull(label)
            .ldc(getLabelText(method))
            .invokeinterface(CodegenUtils.p(Rule.class), "label",
                CodegenUtils.sig(Rule.class, String.class))
            .label(label);

        instructions.insertBefore(retInsn, block.getInstructionList());
    }

    public static String getLabelText(RuleMethod method) {
        if (method.visibleAnnotations == null)
            return method.name;

        AnnotationNode annotation;

        for (Object annotationObj: method.visibleAnnotations) {
            annotation = (AnnotationNode) annotationObj;

            if (!annotation.desc.equals(CodegenUtils.ci(Label.class)))
                continue;

            if (annotation.values == null)
                continue;

            Preconditions.checkState("value".equals(annotation.values.get(0)));
            String labelValue = (String) annotation.values.get(1);
            return labelValue.isEmpty() ? method.name : labelValue;
        }

        return method.name;
    }
}
