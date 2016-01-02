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

package com.github.fge.grappa.matchers.unicode;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.run.context.MatcherContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public final class CodePointMatcherTest
{
    private static final int THE_ANSWER = 42;
    private static final int PILE_OF_POO = 0x1f4a9;

    private InputBuffer buffer;
    private MatcherContext<?> context;
    private int index;

    @BeforeMethod
    public void init()
    {
        buffer = mock(InputBuffer.class);
        context = mock(MatcherContext.class);

        index = THE_ANSWER;

        doAnswer(invocation -> {
            index += (Integer) invocation.getArguments()[0];
            return null;
        }).when(context).advanceIndex(anyInt());
        doAnswer(invocation -> {
            index = (Integer) invocation.getArguments()[0];
            return null;
        }).when(context).setCurrentIndex(anyInt());
        when(context.getInputBuffer()).thenReturn(buffer);
        when(context.getCurrentIndex()).thenReturn(THE_ANSWER);
    }



    @DataProvider
    public Iterator<Object[]> testData()
    {
        List<Object[]> list = new ArrayList<>();

        list.add(new Object[] { 232, -1, false, 0 });
        list.add(new Object[] { 232, 232, true, 1 });
        list.add(new Object[] { 232, 97, false, 0 });
        list.add(new Object[] { PILE_OF_POO, PILE_OF_POO + 2, false, 0 });
        list.add(new Object[] { PILE_OF_POO, PILE_OF_POO, true, 2 });
        return list.iterator();
    }

    @Test(dataProvider = "testData")
    public void matchingWorks(int wanted, int obtained,
                              boolean success, int delta)
    {
        CodePointMatcher matcher = new CodePointMatcher(wanted);
        when(buffer.codePointAt(THE_ANSWER)).thenReturn(obtained);

        assertThat(matcher.match(context)).isEqualTo(success);

        assertThat(index).isEqualTo(THE_ANSWER + delta);
    }
}
