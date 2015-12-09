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

package com.github.fge.grappa.matchers;

import com.github.fge.grappa.run.MatchHandler;
import com.github.fge.grappa.util.MatcherContextBuilder;
import com.github.fge.grappa.util.SimpleMatchHandler;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class EndOfInputMatcherTest
{
    private final MatchHandler handler = SimpleMatchHandler.INSTANCE;

    private MatcherContextBuilder builder;


    @BeforeMethod
    public void initContext()
    {
        builder = new MatcherContextBuilder()
            .withInput("hello").withMatcher(new EndOfInputMatcher());
    }

    @DataProvider
    public Iterator<Object[]> indicesAndBooleans()
    {
        List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{ 0, false });
        list.add(new Object[]{ 3, false });
        list.add(new Object[]{ 4, false });
        list.add(new Object[]{ 5, true });

        return list.iterator();
    }

    @Test(dataProvider = "indicesAndBooleans")
    public void matcherWorksCorrectly(int index, boolean expected)
    {
        boolean actual = handler.match(builder.withIndex(index).build());

        assertThat(actual).isEqualTo(expected);
    }
}
