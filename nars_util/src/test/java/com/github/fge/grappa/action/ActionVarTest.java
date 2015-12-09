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

package com.github.fge.grappa.action;

import com.github.fge.grappa.Grappa;
import com.github.fge.grappa.matchers.base.Matcher;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.support.Var;
import com.github.fge.grappa.test.ParboiledTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class ActionVarTest extends ParboiledTest<Integer>
{

    static class Parser extends BaseParser<Integer>
    {

        @SuppressWarnings("InfiniteRecursion")
        public Rule A() {
            Var<List<String>> list = new Var<>(new ArrayList<>());
            return sequence('a', optional(A(), list.get().add("Text"), push(list.get().size())));
        }

    }

    @Test
    public void test() {
        Parser parser = Grappa.createParser(Parser.class);
        Matcher rule = (Matcher) parser.A();

        assertEquals(rule.getClass().getName(), "com.github.fge.grappa.matchers.wrap.VarFramingMatcher");

        test(rule, "aaaa").hasNoErrors();
    }

}
