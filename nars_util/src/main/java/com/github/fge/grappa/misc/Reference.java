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

package com.github.fge.grappa.misc;

import com.google.common.base.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A simple container holding a reference to another object.
 *
 * @param <T>
 */
public class Reference<T>
{
    private T value;

    /**
     * Create a new Reference with a null value.
     */
    public Reference()
    {
    }

    /**
     * Create a new Reference to the given value object.
     *
     * @param value the value object
     */
    public Reference(@Nullable T value)
    {
        this.value = value;
    }

    /**
     * Sets this references value field to null.
     *
     * @return true
     */
    public final boolean clear()
    {
        return set(null);
    }

    /**
     * Sets this references value object to the given instance.
     *
     * @param value the value
     * @return true
     */
    public final boolean set(@Nullable T value)
    {
        this.value = value;
        return true;
    }

    /**
     * Retrieves this references value object.
     *
     * @return the target
     */
    @Nullable
    public final T get()
    {
        return value;
    }

    /**
     * Retrieves the non null value stored by this var
     *
     * @return the value if non null
     * @throws IllegalStateException value is null
     *
     * @since 1.0.0-beta.10
     */
    @Nonnull
    public final T getNonnull()
    {
        // See javadoc for Guava's Optional; this throws IllegalStateException
        // if value is null
        return Optional.fromNullable(value).get();
    }


    /**
     * Replaces this references value with the given one.
     *
     * @param value the new value
     * @return the previous value
     */
    public final T getAndSet(T value)
    {
        T ret = this.value;
        this.value = value;
        return ret;
    }

    /**
     * @return true if this Reference holds a non-null value
     */
    public final boolean isSet()
    {
        return value != null;
    }
}
