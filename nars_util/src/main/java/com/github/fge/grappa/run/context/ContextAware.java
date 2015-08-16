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

package com.github.fge.grappa.run.context;

import com.github.fge.grappa.matchers.delegate.SequenceMatcher;
import com.github.fge.grappa.rules.Action;
import com.github.fge.grappa.transform.base.InstructionGroup;
import com.github.fge.grappa.transform.process.GroupClassGenerator;

/**
 * Interface that can be implemented by classes containing action methods.
 *
 * <p>If a class implementing this interface is used in a parser, the generated
 * parser will use this interface  to inform the instance of the current
 * context. This applies to {@link Action}s but also to parsers within parsers.
 * </p>
 *
 * <p>Note that implementing this interface currently will not prevent you from
 * suffering the limits of actions; that is, if you are not the second or more
 * member of a {@link SequenceMatcher}, you won't get a context (it will be
 * null in this case).</p>
 *
 * @see GroupClassGenerator#insertSetContextCalls(InstructionGroup, int)
 */
public interface ContextAware<V>
{

    /**
     * Called immediately before any parser action method invocation. Informs
     * the object containing the action about the context to be used for the
     * coming action call.
     *
     * @param context the context
     */
    void setContext(Context<V> context);

}
