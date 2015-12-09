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

import com.github.fge.grappa.transform.TestParser;
import com.github.fge.grappa.transform.base.InstructionGroup;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.github.fge.grappa.transform.generate.ActionClassGenerator;
import com.github.fge.grappa.transform.generate.VarInitClassGenerator;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.github.fge.grappa.transform.AsmTestUtils.getClassDump;
import static org.testng.Assert.assertEquals;

public class ActionClassGeneratorTest extends TransformationTest {

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


    @BeforeClass
    public void setup() throws IOException {
        setup(TestParser.class);
    }

    @Test
    public void testActionClassGeneration() throws Exception {
        RuleMethod method = processMethod("RuleWithComplexActionSetup", processors);

        assertEquals(method.getGroups().size(), 3);

        InstructionGroup group = method.getGroups().get(0);
        assertEquals(getClassDump(group.getGroupClassCode())
            .replaceAll("(?<=\\$)[A-Za-z0-9]{16}", "XXXXXXXXXXXXXXXX"), "" +
                "// class version 51.0 (51)\n" +
                "// access flags 0x1011\n" +
                "public final synthetic class com/github/fge/grappa/transform/VarInit$XXXXXXXXXXXXXXXX extends com/github/fge/grappa/transform/runtime/BaseVarInit  {\n" +
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
                "    LDC \"text\"\n" +
                "    ARETURN\n" +
                "    MAXSTACK = 1\n" +
                "    MAXLOCALS = 1\n" +
                "}\n");

        group = method.getGroups().get(1);
        assertEquals(getClassDump(group.getGroupClassCode())
            .replaceAll("(?<=\\$)[A-Za-z0-9]{16}", "XXXXXXXXXXXXXXXX"), "" +
                "// class version 51.0 (51)\n" +
                "// access flags 0x1011\n" +
                "public final synthetic class com/github/fge/grappa/transform/Action$XXXXXXXXXXXXXXXX extends com/github/fge/grappa/transform/runtime/BaseAction  {\n" +
                '\n' +
                '\n' +
                "  // access flags 0x1001\n" +
                "  public synthetic I field$0\n" +
                '\n' +
                "  // access flags 0x1001\n" +
                "  public synthetic I field$1\n" +
                '\n' +
                "  // access flags 0x1001\n" +
                "  public synthetic I field$2\n" +
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
                "    GETFIELD com/github/fge/grappa/transform/Action$XXXXXXXXXXXXXXXX.field$0 : I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD com/github/fge/grappa/transform/Action$XXXXXXXXXXXXXXXX.field$1 : I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD com/github/fge/grappa/transform/Action$XXXXXXXXXXXXXXXX.field$2 : I\n" +
                "    IADD\n" +
                "    IF_ICMPLE L0\n" +
                "    ICONST_1\n" +
                "    GOTO L1\n" +
                "   L0\n" +
                "   FRAME SAME\n" +
                "    ICONST_0\n" +
                "   L1\n" +
                "   FRAME SAME1 I\n" +
                "    IRETURN\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");

        group = method.getGroups().get(2);
        assertEquals(getClassDump(group.getGroupClassCode())
            .replaceAll("(?<=\\$)[A-Za-z0-9]{16}", "XXXXXXXXXXXXXXXX"), "" +
                "// class version 51.0 (51)\n" +
                "// access flags 0x1011\n" +
                "public final synthetic class com/github/fge/grappa/transform/Action$XXXXXXXXXXXXXXXX extends com/github/fge/grappa/transform/runtime/BaseAction  {\n" +
                '\n' +
                '\n' +
                "  // access flags 0x1001\n" +
                "  public synthetic Lcom/github/fge/grappa/transform/TestParser$$grappa; field$0\n" +
                '\n' +
                "  // access flags 0x1001\n" +
                "  public synthetic I field$1\n" +
                '\n' +
                "  // access flags 0x1001\n" +
                "  public synthetic Lcom/github/fge/grappa/support/Var; field$2\n" +
                '\n' +
                "  // access flags 0x1001\n" +
                "  public synthetic I field$3\n" +
                '\n' +
                "  // access flags 0x1001\n" +
                "  public synthetic I field$4\n" +
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
                "    GETFIELD com/github/fge/grappa/transform/Action$XXXXXXXXXXXXXXXX.field$0 : Lcom/github/fge/grappa/transform/TestParser$$grappa;\n" +
                "    GETFIELD com/github/fge/grappa/transform/TestParser.integer : I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD com/github/fge/grappa/transform/Action$XXXXXXXXXXXXXXXX.field$1 : I\n" +
                "    IADD\n" +
                "    ALOAD 0\n" +
                "    GETFIELD com/github/fge/grappa/transform/Action$XXXXXXXXXXXXXXXX.field$2 : Lcom/github/fge/grappa/support/Var;\n" +
                "    INVOKEVIRTUAL com/github/fge/grappa/support/Var.get ()Ljava/lang/Object;\n" +
                "    CHECKCAST java/lang/String\n" +
                "    INVOKEVIRTUAL java/lang/String.length ()I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD com/github/fge/grappa/transform/Action$XXXXXXXXXXXXXXXX.field$3 : I\n" +
                "    ISUB\n" +
                "    ALOAD 0\n" +
                "    GETFIELD com/github/fge/grappa/transform/Action$XXXXXXXXXXXXXXXX.field$4 : I\n" +
                "    ISUB\n" +
                "    IF_ICMPGE L0\n" +
                "    ICONST_1\n" +
                "    GOTO L1\n" +
                "   L0\n" +
                "   FRAME SAME\n" +
                "    ICONST_0\n" +
                "   L1\n" +
                "   FRAME SAME1 I\n" +
                "    IRETURN\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");
    }

}
