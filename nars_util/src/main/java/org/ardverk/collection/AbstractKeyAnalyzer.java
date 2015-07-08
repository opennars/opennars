/*
 * Copyright 2005-2012 Roger Kapsi, Sam Berlin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.ardverk.collection;

/**
 * An abstract implementation of {@link KeyAnalyzer}.
 */
public abstract class AbstractKeyAnalyzer<K> implements KeyAnalyzer<K> {
  
  @SuppressWarnings("unchecked")
  @Override
  public int compare(K o1, K o2) {
    return ((Comparable<K>)o1).compareTo(o2);
  }
}
