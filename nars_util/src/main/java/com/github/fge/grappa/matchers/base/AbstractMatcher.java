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

package com.github.fge.grappa.matchers.base;

import com.github.fge.grappa.misc.ImmutableGraphNode;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.context.MatcherContext;
import nars.util.data.list.FasterList;

import java.util.Objects;

/**
 * Abstract base class of most regular {@link Matcher}s.
 */
public abstract class AbstractMatcher
    extends ImmutableGraphNode<Matcher>
    implements Matcher, Cloneable
{
    private String label;

    protected AbstractMatcher(String label)
    {
        this(new Rule[0], label);
    }

    protected AbstractMatcher(Rule subRule, String label)
    {
        this(new Rule[]{ Objects.requireNonNull(subRule, "subRule") },
            label);
    }

    protected AbstractMatcher(Rule[] subRules, String label)
    {
        super(/*ImmutableList.copyOf*/ new FasterList(
                toMatchers(Objects.requireNonNull(subRules))
            )
        );
        this.label = label;
    }

    private static Matcher[] toMatchers(Rule... subRules)
    {
        Matcher[] matchers = new Matcher[subRules.length];
        for (int i = 0; i < subRules.length; i++)
            matchers[i] = (Matcher) subRules[i];
        return matchers;
    }

    @Override
    public String getLabel()    {
        return label;
    }

    @Override
    public boolean hasCustomLabel()
    {
        // this is the default implementation for single character matchers
        // complex matchers override with a custom implementation
        return true;
    }

    @Override
    public final String toString()
    {
        return getLabel();
    }

    @Override
    public final AbstractMatcher label(String label)
    {
        if (Objects.equals(label, this.label))
            return this;
        AbstractMatcher clone = createClone();
        clone.label = label;
        return clone;
    }

    // default implementation is to simply delegate to the context
    @Override
    public <V> MatcherContext<V> getSubContext(MatcherContext<V> context)
    {
        return context.getSubContext(this);
    }

    // creates a shallow copy
    private AbstractMatcher createClone()
    {
        try {
            return (AbstractMatcher) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}

