/*
 * Copyright (C) 2014 Francis Galiegue <fgaliegue@gmail.com>
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

package com.github.fge.grappa.actions;

import com.github.fge.grappa.Grappa;
import com.github.fge.grappa.annotations.SkipActionsInPredicates;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.ListeningParseRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public final class ActionsAnnotationsTest
{
    static class Parser
        extends BaseParser<Object>
    {
        protected final Dummy dummy;

        Parser(Dummy dummy)
        {
            this.dummy = dummy;
        }

        Rule notSkipped()
        {
            return sequence(EMPTY, dummy.dummy());
        }

        @SkipActionsInPredicates
        Rule skipped()
        {
            return sequence(EMPTY, dummy.dummy());
        }

        Rule rule1()
        {
            return sequence(test(notSkipped()), ANY);
        }

        Rule rule2()
        {
            return sequence(test(skipped()), ANY);
        }
    }

    private interface Dummy
    {
        boolean dummy();
    }

    private Dummy dummy;

    @BeforeMethod
    public void initParser()
    {
        dummy = mock(Dummy.class);
        when(dummy.dummy()).thenReturn(true);
    }

    @Test
    public void byDefaultActionsRunInPredicates()
    {
        Parser parser
            = Grappa.createParser(Parser.class, dummy);
        new ListeningParseRunner<>(parser.rule1()).run("f");
        verify(dummy).dummy();
    }

    @Test
    public void whenAnnotatedActionsDoNotRunInPredicates()
    {
        Parser parser
            = Grappa.createParser(Parser.class, dummy);
        new ListeningParseRunner<>(parser.rule2()).run("f");
        verify(dummy, never()).dummy();
    }
}
