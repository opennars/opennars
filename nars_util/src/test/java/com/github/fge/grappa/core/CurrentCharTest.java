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

package com.github.fge.grappa.core;

import com.github.fge.grappa.Grappa;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.ListeningParseRunner;
import org.testng.annotations.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public final class CurrentCharTest
{
    static class Dummy
    {
        boolean store(char ignore)
        {
            return true;
        }
    }

    static class Parser
        extends BaseParser<Object>
    {
        protected final Dummy dummy;

        Parser(Dummy dummy)
        {
            this.dummy = dummy;
        }

        Rule rule()
        {
            return sequence(dummy.store(currentChar()), EOI);
        }
    }

    @Test
    public void currentCharWorks()
    {
        Dummy dummy = spy(new Dummy());
        Parser parser = Grappa.createParser(Parser.class, dummy);
        new ListeningParseRunner<>(parser.rule()).run("a");
        verify(dummy).store('a');
    }
}
