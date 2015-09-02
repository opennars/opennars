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
import com.github.fge.grappa.transform.runtime.CacheArguments;

import java.lang.annotation.*;
import java.util.HashMap;

/**
 * Build a cache for {@link Rule}s having arguments
 *
 * <p>This annotation can be used on rules with arguments; for instance:</p>
 *
 * <pre>
 *     Rule matchChar(final char c)
 *     {
 *         return Ch(c);
 *     }
 * </pre>
 *
 * <p>The generated parser will then have a {@link HashMap} whose keys are
 * {@link CacheArguments} instances and values are {@link Rule}s.</p>
 * <p>You must <strong>not</strong> use this annotation for rules having no
 * arguments (this will raise an error).</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached
{
}
