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

import com.github.fge.grappa.rules.Rule;

import java.lang.annotation.*;

/**
 * Apply a custom label to a {@link Rule}
 *
 * <p>The default behaviour of the parser generator is to label a rule after the
 * method name producing that rule. Using this annotation, you can instruct the
 * generator to give the rule a name of your choice.</p>
 *
 * @see Rule#label(String)
 * @see DontLabel
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Label
{
    String value();
}
