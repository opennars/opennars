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

import com.github.fge.grappa.exceptions.InvalidGrammarException;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.transform.generate.ActionClassGenerator;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.List;

import static com.github.fge.grappa.util.CustomAssertions.shouldHaveThrown;

@SuppressWarnings("InstantiatingObjectToGetClassObject")
public class ErrorDetectionTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new ImplicitActionsConverter(),
            new InstructionGroupCreator(),
            new InstructionGroupPreparer(),
            new ActionClassGenerator(true),
            new RuleMethodRewriter()
    );

    @Test
    public synchronized void testRuleWithActionAccessingPrivateField() throws Exception {
        setup(new BaseParser<Object>() {
            private final int privateInt = 5;

            public Rule RuleWithActionAccessingPrivateField() {
                return sequence('a', privateInt == 0);
            }
        }.getClass());

        try {
            processMethod("RuleWithActionAccessingPrivateField", processors);
            shouldHaveThrown(InvalidGrammarException.class);
        } catch (InvalidGrammarException ignored) {
        }
    }

    @Test
    public synchronized void testRuleWithActionAccessingPrivateMethod() throws Exception {
        setup(new BaseParser<Object>() {
            public Rule RuleWithActionAccessingPrivateMethod() {
                return sequence('a', privateAction());
            }

            private boolean privateAction() {
                return true;
            }
        }.getClass());

        try {
            processMethod("RuleWithActionAccessingPrivateMethod", processors);
            shouldHaveThrown(InvalidGrammarException.class);
        } catch (InvalidGrammarException ignored) {
        }
    }

}
