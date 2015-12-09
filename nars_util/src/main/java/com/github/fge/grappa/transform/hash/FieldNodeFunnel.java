/*
 * Copyright (C) 2014 Francis Galiegue <fgaliegue@gmail.com>
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

package com.github.fge.grappa.transform.hash;

import com.google.common.base.Strings;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.objectweb.asm.tree.FieldNode;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link Funnel} for an ASM {@link FieldNode}
 */
@Immutable
@ParametersAreNonnullByDefault
public enum FieldNodeFunnel
    implements Funnel<FieldNode>
{
    INSTANCE
    {
        @Override
        public void funnel(FieldNode from, PrimitiveSink into)
        {
            into.putUnencodedChars(Strings.nullToEmpty(from.name))
                .putUnencodedChars(Strings.nullToEmpty(from.desc))
                .putUnencodedChars(Strings.nullToEmpty(from.signature));
        }
    }
}
