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

package com.github.fge.grappa.transform;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

public enum LoadingOpcode
{
    ;
    private static final Map<Integer, Integer> LOADING_OPCODES;

    static {
        ImmutableMap.Builder<Integer, Integer> builder
            = ImmutableMap.builder();

        builder.put(Type.BOOLEAN, Opcodes.ILOAD);
        builder.put(Type.BYTE, Opcodes.ILOAD);
        builder.put(Type.CHAR, Opcodes.ILOAD);
        builder.put(Type.SHORT, Opcodes.ILOAD);
        builder.put(Type.INT, Opcodes.ILOAD);
        builder.put(Type.DOUBLE, Opcodes.DLOAD);
        builder.put(Type.FLOAT, Opcodes.FLOAD);
        builder.put(Type.LONG, Opcodes.LLOAD);
        builder.put(Type.OBJECT, Opcodes.ALOAD);
        builder.put(Type.ARRAY, Opcodes.ALOAD);

        LOADING_OPCODES = builder.build();
    }

    public static int forType(@Nonnull Type type)
    {
        Objects.requireNonNull(type);
        // Will throw IllegalStateException if optional .isAbsent()
        return Optional.fromNullable(LOADING_OPCODES.get(type.getSort())).get();
    }
}
