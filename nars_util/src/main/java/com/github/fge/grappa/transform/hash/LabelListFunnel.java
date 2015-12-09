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

import com.google.common.collect.Sets;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.objectweb.asm.Label;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Set;

/**
 * A {@link Funnel} for a list of (ASM) {@link Label}s
 *
 * <p>This funnel keeps a set of labels it has already been asked to funnel;
 * when one has already been visited, this funnel does nothing.</p>
 *
 * @see InstructionGroupHasher#visitLabel(Label)
 */
@NotThreadSafe
@ParametersAreNonnullByDefault
public final class LabelListFunnel
    implements Funnel<Label>
{
    private final Set<Label> labels = Sets.newHashSet();
    private int index = 0;

    @Override
    public void funnel(Label from, PrimitiveSink into)
    {
        if (labels.add(from))
            into.putInt(index++);
    }
}
