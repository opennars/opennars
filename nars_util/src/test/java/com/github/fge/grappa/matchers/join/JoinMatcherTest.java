///*
// * Copyright (C) 2014 Francis Galiegue <fgaliegue@gmail.com>
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.github.fge.grappa.matchers.join;
//
//import com.github.fge.grappa.Grappa;
//import com.github.fge.grappa.exceptions.GrappaException;
//import com.github.fge.grappa.parsers.BaseParser;
//import com.github.fge.grappa.rules.Rule;
//import com.github.fge.grappa.run.ListeningParseRunner;
//import com.github.fge.grappa.run.ParseRunner;
//import org.testng.annotations.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.fail;
//
//public final class JoinMatcherTest
//{
//    static class MyParser
//        extends BaseParser<Object>
//    {
//        Rule rule()
//        {
//            return join(zeroOrMore('a'))
//                .using(optional('b').label("foo"))
//                .min(0);
//        }
//    }
//
//    @Test
//    public void joinMatcherYellsIfJoiningRuleMatchesEmpty()
//    {
//        CharSequence input = "aaaabaaaaxaaa";
//        MyParser parser = Grappa.createParser(MyParser.class);
//        ParseRunner<Object> runner
//            = new ListeningParseRunner<>(parser.rule());
//        String expectedMessage = "joining rule (foo) of a JoinMatcher" +
//            " cannot match an empty character sequence!";
//
//        try {
//            runner.run(input);
//            fail("No exception thrown!!");
//        } catch (GrappaException e) {
//            assertThat(e).hasMessage(expectedMessage);
//        }
//    }
// }
