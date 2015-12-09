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

import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.support.Var;
import com.github.fge.grappa.transform.base.InstructionGroup;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.github.fge.grappa.transform.generate.ActionClassGenerator;
import com.github.fge.grappa.transform.generate.VarInitClassGenerator;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.fge.grappa.transform.AsmTestUtils.getClassDump;
import static org.testng.Assert.assertEquals;

public class VarInitClassGeneratorTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new ImplicitActionsConverter(),
            new InstructionGroupCreator(),
            new InstructionGroupPreparer(),
            new ActionClassGenerator(true),
            new VarInitClassGenerator(true)
    );

    static class Parser extends BaseParser<Integer> {

        @SuppressWarnings("UnusedDeclaration")
        public Rule A() {
            Var<List<String>> list = new Var<>(new ArrayList<>());
            Var<Integer> i = new Var<>(26);
            return sequence('a', list.get().add(match()));
        }

    }

    @BeforeClass
    public void setup() throws IOException {
        setup(Parser.class);
    }

    @Test
    public void testVarInitClassGeneration() throws Exception {
        RuleMethod method = processMethod("A", processors);

        assertEquals(method.getGroups().size(), 3);

        InstructionGroup group = method.getGroups().get(0);
        assertEquals(getClassDump(group.getGroupClassCode())
            .replaceAll("(?<=\\$)[A-Za-z0-9]{16}", "XXXXXXXXXXXXXXXX"), "" +
                "// class version 51.0 (51)\n" +
                "// access flags 0x1011\n" +
                "public final synthetic class com/github/fge/grappa/transform/process/VarInit$XXXXXXXXXXXXXXXX extends com/github/fge/grappa/transform/runtime/BaseVarInit  {\n" +
                '\n' +
                '\n' +
                "  // access flags 0x1\n" +
                "  public <init>(Ljava/lang/String;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKESPECIAL com/github/fge/grappa/transform/runtime/BaseVarInit.<init> (Ljava/lang/String;)V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                '\n' +
                "  // access flags 0x1\n" +
                "  public get()Ljava/lang/Object;\n" +
                "    NEW java/util/ArrayList\n" +
                "    DUP\n" +
                "    INVOKESPECIAL java/util/ArrayList.<init> ()V\n" +
                "    ARETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 1\n" +
                "}\n");

        group = method.getGroups().get(1);
        assertEquals(getClassDump(group.getGroupClassCode())
            .replaceAll("(?<=\\$)[A-Za-z0-9]{16}", "XXXXXXXXXXXXXXXX"), "" +
                "// class version 51.0 (51)\n" +
                "// access flags 0x1011\n" +
                "public final synthetic class com/github/fge/grappa/transform/process/VarInit$XXXXXXXXXXXXXXXX extends com/github/fge/grappa/transform/runtime/BaseVarInit  {\n" +
                '\n' +
                '\n' +
                "  // access flags 0x1\n" +
                "  public <init>(Ljava/lang/String;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKESPECIAL com/github/fge/grappa/transform/runtime/BaseVarInit.<init> (Ljava/lang/String;)V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                '\n' +
                "  // access flags 0x1\n" +
                "  public get()Ljava/lang/Object;\n" +
                "    BIPUSH 26\n" +
                "    INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;\n" +
                "    ARETURN\n" +
                "    MAXSTACK = 1\n" +
                "    MAXLOCALS = 1\n" +
                "}\n");

        group = method.getGroups().get(2);
        assertEquals(getClassDump(group.getGroupClassCode())
            .replaceAll("(?<=\\$)[A-Za-z0-9]{16}", "XXXXXXXXXXXXXXXX"), "" +
                "// class version 51.0 (51)\n" +
                "// access flags 0x1011\n" +
                "public final synthetic class com/github/fge/grappa/transform/process/Action$XXXXXXXXXXXXXXXX extends com/github/fge/grappa/transform/runtime/BaseAction  {\n" +
                '\n' +
                '\n' +
                "  // access flags 0x1001\n" +
                "  public synthetic Lcom/github/fge/grappa/support/Var; field$0\n" +
                '\n' +
                "  // access flags 0x1001\n" +
                "  public synthetic Lcom/github/fge/grappa/transform/process/VarInitClassGeneratorTest$Parser$$grappa; field$1\n" +
                '\n' +
                "  // access flags 0x1\n" +
                "  public <init>(Ljava/lang/String;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKESPECIAL com/github/fge/grappa/transform/runtime/BaseAction.<init> (Ljava/lang/String;)V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                '\n' +
                "  // access flags 0x1\n" +
                "  public run(Lcom/github/fge/grappa/run/context/Context;)Z\n" +
                "    ALOAD 0\n" +
                "    GETFIELD com/github/fge/grappa/transform/process/Action$XXXXXXXXXXXXXXXX.field$0 : Lcom/github/fge/grappa/support/Var;\n" +
                "    INVOKEVIRTUAL com/github/fge/grappa/support/Var.get ()Ljava/lang/Object;\n" +
                "    CHECKCAST java/util/List\n" +
                "    ALOAD 0\n" +
                "    GETFIELD com/github/fge/grappa/transform/process/Action$XXXXXXXXXXXXXXXX.field$1 : Lcom/github/fge/grappa/transform/process/VarInitClassGeneratorTest$Parser$$grappa;\n" +
                "    DUP\n" +
                "    ALOAD 1\n" +
                "    INVOKEINTERFACE com/github/fge/grappa/run/context/ContextAware.setContext (Lcom/github/fge/grappa/run/context/Context;)V\n" +
                "    INVOKEVIRTUAL com/github/fge/grappa/transform/process/VarInitClassGeneratorTest$Parser.match ()Ljava/lang/String;\n" +
                "    INVOKEINTERFACE java/util/List.add (Ljava/lang/Object;)Z\n" +
                "    IRETURN\n" +
                "    MAXSTACK = 4\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");
    }

}
