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

package com.github.fge.grappa.transform;

import com.github.fge.grappa.Grappa;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.ListeningParseRunner;
import org.testng.annotations.Test;

public class BugIn101Test {

    static class Parser extends BaseParser<Object> {
        Rule A() {
            Object a = new Object();
            return sequence("a", push(a));
        }
        Rule B() {
            String b = "b";
            return sequence("b", push(b));
        }
        Rule Switch(int i) {
            switch (i) {
                case 0: return sequence(EMPTY, push(1));
            }
            return null;
        }
    }

    @Test
    public void test() throws Exception {
        // threw "java.lang.RuntimeException: Error creating extended parser class:
        // Execution can fall off end of the code" in 1.0.1
        Parser parser = Grappa.createParser(Parser.class);

        // threw "java.lang.NoSuchFieldError: field$1" in 1.0.1
        new ListeningParseRunner<>(parser.B()).run("b");
    }
}
