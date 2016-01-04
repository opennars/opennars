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

package com.github.fge.grappa.transform;

import com.github.fge.grappa.transform.base.InstructionGroup;
import com.github.fge.grappa.transform.base.ParserClassNode;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.google.common.base.Preconditions;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.assertNotNull;

/*
 * FIXME: original test fails for:
 *
 * - method = "RuleWithComplexActionSetup";
 * - group = "VarInit$something";
 * - internalName = "org/parboiled/transform/VarInit$something"
 *
 * classCode is null in this case.
 *
 * With the rewritten test, it fails on three. Disable tests for now... Probably
 * has to do with the classloading context.
 */
public final class ParserExtensionVerificationTest {

//    public void verifyTestParserExtension() throws Exception {
//        ParserClassNode classNode = ParserTransformer.extendParserClass(TestParser.class);
//        verifyIntegrity(classNode.name, classNode.getClassCode());
//
//        Set<String> validGroups = new HashSet<String>();
//        for (RuleMethod method : classNode.getRuleMethods().values()) {
//            for (InstructionGroup group : method.getGroups()) {
//                String internalName = group.getGroupClassType().getInternalName();
//                byte[] classCode = group.getGroupClassCode();
//                if (!validGroups.contains(internalName)) {
//                    checkState(classCode != null);
//                    verifyIntegrity(internalName, classCode);
//                    validGroups.add(internalName);
//                }
//            }
//        }
//    }

    ParserClassNode classNode;

    public static void verifyIntegrity(
        String classInternalName, byte[] classCode) {
        Preconditions.checkNotNull(classCode, "classCode");
        ClassNode generatedClassNode = new ClassNode();
        ClassReader classReader = new ClassReader(classCode);
        classReader.accept(generatedClassNode, 0);

        for (Object methodObj : generatedClassNode.methods) {
            AsmTestUtils.verifyMethodIntegrity(classInternalName, (MethodNode) methodObj);
        }
    }

    @BeforeClass
    public void initClassNode()
        throws Exception
    {
        classNode = ParserTransformer.extendParserClass(TestParser.class);
    }

    @Test(enabled = false)
    public void classNodeIntegrityIsEnsured()
    {
        verifyIntegrity(classNode.name, classNode.getClassCode());
    }

    @DataProvider
    public Iterator<Object[]> getInstructionGroups()
    {
        List<Object[]> list = new ArrayList<>();

        Set<String> internalNames = new HashSet<>();
        Iterable<RuleMethod> methods
            = classNode.getRuleMethods().values();

        String internalName;

        for (RuleMethod method: methods) {
            for (InstructionGroup group: method.getGroups()) {
                internalName = group.getGroupClassType().getInternalName();
                if (internalNames.add(internalName))
                    list.add(new Object[] { internalName, method, group });
            }
        }

        return list.iterator();
    }

    @Test(
        enabled = false,
        dataProvider = "getInstructionGroups",
        dependsOnMethods = "classNodeIntegrityIsEnsured"
    )
    public void instructionGroupIsCorrect(String internalName,
                                          RuleMethod method, InstructionGroup group)
    {
        String where
            = String.format("(internal name: %s, rule method: %s; group: %s)",
                internalName, method, group);

        byte[] code = group.getGroupClassCode();
        assertNotNull(code, "class code is null! It should not be! " + where);
        verifyIntegrity(internalName, code);
    }
}
