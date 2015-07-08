package nars.term.index;

/**
 * Trie specifically designed to map a set of Terms to a target
 * value type (ex: Concept) by sharing common term subsequences
 *
 * Provides the means for creating new terms to grow the tree.
 *
 * For an identity-only equality comparison, ensure that
 * foreign created term keys are not inserted directly
 * but via the putForeign method
 *
 */
public class TermTrie {
}
