/*
 * Copyright (C) 2015 Francis Galiegue <fgaliegue@gmail.com>
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

package com.github.fge.grappa.run;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.exceptions.GrappaException;
import com.github.fge.grappa.internal.NonFinalForTesting;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.context.MatcherContext;
import com.github.fge.grappa.run.events.*;
import com.google.common.eventbus.EventBus;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * The most basic of all {@link ParseRunner} implementations. It runs a rule
 * against a given input text and builds a corresponding {@link ParsingResult}
 * instance. However, it does not report any parse errors nor recover from them.
 * Instead it simply marks the ParsingResult as "unmatched" if the input is not
 * valid with regard to the rule grammar.It never causes the parser to perform
 * more than one parsing run and is the fastest way to determine whether a given
 * input conforms to the rule grammar.
 */
@SuppressWarnings("DesignForExtension")
@ParametersAreNonnullByDefault
@NonFinalForTesting
public class ListeningParseRunner<V>
    extends AbstractParseRunner<V>
    implements MatchHandler
{
    // TODO: does it need to be volatile?
    private volatile Throwable throwable = null;

    private final EventBus bus = new EventBus((exception, context) -> {
        if (throwable == null)
            throwable = exception;
        else
            throwable.addSuppressed(exception);
    });

    /**
     * Creates a new BasicParseRunner instance for the given rule.
     *
     * @param rule the parser rule
     */
    public ListeningParseRunner(Rule rule)
    {
        super(rule);
    }

    // TODO: replace with a supplier mechanism
    public final void registerListener(ParseRunnerListener<V> listener)
    {
        bus.register(listener);
    }

    @Override
    public ParsingResult<V> run(InputBuffer inputBuffer)
    {
        Objects.requireNonNull(inputBuffer, "inputBuffer");
        resetValueStack();

        MatcherContext<V> rootContext
            = createRootContext(inputBuffer, this);
        bus.post(new PreParseEvent<>(rootContext));

        if (throwable != null)
            throw new GrappaException("parsing listener error (before parse)",
                throwable);

        boolean matched = rootContext.runMatcher();
        ParsingResult<V> result
            = createParsingResult(matched, rootContext);

        bus.post(new PostParseEvent<>(result));

        if (throwable != null)
            throw new GrappaException("parsing listener error (after parse)",
                throwable);

        return result;
    }

    @Override
    public <T> boolean match(MatcherContext<T> context)
    {
        Matcher matcher = context.getMatcher();

        PreMatchEvent<T> preMatchEvent = new PreMatchEvent<>(context);
        bus.post(preMatchEvent);

        if (throwable != null)
            throw new GrappaException("parsing listener error (before match)",
                throwable);

        // FIXME: is there any case at all where context.getMatcher() is null?
        @SuppressWarnings("ConstantConditions") boolean match = matcher.match(context);

        MatchContextEvent<T> postMatchEvent = match
            ? new MatchSuccessEvent<>(context)
            : new MatchFailureEvent<>(context);

        bus.post(postMatchEvent);

        if (throwable != null)
            throw new GrappaException("parsing listener error (after match)",
                throwable);

        return match;
    }
}
