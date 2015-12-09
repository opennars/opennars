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

import com.github.fge.grappa.matchers.MatcherType;
import com.github.fge.grappa.matchers.base.CustomDefaultLabelMatcher;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.context.MatcherContext;

import java.util.List;
import java.util.Objects;

/**
 * A {@link Matcher} trying all of its submatchers in sequence and succeeding when the first submatcher succeeds.
 */
public class FirstOfMatcher
    extends CustomDefaultLabelMatcher<FirstOfMatcher>
{
    public FirstOfMatcher(Rule[] subRules)
    {
        super(Objects.requireNonNull(subRules, "subRules"), "firstOf");
    }

    @Override
    public final MatcherType getType()
    {
        return MatcherType.COMPOSITE;
    }

    @Override
    public final <V> boolean match(MatcherContext<V> context)
    {


        List<Matcher> children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Matcher matcher = children.get(i);
            if (matcher.getSubContext(context).runMatcher())
                return true;
        }
        return false;
    }
}
