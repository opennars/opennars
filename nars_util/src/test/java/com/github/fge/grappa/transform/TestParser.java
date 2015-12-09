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

import com.github.fge.grappa.annotations.Cached;
import com.github.fge.grappa.annotations.DontLabel;
import com.github.fge.grappa.annotations.ExplicitActionsOnly;
import com.github.fge.grappa.annotations.Label;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Rule;
import com.github.fge.grappa.support.Var;

import static java.lang.Integer.parseInt;

@SuppressWarnings("UnusedDeclaration")
public class TestParser extends BaseParser<Integer> {

    protected int integer;
    private int privateInt;

    public Rule RuleWithoutAction() {
        return sequence('a', 'b');
    }

    @Label("harry")
    public Rule RuleWithNamedLabel() {
        return sequence('a', 'b');
    }

    public Rule RuleWithLeaf() {
        return sequence('a', 'b');
    }

    public Rule RuleWithDirectImplicitAction() {
        return sequence('a', integer == 0, 'b', 'c');
    }

    public Rule RuleWithIndirectImplicitAction() {
        return sequence('a', 'b', action() || integer == 5);
    }

    public Rule RuleWithDirectExplicitAction() {
        return sequence('a', ACTION(action() && integer > 0), 'b');
    }

    public Rule RuleWithIndirectExplicitAction() {
        return sequence('a', 'b', ACTION(integer < 0 && action()));
    }

    public Rule RuleWithIndirectImplicitParamAction(int param) {
        return sequence('a', 'b', integer == param);
    }

    public Rule RuleWithComplexActionSetup(int param) {
        int i = 26, j = 18;
        Var<String> string = new Var<>("text");
        i += param;
        j -= i;
        return sequence('a' + i, i > param + j, string, ACTION(integer + param < string.get().length() - i - j));
    }

    public Rule BugIn0990() {
        Var<Integer> var = new Var<>();
        return firstOf("10", "2");
    }

    @DontLabel
    public Rule RuleWith2Returns(int param) {
        return param == integer ? sequence('a', ACTION(action())) : eof();
    }

    @DontLabel
    public Rule RuleWithSwitchAndAction(int param) {
        switch (param) {
            case 0: return sequence(EMPTY, push(1));
        }
        return null;
    }

    @ExplicitActionsOnly
    public Rule RuleWithExplicitActionsOnly(int param) {
        Boolean b = integer == param;
        return sequence('a', 'b', b);
    }

    @Cached
    public Rule RuleWithCachedAnd2Params(String string, long aLong) {
        return sequence(string, aLong == integer);
    }

    public Rule RuleWithFakeImplicitAction(int param) {
        Boolean b = integer == param;
        return sequence('a', 'b', b);
    }

    public Rule NumberRule() {
        return sequence(
                oneOrMore(charRange('0', '9')),
                push(parseInt(match()))
        );
    }

    // actions

    public boolean action() {
        return true;
    }

}
