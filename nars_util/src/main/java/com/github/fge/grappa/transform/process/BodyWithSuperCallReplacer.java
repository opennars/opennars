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
import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;
import org.objectweb.asm.tree.InsnList;

import javax.annotation.Nonnull;
import java.util.Objects;

import static com.github.fge.grappa.misc.AsmUtils.createArgumentLoaders;

/**
 * Replaces the method code with a simple call to the super method.
 */
public final class BodyWithSuperCallReplacer
    implements RuleMethodProcessor
{
    @Override
    public boolean appliesTo(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        return !method.isBodyRewritten()
            && method.getOwnerClass() == classNode.getParentClass()
            && method.getLocalVarVariables().isEmpty();
            // if we have local variables we need to create a VarFramingMatcher
            // which needs access to the local variables
    }

    @Override
    public void process(@Nonnull ParserClassNode classNode,
        @Nonnull RuleMethod method)
        throws Exception
    {
        Objects.requireNonNull(classNode, "classNode");
        Objects.requireNonNull(method, "method");
        // replace all method code with a simple call to the super method
        String parentDesc = classNode.getParentType().getInternalName();
        InsnList argumentLoaders = createArgumentLoaders(method.desc);
        CodeBlock block = CodeBlock.newCodeBlock()
            .aload(0)
            .addAll(argumentLoaders)
            // TODO: create .invokeSpecial with MethodNode argument?
            .invokespecial(parentDesc, method.name, method.desc)
            .areturn();
        method.instructions.clear();
        method.instructions.add(block.getInstructionList());
    }
}
