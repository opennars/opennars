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

package com.github.fge.grappa.transform.generate;

import com.github.fge.grappa.exceptions.InvalidGrammarException;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.transform.CodeBlock;
import com.github.fge.grappa.transform.base.ParserClassNode;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Objects;

import static com.github.fge.grappa.misc.AsmUtils.createArgumentLoaders;
import static org.objectweb.asm.Opcodes.*;

/**
 * Adds one constructor for each of the ParserClassNode.constructors,
 * which simply delegates to the respective super constructor.
 */
public   enum ConstructorGenerator
{
    ;

    public static void process(ParserClassNode classNode)
    {
        Objects.requireNonNull(classNode, "classNode");

        List<MethodNode> constructors = classNode.getConstructors();

        if (constructors.isEmpty())
            throw new InvalidGrammarException("parser class \"%s\" has no "
                + "visible constructor for derives classes");

        for (MethodNode constructor: constructors)
            createConstuctor(classNode, constructor);

        createNewInstanceMethod(classNode);
    }

    private static void createConstuctor(ParserClassNode classNode,
                                         MethodNode constructor)
    {
        List<String> exceptions = constructor.exceptions;
        MethodNode newConstructor = new MethodNode(ACC_PUBLIC,
            constructor.name, constructor.desc, constructor.signature,
            exceptions.toArray(new String[exceptions.size()])
        );

        InsnList instructions = newConstructor.instructions;

        CodeBlock block = CodeBlock.newCodeBlock()
            .aload(0)
            .addAll(createArgumentLoaders(constructor.desc))
            .invokespecial(classNode.getParentType().getInternalName(),
                "<init>", constructor.desc)
            .rawReturn();

        instructions.add(block.getInstructionList());

        classNode.methods.add(newConstructor);
    }

    private static void createNewInstanceMethod(ParserClassNode classNode)
    {
        // TODO: replace with Code{Block,GenUtils}
        String desc = "()L" + Type.getType(BaseParser.class)
            .getInternalName() + ';';
        MethodNode method = new MethodNode(ACC_PUBLIC, "newInstance",
            desc, null, null);
        InsnList instructions = method.instructions;

        instructions.add(new TypeInsnNode(NEW, classNode.name));
        instructions.add(new InsnNode(DUP));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, classNode.name,
            "<init>", "()V", false));
        instructions.add(new InsnNode(ARETURN));

        classNode.methods.add(method);
    }
}
