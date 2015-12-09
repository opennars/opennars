package nars.util.time;

/** defines an interval between two comparable values */
public class Between<K extends Comparable<? super K>> {
	
	private final K low, high;
	
	public Between(K low, K high){
        this.low = low;
        this.high = high;
	}

	final K getHigh() {
		return high;
	}



    final K getLow() {
		return low;
	}

//    void setHigh(K high) {
//        this.high = high;
//    }
//	void setLow(K low) {
//		this.low = low;
//	}

    final boolean contains(K p){
		return low.compareTo(p) <= 0 && high.compareTo(p) > 0;
	}
	
	/**
	 * Returns true if this Interval wholly contains i.
	 */
    final boolean contains(Between<K> i){
		return contains(i.low) && contains(i.high);
	}
	
	final boolean overlaps(K low, K high){
		return  this.low.compareTo(high) <= 0 &&
				this.high.compareTo(low) > 0;
	}
	
	final boolean overlaps(Between<K> i){
		return overlaps(i.low,i.high);
	}
	
	@Override
	public final String toString() {
		return String.format("[%s..%s]", low, high);
	}

}
