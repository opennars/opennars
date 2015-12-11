package nars.util.time;

import nars.Global;

import java.util.*;
import java.util.Map.Entry;

public class IntervalTree<K extends Comparable<? super K>, V> {
	
	public IntervalTreeNode<K, V> root = null;

	public List<V> searchOverlapping(Between<K> range){
		List<V> c = Global.newArrayList();
		if(root != null){
			root.getOverlap(range, c);
		}
		return c;
	}

	public final V getEqual(K low, K high){
		return getEqual(new Between<>(low, high));
	}

	public V getEqual(Between<K> range){
		if(root != null){
			return root.getEqual(range);
		}
		return null;
	}
	
	public List<V> searchOverlapping(K low, K high){
		return searchOverlapping(new Between<>(low, high));
	}
	
	/**
	 * Returns a collection of values that wholly contain the range specified.
	 */
	public List<V> searchContaining(Between<K> range){
		List<V> c = Global.newArrayList();
		if(root != null){
			root.getContain(range, c);
		}
		return c;
	}
	
	/**
	 * Returns a collection of values that wholly contain the range specified.
	 */
	public List<V> searchContaining(K low, K high){
		return searchContaining(new Between<>(low, high));
	}
	
	/**
	 * Returns a collection of values that are wholly contained by the range specified.
	 */
	public List<V> searchContainedBy(Between<K> range){
		List<V> c = Global.newArrayList();
		if(root != null){
			root.searchContainedBy(range, c);
		}
		return c;
	}
	
	/**
	 * Returns a collection of values that are wholly contained by the range specified.
	 */
	public List<V> searchContainedBy(K low, K high){
		return searchContainedBy(new Between<>(low, high));
	}
	
	public void removeOverlapping(Between<K> range){
		if(root != null){
			root = root.removeOverlapping(range);
		}
	}
	
	public void removeOverlapping(K low, K high){
		removeOverlapping(new Between<>(low, high));
	}
	
	/**
	 * Returns a collection of values that wholly contain the range specified.
	 */
	public void removeContaining(Between<K> range){
		if(root != null){
			root = root.removeContaining(range);
		}
	}
	
	/**
	 * Returns a collection of values that wholly contain the range specified.
	 */
	public void removeContaining(K low, K high){
		removeContaining(new Between<>(low, high));
	}
	
	/**
	 * Returns a collection of values that are wholly contained by the range specified.
	 */
	public void removeContainedBy(Between<K> range){
		if(root != null){
			root = root.removeContainedBy(range);
		}
	}
	
	/**
	 * Returns a collection of values that are wholly contained by the range specified.
	 */
	public void removeContainedBy(K low, K high){
		removeContainedBy(new Between<>(low, high));
	}
	
	public boolean isEmpty() {
		return root == null;
	}

	public void put(Between<K> key, V value) {
		root = root == null ? new IntervalTreeLeaf<>(key, value) : root.put(key, value);
	}
	
	public void put(K low, K high, V value) {
		put(new Between<>(low, high),value);
	}
	
	
	public int size() {
		return values().size();
	}

	public Collection<V> values() {
		Collection<V> c = Global.newArrayList();
		if(root != null){
			root.values(c);
		}
		return c;
	}

	public Set<Between<K>> keySet() {
		Set<Between<K>> s = Global.newHashSet(1);
		if(root != null){
			root.keySet(s);
		}
		return s;
	}

	public void putAll(Map<Between<K>, V> m) {
		for(Entry<Between<K>, V> intervalVEntry : m.entrySet()){
			put(intervalVEntry.getKey(), intervalVEntry.getValue());
		}
	}

	public void clear() {
		root = null;
	}

	public boolean containsValue(V value) {
		return root != null && root.containsValue(value);
	}

	public Set<Entry<Between<K>, V>> entrySet() {
		Set<Entry<Between<K>, V>> s = Global.newHashSet(size());
		if(root != null){
			root.entrySet(s);
		}
		return s;
	}

	public void remove(V value) {
		if(root != null){
			root = root.remove(value);
		}
	}
	
	public void removeAll(Collection<V> values) {
		if(root != null){
			root = root.removeAll(values);
		}
	}

	public final int height() {
		return root != null ? root.maxHeight() : 0;
	}

	public double averageHeight() {
		if(root == null){
			return 0.0;
		}

		//TODO use IntArrayList
		Collection<Integer> c = new LinkedList<>();
		root.averageHeight(c, 0);
		int count = 0;
		int total = 0;
		for(int i : c){
			total += i;
			count ++;
		}
		return (double) total / count;
		
	}


}
