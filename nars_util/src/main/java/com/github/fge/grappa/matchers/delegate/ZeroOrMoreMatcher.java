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
 * A {@link Matcher} that repeatedly tries its submatcher against the input. Always succeeds.
 */
public final class ZeroOrMoreMatcher
    extends CustomDefaultLabelMatcher<ZeroOrMoreMatcher>
{
    private final Matcher subMatcher;

    public ZeroOrMoreMatcher(Rule subRule)
    {
        super(Objects.requireNonNull(subRule, "subRule"), "zeroOrMore");
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
        int beforeMatch = context.getCurrentIndex();
        int afterMatch;

        Matcher m = this.subMatcher;
        while (m.getSubContext(context).runMatcher()) {
            if ((afterMatch = context.getCurrentIndex()) != beforeMatch) {
                beforeMatch = afterMatch;
                continue;
            }
            error();            //return false;
        }

        return true;
    }

    protected void error() {
        throw new GrappaException("The inner rule of zeroOrMore rule '"
            + getLabel() + "' must not allow empty matches");
    }
}
