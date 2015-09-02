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
 * Override {@link SkipActionsInPredicates} for one particular rule
 *
 * <p>If your parser class is configured to {@link SkipActionsInPredicates}, you
 * can use this annotation on one particular rule to override this behaviour and
 * execute the action(s) of this rule nevertheless.</p>
 *
 * <p>You probably want to use this annotation if your action performs a check
 * of the currently matched input.</p>
 *
 * @see Action
 * @see ContextAware
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DontSkipActionsInPredicates
{
}
