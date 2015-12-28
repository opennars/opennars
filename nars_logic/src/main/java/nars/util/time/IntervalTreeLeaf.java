package nars.util.time;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

class IntervalTreeLeaf<K extends Comparable<? super K>,V> implements IntervalTreeNode<K, V>, Entry<Between<K>, V> {
	
	private final Between<K> key;
	private V value;
	
	IntervalTreeLeaf(K min, K max, V value) {
		this(new Between<>(min, max),value);
	}

	public IntervalTreeLeaf(Between<K> key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		return key + "=" + value;
	}

	@Override
	public final boolean isLeaf() {
		return true;
	}

	@Override
	public IntervalTreeNode<K, V> getLeft() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IntervalTreeNode<K, V> getRight() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(K point) {
		return key.contains(point);
	}
	
	@Override
	public boolean contains(Between<K> interval) {
		return key.contains(interval);
	}

	@Override
	public boolean overlaps(K low, K high) {
		return key.overlaps(low,high);
	}

	@Override
	public boolean overlaps(Between<K> interval) {
		return key.overlaps(interval);
	}

	@Override
	public final K getLow() {
		return key.getLow();
	}

	@Override
	public final K getHigh() {
		return key.getHigh();
	}

	@Override
	public final V getValue() {
		return value;
	}

	@Override
	public IntervalTreeNode<K, V> put(Between<K> key, V value) {
		IntervalTreeNode<K, V> putNode = new IntervalTreeLeaf<>(key, value);
		return this.key.getLow().compareTo(key.getLow()) < 0 ? new IntervalTreeBranch<>(this, putNode) : new IntervalTreeBranch<>(putNode, this);
	}

	@Override
	public final V getEqual(Between<K> range) {
		if (getLow().equals(range.getLow()) && getHigh().equals(range.getHigh())) {
			return getValue();
		}
		return null;
	}


	@Override
	public V getContain(Between<K> range) {
		if (range.contains(key)) {
			return getValue();
		}
		return null;
	}


	@Override
	public final void getOverlap(Between<K> range, Consumer<V> accumulator) {
		if(range.overlaps(key)){
			accumulator.accept(getValue());
		}
	}

	@Override
	public final void getOverlap(Between<K> range,
						   Collection<V> accumulator) {
		if(range.overlaps(key)){
			accumulator.add(getValue());
		}
	}

	@Override
	public void getContain(Between<K> range,
						   Collection<V> accumulator) {
		if(key.contains(range)){
			accumulator.add(getValue());
		}
	}

	@Override
	public final int size() {
		return 1;
	}

	@Override
	public void values(Collection<V> accumulator) {
		accumulator.add(getValue());
	}

	@Override
	public IntervalTreeNode<K, V> remove(V value) {
		return value.equals(getValue()) ? null : this;
	}

	@Override
	public void entrySet(Set<Entry<Between<K>, V>> accumulator) {
		accumulator.add(this);
	}

	@Override
	public final Between<K> getKey() {
		return key;
	}

	@Override
	public V setValue(V value) {
		return this.value = value;
	}

	@Override
	public final boolean containsValue(V value) {
		return getValue().equals(value);
	}

	@Override
	public void keySet(Set<Between<K>> accumulator) {
		accumulator.add(key);
	}

	@Override
	public boolean containedBy(Between<K> interval) {
		return interval.contains(key);
	}

	@Override
	public void searchContainedBy(Between<K> range, Collection<V> accumulator) {
		if(containedBy(range)){
			accumulator.add(getValue());
		}
	}

	@Override
	public Between<K> getRange() {
		return key;
	}

	@Override
	public int maxHeight() {
		return 1;
	}

	@Override
	public IntervalTreeNode<K, V> removeAll(Collection<V> values) {
		return values.contains(getValue()) ? null : this;
	}

	@Override
	public void averageHeight(Collection<Integer> heights, int currentHeight) {
		heights.add(currentHeight + 1);
	}



	@Override
	public IntervalTreeNode<K, V> removeOverlapping(Between<K> range) {
		if(key.overlaps(range)){
			return null;
		}
		return this;
	}

	@Override
	public IntervalTreeNode<K, V> removeContaining(Between<K> range) {
		if(key.contains(range)){
			return null;
		}
		return this;
	}

	@Override
	public IntervalTreeNode<K, V> removeContainedBy(Between<K> range) {
		if(range.contains(key)){
			return null;
		}
		return this;
	}

}
