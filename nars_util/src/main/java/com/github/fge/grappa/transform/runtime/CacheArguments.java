package com.github.fge.grappa.transform.runtime;

import com.github.fge.grappa.annotations.Cached;

import java.util.Arrays;
import java.util.Map;

/**
 * Runtime cache arguments for generated parsers
 *
 * <p>This class is used by generated parsers for rules taking arguments and
 * annotated with {@link Cached}. In this case, a {@link Map} is generated whose
 * keys are instances of this class, and values are already generated rules.</p>
 *
 * <p>What this class basically does is generate a "digest" of rule
 * arguments (as an {@code Object[]}).</p>
 */
public final class CacheArguments
{
    private final Object[] params;

    public CacheArguments(Object... params)
    {
        this.params = Arrays.copyOf(params, params.length);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        CacheArguments other = (CacheArguments) obj;
        return Arrays.deepEquals(params, other.params);
    }

    @Override
    public int hashCode()
    {
        return Arrays.deepHashCode(params);
    }
}
