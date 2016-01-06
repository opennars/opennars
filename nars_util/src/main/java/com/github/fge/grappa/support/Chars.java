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

package com.github.fge.grappa.support;

import com.google.common.base.Optional;
import com.gs.collections.api.map.primitive.CharObjectMap;
import com.gs.collections.impl.map.mutable.primitive.CharObjectHashMap;

public   enum Chars {
    ;
    /**
     * The End-of-Input non-character.
     */
    public static final char EOI = '\uFFFF';

    private static final CharObjectHashMap<String> ESCAPE_MAP = new CharObjectHashMap<>();
    static {
        ESCAPE_MAP.put('\r', "\\r");
        ESCAPE_MAP.put('\n', "\\n");
        ESCAPE_MAP.put('\t', "\\t");
        ESCAPE_MAP.put('\f', "\\f");
        ESCAPE_MAP.put(EOI, "EOI");
        ESCAPE_MAP.compact();
    }
//            = ImmutableMap.<Character, String>builder()
//            .put('\r', "\\r")
//            .put('\n', "\\n")
//            .put('\t', "\\t")
//            .put('\f', "\\f")
//            .put(EOI, "EOI")
//            .build();

    /**
     * Return a map of characters to escape and their replacements
     *
     * @return an escape map (immutable)
     * @see CharsEscaper
     */
    public static CharObjectMap<String> escapeMap() {
        return ESCAPE_MAP;
    }

    public static char escapeChar(char c) {
        String s = ESCAPE_MAP.get(c);
        if (s == null) return 0;

        return s.charAt(0);
    }

    public static String escape(char c) {
        return Optional.fromNullable(ESCAPE_MAP.get(c)).or(String.valueOf(c));
    }
}
