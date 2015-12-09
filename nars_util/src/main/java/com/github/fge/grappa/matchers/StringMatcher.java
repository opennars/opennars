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

package com.github.fge.grappa.matchers;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.matchers.base.AbstractMatcher;
import com.github.fge.grappa.matchers.delegate.SequenceMatcher;
import com.github.fge.grappa.run.context.MatcherContext;

import java.util.Objects;

/**
 * A {@link SequenceMatcher} specialization for sequences of CharMatchers. Performs fast string matching if the
 * current context has it enabled.
 */
public final class StringMatcher
    extends AbstractMatcher
{
    private final String input;

    public StringMatcher(String input)
    {
        super("string(" + input + ')');
        this.input = Objects.requireNonNull(input);
    }
    
    @Override
    public MatcherType getType()
    {
        return MatcherType.TERMINAL;
    }

    @Override
    public <V> boolean match(MatcherContext<V> context)
    {
        int len = input.length();
        int index = context.getCurrentIndex();
        InputBuffer ib = context.getInputBuffer();

//        final String s = ib.extract(index, index + len);
//
//        if (!s.equals(input))
//            return false;

        for (int i = 0; i < len; i++) {
            if (input.charAt(i)!=ib.charAt(index+i))
                return false;
        }

        context.advanceIndex(len);
        return true;
    }
}
