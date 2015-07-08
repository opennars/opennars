package org.ardverk.filter;

/**
 * @author kim 2014年9月2日
 */
public interface TrieFilter {

	public TrieCounter filter(String source) throws Exception;
}
