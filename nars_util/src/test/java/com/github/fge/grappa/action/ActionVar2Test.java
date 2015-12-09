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
import com.github.fge.grappa.misc.Reference;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.test.ParboiledTest;
import org.testng.annotations.Test;

public class ActionVar2Test extends ParboiledTest<Object>
{
    static class Parser extends BaseParser<Object>
    {
        Rule clause() {
            Reference<Integer> count = new Reference<>();
            return sequence(charCount(count), chars(count), '\n');
        }

        Rule charCount(Reference<Integer> count) {
            return sequence(
                '{',
                oneOrMore(charRange('0', '9')),
                count.set(Integer.parseInt(match())),
                '}'
            );
        }

        Rule chars(Reference<Integer> count) {
            return sequence(
                zeroOrMore(
                    count.get() > 0,
                    ANY,
                    count.set(count.get() - 1)
                ),
                count.get() == 0
            );
        }
    }

    @Test
    public void test() {
        Parser parser = Grappa.createParser(Parser.class);
        test(parser.clause(), "{12}abcdefghijkl\n")
                .hasNoErrors();
    }
}
