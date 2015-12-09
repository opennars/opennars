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
import com.github.fge.grappa.support.Characters;
import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * A {@link Matcher} matching a single character out of a given {@link Characters} set.
 */
public final class AnyOfMatcher
    extends AbstractMatcher
{
    private final Characters characters;

    public AnyOfMatcher(Characters characters)
    {
        super(Objects.requireNonNull(characters, "characters").toString());
        Preconditions.checkArgument(!characters.equals(Characters.NONE));
        this.characters = characters;
    }

    @Override
    public MatcherType getType()
    {
        return MatcherType.TERMINAL;
    }

    public Characters getCharacters()
    {
        return characters;
    }

    @Override
    public <V> boolean match(MatcherContext<V> context)
    {
        if (!characters.contains(context.getCurrentChar()))
            return false;
        context.advanceIndex(1);
        return true;
    }
}
