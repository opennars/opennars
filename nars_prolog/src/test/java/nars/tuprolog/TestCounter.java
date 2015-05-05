package nars.tuprolog;

public class TestCounter {
	
	private int value = 0;
	
	public void update() {
		inc();
	}
        
        public void inc() {
		value++;
	}
	
	public int getValue() {
		return value;
	}

}
