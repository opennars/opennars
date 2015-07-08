package org.ardverk.filter.impl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;
import org.ardverk.filter.TrieCounter;
import org.ardverk.filter.TrieFilter;

/**
 * @author kim 2014年9月2日
 */
public class SimpleTrieFilter implements TrieFilter {

	private final static int ONE_STEP = 1;

	private final PatriciaTrie<String, String> trie = new PatriciaTrie<String, String>(StringKeyAnalyzer.CHAR);

	public SimpleTrieFilter(Map<String, String> properies) {
		this.trie.putAll(properies);
	}

	public MergedTrieCounter filter(String source) throws Exception {
		StringBuffer buffer = new StringBuffer(source);
		Judgment judgment = new Judgment();
		for (int index = 0; index < buffer.length(); index += this.step(buffer, judgment, index)) {
		}
		return new MergedTrieCounter(buffer.toString(), judgment.count());
	}

	private int step(StringBuffer buffer, Judgment judgment, int index) {
		String current = String.valueOf(buffer.charAt(index));
		if (this.trie.select(current).getKey().startsWith(current)) {
			for (int step = SimpleTrieFilter.ONE_STEP; step <= (buffer.length() - index); step++) {
				String fragement = buffer.substring(index, index + step);
				judgment.reset(this.trie.select(fragement), fragement);
				if (judgment.same()) {
					buffer.replace(index, index + step, judgment.replace());
					return step;
				} else if (!judgment.challenge()) {
					break;
				}
			}
		}
		return SimpleTrieFilter.ONE_STEP;
	}

	protected class MergedTrieCounter implements TrieCounter {

		private final StringBuffer buffer = new StringBuffer();

		private final AtomicInteger counter = new AtomicInteger();

		protected MergedTrieCounter(String source, int counter) {
			this.buffer.append(source);
			this.counter.addAndGet(counter);
		}

		public MergedTrieCounter merge(MergedTrieCounter counter) {
			this.append(counter.source());
			this.incr(counter.filtered());
			return this;
		}

		public MergedTrieCounter incr() {
			this.counter.incrementAndGet();
			return this;
		}

		public MergedTrieCounter incr(int count) {
			this.counter.addAndGet(count);
			return this;
		}

		public MergedTrieCounter append(String buffer) {
			this.buffer.append(buffer);
			return this;
		}

		@Override
		public String source() {
			return this.buffer.toString();
		}

		@Override
		public int filtered() {
			return this.counter.get();
		}
	}

	private class Judgment {

		private final AtomicInteger counter = new AtomicInteger();

		private Map.Entry<String, String> selected;

		private String fragment;

		private Judgment reset(Map.Entry<String, String> selected, String fragment) {
			this.selected = selected;
			this.fragment = fragment;
			this.counter.addAndGet(this.same() ? 1 : 0);
			return this;
		}

		public int count() {
			return this.counter.get();
		}

		public String replace() {
			return this.selected.getValue();
		}

		public boolean same() {
			return this.selected.getKey().equals(this.fragment);
		}

		public boolean challenge() {
			return this.selected.getKey().startsWith(this.fragment);
		}
	}
}
