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

import com.github.fge.grappa.annotations.*;
import com.github.fge.grappa.transform.base.RuleMethod;
import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Enumeration associating parser or rule annotations to their ASM descriptor
 *
 * @see Type#getDescriptor()
 */
public enum ParserAnnotation
{
    CACHED(Cached.class),
    EXPLICIT_ACTIONS_ONLY(ExplicitActionsOnly.class),
    DONT_LABEL(DontLabel.class),
    DONT_EXTEND(DontExtend.class),
    DONT_SKIP_ACTIONS_IN_PREDICATES(DontSkipActionsInPredicates.class),
    SKIP_ACTIONS_IN_PREDICATES(SkipActionsInPredicates.class),
    ;

    /**
     * @see RuleMethod#moveFlagsTo(RuleMethod)
     */
    private static final Set<ParserAnnotation> FLAGS_COPY
        = EnumSet.of(CACHED, DONT_LABEL);

    /**
     * @see RuleMethod#moveFlagsTo(RuleMethod)
     */
    private static final Set<ParserAnnotation> FLAGS_CLEAR = EnumSet.of(CACHED);

    /**
     * @see RuleMethod#moveFlagsTo(RuleMethod)
     */
    private static final Set<ParserAnnotation> FLAGS_SET
        = EnumSet.of(DONT_LABEL);

    private static final Map<String, ParserAnnotation> REVERSE_MAP;

    static {
        ImmutableMap.Builder<String, ParserAnnotation> builder
            = ImmutableMap.builder();

        for (ParserAnnotation entry: values())
            builder.put(entry.descriptor, entry);

        REVERSE_MAP = builder.build();
    }


    private final String descriptor;

    ParserAnnotation(Class<? extends Annotation> c)
    {
        descriptor = Type.getType(c).getDescriptor();
    }

    /**
     * Record an enumeration value into a set if the descriptor is known
     *
     * @param set the set to record into
     * @param desc the descriptor
     * @return true if the descriptor is known
     */
    public static boolean recordAnnotation(Set<ParserAnnotation> set,
                                           String desc)
    {
        ParserAnnotation annotation = REVERSE_MAP.get(desc);
        if (annotation == null)
            return false;
        set.add(annotation);
        return true;
    }

    /**
     * @see RuleMethod#moveFlagsTo(RuleMethod)
     *
     * @param from set to move flags from
     * @param  to set to move flags to
     */
    public static void moveTo(Set<ParserAnnotation> from,
                              Set<ParserAnnotation> to)
    {
        Set<ParserAnnotation> transferred = EnumSet.copyOf(from);
        transferred.retainAll(FLAGS_COPY);
        to.addAll(transferred);
        from.addAll(FLAGS_SET);
        from.removeAll(FLAGS_CLEAR);
    }
}
