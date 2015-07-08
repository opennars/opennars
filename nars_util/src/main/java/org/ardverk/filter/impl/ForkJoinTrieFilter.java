package org.ardverk.filter.impl;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * @author kim 2014年9月2日
 */
public class ForkJoinTrieFilter extends SimpleTrieFilter {

	private final static int INITIAL_STEP = 0;

	private final ForkJoinPool pool = new ForkJoinPool();

	private final int threshold;

	public ForkJoinTrieFilter(int threshold, Map<String, String> properies) {
		super(properies);
		this.threshold = threshold;
	}

	public MergedTrieCounter filter(String source) throws Exception {
		ForkJoinTask task = new ForkJoinTask(new StringBuffer(source), ForkJoinTrieFilter.INITIAL_STEP, source.length());
		this.pool.submit(task);
		return task.get();
	}

	private class ForkJoinTask extends RecursiveTask<MergedTrieCounter> {

		private static final long serialVersionUID = 1L;

		private final StringBuffer buffer;

		private final int start;

		private final int end;

		public ForkJoinTask(StringBuffer buffer, int start, int end) {
			super();
			this.buffer = buffer;
			this.start = start;
			this.end = end;
		}

		@Override
		protected MergedTrieCounter compute() {
			try {
				if ((this.buffer.length() - this.end) <= ForkJoinTrieFilter.this.threshold) {
					return ForkJoinTrieFilter.super.filter(this.buffer.substring(this.start, this.end));
				} else {
					int split = (this.start + this.end) / 2;
					ForkJoinTask leftTask = new ForkJoinTask(this.buffer, this.start, split);
					ForkJoinTask rightTask = new ForkJoinTask(this.buffer, split + 1, this.end);
					leftTask.fork();
					rightTask.fork();
					return leftTask.join().merge(rightTask.join());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
