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

package com.github.fge.grappa.test;

import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.run.ListeningParseRunner;
import com.github.fge.grappa.run.ParseRunner;
import com.github.fge.grappa.run.ParsingResult;

import static org.assertj.core.api.Fail.fail;

public abstract class ParboiledTest<V> {

    public static class TestResult<V> {
        public final ParsingResult<V> result;

        public TestResult(ParsingResult<V> result) {
            this.result = result;
        }

        public TestResult<V> hasNoErrors() {
//            resultAssert.isSuccess();
            if (result.isSuccess())
                return this;
            fail("Errors detected");
            return this;
        }
    }

    public TestResult<V> test(Rule rule, String input) {
        ParseRunner<V> runner = new ListeningParseRunner<>(rule);
        return new TestResult<>(runner.run(input));
    }
    
    public TestResult<V> test(Rule rule, InputBuffer inputBuffer) {
        ParseRunner<V> runner = new ListeningParseRunner<>(rule);
        return new TestResult<>(runner.run(inputBuffer));
    }
}
