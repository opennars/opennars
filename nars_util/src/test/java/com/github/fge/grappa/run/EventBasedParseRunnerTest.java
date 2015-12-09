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
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.run.context.MatcherContext;
import com.github.fge.grappa.run.events.*;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public final class EventBasedParseRunnerTest
{
    private MatcherContext<Object> context;
    private Matcher matcher;
    private ListeningParseRunner<Object> parseRunner;
    private ParseRunnerListener<Object> listener;

    @BeforeMethod
    public void init()
    {
        //noinspection unchecked
        context = mock(MatcherContext.class);
        matcher = mock(Matcher.class);
        parseRunner = spy(new ListeningParseRunner<>(matcher));
        listener = spy(new ParseRunnerListener<>());
        parseRunner.registerListener(listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void parsingRunTriggersPreAndPostParse()
    {
        InputBuffer buffer = mock(InputBuffer.class);
        ParsingResult<Object> result = mock(ParsingResult.class);

        doReturn(context)
            .when(parseRunner).createRootContext(buffer, parseRunner);
        doReturn(result)
            .when(parseRunner).createParsingResult(anyBoolean(), same(context));

        InOrder inOrder = inOrder(listener);

        ArgumentCaptor<PreParseEvent> preParse
            = ArgumentCaptor.forClass(PreParseEvent.class);
        ArgumentCaptor<PostParseEvent> postParse
            = ArgumentCaptor.forClass(PostParseEvent.class);

        parseRunner.run(buffer);

        inOrder.verify(listener).beforeParse(preParse.capture());
        inOrder.verify(listener).afterParse(postParse.capture());
        inOrder.verifyNoMoreInteractions();

        assertThat(preParse.getValue().getContext()).isSameAs(context);
        assertThat(postParse.getValue().getResult()).isSameAs(result);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void failingMatchRunTriggersPreThenFailedMatchEvents()
    {
        when(context.getMatcher()).thenReturn(matcher);
        // This is the default, but let's make it explicit
        when(matcher.match(context)).thenReturn(false);

        assertThat(parseRunner.match(context)).isFalse();

        InOrder inOrder = inOrder(listener);

        ArgumentCaptor<PreMatchEvent> preMatch
            = ArgumentCaptor.forClass(PreMatchEvent.class);
        ArgumentCaptor<MatchFailureEvent> postMatch
            = ArgumentCaptor.forClass(MatchFailureEvent.class);


        inOrder.verify(listener).beforeMatch(preMatch.capture());
        inOrder.verify(listener).matchFailure(postMatch.capture());

        assertThat(preMatch.getValue().getContext()).isSameAs(context);
        assertThat(postMatch.getValue().getContext()).isSameAs(context);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void successfulMatchRunTriggersPreThenSuccessMatchEvents()
    {
        when(context.getMatcher()).thenReturn(matcher);
        when(matcher.match(context)).thenReturn(true);

        assertThat(parseRunner.match(context)).isTrue();

        InOrder inOrder = inOrder(listener);

        ArgumentCaptor<PreMatchEvent> preMatch
            = ArgumentCaptor.forClass(PreMatchEvent.class);
        ArgumentCaptor<MatchSuccessEvent> postMatch
            = ArgumentCaptor.forClass(MatchSuccessEvent.class);


        inOrder.verify(listener).beforeMatch(preMatch.capture());
        inOrder.verify(listener).matchSuccess(postMatch.capture());
        inOrder.verifyNoMoreInteractions();

        assertThat(preMatch.getValue().getContext()).isSameAs(context);
        assertThat(postMatch.getValue().getContext()).isSameAs(context);
    }
}
