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

package com.github.fge.grappa.parsers;

import com.github.fge.grappa.helpers.ValueBuilder;
import com.github.fge.grappa.support.Var;
import com.google.common.eventbus.EventBus;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A basic parser with an attached {@link EventBus}
 *
 * <p>Basic usage of this parser is as follows:</p>
 *
 * <ul>
 *     <li>you create classes with listening methods (see below);</li>
 *     <li>you register these classes to the parser's bus;</li>
 *     <li>you post objects on the bus from your parser rules.</li>
 * </ul>
 *
 * <p>A "listening method" is a method which obeys the following conditions:</p>
 *
 * <ul>
 *     <li>it is {@code public};</li>
 *     <li>it accepts a single argument, which is the type of the posted event.
 *     </li>
 * </ul>
 *
 * <p>Note that the return values of this method can be anything; generally,
 * though, such methods return {@code void}. Also note that these methods will
 * not only receive objects of the exact type they have subscribed to, but any
 * subtype as well; a method accepting a {@link Number}, for instance, will also
 * receive {@link Integer}s and {@link Double}s.</p>
 *
 * <p>An example class would be:</p>
 *
 * <pre>
 *     public class MyListener
 *     {
 *         private String value;
 *
 *         &#64;Subscribe
 *         public void setValue(final String value)
 *         {
 *             this.value = value;
 *         }
 *     }
 * </pre>
 *
 * <p>In your parser you could then do:</p>
 *
 * <pre>
 *     Rule myRule()
 *     {
 *         return sequence(oneOrMore('a'), postRaw(match()));
 *     }
 * </pre>
 *
 * <p>The parser class also has two other methods to post events: one taking a
 * {@link ValueBuilder} as an argument and another taking a {@link Var} as an
 * argument. These methods will extract the values from both of these classes
 * and feed it to the event bus.</p>
 *
 * <p>For more details on how the bus work, see the {@link EventBus
 * documentation for this class}, along with <a
 * href="https://code.google.com/p/guava-libraries/wiki/EventBusExplained"
 * target="_blank">Guava's user guide article</a>.</p>
 *
 * @param <V> the result type of the parser
 *
 * @see ValueBuilder
 */
public abstract class ListeningParser<V>
    extends BaseParser<V>
{
    protected final EventBus bus = new EventBus();

    /**
     * Register a listener to the event bus
     *
     * <p>The canonical use of this method is to register a listener
     * once you have created your parser, as in:</p>
     *
     * <pre>
     *     final MyParser parser = Parboiled.createParser(MyParser.class);
     *     parser.register(myListener);
     * </pre>
     *
     * <p>It is also usable as an action; you can write rules such as:</p>
     *
     * <pre>
     *     Rule myRule()
     *     {
     *         return sequence(other(), register(...));
     *     }
     * </pre>
     *
     * @param listener the listener
     * @return always true
     *
     * @see EventBus#register(Object)
     */

    public final boolean register(@Nonnull Object listener)
    {
        bus.register(Objects.requireNonNull(listener));
        return true;
    }

    /**
     * Unregister a listener from this parser's {@link EventBus}
     *
     * <p>In a similar fashion as {@link #register(Object)}, you can use this
     * method as an action.</p>
     *
     * @param listener the listener to remove
     * @return always true
     */
    public final boolean unregister(@Nonnull Object listener)
    {
        bus.unregister(Objects.requireNonNull(listener));
        return true;
    }

    /**
     * Post a value on the bus from a {@link ValueBuilder}
     *
     * <p>This method will {@link ValueBuilder#build() build} the value, post
     * the result on the bus and then {@link ValueBuilder#reset() reset} the
     * value builder.</p>
     *
     * @param builder the value builder
     * @param <T> the value type produced by the builder
     * @return always {@code true}
     */
    public final <T> boolean post(@Nonnull ValueBuilder<T> builder)
    {
        Objects.requireNonNull(builder);

        T event = builder.build();
        bus.post(event);
        builder.reset();
        return true;
    }

    /**
     * Post a value on the bus from a {@link Var}
     *
     * <p>This method will {@link Var#get() get} the value of the associated
     * var and post it on the bus.</p>
     *
     * <p>Notes:</p>
     *
     * <ul>
     *     <li>the value <strong>must not be null</strong>;</li>
     *     <li>this method will not affect the var in any way (ie, it will leave
     *     the existing value intact etc).</li>
     * </ul>
     *
     * @param var the var to use
     * @param <T> value type of the var
     * @return always true
     */
    public final <T> boolean post(@Nonnull Var<T> var)
    {
        Objects.requireNonNull(var);
        @SuppressWarnings("ConstantConditions") T value = Objects.requireNonNull(var.get());
        bus.post(value);
        return true;
    }

    /**
     * "Raw" post to the bus
     *
     * <p>Use this method if you want to post any other object than a value
     * wrapped in a {@code Var} or {@code ValueBuilder}.</p>
     *
     * @param object the object (must not be null)
     * @return always true
     */
    public final boolean postRaw(@Nonnull Object object)
    {
        Objects.requireNonNull(object);
        bus.post(object);
        return true;
    }
}
