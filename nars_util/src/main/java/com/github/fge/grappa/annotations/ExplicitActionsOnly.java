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

package com.github.fge.grappa.annotations;

import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.rules.Action;

import java.lang.annotation.*;

/**
 * Don't wrap boolean expressions into {@link Action}s
 *
 * <p>When used in rules, expressions returning {@code boolean}s are wrapped
 * into {@link Action}s by the parser generator. Example:</p>
 *
 * <pre>
 *     Rule myRule()
 *     {
 *         // Only if depth level is less than 5
 *         return Sequence(someRule(), getContext().getLevel() &lt; 5);
 *     }
 * </pre>
 *
 * <p>If this annotation is used (either at the method level or at the class
 * level), such automatic wrapping does not happen anymore and you have to use
 * {@link BaseParser#ACTION(boolean)} to make actions explicit.</p>
 *
 * @see SkipActionsInPredicates
 * @see DontSkipActionsInPredicates
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExplicitActionsOnly
{
}
