package nars.util.time;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

class IntervalTreeLeaf<K extends Comparable<? super K>,V> implements IntervalTreeNode<K, V>, Entry<Interval<K>, V> {
	
	private final Interval<K> key;
	private V value;
	
	IntervalTreeLeaf(K min, K max, V value) {
		this(new Interval<K>(min, max),value);
	}

	public IntervalTreeLeaf(Interval<K> key, V value) {
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
	public boolean contains(Interval<K> interval) {
		return key.contains(interval);
	}

	@Override
	public boolean overlaps(K low, K high) {
		return key.overlaps(low,high);
	}

	@Override
	public boolean overlaps(Interval<K> interval) {
		return key.overlaps(interval);
	}

	@Override
	public K getLow() {
		return key.getLow();
	}

	@Override
	public K getHigh() {
		return key.getHigh();
	}

	@Override
	public final V getValue() {
		return value;
	}

	@Override
	public IntervalTreeNode<K, V> put(Interval<K> key, V value) {
		IntervalTreeNode<K, V> putNode = new IntervalTreeLeaf<K, V>(key, value);
		if(this.key.getLow().compareTo(key.getLow()) < 0){
			return new IntervalTreeBranch<K, V>(this, putNode);
		}else{
			return new IntervalTreeBranch<K, V>(putNode, this);
		}
	}

	@Override
	final public V getEqual(final Interval<K> range) {
		if (getLow().equals(range.getLow()) && getHigh().equals(range.getHigh())) {
			return getValue();
		}
		return null;
	}


	@Override
	public V getContain(Interval<K> range) {
		if (range.contains(key)) {
			return getValue();
		}
		return null;
	}

	@Override
	public void getOverlap(Interval<K> range,
						   Collection<V> accumulator) {
		if(range.overlaps(key)){
			accumulator.add(getValue());
		}
	}

	@Override
	public void getContain(Interval<K> range,
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
		if(value.equals(getValue())){
			return null;
		}else{
			return this;
		}
	}

	@Override
	public void entrySet(Set<Entry<Interval<K>, V>> accumulator) {
		accumulator.add(this);
	}

	@Override
	public final Interval<K> getKey() {
		return key;
	}

	@Override
	public V setValue(V value) {
		V ret = value;
		this.value = value;
		return ret;
	}

	@Override
	public final boolean containsValue(V value) {
		return getValue().equals(value);
	}

	@Override
	public void keySet(Set<Interval<K>> accumulator) {
		accumulator.add(key);
	}

	@Override
	public boolean containedBy(Interval<K> interval) {
		return interval.contains(key);
	}

	@Override
	public void searchContainedBy(Interval<K> range, Collection<V> accumulator) {
		if(containedBy(range)){
			accumulator.add(getValue());
		}
	}

	@Override
	public Interval<K> getRange() {
		return key;
	}

	@Override
	public int maxHeight() {
		return 1;
	}

	@Override
	public IntervalTreeNode<K, V> removeAll(Collection<V> values) {
		if(values.contains(getValue())){
			return null;
		}else{
			return this;
		}
	}

	@Override
	public void averageHeight(Collection<Integer> heights, int currentHeight) {
		heights.add(currentHeight + 1);
	}



	@Override
	public IntervalTreeNode<K, V> removeOverlapping(Interval<K> range) {
		if(key.overlaps(range)){
			return null;
		}
		return this;
	}

	@Override
	public IntervalTreeNode<K, V> removeContaining(Interval<K> range) {
		if(key.contains(range)){
			return null;
		}
		return this;
	}

	@Override
	public IntervalTreeNode<K, V> removeContainedBy(Interval<K> range) {
		if(range.contains(key)){
			return null;
		}
		return this;
	}

}
