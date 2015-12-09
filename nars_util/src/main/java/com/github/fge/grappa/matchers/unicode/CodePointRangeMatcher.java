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

package com.github.fge.grappa.matchers.unicode;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.matchers.MatcherType;
import com.github.fge.grappa.matchers.base.AbstractMatcher;
import com.github.fge.grappa.run.context.MatcherContext;

/**
 * Matcher for a range of Unicode code points
 *
 * @see InputBuffer#codePointAt(int)
 */
public final class CodePointRangeMatcher
    extends AbstractMatcher
{
    private final int low;
    private final int high;

    public CodePointRangeMatcher(int low, int high)
    {
        super(String.format("U+%04X-U+%04X", low, high));
        this.low = low;
        this.high = high;
    }

    @Override
    public MatcherType getType()
    {
        return MatcherType.TERMINAL;
    }

    @Override
    public <V> boolean match(MatcherContext<V> context)
    {
        int codePoint
            = context.getInputBuffer().codePointAt(context.getCurrentIndex());

        if (codePoint < low || codePoint > high)
            return false;

        context.advanceIndex(codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT
            ? 1 : 2);
        return true;
    }
}
