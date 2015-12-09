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

import com.github.fge.grappa.misc.AsmUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Array;

/**
 * A class cache using a Guava {@link LoadingCache}
 *
 * <p>Only one cache exists for each instance.</p>
 */
public final class ClassCache
{
    public static final ClassCache INSTANCE = new ClassCache();

    private final LoadingCache<String, Class<?>> cache;

    private ClassCache()
    {
        cache = CacheBuilder.newBuilder().recordStats()
            .build(ClassCacheLoader.LOADER);
    }

    /**
     * Load a class from a JVM string class descriptor (for instance {@code
     * Ljava/lang/String}
     *
     * <p>Note that for arrays, the class of the array elements is returned.</p>
     *
     * <p>Note also that this uses {@link LoadingCache#getUnchecked(Object)} to
     * retrieve the result.</p>
     *
     * @param className the internal name
     * @return a class
     * @throws UncheckedExecutionException cannot load the class
     */
    public Class<?> loadClass(String className)
    {
        return cache.getUnchecked(className);
    }

    @ParametersAreNonnullByDefault
    private static final class ClassCacheLoader
        extends CacheLoader<String, Class<?>>
    {
        private static final ClassCacheLoader LOADER = new ClassCacheLoader();

        /**
         * Computes or retrieves the value corresponding to {@code key}.
         *
         * @param key the non-null key whose value should be loaded
         * @return the value associated with {@code key}; <b>must not be
         * null</b>
         *
         * @throws ClassNotFoundException if unable to load the result
         * @throws InterruptedException if this method is interrupted. {@code
         * InterruptedException} is
         * treated like any other {@code Exception} in all respects except
         * that, when it is caught,
         * the thread's interrupt status is set
         */
        @Override
        public Class<?> load(String key)
            throws ClassNotFoundException
        {
            // Array...
            if (key.length() > 0 && key.charAt(0) == '[') {
                String elementName = key.substring(1);
                Type type = Type.getType(elementName);
                Class<?> c = AsmUtils.getClassForType(type);
                return Array.newInstance(c, 0).getClass();
            }
            String name = key.replace('/', '.');
            ClassLoader cl = ClassCacheLoader.class.getClassLoader();
            try {
                return cl.loadClass(name);
            } catch (ClassNotFoundException ignored) {
                cl = Thread.currentThread().getContextClassLoader();
                return cl.loadClass(name);
            }
        }
    }

    @Override
    @Nonnull
    public String toString()
    {
        return cache.stats().toString();
    }
}
