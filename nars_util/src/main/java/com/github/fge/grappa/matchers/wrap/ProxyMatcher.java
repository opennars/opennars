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

import java.util.List;
import java.util.Objects;

/**
 * A {@link Matcher} that delegates all {@link Rule} and {@link Matcher} interface methods to another {@link Matcher}.
 * It can also hold a label and a leaf marker and lazily apply these to the underlying {@link Matcher} once it is available.
 */
// TODO: REMOVE!! It is THE pain point in generation today
public final class ProxyMatcher
    implements Matcher, Cloneable
{
    private Matcher target;
    private String label;
    private boolean dirty;

    @Override
    public MatcherType getType()
    {
        if (dirty)
            apply();
        return target.getType();
    }

    @Override
    public List<Matcher> getChildren()
    {
        if (dirty)
            apply();
        return target.getChildren();
    }

    public void setLabel(String label)
    {
        this.label = label;
        updateDirtyFlag();
    }

    /*
     * TODO: here in particular
     *
     * This is UGLY!! Builders to the rescue?
     */
    private void updateDirtyFlag()
    {
        dirty = label != null;
    }

    @Override
    public <V> boolean match(MatcherContext<V> context)
    {
        if (dirty)
            apply();
        return target.match(context);
    }

    @Override
    public String getLabel()
    {
        if (dirty)
            apply();
        return target.getLabel();
    }

    @Override
    public boolean hasCustomLabel()
    {
        if (dirty)
            apply();
        return target.hasCustomLabel();
    }

    @Override
    public String toString()
    {
        if (target == null)
            return super.toString();
        if (dirty)
            apply();
        return target.toString();
    }

    private void apply()
    {
        if (label != null)
            label(label);
    }

    @Override
    public Rule label(String label)
    {
        if (target == null) {
            // if we have no target yet we need to save the label and "apply" it later
            if (this.label == null) {
                setLabel(label);
                return this;
            }

            // this proxy matcher is already waiting for its label application opportunity,
            // so we need to create another proxy level
            ProxyMatcher anotherProxy = createClone();
            anotherProxy.setLabel(label);
            anotherProxy.arm(this);
            return anotherProxy;
        }

        // we already have a target to which we can directly apply the label
        Rule inner = unwrap(target);
        // since relabelling might change the instance we have to update it
        target = (Matcher) inner.label(label);
        setLabel(null);
        return target;
    }

    /**
     * Supplies this ProxyMatcher with its underlying delegate.
     *
     * @param target the Matcher to delegate to
     */
    public void arm(Matcher target)
    {
        this.target = Objects.requireNonNull(target, "target");
    }

    /**
     * Retrieves the innermost Matcher that is not a ProxyMatcher.
     *
     * @param matcher the matcher to unwrap
     * @return the given instance if it is not a ProxyMatcher, otherwise the innermost non-proxy Matcher
     */
    public static Matcher unwrap(Matcher matcher)
    {
        if (matcher instanceof ProxyMatcher) {
            ProxyMatcher proxyMatcher = (ProxyMatcher) matcher;
            if (proxyMatcher.dirty)
                proxyMatcher.apply();
            return proxyMatcher.target == null ? proxyMatcher
                : proxyMatcher.target;
        }
        return matcher;
    }

    @Override
    public <V> MatcherContext<V> getSubContext(MatcherContext<V> context)
    {
        if (dirty)
            apply();
        return target.getSubContext(context);
    }

    // creates a shallow copy
    private ProxyMatcher createClone()
    {
        try {
            return (ProxyMatcher) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
