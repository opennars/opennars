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

import com.github.fge.grappa.matchers.base.AbstractMatcher;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.run.context.MatcherContext;
import com.github.fge.grappa.support.Chars;

import static com.github.fge.grappa.support.Chars.escape;

/**
 * A {@link Matcher} matching a single given character.
 */
public final class CharMatcher
    extends AbstractMatcher
{
    private final char character;

    public CharMatcher(char character)
    {
        super(getLabel(character));
        this.character = character;
    }

    @Override
    public MatcherType getType() {
        return MatcherType.TERMINAL;
    }

    // TODO: remove...
    private static String getLabel(char c)
    {
        if (c == Chars.EOI) return escape(Chars.EOI);
        //return new String( new char[] { '\'', escapeChar(c), '\'' });
        return '\'' + escape(c) + '\'';
    }

    @Override
    public <V> boolean match(MatcherContext<V> context)
    {
        if (context.getCurrentChar() != character)
            return false;
        context.advanceIndex(1);
        return true;
    }
}
