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

package com.github.fge.grappa;

import com.github.fge.grappa.exceptions.InvalidGrammarException;
import com.github.fge.grappa.parsers.BaseParser;
import com.github.fge.grappa.run.ParseRunner;
import com.github.fge.grappa.transform.ParserTransformer;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * Main class providing the high-level entry point into the parboiled library.
 */
public enum Grappa
{
    ;

    /**
     * <p>Creates a parser object whose rule creation methods can then be used
     * with one of the {@link ParseRunner} implementations.</p>
     * <p>Since parboiled needs to extend your parser with certain extra logic
     * (e.g. to prevent infinite recursions in recursive rule definitions) you
     * cannot create your parser object yourself, but have to go through this
     * method. Also your parser class has to be derived from {@link BaseParser}.
     * If you want to use a non-default constructor you can provide its
     * arguments to this method. Make sure your non-default constructor does not
     * use primitive type parameters (like "int") but rather their boxed
     * counterparts (like "Integer"), otherwise the constructor will not be
     * found.</p>
     * <p>Performing the rule analysis and extending the parser class is an
     * expensive process (time-wise) and can take up to several hundred
     * milliseconds for large grammars. However, this cost is only incurred once
     * per parser class and class loader. Subsequent calls to this method are
     * therefore fast once the initial extension has been performed.</p>
     *
     * @param parserClass the type of the parser to create
     * @param constructorArgs optional arguments to the parser class constructor
     * @return the ready to use parser instance
     */
    public static <P extends BaseParser<V>, V> P createParser(
            @Nonnull Class<P> parserClass, Object... constructorArgs)
    {
        Objects.requireNonNull(parserClass, "parserClass");
        try {
            Class<?> extendedClass
                = ParserTransformer.transformParser(parserClass);
            Constructor<?> constructor
                = findConstructor(extendedClass, constructorArgs);
            @SuppressWarnings("unchecked") P ret = (P) constructor.newInstance(constructorArgs);
            return ret;
        } catch (Exception e) { // TODO: catch better than Exception
            throw new RuntimeException(
                "Error creating extended parser class: " + e.getMessage(), e);
        }
    }

    /**
     * Generate the byte code of a transformed parser class
     *
     * <p>When you create a parser using {@link
     * #createParser(Class, Object...)}, Parboiled generates a new class which
     * is the one you actually use for parsing. This method allows to get the
     * byte code of such a generated class in a byte array.</p>
     *
     * @param parserClass the parser class
     * @param <P> class of the parser
     * @param <V> see {@link BaseParser}
     * @return the byte code
     *
     * @throws RuntimeException byte code generation failure
     * @see ParserTransformer#getByteCode(Class)
     */
    public static <P extends BaseParser<V>, V> byte[] getByteCode(
        Class<P> parserClass)
    {
        try {
            return ParserTransformer.getByteCode(parserClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to generate byte code", e);
        }
    }

    private static Constructor<?> findConstructor(Class<?> c,
                                                  Object[] arguments)
    {
        Class<?>[] paramTypes;
        Object argument;

outer:
        for (Constructor<?> constructor : c.getConstructors()) {
            paramTypes = constructor.getParameterTypes();
            if (paramTypes.length != arguments.length)
                continue;
            for (int i = 0; i < arguments.length; i++) {
                argument = arguments[i];
                if (argument != null && !paramTypes[i]
                    .isAssignableFrom(argument.getClass()))
                    continue outer;
                if (argument == null && paramTypes[i].isPrimitive())
                    continue outer;
            }
            return constructor;
        }
        throw new InvalidGrammarException("No constructor found for " + c
            + " and the given " + arguments.length+ " arguments");
    }
}
