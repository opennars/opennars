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

import com.github.fge.grappa.buffers.CharSequenceInputBuffer;
import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.internal.NonFinalForTesting;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.context.MatcherContext;
import com.github.fge.grappa.stack.DefaultValueStack;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Even more efficient
 */
@SuppressWarnings("DesignForExtension")
@ParametersAreNonnullByDefault
@NonFinalForTesting
public class ListeningParseRunner3<V>
        extends AbstractParseRunner<V>
        implements MatchHandler
{
    // TODO: does it need to be volatile?
    private final Throwable throwable = null;
//
//    private static final EventBus bus = new EventBus(new SubscriberExceptionHandler()
//    {
//        @Override
//        public void handleException(final Throwable exception,
//                                    final SubscriberExceptionContext context)
//        {
//            if (throwable == null)
//                throwable = exception;
//            else
//                throwable.addSuppressed(exception);
//        }
//    });

    /**
     * Creates a new BasicParseRunner instance for the given rule.
     *
     * @param rule the parser rule
     */
    public ListeningParseRunner3(Rule rule)
    {
        super(rule);
    }

//    // TODO: replace with a supplier mechanism
//    public final void registerListener(final ParseRunnerListener<V> listener) {
//        throw new RuntimeException(this + " doesn't involve an unnecessary eventbus because we like to conserve Memory");
//        //bus.register(listener);
//    }

    @Override
    public ParsingResult<V> run(InputBuffer inputBuffer)
    {


        //Objects.requireNonNull(inputBuffer, "inputBuffer");
        resetValueStackDOESNTNEEDTOBEFINALPLEASE();

        MatcherContext<V> rootContext = createRootContext(inputBuffer, this);

//        if (busRun())
//            bus.post(new PreParseEvent<>(rootContext));

        /*if (throwable != null)
            throw new GrappaException("parsing listener error (before parse)",
                    throwable);*/

        boolean matched = rootContext.runMatcher();
        ParsingResult<V> result
                = createParsingResult(matched, rootContext);

//        if (busRun())
//            bus.post(new PostParseEvent<>(result));

        /*if (throwable != null)
            throw new GrappaException("parsing listener error (after parse)",
                    throwable);*/

        return result;
    }

    @Override
    public final <T> boolean match(MatcherContext<T> context)
    {
        Matcher matcher = context.getMatcher();

//        if (busMatch()) {
//            final PreMatchEvent<T> preMatchEvent = new PreMatchEvent<>(context);
//            bus.post(preMatchEvent);
//        }

        /*if (throwable != null)
            throw new GrappaException("parsing listener error (before match)",
                    throwable);*/

        // FIXME: is there any case at all where context.getMatcher() is null?
        @SuppressWarnings("ConstantConditions") boolean match = matcher.match(context);


//        if (busMatch()) {
//            final MatchContextEvent<T> postMatchEvent = match
//                    ? new MatchSuccessEvent<>(context)
//                    : new MatchFailureEvent<>(context);
//            bus.post(postMatchEvent);
//        }

        /*if (throwable != null)
            throw new GrappaException("parsing listener error (after match)",
                    throwable);*/


        return match;
    }


    public ParsingResult<V> runWHYISTHESUPERMETHODFINAL(CharSequence input)  {
        //Objects.requireNonNull(input, "input");
        return run(new CharSequenceInputBuffer(input));
    }

    protected void resetValueStackDOESNTNEEDTOBEFINALPLEASE()     {
        //ONLY CREATE A REPLACEMENT VALUESTACK IF IS EMPTY
        if (valueStack == null)
            valueStack = new DefaultValueStack();
        else
            valueStack.clear();
        stackSnapshot = null;
    }

//    public boolean busMatch() { return false; }
//    public boolean busRun() { return false; }
}