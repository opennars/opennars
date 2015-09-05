package nars.util.time;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

interface IntervalTreeNode<K extends Comparable<? super K>,V> {

	boolean isLeaf();
	
	IntervalTreeNode<K,V> getLeft();
	IntervalTreeNode<K,V> getRight();
	
	boolean contains(K point);
	boolean contains(Interval<K> interval);
	boolean containedBy(Interval<K> interval);
	boolean overlaps(K low, K high);
	boolean overlaps(Interval<K> interval);
	
	boolean containsValue(V value);
	
	K getLow();
	K getHigh();
	V getValue();
	
	Interval<K> getRange();
	
	IntervalTreeNode<K, V> put(Interval<K> key, V value);
	
	void getOverlap(Interval<K> range, Collection<V> accumulator);
	/**
	 * Returns a collection of values that wholly contain the range specified.
	 */
	void getContain(Interval<K> range, Collection<V> accumulator);
	/**
	 * Returns a collection of values that are wholly contained by the range specified.
	 */
	void searchContainedBy(Interval<K> range, Collection<V> accumulator);
	
	IntervalTreeNode<K, V> removeOverlapping(Interval<K> range);
	IntervalTreeNode<K, V> removeContaining(Interval<K> range);
	IntervalTreeNode<K, V> removeContainedBy(Interval<K> range);
	
	void values(Collection<V> accumulator);
	void entrySet(Set<Entry<Interval<K>, V>> accumulator);
	void keySet(Set<Interval<K>> accumulator);
	
	IntervalTreeNode<K, V> remove(V value);
	IntervalTreeNode<K, V> removeAll(Collection<V> values);

	int size();
	int maxHeight();
	void averageHeight(Collection<Integer> heights, int currentHeight);

	V getEqual(Interval<K> range);

	V getContain(Interval<K> range);
}
