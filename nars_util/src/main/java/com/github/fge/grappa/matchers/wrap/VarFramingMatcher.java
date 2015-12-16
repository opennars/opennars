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

package com.github.fge.grappa.matchers.wrap;

import com.github.fge.grappa.matchers.MatcherType;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.context.MatcherContext;
import com.github.fge.grappa.support.Var;

import java.util.List;
import java.util.Objects;

/**
 * Special wrapping matcher that manages the creation and destruction of execution frames for a number of action vars.
 */
// TODO: use delegation pattern
public final class VarFramingMatcher
    implements Matcher
{
    private final Matcher inner;
    private final Var<?>[] variables;

    public VarFramingMatcher(Rule inner, Var<?>[] variables)
    {
        this.inner = Objects.requireNonNull((Matcher) inner, "inner");
        this.variables = Objects.requireNonNull(variables, "variables");
    }

    @Override
    public MatcherType getType()
    {
        return inner.getType();
    }

    @Override
    public <V> boolean match(MatcherContext<V> context)
    {
        for (Var<?> var: variables)
            var.enterFrame();

        boolean matched = inner.match(context);

        for (Var<?> var : variables)
            var.exitFrame();

        return matched;
    }

    // GraphNode

    @Override
    public List<Matcher> getChildren()
    {
        return inner.getChildren();
    }

    // Rule

    @Override
    public Rule label(String label)
    {
        return new VarFramingMatcher(inner.label(label), variables);
    }

    // Matcher

    @Override
    public String getLabel()
    {
        return inner.getLabel();
    }

    @Override
    public boolean hasCustomLabel()
    {
        return inner.hasCustomLabel();
    }

    @Override
    public <V> MatcherContext<V> getSubContext(MatcherContext<V> context)
    {
        MatcherContext<V> subContext = inner.getSubContext(context);
         // we need to inject ourselves here otherwise we get cut out
        subContext.setMatcher(this);
        return subContext;
    }

    @Override
    public String toString()
    {
        return inner.toString();
    }

    /**
     * Retrieves the innermost Matcher that is not a VarFramingMatcher.
     *
     * @param matcher the matcher to unwrap
     * @return the given instance if it is not a VarFramingMatcher, otherwise the innermost Matcher
     */
    public static Matcher unwrap(Matcher matcher) {
        while (true) {
            if (!(matcher instanceof VarFramingMatcher))
                return matcher;
            matcher = ((VarFramingMatcher) matcher).inner;
        }
    }
}
