package nars.prolog;

public class TestCounter {
	
	private int value = 0;
	
	public void update() {
		value++;
	}
        
        public void inc() {
		value++;
	}
	
	public int getValue() {
		return value;
	}

}
