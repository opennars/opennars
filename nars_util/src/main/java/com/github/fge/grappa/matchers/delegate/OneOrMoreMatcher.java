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

package com.github.fge.grappa.matchers.delegate;

import com.github.fge.grappa.exceptions.GrappaException;
import com.github.fge.grappa.matchers.MatcherType;
import com.github.fge.grappa.matchers.base.CustomDefaultLabelMatcher;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.context.MatcherContext;

import java.util.Objects;

/**
 * A {@link Matcher} that repeatedly tries its submatcher against the input.
 * Succeeds if its submatcher succeeds at least once.
 */
public final class OneOrMoreMatcher
    extends CustomDefaultLabelMatcher<OneOrMoreMatcher>
{
    private final Matcher subMatcher;

    public OneOrMoreMatcher(Rule subRule)
    {
        super(Objects.requireNonNull(subRule, "subRule"), "oneOrMore");
        subMatcher = getChildren().get(0);
    }

    @Override
    public MatcherType getType()
    {
        return MatcherType.COMPOSITE;
    }

    @Override
    public <V> boolean match(MatcherContext<V> context)
    {

        Matcher subMatcher = this.subMatcher;

        boolean matched = subMatcher.getSubContext(context).runMatcher();
        if (!matched)
            return false;

        // collect all further matches as well
        // TODO: "optimize" first cyle away; also relevant for ZeroOrMoreMatcher
        int beforeMatch = context.getCurrentIndex();
        int afterMatch;
        while (subMatcher.getSubContext(context).runMatcher()) {
            afterMatch = context.getCurrentIndex();

            if (afterMatch == beforeMatch) err();

            beforeMatch = afterMatch;
        }

        return true;
    }

    private void err() {
        throw new GrappaException("The inner rule of oneOrMore rule '"
                + getLabel() + "' must not allow empty matches");
    }
}
