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
import com.github.fge.grappa.annotations.Label;
import com.github.fge.grappa.parsers.BaseActions;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Action;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.context.Context;
import com.github.fge.grappa.test.ParboiledTest;
import org.testng.annotations.Test;

public class ActionTest extends ParboiledTest<Integer>
{

    public static class Actions extends BaseActions<Integer>
    {

        public boolean addOne() {
            Integer i = getContext().getValueStack().pop();
            getContext().getValueStack().push(i + 1);
            return true;
        }
    }

    public static class Parser extends BaseParser<Integer>
    {

        final Actions actions = new Actions();

        public Rule A() {
            return sequence(
                    'a',
                    push(42),
                    B(18),
                    stringAction("lastText:" + match())
            );
        }

        public Rule B(int i) {
            int j = i + 1;
            return sequence(
                    'b',
                    push(timesTwo(i + j)),
                    C(),
                    push(pop()) // no effect
            );
        }

        public Rule C() {
            return sequence(
                    'c',
                    push(pop()), // no effect
                    new Action() {
                        @Override
                        public boolean run(Context context) {
                            return getContext() == context;
                        }
                    },
                    D(1)
            );
        }

        @Label("Last")
        public Rule D(int i) {
            return sequence(
                    'd', dup(),
                    push(i),
                    actions.addOne()
            );
        }

        public boolean stringAction(String string) {
            return "lastText:bcd".equals(string);
        }

        // ************* ACTIONS **************

        public int timesTwo(int i) {
            return i * 2;
        }

    }

    @Test
    public void test() {
        Parser parser = Grappa.createParser(Parser.class);
        test(parser.A(), "abcd").hasNoErrors();
    }

}
