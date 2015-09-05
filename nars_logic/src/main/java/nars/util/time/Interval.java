package nars.util.time;

public class Interval<K extends Comparable<? super K>> {
	
	private K low, high;
	
	public Interval(K low, K high){
		this.setLow(low);
		this.setHigh(high);
	}

	K getHigh() {
		return high;
	}

	void setHigh(K high) {
		this.high = high;
	}

	K getLow() {
		return low;
	}

	void setLow(K low) {
		this.low = low;
	}
	
	boolean contains(K p){
		return low.compareTo(p) <= 0 && high.compareTo(p) > 0;
	}
	
	/**
	 * Returns true if this Interval wholly contains i.
	 */
	boolean contains(Interval<K> i){
		return contains(i.low) && contains(i.high);
	}
	
	final boolean overlaps(final K low, final K high){
		return  this.low.compareTo(high) <= 0 &&
				this.high.compareTo(low) > 0;
	}
	
	final boolean overlaps(final Interval<K> i){
		return overlaps(i.low,i.high);
	}
	
	@Override
	public String toString() {
		return "Interval["+low+".."+high+"]";
	}

}
