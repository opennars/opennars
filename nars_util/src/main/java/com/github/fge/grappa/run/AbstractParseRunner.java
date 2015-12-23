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

package com.github.fge.grappa.run;

import com.github.fge.grappa.buffers.CharSequenceInputBuffer;
import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.internal.NonFinalForTesting;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.context.DefaultMatcherContext;
import com.github.fge.grappa.run.context.MatcherContext;
import com.github.fge.grappa.stack.DefaultValueStack;
import com.github.fge.grappa.stack.ValueStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class AbstractParseRunner<V>
    implements ParseRunner<V>
{
    protected final Matcher rootMatcher;
    protected ValueStack<V> valueStack;
    protected Object stackSnapshot;

    protected AbstractParseRunner(@Nonnull Rule rule)
    {
        rootMatcher = Objects.requireNonNull((Matcher) rule, "rule");
    }

    public final ValueStack<V> getValueStack()
    {
        return valueStack;
    }

    @Override
    public final ParsingResult<V> run(CharSequence input)
    {
        Objects.requireNonNull(input, "input");
        return run(new CharSequenceInputBuffer(input));
    }

    protected final void resetValueStack()
    {
        // TODO: write a "memoizing" API
        if (valueStack!=null)
            valueStack.clear();
        else
            valueStack = new DefaultValueStack<>();

        stackSnapshot = null;
    }

    @NonFinalForTesting
    protected MatcherContext<V> createRootContext(
        InputBuffer inputBuffer, MatchHandler matchHandler)
    {
        return new DefaultMatcherContext<>(inputBuffer, valueStack,
            matchHandler, rootMatcher);
    }

    @NonFinalForTesting
    protected ParsingResult<V> createParsingResult(boolean matched,
                                                   MatcherContext<V> rootContext)
    {
        return new ParsingResult<>(matched, valueStack,
            rootContext.getInputBuffer());
    }
}
