package com.github.fge.grappa.support;

import com.google.common.escape.ArrayBasedUnicodeEscaper;
import com.google.common.escape.CharEscaper;
import com.gs.collections.api.map.primitive.CharObjectMap;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ArrayBasedCharEscaper extends CharEscaper {

  // The replacement array (see ArrayBasedEscaperMap).
  private final char[][] replacements;
  // The number of elements in the replacement array.
  private final int replacementsLength;
  // The first character in the safe range.
  private final char safeMin;
  // The last character in the safe range.
  private final char safeMax;

  /**
   * An implementation-specific parameter class suitable for initializing
   * {@link com.google.common.escape.ArrayBasedCharEscaper} or {@link ArrayBasedUnicodeEscaper} instances.
   * This class should be used when more than one escaper is created using the
   * same character replacement mapping to allow the underlying (implementation
   * specific) data structures to be shared.
   *
   * <p>The size of the data structure used by ArrayBasedCharEscaper and
   * ArrayBasedUnicodeEscaper is proportional to the highest valued character that
   * has a replacement. For example a replacement map containing the single
   * character '{@literal \}u1000' will require approximately 16K of memory.
   * As such sharing this data structure between escaper instances is the primary
   * goal of this class.
   *
   * @author David Beaumont
   * @since 15.0
   */

  public static ArrayBasedEscaperMap create(
          CharObjectMap<String> replacements) {
    return new ArrayBasedEscaperMap(createReplacementArray(replacements));
  }

  // Immutable empty array for when there are no replacements.
  private static final char[][] EMPTY_REPLACEMENT_ARRAY = new char[0][0];

  static char[][] createReplacementArray(CharObjectMap<String> map) {
    checkNotNull(map);  // GWT specific check (do not optimize)
    if (map.isEmpty()) {
      return EMPTY_REPLACEMENT_ARRAY;
    }
    char max = map.keysView().max();// Collections.max(map.keySet());
    char[][] replacements = new char[max + 1][];
    map.forEachKeyValue((c,v) -> replacements[c] = v.toCharArray());
    return replacements;
  }


  public static final class ArrayBasedEscaperMap {
    /**
     * Returns a new ArrayBasedEscaperMap for creating ArrayBasedCharEscaper or
     * ArrayBasedUnicodeEscaper instances.
     *
     * @param replacements a map of characters to their escaped representations
     */

    // The underlying replacement array we can share between multiple escaper
    // instances.
    private final char[][] replacementArray;

    public ArrayBasedEscaperMap(char[][] replacementArray) {
      this.replacementArray = replacementArray;
    }

    // Returns the non-null array of replacements for fast lookup.
    char[][] getReplacementArray() {
      return replacementArray;
    }

    // Creates a replacement array from the given map. The returned array is a
    // linear lookup table of replacement character sequences indexed by the
    // original character value.

  }

  /**
   * Creates a new ArrayBasedCharEscaper instance with the given replacement map
   * and specified safe range. If {@code safeMax < safeMin} then no characters
   * are considered safe.
   *
   * <p>If a character has no mapped replacement then it is checked against the
   * safe range. If it lies outside that, then {@link #escapeUnsafe} is
   * called, otherwise no escaping is performed.
   *
   * @param replacementMap a map of characters to their escaped representations
   * @param safeMin the lowest character value in the safe range
   * @param safeMax the highest character value in the safe range
   */
  protected ArrayBasedCharEscaper(CharObjectMap<String> replacementMap,
      char safeMin, char safeMax) {

    this(create(replacementMap), safeMin, safeMax);
  }

  /**
   * Creates a new ArrayBasedCharEscaper instance with the given replacement map
   * and specified safe range. If {@code safeMax < safeMin} then no characters
   * are considered safe. This initializer is useful when explicit instances of
   * ArrayBasedEscaperMap are used to allow the sharing of large replacement
   * mappings.
   *
   * <p>If a character has no mapped replacement then it is checked against the
   * safe range. If it lies outside that, then {@link #escapeUnsafe} is
   * called, otherwise no escaping is performed.
   *
   * @param escaperMap the mapping of characters to be escaped
   * @param safeMin the lowest character value in the safe range
   * @param safeMax the highest character value in the safe range
   */
  protected ArrayBasedCharEscaper(ArrayBasedEscaperMap escaperMap,
      char safeMin, char safeMax) {

    checkNotNull(escaperMap);  // GWT specific check (do not optimize)
    replacements = escaperMap.getReplacementArray();
    replacementsLength = replacements.length;
    if (safeMax < safeMin) {
      // If the safe range is empty, set the range limits to opposite extremes
      // to ensure the first test of either value will (almost certainly) fail.
      safeMax = Character.MIN_VALUE;
      safeMin = Character.MAX_VALUE;
    }
    this.safeMin = safeMin;
    this.safeMax = safeMax;
  }

  /*
   * This is overridden to improve performance. Rough benchmarking shows that
   * this almost doubles the speed when processing strings that do not require
   * any escaping.
   */
  @Override
  public final String escape(String s) {
    checkNotNull(s);  // GWT specific check (do not optimize).
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c < replacementsLength && replacements[c] != null ||
          c > safeMax || c < safeMin) {
        return escapeSlow(s, i);
      }
    }
    return s;
  }

  /**
   * Escapes a single character using the replacement array and safe range
   * values. If the given character does not have an explicit replacement and
   * lies outside the safe range then {@link #escapeUnsafe} is called.
   */
  @Override protected final char[] escape(char c) {
    if (c < replacementsLength) {
      char[] chars = replacements[c];
      if (chars != null) {
        return chars;
      }
    }
    if (c >= safeMin && c <= safeMax) {
      return null;
    }
    return escapeUnsafe(c);
  }

  /**
   * Escapes a {@code char} value that has no direct explicit value in the
   * replacement array and lies outside the stated safe range. Subclasses should
   * override this method to provide generalized escaping for characters.
   *
   * <p>Note that arrays returned by this method must not be modified once they
   * have been returned. However it is acceptable to return the same array
   * multiple times (even for different input characters).
   *
   * @param c the character to escape
   * @return the replacement characters, or {@code null} if no escaping was
   *         required
   */
  // TODO(user,cpovirk): Rename this something better once refactoring done
  protected abstract char[] escapeUnsafe(char c);
}
