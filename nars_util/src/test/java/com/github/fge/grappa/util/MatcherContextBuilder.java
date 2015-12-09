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

package com.github.fge.grappa.util;

import com.github.fge.grappa.buffers.CharSequenceInputBuffer;
import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.run.context.DefaultMatcherContext;
import com.github.fge.grappa.run.context.MatcherContext;
import com.github.fge.grappa.stack.DefaultValueStack;
import com.github.fge.grappa.stack.ValueStack;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public final class MatcherContextBuilder
{
    private static final ValueStack<Object> STACK
        = new DefaultValueStack<>();
    private InputBuffer buffer = null;
    private Matcher matcher = null;
    private int index = 0;

    public MatcherContextBuilder withInput(@Nonnull String input)
    {
        buffer = new CharSequenceInputBuffer(input);
        return this;
    }

    public MatcherContextBuilder withMatcher(@Nonnull Matcher matcher)
    {
        this.matcher = Preconditions.checkNotNull(matcher);
        return this;
    }

    public MatcherContextBuilder withIndex(int index)
    {
        this.index = index;
        return this;
    }

    public MatcherContext<Object> build()
    {
        DefaultMatcherContext<Object> ret = new DefaultMatcherContext<>(
            buffer, STACK, SimpleMatchHandler.INSTANCE, matcher);

        ret.setCurrentIndex(index);
        return ret;
    }
}
