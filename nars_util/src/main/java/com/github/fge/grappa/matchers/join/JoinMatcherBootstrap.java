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

package com.github.fge.grappa.matchers.join;

import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Rule;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Bootstrapping a {@link JoinMatcher}
 *
 * <p>An instance of this class is created by {@link BaseParser#join(Object)}.
 * </p>
 *
 * <p>Its two arguments are a {@link BaseParser} instance and the object to use
 * as a rule which will become the {@link JoinMatcher}'s "joining" rule; this
 * object is converted to a {@link Rule} using {@link
 * BaseParser#toRule(Object)}.</p>
 *
 * <p>Its only method, {@link #using(Object)}, produces a {@link
 * JoinMatcherBuilder}; the argument to this method (converted to a rule in a
 * similar fashion as the above) will become the "joined" rule of the produced
 * matcher.</p>
 *
 * @param <V> value type produced by the parser
 * @param <P> parser class
 *
 * @see JoinMatcherBuilder
 */
@Beta
public final class JoinMatcherBootstrap<V, P extends BaseParser<V>>
{
    private final P parser;
    private final Rule joined;

    @VisibleForTesting
    static <T, E extends BaseParser<T>> JoinMatcherBootstrap<T, E> create(
        E parser, Object joined)
    {
        return new JoinMatcherBootstrap<>(parser, joined);
    }

    public JoinMatcherBootstrap(@Nonnull P parser,
        @Nonnull Object joined)
    {
        this.parser = Objects.requireNonNull(parser);
        this.joined = parser.toRule(joined);
    }

    public JoinMatcherBuilder using(@Nonnull Object joining)
    {
        return new JoinMatcherBuilder(joined, parser.toRule(joining));
    }
}
