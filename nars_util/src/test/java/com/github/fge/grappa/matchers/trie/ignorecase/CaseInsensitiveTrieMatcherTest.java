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

package com.github.fge.grappa.matchers.trie.ignorecase;

import com.github.fge.grappa.matchers.trie.CaseInsensitiveTrieMatcher;
import com.github.fge.grappa.matchers.trie.Trie;
import com.github.fge.grappa.matchers.trie.TrieBuilder;
import com.github.fge.grappa.run.context.MatcherContext;
import com.github.fge.grappa.util.MatcherContextBuilder;
import com.google.common.collect.Lists;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

public final class CaseInsensitiveTrieMatcherTest
{
    private static final String[] KEYWORDS = {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch",
        "char", "class", "const", "continue", "default", "double", "do",
        "else", "enum", "extends", "finally", "final", "float", "for",
        "goto", "if", "implements", "import", "instanceof", "interface", "int",
        "long", "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch",
        "synchronized", "this", "throws", "throw", "transient", "try", "void",
        "volatile", "while", "false", "null", "true"
    };

    private final CaseInsensitiveTrieMatcher matcher;

    public CaseInsensitiveTrieMatcherTest()
    {
        TrieBuilder builder = Trie.newBuilder();

        for (String keyword: KEYWORDS)
            builder.addWord(keyword);

        matcher = new CaseInsensitiveTrieMatcher(builder.build());
    }

    @DataProvider
    public Iterator<Object[]> getMatchData()
    {
        List<Object[]> list = Lists.newArrayList();

        MatcherContextBuilder builder = new MatcherContextBuilder()
            .withMatcher(matcher);

        String input;
        boolean matched;
        int index;
        MatcherContext<Object> context;

        // Full match...
        input = "abStract";
        matched = true;
        index = 8;
        context = builder.withInput(input).build();
        list.add(new Object[] { context, matched, index });

        // Match within string...
        input = "ASSERTIONS";
        matched = true;
        index = 6;
        context = builder.withInput(input).build();
        list.add(new Object[] { context, matched, index });

        // No match -- just
        input = "volatil";
        matched = false;
        index = 0;
        context = builder.withInput(input).build();
        list.add(new Object[] { context, matched, index });

        // Match but not at current index: no dice...
        input = "an Abstract Method";
        matched = false;
        index = 0;
        context = builder.withInput(input).build();
        list.add(new Object[] { context, matched, index });

        // "near" match of a longer word...
        input = "doubl";
        matched = true;
        index = 2;
        context = builder.withInput(input).build();
        list.add(new Object[] { context, matched, index });

        // Match at index 0, even with trainling text...
        input = "instanceof Integer";
        matched = true;
        index = 10;
        context = builder.withInput(input).build();
        list.add(new Object[] { context, matched, index });

        return list.iterator();
    }

    @Test(dataProvider = "getMatchData")
    public void trieMatchingWorksCorrectly(MatcherContext<Object> ctx,
                                           boolean matched, int index)
    {
        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(matcher.match(ctx)).as("match/no match")
            .isEqualTo(matched);

        soft.assertThat(ctx.getCurrentIndex()).as("post match run index")
            .isEqualTo(index);

        soft.assertAll();
    }
}
