/* Copyright 2009 - 2010 The Stajistics Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nars.util.meter.key;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 *
 * @author The Stajistics Project
 */
public final class StatsKeyUtil {

    private StatsKeyUtil() {}

    /**
     * Obtain the name of the parent key of <tt>keyName</tt>, or <tt>null</tt> if it doesn't have a parent.
     * @param keyName
     * @return
     */
    public static String parentKeyName(final String keyName) {
//        String parentKeyName = null;
//
//        int i = keyName.lastIndexOf(StatsConstants.KEY_HIERARCHY_DELIMITER);
//        if (i > -1) {
//            parentKeyName = keyName.substring(0, i);
//        }
//
//        return parentKeyName;
        return null;
    }

    /**
     * Get a list containing the key name hierarchy for the given <tt>keyName</tt>. 
     * @param keyName
     * @return
     */
    public static List<String> keyNameHierarchy(final String keyName, boolean ascending) {
        final List<String> result = new ArrayList<String>(8);

        if (ascending) {
            int i = -1;
            for (;;) {
                i = keyName.indexOf(StatsConstants.KEY_HIERARCHY_DELIMITER, i + 1);
                if (i == -1) {
                    result.add(keyName);
                    break;
                }

                result.add(keyName.substring(0, i));
            }
        } else {
            result.add(keyName);

            int i = keyName.length();
            for (;;) {
                i = keyName.lastIndexOf(StatsConstants.KEY_HIERARCHY_DELIMITER, i - 1);
                if (i == -1) {
                    break;
                }

                result.add(keyName.substring(0, i));
            }
        }

        return result;
    }

//    public static List<StatsKey> keyHierarchy(final String keyName, final StatsKeyFactory keyFactory, final boolean ascending) {
//        final List<String> keyNameHierarchy = keyNameHierarchy(keyName, ascending);
//        final int keyCount = keyNameHierarchy.size();
//        final List<StatsKey> result = new ArrayList<StatsKey>(keyCount);
//
//        for (int i = 0; i < keyCount; i++) {
//            result.add(keyFactory.createKey(keyNameHierarchy.get(i)));
//        }
//
//        return result;
//    }

//    public static StatsKey keyForFailure(final StatsKey key,
//                                         final Throwable failure) {
//        if (key == null) {
//            return NullStatsKey.getInstance();
//        }
//
//        String failureClassName = (failure == null) ? null : failure.getClass().getName();
//
//        StatsKey failureKey = key.buildCopy()
//                                 .withNameSuffix("exception")
//                                 .withAttribute("threw", failureClassName)
//                                 .newKey();
//        return failureKey;
//    }
}
