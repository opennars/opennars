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

import com.github.fge.grappa.rules.Action;
import com.github.fge.grappa.run.context.ContextAware;

import java.lang.annotation.*;

/**
 * Do not run actions in "predicate" rules ({@code Test} and {@code TestNot})
 *
 * <p>{@link Action}s can be used to perform user defined operations, which can
 * include, for instance, setting values into beans etc. For instance:</p>
 *
 * <pre>
 *     Rule myRule()
 *     {
 *         return Sequence(someRule(), storeSomeValue());
 *     }
 * </pre>
 *
 * <p>The problem is that you may also use such rules in a {@code Test} or
 * {@code TestNot} rule such as in:</p>
 *
 * <pre>
 *     Rule otherRule()
 *     {
 *         return Sequence(TestNot(myRule()), someOtherRule());
 *     }
 * </pre>
 *
 * <p>In this case, you may not want {@code storeSomeValue()} to be executed in
 * that particular context. This annotation helps with that.</p>
 *
 * <p>You can either use it at the rule level or at the class level. If at the
 * class level, it will affect all rules with defined actions.</p>
 *
 * <p>If at the class level, you can also use the {@link
 * DontSkipActionsInPredicates} annotation on a rule to override this
 * annotation.</p>
 *
 * @see Action
 * @see ContextAware
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SkipActionsInPredicates
{
}
